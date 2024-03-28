package org.springframework.aop.aspectj.aspect;

/**
 * Aspect工厂，用于创建Aspect对象
 */
public interface AspectInstanceFactory {

    Object getAspectInstance();
}
