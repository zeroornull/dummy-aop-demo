package org.springframework.core.parametername;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.common.Nullable;

public class PrioritizedParameterNameDiscoverer implements ParameterNameDiscoverer {

	private final List<ParameterNameDiscoverer> parameterNameDiscoverers = new ArrayList<>(2);


	/**
	 * Add a further {@link ParameterNameDiscoverer} delegate to the list of
	 * discoverers that this {@code PrioritizedParameterNameDiscoverer} checks.
	 */
	public void addDiscoverer(ParameterNameDiscoverer pnd) {
		this.parameterNameDiscoverers.add(pnd);
	}


	@Override
	@Nullable
	public String[] getParameterNames(Method method) {
		for (ParameterNameDiscoverer pnd : this.parameterNameDiscoverers) {
			String[] result = pnd.getParameterNames(method);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@Override
	@Nullable
	public String[] getParameterNames(Constructor<?> ctor) {
		for (ParameterNameDiscoverer pnd : this.parameterNameDiscoverers) {
			String[] result = pnd.getParameterNames(ctor);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

}
