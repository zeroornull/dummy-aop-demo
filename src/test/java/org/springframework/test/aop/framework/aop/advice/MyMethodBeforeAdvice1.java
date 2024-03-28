package org.springframework.test.aop.framework.aop.advice;

import org.springframework.aop.support.advice.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * @author: xxp
 * @date: 2024/3/28 6:32
 * @description: TODO
 **/
public class MyMethodBeforeAdvice1 implements MethodBeforeAdvice {
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("MyMethodBeforeAdvice1.before");
    }
}
