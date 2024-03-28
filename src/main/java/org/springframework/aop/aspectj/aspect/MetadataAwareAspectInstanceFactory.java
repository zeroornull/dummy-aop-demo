package org.springframework.aop.aspectj.aspect;

import org.springframework.aop.aspectj.support.AspectMetadata;

/**
 * 扩展AspectInstanceFactory
 * - getAspectInstance（继承）：获取Aspect对象
 * - getAspectMetadata（新增）：获取AspectMetadata信息
 */
public interface MetadataAwareAspectInstanceFactory extends AspectInstanceFactory{
    
    AspectMetadata getAspectMetadata();
}
