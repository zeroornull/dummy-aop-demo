package org.springframework.test.aop.framework.aspectj.targetsource;

public class Teacher {

    private String name;

    public Teacher() {
    }

    public Teacher(String name) {
        this.name = name;
    }

    public void sayHello() {
        System.out.println("hello, I'm " + name);
    }

    public void teach(String subject) {
        System.out.println("I'm teaching " + subject);
    }

    public String giveSpeech() {
        System.out.println("give a speech");
        return "English speech";
    }
}