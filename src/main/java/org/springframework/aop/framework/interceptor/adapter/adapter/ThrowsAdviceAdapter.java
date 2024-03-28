package org.springframework.aop.framework.interceptor.adapter.adapter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.interceptor.ThrowsAdviceInterceptor;
import org.springframework.aop.support.advice.ThrowsAdvice;
import org.springframework.aop.support.advisor.Advisor;

import java.io.Serializable;

public class ThrowsAdviceAdapter implements AdvisorAdapter, Serializable {

    public boolean supportsAdvice(Advice advice) {
        return advice instanceof ThrowsAdvice;
    }

    public MethodInterceptor getInterceptor(Advisor advisor) {
        return new ThrowsAdviceInterceptor(advisor.getAdvice());
    }
}
