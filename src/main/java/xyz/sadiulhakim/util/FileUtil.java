package xyz.sadiulhakim.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    private static final String BASE_PATH = "uploads/";

    private FileUtil() {
    }

    public static void uploadFile(String folderName, String fileName, InputStream is) {
        Thread.ofVirtual().name("#FileUploadingThread-", 0).start(() -> {
            try {

                File file = new File(BASE_PATH, (folderName + fileName));
                Files.copy(is, file.toPath());
            } catch (Exception ex) {
                LOGGER.error("FileUtil.upload :: failed to upload file {}", (folderName + fileName));
            }
        });
    }

    public static void uploadFile(String folderName, String fileName, byte[] content) {
        Thread.ofVirtual().name("#FileUploadingThread-", 0).start(() -> {
            try {
                Path path = Paths.get(BASE_PATH, (folderName + fileName));
                Files.write(path, content);
            } catch (Exception ex) {
                LOGGER.error("FileUtil.uploadFile :: failed to upload file {}", (folderName + fileName));
            }
        });
    }

    public static String getUniqueFileName(String fileName, int length) {
        return SecureTextGenerator.generateRandomText(length) + "." + getFileExtension(fileName);
    }

    public static boolean deleteFile(String folderName, String fileName) {
        try {

            File file = new File(BASE_PATH, (folderName + fileName));
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception ex) {
            LOGGER.error("FileUtil.upload :: failed to delete file {}", (folderName + fileName));
            return false;
        }

        return false;
    }

    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}