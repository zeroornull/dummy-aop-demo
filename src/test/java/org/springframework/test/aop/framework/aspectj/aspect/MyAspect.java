package org.springframework.test.aop.framework.aspectj.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class MyAspect {
    
    @Pointcut("execution(* org.springframework.test.aop.framework.aspectj.targetsource.Teacher.*(..))")
    public void pointcutExpression() {
    }

    @Before(value = "pointcutExpression()")
    public void beforeMethod(JoinPoint joinPoint) {
        System.out.println("beforeMethod: " + joinPoint.getSignature().getName());
    }

    @After(value = "pointcutExpression()")
    public void afterMethod(JoinPoint joinPoint) {
        System.out.println("afterMethod: " + joinPoint.getSignature().getName());
    }

    @AfterReturning(value = "pointcutExpression()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, String result) {
        System.out.println("afterReturning: " + joinPoint.getSignature().getName() + ", result: " + result);
    }

    @Around(value = "pointcutExpression()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around_before: " + joinPoint.getSignature().getName());
        Object result = joinPoint.proceed();
        System.out.println("around_after: " + joinPoint.getSignature().getName());
        return result;
    }
    
}
