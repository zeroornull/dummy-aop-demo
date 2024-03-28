package org.springframework.aop.aspectj.advisor.factory;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.comparator.InstanceComparator;
import cn.hutool.core.util.StrUtil;
import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.*;
import org.springframework.aop.aspectj.advice.*;
import org.springframework.aop.aspectj.advisor.InstantiationModelAwarePointcutAdvisorImpl;
import org.springframework.aop.aspectj.aspect.LazySingletonAspectInstanceFactoryDecorator;
import org.springframework.aop.aspectj.aspect.MetadataAwareAspectInstanceFactory;
import org.springframework.aop.aspectj.pointcut.AspectJExpressionPointcut;
import org.springframework.aop.support.advisor.Advisor;
import org.springframework.core.common.Nullable;
import org.springframework.core.exception.AopConfigException;
import org.springframework.core.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReflectiveAspectJAdvisorFactory extends AbstractAspectJAdvisorFactory implements Serializable {
    // Exclude @Pointcut methods
    private static final ReflectionUtils.MethodFilter adviceMethodFilter = ReflectionUtils.USER_DECLARED_METHODS
            .and(method -> (AnnotationUtil.getAnnotation(method, Pointcut.class) == null));

    // 构造参数的顺序即Advice的执行顺序
    private static final Comparator<Method> adviceMethodComparator = new InstanceComparator<>(
            Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class);

    @Override
    public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
        Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
        validate(aspectClass);

        // 装饰器包一下
        MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory =
                new LazySingletonAspectInstanceFactoryDecorator(aspectInstanceFactory);

        List<Advisor> advisors = new ArrayList<>();
        for (Method method : getAdvisorMethods(aspectClass)) {
            Advisor advisor = getAdvisor(method, lazySingletonAspectInstanceFactory);
            if (advisor != null) {
                advisors.add(advisor);
            }
        }

        return advisors;
    }

    private List<Method> getAdvisorMethods(Class<?> aspectClass) {
        List<Method> methods = new ArrayList<>();
        ReflectionUtils.doWithMethods(aspectClass, methods::add, adviceMethodFilter);
        if (methods.size() > 1) {
            methods.sort(adviceMethodComparator);
        }
        return methods;
    }

    @Override
    @Nullable
    public Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory) {

        validate(aspectInstanceFactory.getAspectMetadata().getAspectClass());

        AspectJExpressionPointcut expressionPointcut = getPointcut(
                candidateAdviceMethod, aspectInstanceFactory.getAspectMetadata().getAspectClass());
        if (expressionPointcut == null) {
            return null;
        }

        return new InstantiationModelAwarePointcutAdvisorImpl(
                expressionPointcut, candidateAdviceMethod, this, aspectInstanceFactory);
    }

    @Nullable
    private AspectJExpressionPointcut getPointcut(Method candidateAdviceMethod, Class<?> candidateAspectClass) {
        AspectJAnnotation<?> aspectJAnnotation =
                AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
        if (aspectJAnnotation == null) {
            return null;
        }

        AspectJExpressionPointcut expressionPointcut =
                new AspectJExpressionPointcut(candidateAspectClass, new String[0], new Class<?>[0]);
        expressionPointcut.setExpression(aspectJAnnotation.getPointcutExpression());
        return expressionPointcut;
    }


    @Override
    @Nullable
    public Advice getAdvice(Method candidateAdviceMethod,
                            AspectJExpressionPointcut expressionPointcut,
                            MetadataAwareAspectInstanceFactory aspectInstanceFactory) {

        Class<?> candidateAspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
        validate(candidateAspectClass);

        AspectJAnnotation<?> aspectJAnnotation =
                AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
        if (aspectJAnnotation == null) {
            return null;
        }

        // If we get here, we know we have an AspectJ method.
        // Check that it's an AspectJ-annotated class
        if (!isAspect(candidateAspectClass)) {
            throw new AopConfigException("Advice must be declared inside an aspect type: " +
                    "Offending method '" + candidateAdviceMethod + "' in class [" +
                    candidateAspectClass.getName() + "]");
        }

        AbstractAspectJAdvice springAdvice;

        switch (aspectJAnnotation.getAnnotationType()) {
            case AtPointcut:
                return null;
            case AtAround:
                springAdvice = new AspectJAroundAdvice(
                        candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
                break;
            case AtBefore:
                springAdvice = new AspectJMethodBeforeAdvice(
                        candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
                break;
            case AtAfter:
                springAdvice = new AspectJAfterAdvice(
                        candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
                break;
            case AtAfterReturning:
                springAdvice = new AspectJAfterReturningAdvice(
                        candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
                AfterReturning afterReturningAnnotation = (AfterReturning) aspectJAnnotation.getAnnotation();
                if (StrUtil.isNotBlank(afterReturningAnnotation.returning())) {
                    springAdvice.setReturningName(afterReturningAnnotation.returning());
                }
                break;
            case AtAfterThrowing:
                springAdvice = new AspectJAfterThrowingAdvice(
                        candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
                AfterThrowing afterThrowingAnnotation = (AfterThrowing) aspectJAnnotation.getAnnotation();
                if (StrUtil.isNotBlank(afterThrowingAnnotation.throwing())) {
                    springAdvice.setThrowingName(afterThrowingAnnotation.throwing());
                }
                break;
            default:
                throw new UnsupportedOperationException(
                        "Unsupported advice type on method: " + candidateAdviceMethod);
        }

        springAdvice.calculateArgumentBindings();

        return springAdvice;
    }
}
