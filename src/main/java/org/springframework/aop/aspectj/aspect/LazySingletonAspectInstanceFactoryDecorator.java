package org.springframework.aop.aspectj.aspect;

import java.io.Serializable;

import org.springframework.aop.aspectj.support.AspectMetadata;
import org.springframework.core.common.Nullable;

import cn.hutool.core.lang.Assert;

/**
 * AspectInstanceFactory具体实现。
 * 这里我做了简化，在Spring中实际由BeanFactoryAspectInstanceFactory实现，LazySingletonAspectInstanceFactoryDecorator这是装饰器
 */
public class LazySingletonAspectInstanceFactoryDecorator implements MetadataAwareAspectInstanceFactory, Serializable {

    private final MetadataAwareAspectInstanceFactory awareAspectInstanceFactory;

    @Nullable
    private volatile Object materialized;

    public LazySingletonAspectInstanceFactoryDecorator(MetadataAwareAspectInstanceFactory awareAspectInstanceFactory) {
        Assert.notNull(awareAspectInstanceFactory, "AspectInstanceFactory must not be null");
        this.awareAspectInstanceFactory = awareAspectInstanceFactory;
    }

    @Override
    public Object getAspectInstance() {
        // 只有第一次调用时才会进行初始化（装饰）
        if (this.materialized == null) {
            this.materialized = this.awareAspectInstanceFactory.getAspectInstance();
            return this.materialized;
        }
        return materialized;
    }

    @Override
    public AspectMetadata getAspectMetadata() {
        return this.awareAspectInstanceFactory.getAspectMetadata();
    }

    @Override
    public String toString() {
        return "LazySingletonAspectInstanceFactoryDecorator: decorating " + this.awareAspectInstanceFactory;
    }

}
