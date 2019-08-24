package xyz.cglzwz.hotloading.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Spring容器工具类
 * 从主类保存容器到此类静态变量即可
 *
 * @author chgl16
 * @date 2019/8/24 16:19
 */
public class SpringContextUtil {

    private static ApplicationContext applicationContext;

    /**
     * 获取上下文
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 设置上下文
     *
     * @param applicationContext
     */
    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 通过名字获取bean
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 通过类型获取
     *
     * @param type
     * @return
     */
    public static Object getBean(Class<?> type) {
        return applicationContext.getBean(type);
    }
}

