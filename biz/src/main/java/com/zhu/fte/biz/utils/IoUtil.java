package com.zhu.fte.biz.utils;

import com.zhu.fte.biz.act.SqlSessionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TODO
 *
 * @author zhujiqian
 * @date 2021/3/13 22:39
 */
@Slf4j
public  class IoUtil {

    public static String readNextTrimmedLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line != null) {
            line = line.trim();
        }
        return line;
    }

    public static InputStream getResourceAsStream(String name) {
        InputStream resourceStream = null;
        ClassLoader classLoader = null;
        if (resourceStream == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
            resourceStream = classLoader.getResourceAsStream(name);
            if (resourceStream == null) {
                classLoader = IoUtil.class.getClassLoader();
                resourceStream = classLoader.getResourceAsStream(name);
            }
        }

        return resourceStream;
    }

    public static byte[] readInputStream(InputStream inputStream, String inputStreamName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[16384];

        try {
            for(int bytesRead = inputStream.read(buffer); bytesRead != -1; bytesRead = inputStream.read(buffer)) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception var5) {
            log.error("couldn't read input stream " + inputStreamName, var5);
        }

        return outputStream.toByteArray();
    }


    public static void closeSilently(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException var2) {
        }

    }
}
