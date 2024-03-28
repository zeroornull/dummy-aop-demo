package org.springframework.aop.framework.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.advice.ThrowsAdvice;
import org.springframework.aop.support.advice.base.AfterAdvice;
import org.springframework.core.common.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 异常增强拦截器
 */
public class ThrowsAdviceInterceptor implements MethodInterceptor, AfterAdvice {

    private static final String AFTER_THROWING = "afterThrowing";

    private final Object throwsAdvice;

    private final Map<Class<?>, Method> exceptionHandlerMap = new HashMap<>();

    public ThrowsAdviceInterceptor(Object advice) {
        this.throwsAdvice = advice;
        Method[] methods = throwsAdvice.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(AFTER_THROWING)
                && (method.getParameterCount() == 1 || method.getParameterCount() == 4)) {
                Class<?> throwableParam = method.getParameterTypes()[method.getParameterCount() - 1];
                if (Throwable.class.isAssignableFrom(throwableParam)) {
                    this.exceptionHandlerMap.put(throwableParam, method);
                }
            }
        }
        if (this.exceptionHandlerMap.isEmpty()) {
            throw new IllegalArgumentException(
                "At least one handler method must be found in class [" + throwsAdvice.getClass() + "]");
        }
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        } catch (Throwable ex) {
            Method handlerMethod = getExceptionHandler(ex);
            if (handlerMethod != null) {
                invokeHandlerMethod(mi, ex, handlerMethod);
            }
            throw ex;
        }
    }

    private void invokeHandlerMethod(MethodInvocation mi, Throwable ex, Method method) throws Throwable {
        Object[] handlerArgs;
        if (method.getParameterCount() ==1){
            handlerArgs = new Object[] {ex};
        }else {
            handlerArgs = new Object[] {null, mi.getMethod(), mi.getArguments(), mi.getThis()};
        }
        try {
            method.invoke(this.throwsAdvice, handlerArgs);
        } catch (InvocationTargetException targetEx) {
            throw targetEx.getTargetException();
        }
    }

    @Nullable
    private Method getExceptionHandler(Throwable ex) {
        Class<?> exceptionClass = ex.getClass();
        Method handler = this.exceptionHandlerMap.get(exceptionClass);
        while (handler == null && exceptionClass != Throwable.class) {
            exceptionClass = exceptionClass.getSuperclass();
            handler = this.exceptionHandlerMap.get(exceptionClass);
        }
        return handler;
    }
}
