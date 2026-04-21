package com.sunwayworld.cloud.module.lcdp.support;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.springframework.web.multipart.MultipartFile;

import com.sunwayworld.framework.exception.FileException;

public class LcdpResourceFileUtils {

    /**
     * File to byte[]
     *
     * @param file
     * @return
     */
    public static byte[] fileToBytes(File file) {
        byte[] data = null;
        InputStream inputStream = null;
        if (file != null) {
            try {
                inputStream = new FileInputStream(file);
                data = new byte[inputStream.available()];
                inputStream.read(data);
            } catch (Exception e) {
                throw new FileException(e);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new FileException(e);
                }
            }
        }
        return data;
    }

    /**
     * File to 64bit Str
     *
     * @param file
     * @return
     */
    public static String fileToBase64Str(File file) {
        String data = null;
        if (file != null) {
            try {
                byte[] datas = fileToBytes(file);
                if (datas != null) {
                    data = Base64.encodeBase64String(datas);
                }
            } catch (Exception e) {
                throw new FileException(e);
            }

        }
        return data;
    }

    /**
     * MultipartFile to byte[]
     *
     * @param multipartFile
     * @return
     */
    public static byte[] multipartFileToBytes(MultipartFile multipartFile) {
        byte[] data = null;
        InputStream inputStream = null;
        if (multipartFile != null) {
            try {
                inputStream = multipartFile.getInputStream();
                data = new byte[inputStream.available()];
                inputStream.read(data);
            } catch (Exception e) {
                throw new FileException(e);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new FileException(e);
                }
            }
        }
        return data;
    }

    /**
     * MultipartFile to 64bit Str
     *
     * @param multipartFile
     * @return
     */
    public static String multipartFileToBase64Str(MultipartFile multipartFile) {
        String data = null;
        try {
            byte[] datas = multipartFileToBytes(multipartFile);
            if (datas != null) {
                data = Base64.encodeBase64String(datas);
            }
        } catch (Exception e) {
            throw new FileException(e);
        }
        return data;
    }

    /**
     * byte[] to file
     *
     * @param buf
     * @param filePath
     * @param fileName
     */
    public static void byteToFile(byte[] buf, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        } catch (Exception e) {
            throw new FileException(e);
        } finally {
            try {
                bos.close();
                fos.close();
            } catch (IOException e) {
                throw new FileException(e);
            }
        }
    }


}
