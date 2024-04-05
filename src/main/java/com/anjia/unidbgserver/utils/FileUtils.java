package com.anjia.unidbgserver.utils;

import com.anjia.unidbgserver.exception.BusinessException;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

/**
 * 将classpath下的文件copy到临时目录下
 *
 * @author AnJia
 * @since 2021-09-07 17:58
 */
public class FileUtils {
    public static File getTempFile(String classPathFileName) {
        try {
            File soLibFile = new File(System.getProperty("java.io.tmpdir"), classPathFileName);
            if (!soLibFile.exists()) {
                org.apache.commons.io.FileUtils.copyInputStreamToFile(new ClassPathResource(classPathFileName).getInputStream(), soLibFile);
            }
            return soLibFile;
        } catch (IOException e) {
            throw new BusinessException(-998, classPathFileName + "文件未找到");
        }

    }

}
