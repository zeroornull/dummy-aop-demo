package org.springframework.test.aop.framework.aop.advice;


import org.springframework.aop.support.advice.ThrowsAdvice;

public class MyThrowsAdvice2 implements ThrowsAdvice {

    public void afterThrowing(IllegalArgumentException ex) {
        System.out.println("MyThrowsAdvice2.IllegalArgumentException");
    }

}
