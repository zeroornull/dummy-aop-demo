package org.springframework.core.parametername;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.core.common.Nullable;

public interface ParameterNameDiscoverer {

	@Nullable
	String[] getParameterNames(Method method);

	@Nullable
	String[] getParameterNames(Constructor<?> ctor);

}
