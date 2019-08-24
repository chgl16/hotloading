package xyz.cglzwz.hotloading.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import xyz.cglzwz.hotloading.bo.DynamicInterface;
import xyz.cglzwz.hotloading.service.HotLoadingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.cglzwz.hotloading.util.CustomClassLoader;
import xyz.cglzwz.hotloading.util.FileStoreUtil;
import xyz.cglzwz.hotloading.util.RegisterBeanUtil;

import java.net.MalformedURLException;


/**
 * @author t_guanlinchen
 * @date 2019/8/20 18:26
 */
@Service
public class HotLoadingServiceImpl implements HotLoadingService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CustomClassLoader customClassLoader;

    @Override
    public boolean loadClass(MultipartFile file, String packagePath) throws ClassNotFoundException, MalformedURLException {
        // 保存文件
        String absolutePath = FileStoreUtil.store(file, packagePath);

        // 是无法在jar包里面创建文件的
        // FileStoreUtil.newFile(file.getOriginalFilename());

        // 类全名，如 java.util.concurrent.ConcurrentHashMap
        String name = packagePath + "." + file.getOriginalFilename().split("\\.")[0];
        logger.warn("待加载类全名：{}", name);

        // 自定义类加载器加载保存的class文件
        Class<?> cls = customClassLoader.loadClass(name);

        // 注册的beanName
        String beanName = StringUtils.uncapitalize(file.getOriginalFilename().split("\\.")[0]);

        // 注册到Spring IOC容器，获取实例调用方法测试
        DynamicInterface instance = (DynamicInterface) RegisterBeanUtil.register(cls, beanName);
        instance.sayHi();

        return true;
    }
}
