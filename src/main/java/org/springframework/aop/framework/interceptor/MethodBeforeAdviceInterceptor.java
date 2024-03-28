package org.springframework.aop.framework.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.advice.MethodBeforeAdvice;
import org.springframework.aop.support.advice.base.BeforeAdvice;

import java.lang.reflect.Method;

/**
 * 前置增强拦截器
 */

public class MethodBeforeAdviceInterceptor implements MethodInterceptor, BeforeAdvice {

    private MethodBeforeAdvice advice;

    public MethodBeforeAdviceInterceptor() {}

    public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
        this.advice = advice;
    }

    public void setAdvice(MethodBeforeAdvice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        this.advice.before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}
