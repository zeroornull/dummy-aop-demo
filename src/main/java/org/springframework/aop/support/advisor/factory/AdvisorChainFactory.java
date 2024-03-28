package org.springframework.aop.support.advisor.factory;

import org.springframework.aop.framework.proxyfactory.Advised;
import org.springframework.core.common.Nullable;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 负责构建AdvisorChain
 */
public interface AdvisorChainFactory {
    
        /**
        * 获取拦截器链
        * - 先从config中获取所有的advisor
        * - 遍历出与目标方法匹配的advisor
        * - 将advisor适配成MethodInterceptor返回
        *
        * @param config      代理工厂配置（内含Advisor）
        * @param method      目标方法
        * @param targetClass 目标类
        */
        List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, @Nullable Class<?> targetClass);
}
