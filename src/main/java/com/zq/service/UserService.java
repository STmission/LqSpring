package com.zq.service;

import com.spring.*;

/**
 * @program: LqSpring
 * @description: 业务层
 * @author: liuqi
 * @create: 2023-02-27 15:42
 **/
@Componet("userService")
public class UserService implements BeanNameAware ,InitializingBean{


    @Autowired
    private  OrderService orderService;

    private String beanName;

    @Override
    public void getBean(String name) {
        this.beanName = name;
        System.out.println("BeanNameAware接口的getBean():获取bean名称"+beanName);
    }

    public void  testAutowired(){
        System.out.println(orderService);
    }

    @Override
    public void afeterPropertiesSet() {
        //验证属性是否为null，给某个属性赋值。作为一个初始化bean的属性处理
        System.out.println("InitializingBean接口的初始化bean的属性处理！！！");
    }
}
