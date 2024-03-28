package org.springframework.aop.framework.interceptor.adapter.adapter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.interceptor.AfterReturningAdviceInterceptor;
import org.springframework.aop.support.advice.AfterReturningAdvice;
import org.springframework.aop.support.advisor.Advisor;

import java.io.Serializable;

public class AfterReturningAdviceAdapter implements AdvisorAdapter, Serializable {
    @Override
    public boolean supportsAdvice(Advice advice) {
        return (advice instanceof AfterReturningAdvice);
    }

    @Override
    public MethodInterceptor getInterceptor(Advisor advisor) {
        AfterReturningAdvice advice = (AfterReturningAdvice) advisor.getAdvice();
        return new AfterReturningAdviceInterceptor(advice);
    }
}
