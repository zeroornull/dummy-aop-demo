package org.springframework.test.aop.framework.aspectj;

import org.junit.Test;
import org.springframework.aop.aspectj.advisor.factory.ReflectiveAspectJAdvisorFactory;
import org.springframework.aop.aspectj.pointcut.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.support.ExposeInvocationInterceptor;
import org.springframework.aop.framework.TargetSource;
import org.springframework.aop.framework.proxyfactory.ProxyFactory;
import org.springframework.aop.support.advisor.Advisor;
import org.springframework.test.aop.framework.aspectj.aspect.MyAspect;
import org.springframework.test.aop.framework.aspectj.aspect.MyAspectInstanceFactory;
import org.springframework.test.aop.framework.aspectj.targetsource.Teacher;

import java.util.List;

public class AspectJAopTest {

    /**
     * 大家留意一下测试案例中aop和aspectj两个包的结构 - aop：advice/pointcut/target，其中advice+pointcut组成advisor，由advisor去选择target进行增强 -
     * aspectj：aspect/target
     *
     * 最终两者都实现了AOP，这说明什么？说明Aspect相当于advice+pointcut。
     * 实际上也确实如此。Aspect定义的@Before等方法会被解析为Advice，而@Pointcut注解中的表达式会被用于创建{@link AspectJExpressionPointcut}
     * 所以，编写一个Aspect等于编写了多个Advice和Pointcut。
     */
    @Test
    public void testAspectJ() {
        // 自己实现一个aspectInstanceFactory，用于反射创建Aspect对象
        MyAspectInstanceFactory aspectInstanceFactory = new MyAspectInstanceFactory("myAspect", MyAspect.class);
        // 把aspectInstanceFactory传入ReflectiveAspectJAdvisorFactory，解析Aspect生成AdvisorChain
        ReflectiveAspectJAdvisorFactory advisorFactory = new ReflectiveAspectJAdvisorFactory();
        List<Advisor> advisors = advisorFactory.getAdvisors(aspectInstanceFactory);
        // 创建ProxyFactory，传入Advisors+TargetSource，得到代理对象
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addAdvisor(ExposeInvocationInterceptor.ADVISOR);
        proxyFactory.addAdvisors(advisors.toArray(new Advisor[0]));
        proxyFactory.setTargetSource(new TargetSource(new Teacher("Rod Johnson")));
        Teacher proxy = (Teacher)proxyFactory.getProxy();
        proxy.giveSpeech();
    }
}
