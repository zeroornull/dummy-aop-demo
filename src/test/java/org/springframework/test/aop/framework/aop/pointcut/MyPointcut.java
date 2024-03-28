package org.springframework.test.aop.framework.aop.pointcut;

import org.springframework.aop.support.pointcut.ClassFilter;
import org.springframework.aop.support.pointcut.MethodMatcher;
import org.springframework.aop.support.pointcut.Pointcut;
import org.springframework.test.aop.framework.aop.targetsource.Singer;

/**
 * 切点：为Singer类的dance方法增强
 */
public class MyPointcut implements Pointcut {
    @Override
    public ClassFilter getClassFilter() {
        return Singer.class::isAssignableFrom;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return (method, targetClass) -> "dance".equals(method.getName());
    }
}
