package xyz.cglzwz.hotloading.service.impl;

import xyz.cglzwz.hotloading.service.HotLoadingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;


/**
 * @author t_guanlinchen
 * @date 2019/8/20 18:26
 */
@Service
public class HotLoadingServiceImpl implements HotLoadingService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean loadClass(MultipartFile file, String packagePath) throws ClassNotFoundException, MalformedURLException {

        return true;
    }
}
