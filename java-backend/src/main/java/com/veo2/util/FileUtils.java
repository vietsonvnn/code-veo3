package com.veo2.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Create directory if it doesn't exist
     */
    public void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("Created directory: {}", path);
            } else {
                log.error("Failed to create directory: {}", path);
            }
        }
    }

    /**
     * Create directories for a file path
     */
    public void createParentDirectories(String filePath) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean created = parent.mkdirs();
            if (created) {
                log.info("Created parent directories for: {}", filePath);
            }
        }
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String path) {
        return new File(path).exists();
    }

    /**
     * Delete file
     */
    public boolean deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                log.info("Deleted file: {}", path);
            }
            return deleted;
        }
        return false;
    }

    /**
     * Copy file
     */
    public void copyFile(String sourcePath, String destPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path dest = Paths.get(destPath);

        createParentDirectories(destPath);
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        log.info("Copied file from {} to {}", sourcePath, destPath);
    }

    /**
     * Move file
     */
    public void moveFile(String sourcePath, String destPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path dest = Paths.get(destPath);

        createParentDirectories(destPath);
        Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
        log.info("Moved file from {} to {}", sourcePath, destPath);
    }

    /**
     * Get file size in bytes
     */
    public long getFileSize(String path) {
        File file = new File(path);
        return file.exists() ? file.length() : 0;
    }

    /**
     * Get file extension
     */
    public String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Build file path with scriptId
     */
    public String buildVideoPath(String baseDir, String scriptId, String filename) {
        return String.format("%s/%s/%s", baseDir, scriptId, filename);
    }

    /**
     * Get video filename for scene
     */
    public String getSceneFilename(int sceneNumber) {
        return String.format("scene_%03d.mp4", sceneNumber);
    }

    /**
     * Get thumbnail filename for scene
     */
    public String getThumbnailFilename(int sceneNumber) {
        return String.format("scene_%03d_thumb.jpg", sceneNumber);
    }

    /**
     * Clean up old files in directory
     */
    public void cleanupOldFiles(String directory, int daysOld) {
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60 * 60 * 1000);
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        log.info("Deleted old file: {}", file.getName());
                    }
                }
            }
        }
    }

    /**
     * Get all files in directory with extension
     */
    public File[] getFilesWithExtension(String directory, String extension) {
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            return new File[0];
        }

        return dir.listFiles((dir1, name) -> name.toLowerCase().endsWith("." + extension));
    }
}
