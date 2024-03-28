package org.springframework.aop.aspectj.advice;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.aop.aspectj.aspect.AspectInstanceFactory;
import org.springframework.aop.aspectj.pointcut.AspectJExpressionPointcut;
import org.springframework.aop.support.advice.MethodBeforeAdvice;
import org.springframework.core.common.Nullable;

public class AspectJMethodBeforeAdvice extends AbstractAspectJAdvice
		implements MethodBeforeAdvice, Serializable {

	public AspectJMethodBeforeAdvice(Method aspectJBeforeAdviceMethod,
									 AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {
		super(aspectJBeforeAdviceMethod, pointcut, aif);
	}


	@Override
	public void before(Method method, Object[] args, @Nullable Object target) throws Throwable {
		invokeAdviceMethod(getJoinPointMatch(), null, null);
	}

}
