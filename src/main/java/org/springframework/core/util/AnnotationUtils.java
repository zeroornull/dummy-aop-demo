package org.springframework.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.core.common.Nullable;

import cn.hutool.core.util.StrUtil;

public abstract class AnnotationUtils {

    @Nullable
    public static Object getValue(@Nullable Annotation annotation, @Nullable String attributeName) {
        if (annotation == null || StrUtil.isBlank(attributeName)) {
            return null;
        }
        try {
            Method method = annotation.annotationType().getDeclaredMethod(attributeName);
            return invokeAnnotationMethod(method, annotation);
        } catch (Throwable ex) {
            return null;
        }
    }

    static Object invokeAnnotationMethod(Method method, Object annotation) {
        if (Proxy.isProxyClass(annotation.getClass())) {
            try {
                InvocationHandler handler = Proxy.getInvocationHandler(annotation);
                return handler.invoke(annotation, method, null);
            } catch (Throwable ex) {
                // ignore and fall back to reflection below
            }
        }
        try {
            return method.invoke(annotation);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not invoke method '" + method.getName() + "' on annotation: " + e.getMessage(), e);
        }
    }
}
