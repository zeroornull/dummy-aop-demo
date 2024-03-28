package org.springframework.aop.framework.proxyfactory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.TargetSource;
import org.springframework.aop.support.advisor.Advisor;
import org.springframework.aop.support.advisor.DefaultPointcutAdvisor;
import org.springframework.aop.support.advisor.factory.AdvisorChainFactory;
import org.springframework.aop.support.advisor.factory.DefaultAdvisorChainFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AdvisedSupport extends ProxyConfig implements Advised{
    
    private TargetSource targetSource;
    
    private final List<Advisor> advisors = new ArrayList<>();
    
    private final AdvisorChainFactory advisorChainFactory = new DefaultAdvisorChainFactory();
    
    private final transient Map<Integer, List<Object>> methodCache;

    public AdvisedSupport() {
        this.methodCache = new ConcurrentHashMap<>(32);
    }

    /**
     * 获取与目标方法匹配的拦截器
     */
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) {
        Integer cacheKey = method.hashCode();
        List<Object> cached = this.methodCache.get(cacheKey);
        if (cached == null) {
            cached = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(this, method, targetClass);
            this.methodCache.put(cacheKey, cached);
        }
        return cached;
    }

    @Override
    public void setTargetSource(TargetSource targetSource) {
        this.targetSource = targetSource;
    }

    @Override
    public TargetSource getTargetSource() {
        return targetSource;
    }

    @Override
    public List<Advisor> getAdvisors() {
        return advisors;
    }

    @Override
    public void addAdvisor(Advisor advisor) {
        advisors.add(advisor);
    }

    public void addAdvisors(Advisor... advisors) {
        addAdvisors(Arrays.asList(advisors));
    }

    private <T> void addAdvisors(Collection<Advisor> advisors) {
        if (!CollUtil.isEmpty(advisors)) {
            for (Advisor advisor : advisors) {
                Assert.notNull(advisor, "Advisor must not be null");
                this.advisors.add(advisor);
            }
            adviceChanged();
        }
    }

    protected void adviceChanged() {
        this.methodCache.clear();
    }

    @Override
    public void addAdvice(Advice advice) {
        addAdvisor(new DefaultPointcutAdvisor(advice));
    }

    // ------- getter/setter --------
    
    
    
}
