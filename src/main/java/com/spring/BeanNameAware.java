package com.spring;


//Bean名称感知
public interface BeanNameAware {

    public abstract void getBean(String name);
}
