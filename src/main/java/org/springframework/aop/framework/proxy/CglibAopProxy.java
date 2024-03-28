package org.springframework.aop.framework.proxy;

import org.springframework.aop.framework.proxyfactory.AdvisedSupport;

/**
 * CGLib动态代理
 */
public class CglibAopProxy implements AopProxy {
    
    private final AdvisedSupport advised;

    public CglibAopProxy(AdvisedSupport advised) {
        this.advised = advised;
    }

    @Override
    public Object getProxy() {
        return null;
    }
}
