package org.springframework.aop.aspectj.support;

import org.springframework.aop.support.pointcut.Pointcut;
import org.springframework.core.common.Nullable;

/**
 * 支持表达式的Pointcut，比如AspectJExpressionPointcut实现类
 */
public interface ExpressionPointcut extends Pointcut {

    @Nullable
    String getExpression();

}
