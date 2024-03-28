package org.springframework.aop.support.advisor;

import org.aopalliance.aop.Advice;
import org.springframework.aop.support.pointcut.Pointcut;
import org.springframework.core.common.Nullable;

import java.util.StringJoiner;

/**
 * 默认的PointcutAdvisor实现，如果不指定，默认匹配任何类、任何方法，而且Advice为空
 */
public class DefaultPointcutAdvisor implements PointcutAdvisor {

    private Pointcut pointcut = Pointcut.TRUE;
    private Advice advice = Advisor.EMPTY_ADVICE;

    public DefaultPointcutAdvisor() {}

    public DefaultPointcutAdvisor(Advice advice) {
        this(Pointcut.TRUE, advice);
    }

    public DefaultPointcutAdvisor(Pointcut aTrue, Advice advice) {
        this.pointcut = pointcut;
        setAdvice(advice);
    }

    public void setPointcut(@Nullable Pointcut pointcut) {
        this.pointcut = (pointcut != null ? pointcut : Pointcut.TRUE);
    }

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": pointcut [" + getPointcut() + "]; advice [" + getAdvice() + "]";
    }
}
