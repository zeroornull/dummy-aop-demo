package org.springframework.aop.aspectj.support;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.advisor.Advisor;
import org.springframework.aop.support.advisor.DefaultPointcutAdvisor;
import org.springframework.core.common.Nullable;

import cn.hutool.core.thread.threadlocal.NamedThreadLocal;

/**
 * 特殊的MethodInterceptor，在AspectJ AOP中，它与BeforeMethodInterceptor等一起组成拦截器链，且必须放在开头。
 * 本质是一个ThreadLocal，用于存取本次请求中的MethodInvocation
 */
public final class ExposeInvocationInterceptor implements MethodInterceptor, Serializable {

    private ExposeInvocationInterceptor() {
    }

    /**
     * ExposeInvocationInterceptor（单例）
     */
    public static final ExposeInvocationInterceptor INSTANCE = new ExposeInvocationInterceptor();

    /**
     * 由ExposeInvocationInterceptor构建的Advisor（单例）
     */
    public static final Advisor ADVISOR = new DefaultPointcutAdvisor(INSTANCE) {
        @Override
        public String toString() {
            return ExposeInvocationInterceptor.class.getName() + ".ADVISOR";
        }
    };

    /**
     * ThreadLocal，存取MethodInvocation
     */
    private static final ThreadLocal<MethodInvocation> invocation = new NamedThreadLocal<>("Current AOP method invocation");

    public static MethodInvocation currentInvocation() throws IllegalStateException {
        MethodInvocation mi = invocation.get();
        if (mi == null) {
            throw new IllegalStateException(
                    "No MethodInvocation found: Check that an AOP invocation is in progress and that the " +
                            "ExposeInvocationInterceptor is upfront in the interceptor chain. Specifically, note that " +
                            "advices with order HIGHEST_PRECEDENCE will execute before ExposeInvocationInterceptor! " +
                            "In addition, ExposeInvocationInterceptor and ExposeInvocationInterceptor.currentInvocation() " +
                            "must be invoked from the same thread.");
        }
        return mi;
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation mi) throws Throwable {
        MethodInvocation oldInvocation = invocation.get();
        invocation.set(mi);
        try {
            // 直接跳到下一个
            return mi.proceed();
        } finally {
            invocation.set(oldInvocation);
        }
    }

}
