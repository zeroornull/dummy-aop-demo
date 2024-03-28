package org.springframework.aop.aspectj.advisor;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.springframework.aop.aspectj.advisor.factory.AspectJAdvisorFactory;
import org.springframework.aop.aspectj.aspect.MetadataAwareAspectInstanceFactory;
import org.springframework.aop.aspectj.pointcut.AspectJExpressionPointcut;
import org.springframework.aop.support.pointcut.Pointcut;
import org.springframework.core.common.Nullable;

/**
 * 这是AspectJ中的默认Advisor，内部持有Advice和Pointcut
 */
public final class InstantiationModelAwarePointcutAdvisorImpl
        implements InstantiationModelAwarePointcutAdvisor, Serializable {

    private static final Advice EMPTY_ADVICE = new Advice() {
    };

    private final Pointcut pointcut;

    @Nullable
    private Advice instantiatedAdvice;

    private final AspectJExpressionPointcut declaredPointcut;

    private final Method aspectJAdviceMethod;

    private final AspectJAdvisorFactory aspectJAdvisorFactory;

    private final MetadataAwareAspectInstanceFactory aspectInstanceFactory;


    public InstantiationModelAwarePointcutAdvisorImpl(AspectJExpressionPointcut declaredPointcut,
                                                      Method aspectJAdviceMethod,
                                                      AspectJAdvisorFactory aspectJAdvisorFactory,
                                                      MetadataAwareAspectInstanceFactory aspectInstanceFactory) {

        this.declaredPointcut = declaredPointcut;
        this.aspectJAdviceMethod = aspectJAdviceMethod;
        this.aspectJAdvisorFactory = aspectJAdvisorFactory;
        this.aspectInstanceFactory = aspectInstanceFactory;

        if (aspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
            // 先这样写，简易版AspectJ走不到这里
            this.pointcut = null;
        } else {
            // A singleton aspect.
            this.pointcut = this.declaredPointcut;
            this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
        }
    }


    /**
     * The pointcut for Spring AOP to use.
     * Actual behaviour of the pointcut will change depending on the state of the advice.
     */
    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    /**
     * Lazily instantiate advice if necessary.
     */
    @Override
    public synchronized Advice getAdvice() {
        if (this.instantiatedAdvice == null) {
            this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
        }
        return this.instantiatedAdvice;
    }

    private Advice instantiateAdvice(AspectJExpressionPointcut pointcut) {
        Advice advice = this.aspectJAdvisorFactory.getAdvice(this.aspectJAdviceMethod, pointcut, this.aspectInstanceFactory);
        return (advice != null ? advice : EMPTY_ADVICE);
    }

    public MetadataAwareAspectInstanceFactory getAspectInstanceFactory() {
        return this.aspectInstanceFactory;
    }

    public AspectJExpressionPointcut getDeclaredPointcut() {
        return this.declaredPointcut;
    }

    @Override
    public String toString() {
        return "InstantiationModelAwarePointcutAdvisor: expression [" + getDeclaredPointcut().getExpression() +
                "]; advice method [" + this.aspectJAdviceMethod + "]; perClauseKind=" +
                this.aspectInstanceFactory.getAspectMetadata().getAjType().getPerClause().getKind();
    }

}