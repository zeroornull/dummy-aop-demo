package org.springframework.test.aop.framework.aop;

import org.junit.Test;
import org.springframework.aop.framework.TargetSource;
import org.springframework.aop.framework.proxyfactory.ProxyFactory;
import org.springframework.aop.support.advisor.DefaultPointcutAdvisor;
import org.springframework.aop.support.pointcut.Pointcut;
import org.springframework.test.aop.framework.aop.advice.*;
import org.springframework.test.aop.framework.aop.pointcut.MyPointcut;
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
        Singer proxy = (Singer)proxyFactory.getProxy();
        proxy.sing();
    }

    /**
     * 看完这个测试案例，回答下面的问题： 如果说Advice是增强，那么Advisor就是具有"自我意识"的增强，有了Pointcut，它就知道了应该给哪些类的哪些方法进行增强。
     * 那么，上面案例中我们直接往ProxyFactory中addAdvice，而不是addAdvisor。此时Advice的判断逻辑是什么呢？ （答案就在addAdvice内部，点进去就知道了）
     */
    @Test
    public void testProxyFactoryWithAdvisor() {
        // 切入点
        Pointcut pointcut = new MyPointcut();
        // 前置增强
        MyMethodBeforeAdvice1 advice1 = new MyMethodBeforeAdvice1();
        // 后置增强
        MyMethodAfterAdvice2 advice2 = new MyMethodAfterAdvice2();

        // Advisor = Advice + Pointcut
        DefaultPointcutAdvisor advisor1 = new DefaultPointcutAdvisor(pointcut, advice1);
        DefaultPointcutAdvisor advisor2 = new DefaultPointcutAdvisor(pointcut, advice2);

        // 构建ProxyFactory：目标对象+增强逻辑
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(advisor1);
        proxyFactory.addAdvisor(advisor2);
        proxyFactory.setTargetSource(new TargetSource(new Singer("Rod Johnson")));
        Singer proxy = (Singer) proxyFactory.getProxy();

        // 调用代理对象方法
        proxy.playBasketball();
        System.out.println("————————————————————————————————");
        proxy.dance();
    }
}
