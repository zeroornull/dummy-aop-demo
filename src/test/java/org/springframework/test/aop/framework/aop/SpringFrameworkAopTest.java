package org.springframework.test.aop.framework.aop;

import org.junit.Test;
import org.springframework.aop.framework.TargetSource;
import org.springframework.aop.framework.proxyfactory.ProxyFactory;
import org.springframework.test.aop.framework.aop.advice.*;
import org.springframework.test.aop.framework.aop.targetsource.Singer;

public class SpringFrameworkAopTest {

    @Test
    public void SpringFrameworkAopTest() {
        // 构建ProxyFactory：目标对象+增强逻辑
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvice(new MyMethodBeforeAdvice1());
        proxyFactory.addAdvice(new MyMethodBeforeAdvice2());
        proxyFactory.addAdvice(new MyMethodAfterAdvice1());
        proxyFactory.addAdvice(new MyMethodAfterAdvice2());
        proxyFactory.addAdvice(new MyThrowsAdvice1());
        proxyFactory.addAdvice(new MyThrowsAdvice2());
        proxyFactory.setTargetSource(new TargetSource(new Singer("Rod Johnson")));
        Singer proxy = (Singer) proxyFactory.getProxy();
        proxy.sing();
    }
}
