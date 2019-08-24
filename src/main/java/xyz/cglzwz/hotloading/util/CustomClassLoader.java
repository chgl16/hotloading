package xyz.cglzwz.hotloading.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 自定义类加载器加载外部class
 * 不需要重写加载的双亲委派逻辑
 *
 * @author chgl16
 * @date 2019/8/24 16:24
 */
@Component
public class CustomClassLoader extends ClassLoader {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 主要为了指定父加载器为Spring类加载器
     * 因此在构造方法中配置调用
     * 同时此加载器必须由Spring类加载器加载，即必须声明为@Component
     */
    public CustomClassLoader() {
        super(CustomClassLoader.class.getClassLoader());
    }

    /**
     * 加载某个目录下的一个class文件，返回类的类类型
     *
     * @param name 类的全名，如 java.util.concurrent.ConcurrentHashMap
     * @return
     */
    @Override
    protected  Class<?> findClass(String name) {
        // 这个路径就是网络路径，必须都是 "/"，不能存在 "\"，否则URL转换失败
        String myPath = "file:///" + FileStoreUtil.BASE_FOLDER.replace("\\", "/") + name.replace(".","/") + ".class";

        /*
         * !!本打算以此去掉可能存在的后缀BeanInfo和Customizer的class信息类加载失败的问题
         * 》实践结果：无法去掉，这是spring加载器无法加载，本加载器被迫加载造成的错误，
         * 》而如果class在classpath下是没有问题，但是这个问题不影响使用，因此隐藏了日志
         */
//        myPath = myPath.replace("BeanInfo", "").replace("Customizer", "");

        logger.warn("使用了自定义加载器加载该类: " +  myPath);

        byte[] classBytes = null;
        Path path = null;
        try {
            path = Paths.get(new URI(myPath));
            classBytes = Files.readAllBytes(path);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        Class cls = defineClass(name, classBytes, 0, classBytes.length);

        return cls;
    }
}

