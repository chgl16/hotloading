package xyz.cglzwz.hotloading.service;

import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;

/**
 * @author chgl16
 * @date 2019/8/24 13:12
 */
public interface HotLoadingService {

    /**
     * 保存并且加载注册class
     *
     * @param file class文件
     * @param packagePath 包路径
     * @return
     */
    public boolean loadClass(MultipartFile file, String packagePath) throws ClassNotFoundException, MalformedURLException;
}
