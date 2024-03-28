package org.springframework.aop.aspectj.advice;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.aspectj.aspect.AspectInstanceFactory;
import org.springframework.aop.aspectj.pointcut.AspectJExpressionPointcut;
import org.springframework.aop.support.advice.base.AfterAdvice;
import org.springframework.core.common.Nullable;

public class AspectJAfterThrowingAdvice extends AbstractAspectJAdvice
		implements MethodInterceptor, AfterAdvice, Serializable {

	public AspectJAfterThrowingAdvice(Method aspectJBeforeAdviceMethod,
									  AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {
		super(aspectJBeforeAdviceMethod, pointcut, aif);
	}

	@Override
	public void setThrowingName(String name) {
		 setThrowingNameNoCheck(name);
	}

	@Override
	@Nullable
	public Object invoke(MethodInvocation mi) throws Throwable {
		try {
			return mi.proceed();
		}
		catch (Throwable ex) {
			if (shouldInvokeOnThrowing(ex)) {
				invokeAdviceMethod(getJoinPointMatch(), null, ex);
			}
			throw ex;
		}
	}


	private boolean shouldInvokeOnThrowing(Throwable ex) {
		return getDiscoveredThrowingType().isAssignableFrom(ex.getClass());
	}

}
