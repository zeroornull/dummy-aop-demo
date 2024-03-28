package org.springframework.aop.framework.interceptor.adapter.adapter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.interceptor.MethodBeforeAdviceInterceptor;
import org.springframework.aop.support.advice.MethodBeforeAdvice;
import org.springframework.aop.support.advisor.Advisor;

import java.io.Serializable;

public class MethodBeforeAdviceAdapter implements AdvisorAdapter, Serializable {

    @Override
    public boolean supportsAdvice(Advice advice) {
        return (advice instanceof MethodBeforeAdvice);
    }

    @Override
    public MethodInterceptor getInterceptor(Advisor advisor) {
        MethodBeforeAdvice advice = (MethodBeforeAdvice)advisor.getAdvice();
        return new MethodBeforeAdviceInterceptor(advice);
    }
}
