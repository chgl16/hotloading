package xyz.cglzwz.hotloading.controller;

import xyz.cglzwz.hotloading.service.HotLoadingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author chgl16
 * @date 2019/8/24 12:17
 */
@RestController
@RequestMapping("/hotLoading")
public class HotLoadingController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HotLoadingService hotLoadingService;

    @GetMapping("/view")
    public ModelAndView view() {
        return new ModelAndView("hotloading/hot_loading");
    }

    @PostMapping("/load")
    public String loadClassFile(@RequestParam("classFile") MultipartFile file, @RequestParam("packagePath") String packagePath) {
        try {
            hotLoadingService.loadClass(file, packagePath);
            return "success";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }
}
