package org.springframework.aop.framework.proxyfactory;

public class ProxyFactory extends AdvisedSupport{

    public ProxyFactory() {
    }
    
    public Object getProxy() {
        return createAopProxy().getProxy();
    }

    private ProxyFactory createAopProxy() {
        if (this.isProxyTargetClass() || this.getTargetSource().getTargetClass().length == 0) {
            return new CglibAopProxy(this);
        } else {
            return new JdkDynamicAopProxy(this);
        }
    }

}
