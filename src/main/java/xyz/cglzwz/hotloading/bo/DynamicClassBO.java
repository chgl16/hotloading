package xyz.cglzwz.hotloading.bo;

import org.springframework.beans.factory.annotation.Autowired;
import xyz.cglzwz.hotloading.entity.PropertyBean;

/**
 * 测试的class
 * 对应还有个完全不在项目的DynamicClass2BO
 *
 * @author chgl16
 * @date 2019/8/24 16:04
 */
public class DynamicClassBO implements DynamicInterface {

    @Autowired(required = false)
    public PropertyBean propertyBean;

    public void sayHi() {
        System.out.println("Hi: 第一个实现类**************************");
        propertyBean.fun();

    }

    public PropertyBean getPropertyBean() {
        return propertyBean;
    }

    public void setPropertyBean(PropertyBean propertyBean) {
        this.propertyBean = propertyBean;
    }
}
