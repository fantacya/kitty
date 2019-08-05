package com.fantacya.kitty.concurrent;

import com.fantacya.kitty.concurrent.exception.ExecutionError;
import com.fantacya.kitty.concurrent.exception.UncheckedExecutionException;
import com.fantacya.kitty.concurrent.exception.UncheckedTimeoutException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A copy from guava.
 * A TimeLimiter that runs method calls in the background using an {@link ExecutorService}. If the
 * time limit expires for a given method call, the thread running the call will be interrupted.
 */
public final class SimpleTimeLimiter implements TimeLimiter {

    private final ExecutorService executor;

    private SimpleTimeLimiter(ExecutorService executor) {
        this.executor = Objects.requireNonNull(executor);
    }

    /**
     * Creates a TimeLimiter instance using the given executor service to execute method calls.
     *
     * <p><b>Warning:</b> using a bounded executor may be counterproductive! If the thread pool fills
     * up, any time callers spend waiting for a thread may count toward their time limit, and in this
     * case the call may even time out before the target method is ever invoked.
     *
     * @param executor the ExecutorService that will execute the method calls on the target objects;
     *                 for example, a {@link Executors#newCachedThreadPool()}.
     * @since 22.0
     */
    public static SimpleTimeLimiter create(ExecutorService executor) {
        return new SimpleTimeLimiter(executor);
    }

    @Override
    public <T> T newProxy(
            final T target,
            Class<T> interfaceType,
            final Duration timeout) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(interfaceType);
        Objects.requireNonNull(timeout);
        Objects.requireNonNull(interfaceType.isInterface(), "interfaceType must be an interface type");

        final Set<Method> interruptibleMethods = findInterruptibleMethods(interfaceType);

