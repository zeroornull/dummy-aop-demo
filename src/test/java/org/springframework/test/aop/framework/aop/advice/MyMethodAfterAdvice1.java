package org.springframework.test.aop.framework.aop.advice;

import org.springframework.aop.support.advice.AfterReturningAdvice;

import java.lang.reflect.Method;

/**
 * @author: xxp
 * @date: 2024/3/27 5:47
 * @description: TODO
 **/
public class MyMethodAfterAdvice1 implements AfterReturningAdvice {

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println("MyMethodAfterAdvice1.after");
    }
}
