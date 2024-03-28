package org.springframework.aop.framework.proxy;

/**
 * AOP代理接口，用于生产代理对象
 */
public interface AopProxy {

	Object getProxy();
}
