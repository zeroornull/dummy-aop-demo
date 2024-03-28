package org.springframework.aop.framework.proxy;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.framework.proxyfactory.AdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * JDK动态代理
 */
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
    private final AdvisedSupport advised;
    
    public JdkDynamicAopProxy(AdvisedSupport advised) {
        this.advised = advised;
    }
    
    /**
     * 返回代理对象
     */
    @Override
    public Object getProxy() {
        // 创建JDK动态代理
        return Proxy.newProxyInstance(getClass().getClassLoader(), advised.getTargetSource().getTargetClass(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取目标对象
        Object target = advised.getTargetSource().getTarget();
        Class<?> targetClass = target.getClass();
        // 获取拦截器链
        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
        if (chain == null || chain.isEmpty()) {
            return method.invoke(target, args);
        } else {
            // 创建MethodInvocation
            MethodInvocation invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
            // 执行proceed：依次执行拦截器链中的拦截器，最后执行目标方法
            return invocation.proceed();
        }
    }
}
