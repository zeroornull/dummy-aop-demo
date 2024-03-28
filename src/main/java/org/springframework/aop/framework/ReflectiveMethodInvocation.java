package org.springframework.aop.framework;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.common.Nullable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectiveMethodInvocation implements ProxyMethodInvocation, Cloneable {

    // 代理对象
    protected final Object proxy;
    // 目标对象
    protected final Object target;
    // 目标对象Class
    protected final Class<?> targetClass;

    // 1.方法
    protected final Method method;
    // 2.方法拦截器（增强）
    protected final List<Object> interceptorsAndDynamicMethodMatchers;
    // 3.方法参数
    protected Object[] arguments;

    private int currentInterceptorIndex = -1;

    @Nullable
    private Map<String, Object> userAttributes;

    public ReflectiveMethodInvocation(Object proxy, Object target, Method method, Object[] arguments,
        Class<?> targetClass, List<Object> chain) {
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.targetClass = targetClass;
        this.arguments = adaptArgumentsIfNecessary(method, arguments);
        this.interceptorsAndDynamicMethodMatchers = chain;
    }

    @Override
    public Object proceed() throws Throwable {
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            // 所有interceptor执行完毕，调用目标method
            return invokeJoinPoint();
        }

        // 逐个执行interceptor
        Object interceptorOrInterceptionAdvice =
            this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
        return ((MethodInterceptor)interceptorOrInterceptionAdvice).invoke(this);
    }

    protected Object invokeJoinPoint() throws Throwable {
        return method.invoke(this.target, this.arguments);
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public void setArguments(Object... arguments) {
        this.arguments = arguments;
    }

    @Override
    public Object getThis() {
        return target;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return method;
    }

    @Override
    public Object getProxy() {
        return this.proxy;
    }

    @Override
    public MethodInvocation invocableClone() {
        Object[] cloneArguments = this.arguments;
        if (this.arguments.length > 0) {
            // Build an independent copy of the arguments array.
            cloneArguments = this.arguments.clone();
        }
        return invocableClone(cloneArguments);
    }

    @Override
    public MethodInvocation invocableClone(Object... arguments) {
        if (this.userAttributes == null) {
            this.userAttributes = new HashMap<>();
        }

        try {
            // 对当前MethodInvocation进行克隆
            ReflectiveMethodInvocation clone = (ReflectiveMethodInvocation)clone();
            clone.arguments = arguments;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Should be able to clone object of type [" + getClass() + "]: " + e);
        }
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        if (value != null) {
            if (this.userAttributes == null) {
                this.userAttributes = new HashMap<>();
            }
            this.userAttributes.put(key, value);
        } else {
            if (this.userAttributes != null) {
                this.userAttributes.remove(key);
            }
        }
    }

    @Override
    public Object getUserAttribute(String key) {
        return (this.userAttributes != null ? this.userAttributes.get(key) : null);
    }

    private Object[] adaptArgumentsIfNecessary(Method method, Object[] arguments) {
        if (arguments == null) {
            return new Object[0];
        }
        return arguments;
    }

}
