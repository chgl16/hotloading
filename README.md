# 热加载模块
    保证服务不重启的情况下热加载外部class到Spring容器，并实现属性依赖注入
    
## 一、需求描述
![效果展示](https://i.loli.net/2019/08/24/waHUJ1583f6iOgP.png)

&emsp;&emsp;服务使用过程中，希望在不重启服务的情况下，动态加载某些class/scalar/jar到项目中使用，
即托管到Spring容器，并能实现属性依赖的注入。（本模块只加载class），同时也保证 **热更新**  
&emsp;&emsp;这些外部网络加载来的class一般实现服务里的某些业务接口，即希望后续动作bean组件使用。
指定beanName即可，这里默认beanName为类名lowCamel形式。

## 二、核心实现

### ①相关代码   

| 类 & 接口 | 说明  |
| :---: | :---:|
| FileStoreUtil | 文件保存工具类 |
| CustomClassLoader | 自定义类加载器，指定父加载器为Spring的加载器 |
| SpringContextUtil | 服务的spring容器操作工具类 |
| RegisterBeanUtil | 动态注册bean到容器的工具类，会注入依赖 |
| DynamicInterface | 模拟需求存在服务的class接口|
| DynamicClass | 模拟需求需要加载的class，提供参考，加载的是类似的DynamicClass2 |

### ②实现方法
##### 方法1： class文件直接保存到classpath对应的包目录下。  
方法这种方法实现比较简单，不需要自定义类加载器，直接  
```java
// 反射获取Class，加载到方法区
Class<?> cls = Class.forName("class全名");

// 调用注册方法，托管到Spring容器
register(Class<?> cls, String beanName);
```
但是这种方法拓展性很差，一般服务打包为jar包，根本无法往里面存文件。  

##### 方法2： 自定义类加载器  
这个方法不需要保存到项目classpath中class对应的目录下。可以任意目录，Class.formName()对比加载器最大就是这个区别  
>本项目保存到classpath:resource/hotLoadingClasses只为方便测试  

* CustomClassLoader
```java
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
```
自定义类加载器只需要重写findClass(name)方法即可，调用的时候是调用loadClass(name)方法，因为这里不需要
破坏双亲委派逻辑，调用链为loadClass -> findClass -> defineClass。  

这里需要注入的是 **要保证自定义的类加载器父加载器是Spring加载器** ，因为加载的class存在某些spring特性类（比如注解@Autowrited）,这些是需要Spring加载器加载的。  

实现方法很简单，把CustomClassLoader注册为@Component，即自定义的加载器被Spring加载器加载，那么构造方法的super(CustomClassLoader.class.getClassLoader())就是指定了父加载器。  

ClassLoader源码
```java
 /**
 * Creates a new class loader using the specified parent class loader for
 * delegation.
 *
 * <p> If there is a security manager, its {@link
 * SecurityManager#checkCreateClassLoader()
 * <tt>checkCreateClassLoader</tt>} method is invoked.  This may result in
 * a security exception.  </p>
 *
 * @param  parent
 *         The parent class loader
 *
 * @throws  SecurityException
 *          If a security manager exists and its
 *          <tt>checkCreateClassLoader</tt> method doesn't allow creation
 *          of a new class loader.
 *
 * @since  1.2
 */
protected ClassLoader(ClassLoader parent) {
    this(checkCreateClassLoader(), parent);
}
```    
需要加载的class类似如下：
```java
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
```
加载这个类用到的是自定义加载器的findClass方法，但是里面的@Autowired标注的属性会委派到Spring加载器加载。  
因此如果属性使用@Autowired如此，在构造bean的时候是不需要手动注入属性的，Spring加载胡自动注入。  
>@Autowired的属性这里不需要setter/getter方法，如果没用此注解需要提供setter方法，不然无法构造属性注入  
```java
// Class注入属性
Field[] fields = cls.getFields();
for (Field field : fields) {
    // 容器存在这个属性则注入，按编码习惯的名字，没有按类型。（用户输入的也是名字）
    if (defaultListableBeanFactory.isBeanNameInUse(field.getName())) {
        /*
           @第一个参数是类的属性名，第二个是容器中需要的bean的beanNam
           1. 如果是@Autowired注解的属性不需要这样添加了，注释掉下面代码
           2. 如果不是@Autowired需要另外添加setter方法
         */
        beanDefinitionBuilder.addPropertyReference(field.getName(), field.getName());
    }
}
```  

* ApplicationContext
```java
@SpringBootApplication
public class HotloadingApplication {

    public static void main(String[] args) {
        ApplicationContext ac = SpringApplication.run(HotloadingApplication.class, args);
        // 保存同一个容器使用
        SpringContextUtil.setApplicationContext(ac);
    }
}
```
容器在启动类处保存到工具类即可。保证容器唯一。  
```java
@Autowired
private ApplicationContext applicationContext;
```
这种会失败错误。  

* RegisterBeanUtil
```java
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
```
最核心的一个工具类，这些spring都封装很多了，spring容器相当于把加载到方法区Class获取创建id-class对象的形式维护到一个map中，  
这里的builder先获取class定义（创建者模型创建了一个实例，bean生命周期第一步）然后加入属性（其实属性setter注入就是bean周期的第二步）。  
最后就算注册大容器了。  

>这里的判断容器是否存在一个bean这里不能使用SpringContext.getBean(beanName) 或者 defaultListableBeanFactory.getBean(beanName)判断。  
>因为这个getBean方法必须保证bean存在容器的，不存在不会有null返回，直接异常中断程序，当然可以选择捕获异常不抛出保证程序继续执行。  
使用boolean defaultListableBeanFactory.isBeanNameInUse(beanName)才是正解


# 三、问题事项
1. 会报错XxxBeanInfo.class 或者 XxxCustomizer.class找不到  
>Xxx表示的是加载的类名，调试源码显示除了加载DynamicClassBO，还加载了DynamicClassBOBeanInfo和DynamicClassBOCustomizer  
  
这两个不知道为啥还是调用了自定义加载器的findClass方法，因为这两个应该是Spring加载器加载的，是一些bean的属性和信息类。不过并不影响使用  
```console
2019-08-24 22:07:54.476  WARN 4420 --- [nio-8080-exec-8] x.c.hotloading.util.CustomClassLoader    : 使用了自定义加载器加载该类: file:///F:/project/hotloading/src/main/resources/hotLoadingClasses/xyz/cglzwz/hotloading/bo/DynamicClass7BOCustomizer.class
java.nio.file.NoSuchFileException: F:\project\hotloading\src\main\resources\hotLoadingClasses\xyz\cglzwz\hotloading\bo\DynamicClass7BOCustomizer.class
	at sun.nio.fs.WindowsException.translateToIOException(WindowsException.java:79)
	at sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:97)
	at sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:102)
	at sun.nio.fs.WindowsFileSystemProvider.newByteChannel(WindowsFileSystemProvider.java:230)
	at java.nio.file.Files.newByteChannel(Files.java:361)
	at java.nio.file.Files.newByteChannel(Files.java:407)
	at java.nio.file.Files.readAllBytes(Files.java:3152)
	at xyz.cglzwz.hotloading.util.CustomClassLoader.findClass(CustomClassLoader.java:59)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:348)
	at com.sun.beans.finder.ClassFinder.findClass(ClassFinder.java:103)
	at java.beans.Introspector.findCustomizerClass(Introspector.java:1301)
	at java.beans.Introspector.getTargetBeanDescriptor(Introspector.java:1295)
	at java.beans.Introspector.getBeanInfo(Introspector.java:425)
	at java.beans.Introspector.getBeanInfo(Introspector.java:262)
	at java.beans.Introspector.getBeanInfo(Introspector.java:204)
	at org.springframework.beans.CachedIntrospectionResults.getBeanInfo(CachedIntrospectionResults.java:248)
	at org.springframework.beans.CachedIntrospectionResults.<init>(CachedIntrospectionResults.java:273)
	at org.springframework.beans.CachedIntrospectionResults.forClass(CachedIntrospectionResults.java:177)
	at org.springframework.beans.BeanWrapperImpl.getCachedIntrospectionResults(BeanWrapperImpl.java:174)
	at org.springframework.beans.BeanWrapperImpl.getLocalPropertyHandler(BeanWrapperImpl.java:230)
	at org.springframework.beans.BeanWrapperImpl.getLocalPropertyHandler(BeanWrapperImpl.java:63)
```  
当然如果使用方法1不会出现这种情况。比较保存到外面路径的class本身就无法生成对应的XxxBeanInfo和XxxCustomizer到相同目录下。  
疑点还是不知道为是作为父加载器的spring加载器不加载。  
>这种异常不影响程序执行，不会中断，为解决时可以捕获异常不处理即可。  
```java
@Override
protected  Class<?> findClass(String name) {
    // ...    

    byte[] classBytes = null;
    Path path = null;
    try {
        path = Paths.get(new URI(myPath));
        classBytes = Files.readAllBytes(path);
    } catch (IOException | URISyntaxException e) {
        // 捕获不处理，不打印
        // e.printStackTrace();
    }
    Class cls = defineClass(name, classBytes, 0, classBytes.length);
    return cls;
}
```