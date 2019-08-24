package xyz.cglzwz.hotloading.util;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Field;

/**
 * 注册Bean
 *
 * @author chgl16
 * @date 2019/8/24 16:20
 */
public class RegisterBeanUtil {

    /**
     * 注册一个Class到IOC容器，并且返回调用
     * class不要求在classpath路径下
     *
     * @param cls  注册的bean Class
     * @param beanName 注册的bean Id
     * @return
     */
    public static Object register(Class<?> cls, String beanName) {

        //将applicationContext转换为ConfigurableApplicationContext
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) SpringContextUtil.getApplicationContext();

        // 获取bean工厂并转换为DefaultListableBeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();

        /*
            如果存在需要热更新，即先从工厂删去
            这里不能使用SpringContext.getBean(beanName) 或者 defaultListableBeanFactory.getBean(beanName)判断
            因为这个getBean方法必须保证bean存在容器的，不存在不会有null返回，直接异常中断程序，当然可以选择捕获异常不抛出保证程序继续执行
         */
        if (defaultListableBeanFactory.isBeanNameInUse(beanName)) {
            defaultListableBeanFactory.removeBeanDefinition(beanName);
        }

        // 通过BeanDefinitionBuilder创建bean定义
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(cls);

        // 所有属性
        Field[] fields = cls.getFields();
        for (Field field : fields) {
            // 容器存在这个属性则注入，按编码习惯的名字，没有按类型。（用户输入的也是名字）
            if (defaultListableBeanFactory.isBeanNameInUse(field.getName())) {
                // 第一个参数是类的属性名，第二个是容器中需要的bean的beanNam
                beanDefinitionBuilder.addPropertyReference(field.getName(), field.getName());
            }
        }

        // 注册bean
        defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getRawBeanDefinition());

        // 返回
        return SpringContextUtil.getBean(beanName);
    }

    /**
     * 注册一个Class到IOC容器，并且返回调用
     * 此name对应的class必须在classpath下
     * 这种不会出现BeanInfo和Customize的后缀问题
     *
     * @param name 类的全名，如java.util.concurrent.ConcurrentHashMap
     * @param beanName 注册的bean Id
     * @return
     */
    public static Object register(String name, String beanName) {
        Class<?> cls = null;
        try {
            // 如果此name对应的class必须在classpath下，则反射成功
            cls = Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return register(cls, beanName);
    }

}

