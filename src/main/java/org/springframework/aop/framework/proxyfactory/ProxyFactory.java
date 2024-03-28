package org.springframework.aop.framework.proxyfactory;

import org.springframework.aop.framework.proxy.AopProxy;
import org.springframework.aop.framework.proxy.CglibAopProxy;
import org.springframework.aop.framework.proxy.JdkDynamicAopProxy;

public class ProxyFactory extends AdvisedSupport{

    public ProxyFactory() {
    }
    
    public Object getProxy() {
        return createAopProxy().getProxy();
    }

    private AopProxy createAopProxy() {
        if (this.isProxyTargetClass() || this.getTargetSource().getTargetClass().length == 0) {
            return new CglibAopProxy(this);
        } else {
            return new JdkDynamicAopProxy(this);
        }
    }

}
