package com.zq.service;

import com.spring.Componet;
import com.spring.Service;

/**
 * @program: LqSpring
 * @description: 代理对象实现类
 * @author: liuqi
 * @create: 2023-03-01 14:10
 **/
@Componet("personService")
public class PersonServiceImpl implements PersonService{


    @Override
    public void test() {
        System.out.println("test方法执行！！！");
    }
}
