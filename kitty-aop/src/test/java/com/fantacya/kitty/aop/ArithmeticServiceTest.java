package com.fantacya.kitty.aop;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ArithmeticServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(ArithmeticService.class);

    @Autowired
    private ArithmeticService arithmeticService;

    @Test
    public void add() throws Exception {
        int r = arithmeticService.add(1, 2);
        LOG.info("returned {}", r);
    }

    @Test
    public void multiply() throws Exception {
        int r = arithmeticService.multiply(3, 4);
        LOG.info("returned {}", r);
    }

    @Test
    public void divide() throws Exception {
        Throwable throwable = null;
        try {
            int r = arithmeticService.divide(3, 0);
            LOG.info("returned {}", r);
        } catch (Throwable t) {
            throwable = t;
        }

        Assert.assertNotNull(throwable);
        Assert.assertEquals(throwable.getClass(), ArithmeticException.class);
    }
}