        InvocationHandler handler =
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object obj, final Method method, final Object[] args)
                            throws Throwable {
                        Callable<Object> callable =
                                new Callable<Object>() {
                                    @Override
                                    public Object call() throws Exception {
                                        try {
                                            return method.invoke(target, args);
                                        } catch (InvocationTargetException e) {
                                            throw throwCause(e, false /* combineStackTraces */);
                                        }
                                    }
                                };
                        return callWithTimeout(
                                callable, timeout, interruptibleMethods.contains(method));
                    }
                };
        return newProxy(interfaceType, handler);
    }

    private static <T> T newProxy(Class<T> interfaceType, InvocationHandler handler) {
        Object object =
                Proxy.newProxyInstance(
                        interfaceType.getClassLoader(), new Class<?>[]{interfaceType}, handler);
        return interfaceType.cast(object);
    }

    private <T> T callWithTimeout(
            Callable<T> callable, Duration timeout, boolean amInterruptible)
            throws Exception {
        Objects.requireNonNull(callable);
        Objects.requireNonNull(timeout);

        Future<T> future = executor.submit(callable);

        try {
            if (amInterruptible) {
                try {
                    return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    future.cancel(true);
                    throw e;
                }
            } else {
                return getUninterruptibly(future, timeout);
            }
        } catch (ExecutionException e) {
            throw throwCause(e, true /* combineStackTraces */);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new UncheckedTimeoutException(e);
        }
    }

    @Override
    public <T> T callWithTimeout(Callable<T> callable, Duration timeout)
            throws TimeoutException, InterruptedException, ExecutionException {
        Objects.requireNonNull(callable);
        Objects.requireNonNull(timeout);

        Future<T> future = executor.submit(callable);

        try {
            return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (InterruptedException | TimeoutException e) {
            future.cancel(true /* mayInterruptIfRunning */);
            if (e instanceof TimeoutException) {
                throw new UncheckedTimeoutException(e);
            }
            throw e;
        } catch (ExecutionException e) {
            wrapAndThrowExecutionExceptionOrError(e.getCause());
            throw new AssertionError();
        }
    }

    @Override
    public <T> T callUninterruptiblyWithTimeout(
            Callable<T> callable, Duration timeout)
            throws TimeoutException, ExecutionException {
        Objects.requireNonNull(callable);
        Objects.requireNonNull(timeout);

        Future<T> future = executor.submit(callable);

        try {
            return getUninterruptibly(future, timeout);
        } catch (TimeoutException e) {
            future.cancel(true /* mayInterruptIfRunning */);
            throw e;
        } catch (ExecutionException e) {
            wrapAndThrowExecutionExceptionOrError(e.getCause());
            throw new AssertionError();
        }
    }

    @Override
    public void runWithTimeout(Runnable runnable, Duration timeout)
            throws TimeoutException, InterruptedException {
        Objects.requireNonNull(runnable);
        Objects.requireNonNull(timeout);

        Future<?> future = executor.submit(runnable);

        try {
            future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (InterruptedException | TimeoutException e) {
            future.cancel(true /* mayInterruptIfRunning */);
            throw e;
        } catch (ExecutionException e) {
            wrapAndThrowRuntimeExecutionExceptionOrError(e.getCause());
            throw new AssertionError();
        }
    }

    @Override
    public void runUninterruptiblyWithTimeout(
            Runnable runnable, Duration timeout) throws TimeoutException {
        Objects.requireNonNull(runnable);
        Objects.requireNonNull(timeout);

        Future<?> future = executor.submit(runnable);

        try {
            getUninterruptibly(future, timeout);
        } catch (TimeoutException e) {
            future.cancel(true /* mayInterruptIfRunning */);
            throw e;
        } catch (ExecutionException e) {
            wrapAndThrowRuntimeExecutionExceptionOrError(e.getCause());
            throw new AssertionError();
        }
    }

    private static Exception throwCause(Exception e, boolean combineStackTraces) throws Exception {
        Throwable cause = e.getCause();
        if (cause == null) {
            throw e;
        }
        if (combineStackTraces) {
            StackTraceElement[] combined =
                    concatArrays(cause.getStackTrace(), e.getStackTrace(), StackTraceElement.class);
            cause.setStackTrace(combined);
        }
        if (cause instanceof Exception) {
            throw (Exception) cause;
        }
        if (cause instanceof Error) {
            throw (Error) cause;
        }
        // The cause is a weird kind of Throwable, so throw the outer exception.
        throw e;
    }

    private static Set<Method> findInterruptibleMethods(Class<?> interfaceType) {
        Set<Method> set = new HashSet<>();
        for (Method m : interfaceType.getMethods()) {
            if (declaresInterruptedEx(m)) {
                set.add(m);
            }
        }
        return set;
    }

    private static boolean declaresInterruptedEx(Method method) {
        for (Class<?> exType : method.getExceptionTypes()) {
            // debate: == or isAssignableFrom?
            if (exType == InterruptedException.class) {
                return true;
            }
        }
        return false;
    }

    private static <V> V getUninterruptibly(Future<V> future, Duration timeout)
            throws ExecutionException, TimeoutException {
        boolean interrupted = false;
        try {
            long remainingNanos = timeout.toNanos();
            long end = System.nanoTime() + remainingNanos;

            while (true) {
                try {
                    // Future treats negative timeouts just like zero.
                    return future.get(remainingNanos, NANOSECONDS);
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static <T> T[] concatArrays(T[] first, T[] second, Class<T> type) {
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(type, first.length + second.length);
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private void wrapAndThrowExecutionExceptionOrError(Throwable cause) throws ExecutionException {
        if (cause instanceof Error) {
            throw new ExecutionError((Error) cause);
        } else if (cause instanceof RuntimeException) {
            throw new UncheckedExecutionException(cause);
        } else {
            throw new ExecutionException(cause);
        }
    }

    private void wrapAndThrowRuntimeExecutionExceptionOrError(Throwable cause) {
        if (cause instanceof Error) {
            throw new ExecutionError((Error) cause);
        } else {
            throw new UncheckedExecutionException(cause);
        }
    }
}
