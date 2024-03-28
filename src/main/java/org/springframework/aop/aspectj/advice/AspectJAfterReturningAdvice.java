package org.springframework.aop.aspectj.advice;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.aop.aspectj.aspect.AspectInstanceFactory;
import org.springframework.aop.aspectj.pointcut.AspectJExpressionPointcut;
import org.springframework.aop.support.advice.AfterReturningAdvice;
import org.springframework.aop.support.advice.base.AfterAdvice;
import org.springframework.core.common.Nullable;
import org.springframework.core.util.ClassUtils;

public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice
        implements AfterReturningAdvice, AfterAdvice, Serializable {

    public AspectJAfterReturningAdvice(Method aspectJBeforeAdviceMethod,
                                       AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {
        super(aspectJBeforeAdviceMethod, pointcut, aif);
    }

    @Override
    public void setReturningName(String name) {
         setReturningNameNoCheck(name);
    }

    @Override
    public void afterReturning(@Nullable Object returnValue, Method method, Object[] args, @Nullable Object target) throws Throwable {
        if (shouldInvokeOnReturnValueOf(method, returnValue)) {
            invokeAdviceMethod(getJoinPointMatch(), returnValue, null);
        }
    }

    private boolean shouldInvokeOnReturnValueOf(Method method, @Nullable Object returnValue) {
        Class<?> type = getDiscoveredReturningType();
        return matchesReturnValue(type, method, returnValue);
    }

    private boolean matchesReturnValue(Class<?> type, Method method, @Nullable Object returnValue) {
        if (returnValue != null) {
            return ClassUtils.isAssignableValue(type, returnValue);
        }
        else if (Object.class == type && void.class == method.getReturnType()) {
            return true;
        }
        else {
            return ClassUtils.isAssignable(type, method.getReturnType());
        }
    }

}
