package org.springframework.aop.framework.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.framework.proxyfactory.AdvisedSupport;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

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
        // 创建CGLib动态代理
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(advised.getTargetSource().getTarget().getClass());
        enhancer.setInterfaces(advised.getTargetSource().getTargetClass());
        enhancer.setCallback(new DynamicAdvisedInterceptor(advised));
        return enhancer.create();
    }

    private static class DynamicAdvisedInterceptor implements MethodInterceptor {
        private final AdvisedSupport advised;

        private DynamicAdvisedInterceptor(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            // 获取目标对象
            Object target = advised.getTargetSource().getTarget();
            Class<?> targetClass = target.getClass();
            // 获取拦截器链
            List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
            if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
                // 直接调用目标方法
                return methodProxy.invoke(target, args);
            } else {
                // 创建MethodInvocation，并调用proceed方法：依次执行拦截器链中的拦截器，最后执行目标方法
                return new CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy)
                    .proceed();
            }
        }

        private static class CglibMethodInvocation extends ReflectiveMethodInvocation {

            private final MethodProxy methodProxy;

            public CglibMethodInvocation(Object proxy, Object target, Method method, Object[] args,
                Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers, MethodProxy methodProxy) {
                super(proxy, target, method, args, targetClass, interceptorsAndDynamicMethodMatchers);
                // 仅对不是从Object继承的[公共方法]进行初始化
                this.methodProxy =
                    (Modifier.isPublic(method.getModifiers()) && method.getDeclaringClass() != Object.class)
                        ? methodProxy : null;

            }

            @Override
            public Object proceed() throws Throwable {
                return super.proceed();
            }

            /**
             * 执行目标方法；尽量用CGLib的methodProxy，性能比反射调用好一些
             */
            @Override
            protected Object invokeJoinPoint() throws Throwable {
                if (this.methodProxy != null) {
                    // 使用CGLib的methodProxy调用目标方法
                    return this.methodProxy.invoke(this.target, this.arguments);
                } else {
                    // 使用反射调用目标方法
                    return super.invokeJoinPoint();
                }
            }
        }
    }

}
