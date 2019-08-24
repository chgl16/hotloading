package xyz.cglzwz.hotloading.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * class文件保存
 *
 * @author chgl16
 * @date 2019/8/24 16:14
 */
public class FileStoreUtil {

    /**
     * 绝对路径，MultipartFile使用相对路径会加载到个tmp临时目录，会出错
     */
    public static final String BASE_FOLDER = "F:\\project\\hotloading\\src\\main\\resources\\hotLoadingClasses\\";

    /**
     * 直接丢到classpath下
     */
//    public static final String BASE_FOLDER = "F:\\project\\hotloading\\target\\classes\\";

    private static final Logger logger = LoggerFactory.getLogger(FileStoreUtil.class);

    /**
     * 保存上传的class文件
     * 会自动创建不存在的目录，文件路径：BASE_FOLDER/定义的包路径/*.class
     *
     * @param file
     * @param packagePath
     * @return 文件保存后的绝对路径
     */
    public static String store(MultipartFile file, String packagePath) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        logger.warn("文件类型：{}", contentType);
        logger.warn("文件名字：{}", fileName);
        logger.warn("包路径：{}", packagePath);

        // 保存文件, '.' 需要正则转义
        String path = BASE_FOLDER + packagePath.replaceAll("\\.", "/") + "/"+  fileName;
        logger.warn("文件保存后绝对路径：{}", path);

        File desFile = new File(path);

        try {
            FileUtils.copyInputStreamToFile(file.getInputStream(), desFile);
        } catch (Exception e) {
            logger.error(e.getMessage() + e);
        }

        return path;
    }

    public static void newFile(String name) {
        String path = FileStoreUtil.class.getResource("/").toString().substring(6);
        File file = new File(path, name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
