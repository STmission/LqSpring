package com.spring;

/**
 * @program: LqSpring
 * @description: bean定义属性对象
 * @author: liuqi
 * @create: 2023-02-28 10:09
 **/
public class BeanDefinition {

    private Class clazz;
    private String scope;

    public BeanDefinition() {
    }

    public BeanDefinition(Class clazz, String scope) {
        this.clazz = clazz;
        this.scope = scope;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
