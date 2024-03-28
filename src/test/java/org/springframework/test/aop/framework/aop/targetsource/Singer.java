package org.springframework.test.aop.framework.aop.targetsource;

import java.util.concurrent.ThreadLocalRandom;

public class Singer {
    private String name;

    public Singer() {
    }

    public Singer(String name) {
        this.name = name;
    }

    public void sing() {
        System.out.println("唱歌");
        int factor = ThreadLocalRandom.current().nextInt(10);
        if (factor > 4 && factor < 8) {
            throw new IllegalArgumentException();
        } else if (factor > 8) {
            throw new NullPointerException();
        }
    }

    public void dance() {
        System.out.println("跳舞");
    }

    public void playBasketball() {
        System.out.println("打篮球");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}