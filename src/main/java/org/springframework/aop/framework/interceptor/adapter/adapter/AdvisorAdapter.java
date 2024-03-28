package org.springframework.aop.framework.interceptor.adapter.adapter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.support.advisor.Advisor;

public interface AdvisorAdapter {
    boolean supportsAdvice(Advice advice);

    MethodInterceptor getInterceptor(Advisor advisor);
}
