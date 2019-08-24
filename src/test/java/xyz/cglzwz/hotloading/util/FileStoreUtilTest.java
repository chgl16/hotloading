package xyz.cglzwz.hotloading.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class FileStoreUtilTest {

    @Test
    public void newFile() {
        String path = FileStoreUtilTest.class.getResource("/").toString().substring(6);
        System.out.println(path);
        String name = "a.txt";
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