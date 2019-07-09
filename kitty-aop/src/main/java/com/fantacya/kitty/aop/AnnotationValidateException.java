package com.fantacya.kitty.aop;

import org.springframework.beans.factory.support.BeanDefinitionValidationException;

/**
 * @description:
 * @author: harri2012
 * @created: 2019-07-07 13:33
 */
public class AnnotationValidateException extends BeanDefinitionValidationException {

    public AnnotationValidateException(String msg) {
        super(msg);
    }

    public AnnotationValidateException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
