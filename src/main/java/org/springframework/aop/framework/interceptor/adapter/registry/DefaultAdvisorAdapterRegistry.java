package org.springframework.aop.framework.interceptor.adapter.registry;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.interceptor.adapter.adapter.AdvisorAdapter;
import org.springframework.aop.framework.interceptor.adapter.adapter.AfterReturningAdviceAdapter;
import org.springframework.aop.framework.interceptor.adapter.adapter.MethodBeforeAdviceAdapter;
import org.springframework.aop.framework.interceptor.adapter.adapter.ThrowsAdviceAdapter;
import org.springframework.aop.support.advisor.Advisor;
import org.springframework.core.exception.UnknownAdviceTypeException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认Advisor适配器，负责将Advice适配成MethodInterceptor - MethodBeforeAdvice --> MethodBeforeAdviceInterceptor -
 * AfterReturningAdvice --> AfterReturningAdviceInterceptor - ThrowsAdvice --> ThrowsAdviceInterceptor
 */
public class DefaultAdvisorAdapterRegistry implements AdvisorAdapterRegistry, Serializable {

    // 单例模式
    private static final AdvisorAdapterRegistry INSTANCE = new DefaultAdvisorAdapterRegistry();

    public static AdvisorAdapterRegistry getInstance() {
        return INSTANCE;
    }

    private final List<AdvisorAdapter> adapters = new ArrayList<>(3);

    private DefaultAdvisorAdapterRegistry() {
        registerAdvisorAdapter(new MethodBeforeAdviceAdapter());
        registerAdvisorAdapter(new AfterReturningAdviceAdapter());
        registerAdvisorAdapter(new ThrowsAdviceAdapter());
    }

    @Override
    public void registerAdvisorAdapter(AdvisorAdapter adapter) {
        this.adapters.add(adapter);

    }

    @Override
    public MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException {
        List<MethodInterceptor> interceptors = new ArrayList<>(3);
        Advice advice = advisor.getAdvice();
        // 如果当前advice是MethodInterceptor类型，直接添加到interceptors中
        if (advice instanceof MethodInterceptor) {
            interceptors.add((MethodInterceptor)advice);
        }
        // 遍历适配器列表，尝试将advice适配成MethodInterceptor并添加到interceptors中
        for (AdvisorAdapter adapter : this.adapters) {
            if (adapter.supportsAdvice(advice)) {
                interceptors.add(adapter.getInterceptor(advisor));
            }
        }
        if (interceptors.isEmpty()) {
            throw new UnknownAdviceTypeException(advisor.getAdvice());
        }
        return interceptors.toArray(new MethodInterceptor[0]);
    }
}
