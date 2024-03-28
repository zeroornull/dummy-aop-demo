package org.springframework.aop.framework.proxyfactory;

import java.io.Serializable;

/**
 * 代理配置类，所有与代理对象创建相关的类都应继承ProxyConfig，以确保具有一致的属性。
 */
public class ProxyConfig implements Serializable {

    /** use serialVersionUID from Spring 1.2 for interoperability. */
    private static final long serialVersionUID = -8409359707199703185L;

    // 是否直接代理目标Class（CGLib），默认false。详见@EnableAspectJAutoProxy
    private boolean proxyTargetClass = false;

    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.proxyTargetClass = proxyTargetClass;
    }

    public boolean isProxyTargetClass() {
        return this.proxyTargetClass;
    }

    @Override
    public String toString() {
        return "proxyTargetClass=" + this.proxyTargetClass + "; ";
    }
}
