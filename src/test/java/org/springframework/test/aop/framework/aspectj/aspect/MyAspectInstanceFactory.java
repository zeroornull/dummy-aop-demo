package org.springframework.test.aop.framework.aspectj.aspect;

import org.springframework.aop.aspectj.aspect.MetadataAwareAspectInstanceFactory;
import org.springframework.aop.aspectj.support.AspectMetadata;

/**
 * Aspect实例工厂，根据AspectMetadata（元数据）创建Aspect对象
 */
public class MyAspectInstanceFactory implements MetadataAwareAspectInstanceFactory {
    private final String aspectName;
    private final AspectMetadata aspectMetadata;
    private volatile Object aspectInstance;

    public MyAspectInstanceFactory(String aspectName, Class<?> clazz) {
        this.aspectName = aspectName;
        this.aspectMetadata = new AspectMetadata(clazz, aspectName);
    }

    @Override
    public Object getAspectInstance() {
        if (aspectInstance == null) {
            try {
                return aspectMetadata.getAspectClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Failed to create aspect instance: " + e.getMessage(), e);
            }
        }
        return aspectInstance;
    }

    @Override
    public AspectMetadata getAspectMetadata() {
        return aspectMetadata;
    }
}
