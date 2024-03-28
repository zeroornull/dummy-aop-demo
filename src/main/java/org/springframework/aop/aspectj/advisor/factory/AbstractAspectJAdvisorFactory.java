package org.springframework.aop.aspectj.advisor.factory;

import cn.hutool.core.annotation.AnnotationUtil;
import org.aspectj.lang.annotation.*;
import org.springframework.core.common.Nullable;
import org.springframework.core.exception.AopConfigException;
import org.springframework.core.util.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAspectJAdvisorFactory implements AspectJAdvisorFactory {
    private static final Class<?>[] ASPECTJ_ANNOTATION_CLASSES = new Class<?>[] {Pointcut.class, Around.class,
        Before.class, After.class, AfterReturning.class, AfterThrowing.class};

    @Override
    public boolean isAspect(Class<?> clazz) {
        return AnnotationUtil.getAnnotationValue(clazz, Aspect.class) != null;
    }

    @Override
    public void validate(Class<?> aspectClass) throws AopConfigException {
        // 父类标注@Aspect却不是抽象类，则报错
        Class<?> superclass = aspectClass.getSuperclass();
        if (superclass.getAnnotation(Aspect.class) != null && !Modifier.isAbstract(superclass.getModifiers())) {
            throw new AopConfigException(
                "[" + aspectClass.getName() + "] cannot extend concrete aspect [" + superclass.getName() + "]");
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static AspectJAnnotation<?> findAspectJAnnotationOnMethod(Method method) {
        for (Class<?> clazz : ASPECTJ_ANNOTATION_CLASSES) {
            AspectJAnnotation<?> foundAnnotation = findAnnotation(method, (Class<Annotation>)clazz);
            if (foundAnnotation != null) {
                return foundAnnotation;
            }
        }
        return null;
    }

    @Nullable
    private static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Method method, Class<A> toLookFor) {
        A result = AnnotationUtil.getAnnotation(method, toLookFor);
        if (result != null) {
            return new AspectJAnnotation<>(result);
        } else {
            return null;
        }
    }

    protected enum AspectJAnnotationType {
        AtPointcut, AtAround, AtBefore, AtAfter, AtAfterReturning, AtAfterThrowing
    }

    protected static class AspectJAnnotation<A extends Annotation> {

        private static final String[] EXPRESSION_ATTRIBUTES = new String[] {"pointcut", "value"};

        private static Map<Class<?>, AspectJAnnotationType> annotationTypeMap = new HashMap<>(8);

        static {
            annotationTypeMap.put(Pointcut.class, AspectJAnnotationType.AtPointcut);
            annotationTypeMap.put(Around.class, AspectJAnnotationType.AtAround);
            annotationTypeMap.put(Before.class, AspectJAnnotationType.AtBefore);
            annotationTypeMap.put(After.class, AspectJAnnotationType.AtAfter);
            annotationTypeMap.put(AfterReturning.class, AspectJAnnotationType.AtAfterReturning);
            annotationTypeMap.put(AfterThrowing.class, AspectJAnnotationType.AtAfterThrowing);
        }

        private final A annotation;

        private final AspectJAnnotationType annotationType;

        private final String pointcutExpression;

        private final String argumentNames;

        public AspectJAnnotation(A annotation) {
            this.annotation = annotation;
            this.annotationType = determineAnnotationType(annotation);
            try {
                this.pointcutExpression = resolveExpression(annotation);
                Object argNames = AnnotationUtils.getValue(annotation, "argNames");
                this.argumentNames = (argNames instanceof String ? (String)argNames : "");
            } catch (Exception ex) {
                throw new IllegalArgumentException(annotation + " is not a valid AspectJ annotation", ex);
            }
        }

        private AspectJAnnotationType determineAnnotationType(A annotation) {
            AspectJAnnotationType type = annotationTypeMap.get(annotation.annotationType());
            if (type != null) {
                return type;
            }
            throw new IllegalStateException("Unknown annotation type: " + annotation);
        }

        private String resolveExpression(A annotation) {
            for (String attributeName : EXPRESSION_ATTRIBUTES) {
                Object val = AnnotationUtils.getValue(annotation, attributeName);
                if (val instanceof String) {
                    String str = (String)val;
                    if (!str.isEmpty()) {
                        return str;
                    }
                }
            }
            throw new IllegalStateException("Failed to resolve expression: " + annotation);
        }

        public AspectJAnnotationType getAnnotationType() {
            return this.annotationType;
        }

        public A getAnnotation() {
            return this.annotation;
        }

        public String getPointcutExpression() {
            return this.pointcutExpression;
        }

        public String getArgumentNames() {
            return this.argumentNames;
        }

        @Override
        public String toString() {
            return this.annotation.toString();
        }
    }
}
