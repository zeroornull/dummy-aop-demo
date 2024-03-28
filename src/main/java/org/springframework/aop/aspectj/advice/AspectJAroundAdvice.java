package org.springframework.aop.aspectj.advice;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.springframework.aop.aspectj.aspect.AspectInstanceFactory;
import org.springframework.aop.aspectj.joinpoint.MethodInvocationProceedingJoinPoint;
import org.springframework.aop.aspectj.pointcut.AspectJExpressionPointcut;
import org.springframework.aop.framework.ProxyMethodInvocation;
import org.springframework.core.common.Nullable;

public class AspectJAroundAdvice extends AbstractAspectJAdvice
		implements MethodInterceptor, Serializable {

	public AspectJAroundAdvice(Method aspectJAroundAdviceMethod,
							   AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {
		super(aspectJAroundAdviceMethod, pointcut, aif);
	}


	@Override
	protected boolean supportsProceedingJoinPoint() {
		return true;
	}

	@Override
	@Nullable
	public Object invoke(MethodInvocation mi) throws Throwable {
		if (!(mi instanceof ProxyMethodInvocation)) {
			throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
		}
		ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
		ProceedingJoinPoint pjp = lazyGetProceedingJoinPoint(pmi);
		JoinPointMatch jpm = getJoinPointMatch(pmi);
		return invokeAdviceMethod(pjp, jpm, null, null);
	}

	protected ProceedingJoinPoint lazyGetProceedingJoinPoint(ProxyMethodInvocation rmi) {
		return new MethodInvocationProceedingJoinPoint(rmi);
	}

}
