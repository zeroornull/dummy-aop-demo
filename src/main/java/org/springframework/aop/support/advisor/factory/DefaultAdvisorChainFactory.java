package org.springframework.aop.support.advisor.factory;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.interceptor.adapter.registry.AdvisorAdapterRegistry;
import org.springframework.aop.framework.interceptor.adapter.registry.DefaultAdvisorAdapterRegistry;
import org.springframework.aop.framework.proxyfactory.Advised;
import org.springframework.aop.support.advisor.Advisor;
import org.springframework.aop.support.advisor.PointcutAdvisor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: xxp
 * @date: 2024/3/27 6:04
 * @description: TODO
 **/
public class DefaultAdvisorChainFactory implements AdvisorChainFactory {

    @Override
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method,
        Class<?> targetClass) {
        AdvisorAdapterRegistry registry = DefaultAdvisorAdapterRegistry.getInstance();
        Advisor[] advisors = config.getAdvisors().toArray(new Advisor[0]);
        List<Object> interceptorList = new ArrayList<>(advisors.length);
        Class<?> actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());
        for (Advisor advisor : advisors) {
            // 是PointcutAdvisor，则取出Pointcut去匹配
            if (advisor instanceof PointcutAdvisor) {
                PointcutAdvisor pointcutAdvisor = (PointcutAdvisor)advisor;
                if (pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass)
                    && (pointcutAdvisor.getPointcut().getMethodMatcher().matches(method, actualClass))) {
                    MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
                    interceptorList.addAll(Arrays.asList(interceptors));
                }
            }
            // 否则默认匹配通过
            else {
                MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
                interceptorList.addAll(Arrays.asList(interceptors));
            }
        }
        return interceptorList;
    }

}
