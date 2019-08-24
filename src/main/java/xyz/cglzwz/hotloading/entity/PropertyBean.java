package xyz.cglzwz.hotloading.entity;

import org.springframework.stereotype.Component;

/**
 * 测试加载class的属性类
 *
 * @author chgl16
 * @date 2019/8/24 16:08
 */
@Component
public class PropertyBean {

    public PropertyBean() {
        System.out.println("**************进入PropertyBean构造方法***************");
        System.out.println("propertyBean的类加载器:" + this.getClass().getClassLoader().toString());
    }

    public void fun(){
        System.out.println("*************调用了PropertyBean的fun方法***********");
    }


}