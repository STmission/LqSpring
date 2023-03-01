package com.spring;

//初始化bean的时候自己需要做的处理
public interface InitializingBean {

    public abstract void afeterPropertiesSet();
}
