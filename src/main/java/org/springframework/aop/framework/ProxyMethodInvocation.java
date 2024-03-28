package org.springframework.aop.framework;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.common.Nullable;

/**
 * 反射的Method是对方法的抽象，它包含一个方法的所有信息，如名称、参数和返回类型。
 * 而MethodInvocation则是对方法调用的抽象，更偏向动态的概念。
 * 对于AOP而言，一次方法调用常常伴随着多个MethodInterceptor。
 * 所以，ProxyMethodInvocation的具体实现中最主要的成员变量是：
 * - Method
 * - MethodInterceptorChain
 */
public interface ProxyMethodInvocation extends MethodInvocation {
    Object getProxy();
    
    MethodInvocation invocableClone();
    
    MethodInvocation invocableClone(Object... arguments);
    
    void setArguments(Object... arguments);
    
    void setUserAttribute(String key, Object value);
    
    @Nullable
    Object getUserAttribute(String key);
    
    
    
    
    
    
}
