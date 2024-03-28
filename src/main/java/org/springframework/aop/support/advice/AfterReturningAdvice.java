package org.springframework.aop.support.advice;

import org.springframework.aop.support.advice.base.AfterAdvice;

import java.lang.reflect.Method;

public interface AfterReturningAdvice extends AfterAdvice {
    void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
