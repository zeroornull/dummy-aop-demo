package org.springframework.aop.support.advice;

import org.springframework.aop.support.advice.base.BeforeAdvice;

import java.lang.reflect.Method;

public interface MethodBeforeAdvice extends BeforeAdvice {
    void before(Method method, Object[] args, Object target) throws Throwable;
}
