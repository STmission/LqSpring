package com.zq.service;

import com.spring.BeanPostProcessor;
import com.spring.Componet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @program: LqSpring
 * bean初始化之前之后处理器；
 * @description: 进行bean的属性初始化之前处理和属性初始化处理
 * 1、所有的bean都会走，
 * 2、而且可以声明多个，添加到集合中每个处理都会走进行bean初始化的之前处理和之后处理，进行多个bean初始化之前执行完，才会进行bean的初始化，再进行多个bean初始化之后执行完。
 * 3、在多个初始化之前和之后处理实现类时，如果需要保证处理的顺序，则通过注解指定数字1、2顺序走。
 *
 * @author: liuqi
 * @create: 2023-02-28 16:02
 **/
@Componet("instantiationAwareBeanPostProcessor")
public class InstantiationAwareBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        //所有bean都会进行bean的属性初始化之前处理 除它自己以外
        System.out.println("进行bean的属性初始化之前处理:"+beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        //所有bean都会进行bean的属性初始化之后处理 除它自己以外
        System.out.println("进行bean的属性初始化之后处理:"+beanName);

        //需不需要进行aop切面，判断是否有这个注解@aspect
        if("personService".equals(beanName)){
            //进行jdk的动态代理 必须要实现接口
            //返回指定接口的代理类实例，该代理类将方法调用调度到指定的调用处理程序。
            // 参数：类加载器，接口，调用处理程序


            Class<?>[] interfaces = bean.getClass().getInterfaces();
            for (Class<?> anInterface : interfaces) {
                System.out.println("接口有："+anInterface);
            }

            //判断切点pointcut
            //再判断是用的切点的哪些通知，五个通知中最全的是@Around注解，进行环绕通知，
            // 5.0版本 正常：方法调用之前-》原方法执行-》方法执行完结果执行-》最终执行
            // 5.0版本 正常：方法调用之前-》原方法执行-》异常执行-》最终执行
            Object proxyInstance = Proxy.newProxyInstance(InstantiationAwareBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理的逻辑");
                    return  method.invoke(bean,args);//调用原来实现类的方法，也就是代理类的方法
                }
            });
            return proxyInstance;

        }
        return bean;
    }
}
