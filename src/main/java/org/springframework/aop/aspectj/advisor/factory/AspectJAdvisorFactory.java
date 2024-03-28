package org.springframework.aop.aspectj.advisor.factory;

import org.aopalliance.aop.Advice;
import org.springframework.aop.aspectj.aspect.MetadataAwareAspectInstanceFactory;
import org.springframework.aop.aspectj.pointcut.AspectJExpressionPointcut;
import org.springframework.aop.support.advisor.Advisor;
import org.springframework.core.common.Nullable;
import org.springframework.core.exception.AopConfigException;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author: xxp
 * @date: 2024/3/29 6:04
 * @description: TODO
 **/
public interface AspectJAdvisorFactory {
    boolean isAspect(Class<?> clazz);
    
    void validate(Class<?> aspectClass)throws AopConfigException;
    
    List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory);
    
    @Nullable
    Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory);
    
    @Nullable
    Advice getAdvice(Method candidateAdviceMethod,
                     AspectJExpressionPointcut expressionPointcut,
                     MetadataAwareAspectInstanceFactory aspectInstanceFactory);
}
