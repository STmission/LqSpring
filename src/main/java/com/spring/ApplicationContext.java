package com.spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @program: LqSpring
 * @description: spring配置类 应用上下文
 * @author: liuqi
 * @create: 2023-02-27 15:30
 **/
public class ApplicationContext {

    //1、判断配置类是否是含有@ComponetScan注解 解析注解 获取扫描路径
    //2、通过类加载器加载扫描路径的资源（target包下面的相对路径），
    //3、再获取文件，再通过类加载器把所有全限类名，进行判断是否有Spring的Bean注解（例如@Service @Controler @Repository）
    //4、再进行创建bean对象
    //5、bean它是有单例模式和原型模式（根据注解Scope来判断）默认是单例模式，那肯定就是一个key，一个value。则使用map集合来存储
    //为保证线程安全则使用ConcurrentHashMap来存储单例bean。
    //6、而上面的只记录了单例bean，那原型bean怎么办呢？所以还是需要把scope属性和class两个属性进行记录，后面再进行判断的时候再进行
    //单例池中获取单例或者原型模式直接new 一个对象。
    //为保证线性安全则使用ConcurrentHashmap来存储BeanDefinition 即bean的定义。
    //再通过getBean判断来获取对应的单例bean或者原型bean。

    ConcurrentHashMap<String,Object> singletonObjects = new ConcurrentHashMap<String,Object>(); //单例池
    ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String,BeanDefinition>(); //bean定义
    CopyOnWriteArrayList<BeanPostProcessor> copyOnWriteArrayListBeanPostProcessor = new CopyOnWriteArrayList<BeanPostProcessor>();//存储实现了该bean初始化的前置和后置处理的类


    private Class configClass;


    public ApplicationContext(Class configClass) {
        this.configClass = configClass;
        //解析配置类 ComponentScan注解--》扫描路径--》扫描--》BeanDefinition--》beanDefinitionMap
        conponetScan(configClass);

        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            BeanDefinition beanDefinition = beanDefinitionEntry.getValue();

            if(beanDefinition.getScope().equals("singleton")){
                Object bean = creatBean(beanName,beanDefinition);
                singletonObjects.put(beanName,bean);
            }


        }


    }
    //bean创建过程 依赖注入（获取所有属性字段判断）-》aware回调赋值bean-》处理类初始化之前-》初始化-》处理初始化之后
    private Object creatBean(String beanName ,BeanDefinition beanDefinition) {
        try {
            Class clazz = beanDefinition.getClazz();
            Object instance = clazz.getDeclaredConstructor().newInstance();

            //依赖注入
            for (Field declaredField : clazz.getDeclaredFields()) {
                if(declaredField.isAnnotationPresent(Autowired.class)){
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    //将此对象在 Field 指定对象参数上表示的字段设置为指定的新值。如果基础字段具有基元类型，则会自动解开新值的包装
                    /*obj – 应修改其字段的对象
                    值 – 要修改的 obj 字段的新值*/
                    declaredField.set(instance,bean);
                }
            }

            //Aware回调
            //是否实现了BeanNameAware接口，来进行getBean的名称赋值
            if(instance instanceof BeanNameAware){
                ((BeanNameAware) instance).getBean(beanName);
            }


            //初始化之前的bean处理

            if(!(instance instanceof BeanPostProcessor)){
                Iterator<BeanPostProcessor> iteratorBefore = copyOnWriteArrayListBeanPostProcessor.iterator();
                while (iteratorBefore.hasNext()){
                    BeanPostProcessor beanPostProcessor = iteratorBefore.next();
                    instance  = beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
                }
            }


            //初始化
            if(instance instanceof  InitializingBean){
               ((InitializingBean) instance).afeterPropertiesSet();
            }

            //初始化之后的bean处理
            if(!(instance instanceof BeanPostProcessor)){
                Iterator<BeanPostProcessor> iteratorAfter = copyOnWriteArrayListBeanPostProcessor.iterator();
                while (iteratorAfter.hasNext()){
                    BeanPostProcessor beanPostProcessor = iteratorAfter.next();
                    instance  = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
                }
            }

            //在bean之前进行aop的切面编程处理。
            //两种反射，jdk自带的兄弟类，cglib儿子类


            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    //扫描包名下的所有类，进行bean注册
    private void conponetScan(Class configClass) {
        //解析配置类的扫描组件得到扫描的包名
        ComponetScan componetScanAnnotation = (ComponetScan) configClass.getDeclaredAnnotation(ComponetScan.class);
        String path = componetScanAnnotation.value();

        //通过类加载器获取该包名下的资源。
        //1、类加载器有三种
        //Bootstrap => jre/lib
        //Ext       => jre/lib/ext
        //App       => classPath路径下的

        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource(path.replace(".", "//"));

        File file = new File(resource.getFile());

        if(file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {

                String fileName = f.getAbsolutePath();
                if(fileName.endsWith(".class")){
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    className = className.replace("/", ".");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Componet.class)){
                            //表示当前是一个bean
                            //需要进行创建
                            System.out.println("Componet注解："+clazz);

                            //判断它的老爸是不是该接口
                            if(BeanPostProcessor.class.isAssignableFrom(clazz)){
                                //如果是实现了初始化属性的处理接口则添加到list中
                                copyOnWriteArrayListBeanPostProcessor.add((BeanPostProcessor)clazz.getDeclaredConstructor().newInstance());
                            }
                            //
                            Componet componetAnnotation = clazz.getDeclaredAnnotation(Componet.class);
                            String beanName = componetAnnotation.value();

                            BeanDefinition beanDefinition = new BeanDefinition();

                            if(clazz.isAnnotationPresent(Scope.class)){
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            }else{
                                beanDefinition.setScope("singleton");
                            }
                            beanDefinition.setClazz(clazz);

                            beanDefinitionMap.put(beanName,beanDefinition);

                        }

                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }



            }
        }
    }


    //获取单例bean或者new 一个新的对象
    public Object getBean(String beanName){
        if(beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                return singletonObjects.get(beanName);
            }else{
               //创建bean对象
                return creatBean(beanName,beanDefinition);
            }

        }else {
            throw new NullPointerException();
        }


    }

}
