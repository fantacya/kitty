package com.fantacya.kitty.aop;

import com.fantacya.kitty.aop.annotation.LogAdvised;
import com.fantacya.kitty.aop.annotation.RandomAdvised;
import org.springframework.stereotype.Service;

@Service
public class ArithmeticService {

    @LogAdvised
    @RandomAdvised(bound = 10)
    public int add(int a, int b) {
        return a + b;
    }

    @RandomAdvised
    public int multiply(int a, int b) {
        return a * b;
    }

    @LogAdvised
    public int divide(int a, int b) {
        return a / b;
    }
}
