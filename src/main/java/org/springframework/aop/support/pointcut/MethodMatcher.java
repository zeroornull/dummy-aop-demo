package org.springframework.aop.support.pointcut;

import java.lang.reflect.Method;


public interface MethodMatcher {

	boolean matches(Method method, Class<?> targetClass);

	MethodMatcher TRUE = new MethodMatcher() {
		@Override
		public boolean matches(Method method, Class<?> targetClass) {
			return true;
		}
	};
}
