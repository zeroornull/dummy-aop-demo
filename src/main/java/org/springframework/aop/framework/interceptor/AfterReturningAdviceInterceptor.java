package org.springframework.aop.framework.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.advice.AfterReturningAdvice;
import org.springframework.aop.support.advice.base.AfterAdvice;

/**
 * 后置增强拦截器
 */
public class AfterReturningAdviceInterceptor implements MethodInterceptor, AfterAdvice {

    private AfterReturningAdvice advice;

    public AfterReturningAdviceInterceptor() {}

    public AfterReturningAdviceInterceptor(AfterReturningAdvice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.advice.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }
}
