package org.springframework.aop.support.advisor.factory;

import org.springframework.aop.framework.interceptor.adapter.registry.AdvisorAdapterRegistry;
import org.springframework.aop.framework.proxyfactory.Advised;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author: xxp
 * @date: 2024/3/27 6:04
 * @description: TODO
 **/
public class DefaultAdvisorChainFactory implements AdvisorChainFactory {


    @Override
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, Class<?> targetClass) {
        AdvisorAdapterRegistry registry = DefaultAdvisorAdapterRegistry.getInstance();
        return null;
    }
}
