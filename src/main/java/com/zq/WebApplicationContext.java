package com.zq;

import com.spring.ApplicationContext;
import com.zq.service.PersonService;
import com.zq.service.UserService;

/**
 * @program: LqSpring
 * @description: 启动入口
 * @author: liuqi
 * @create: 2023-02-27 15:46
 **/
public class WebApplicationContext {

    public static void main(String[] args) {
        //spring默认是XmlWebApplicationContext（解析xml中的bean标签这些进行实例化bean加入到IOC容器中）
        //Spring启动主要的逻辑在父类ContextLoader的方法initWebApplicationContext实现。ContextLoaderListener的作用就是启动web容器时自动装配ApplicationContext的配置信息。更细化一点讲，Spring的启动过程其实就是Spring IOC容器的启动过程。
        //把bean都加入到Spring的IOC容器中
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);

        UserService userServcie1 = (UserService)applicationContext.getBean("userService");
        System.out.println(userServcie1);
        userServcie1.testAutowired();


        PersonService personService = (PersonService)applicationContext.getBean("personService");
        personService.test();
    }
}
