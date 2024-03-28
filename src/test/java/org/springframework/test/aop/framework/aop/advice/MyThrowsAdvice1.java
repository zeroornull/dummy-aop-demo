package org.springframework.test.aop.framework.aop.advice;


import org.springframework.aop.support.advice.ThrowsAdvice;

public class MyThrowsAdvice1 implements ThrowsAdvice {

    public void afterThrowing(NullPointerException ex) {
        System.out.println("MyThrowsAdvice1.NullPointerException");
    }

}
