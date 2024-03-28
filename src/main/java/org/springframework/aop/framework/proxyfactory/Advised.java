package org.springframework.aop.framework.proxyfactory;

import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.TargetSource;
import org.springframework.aop.support.advisor.Advisor;

import java.util.List;

public interface Advised {
    
    boolean isProxyTargetClass();

    void setTargetSource(TargetSource targetSource);

    TargetSource getTargetSource();

    List<Advisor> getAdvisors();

    void addAdvisor(Advisor advisor);

    void addAdvice(Advice advice);
    
}
