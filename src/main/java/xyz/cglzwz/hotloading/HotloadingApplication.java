package xyz.cglzwz.hotloading;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import xyz.cglzwz.hotloading.util.SpringContextUtil;

@SpringBootApplication
public class HotloadingApplication {

    public static void main(String[] args) {
        ApplicationContext ac = SpringApplication.run(HotloadingApplication.class, args);
        // 保存同一个容器使用
        SpringContextUtil.setApplicationContext(ac);
    }

}
