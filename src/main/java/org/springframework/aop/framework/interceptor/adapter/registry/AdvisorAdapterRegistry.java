package org.springframework.aop.framework.interceptor.adapter.registry;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.interceptor.adapter.adapter.AdvisorAdapter;
import org.springframework.aop.support.advisor.Advisor;
import org.springframework.core.exception.UnknownAdviceTypeException;

/**
 * Advisor适配器注册表：
 * - 存储Advisor适配器（registerAdvisorAdapter）
 * - 将Advisor适配成MethodInterceptor（getInterceptors）
 * <p>
 * Advice不就是我们常说的方法增强，或者所谓的通知吗，为什么需要适配？
 * <p>
 * 一方面，最初Spring定义了Advice接口来表示切面的概念。后来，AOP Alliance（AOP联盟）成立，它是一个由多个AOP框架共同制定标准的组织。
 * AOP Alliance引入了Interceptor接口，作为一个更通用的AOP概念，代表切面中的拦截器。
 * 为了整合不同的AOP框架，Spring选择将自己的Advice接口适配成AOP Alliance的Interceptor接口。
 * 这样一来，Spring可以更好地与其他AOP框架协同工作，同时也提供了一种通用的AOP抽象，使得开发者在使用AOP时更加灵活。
 * 这个适配过程实际上是为了达到AOP概念的统一，使得不同AOP框架在Spring中可以更加无缝地集成和交互。
 * <p>
 * 另一个方面，观察Advice子接口，你会发现不同的Advice定义的方法各异：
 * - void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable
 * - void before(Method method, Object[] args, Object target) throws Throwable;
 * 通过将这些各异的Advice适配成aopalliance的MethodInterceptor，可以统一API。
 */
public interface AdvisorAdapterRegistry {
    /**
     * 将Advisor适配成MethodInterceptor
     * 为什么传入一个Advisor，返回值却是MethodInterceptor数组？
     * 因为一个Advice可能同时实现多种类型的Advice接口，比如 SomeAdvice implements BeforeAdvice, AfterReturningAdvice
     * 此时SomeAdvice需要实现多个通知方法，自然可以适配成多个MethodInterceptor
     */
    MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException;

    void registerAdvisorAdapter(AdvisorAdapter adapter);
}
