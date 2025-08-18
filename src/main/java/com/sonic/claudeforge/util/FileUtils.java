// src/main/java/com/sonic/claudeforge/util/FileUtils.java
package com.sonic.claudeforge.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * File utility class for file operations
 */
@Component
public class FileUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
    
    /**
     * Create directory if it doesn't exist
     */
    public void createDirectory(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.debug("Created directory: {}", directoryPath);
            }
        } catch (Exception e) {
            logger.error("Failed to create directory: {}", directoryPath, e);
            throw new RuntimeException("Failed to create directory: " + directoryPath, e);
        }
    }
    
    /**
     * Write content to file
     */
    public void writeFile(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            logger.debug("Written file: {} ({} chars)", filePath, content.length());
        } catch (Exception e) {
            logger.error("Failed to write file: {}", filePath, e);
            throw new RuntimeException("Failed to write file: " + filePath, e);
        }
    }
    
    /**
     * Read file content as string
     */
    public String readFileContent(File file) {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Failed to read file: {}", file.getAbsolutePath(), e);
            return "";
        }
    }
    
    /**
     * Find all Java files in directory recursively
     */
    public List<File> findJavaFiles(String directoryPath) {
        List<File> javaFiles = new ArrayList<>();
        File directory = new File(directoryPath);
        
        if (directory.exists() && directory.isDirectory()) {
            findJavaFilesRecursive(directory, javaFiles);
        }
        
        return javaFiles;
    }
    
    private void findJavaFilesRecursive(File directory, List<File> javaFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findJavaFilesRecursive(file, javaFiles);
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
    }
    
    /**
     * Check if file exists
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * Delete file
     */
    public boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (Exception e) {
            logger.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * Copy file
     */
    public void copyFile(String sourcePath, String targetPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            
            // Create target directory if needed
            createDirectory(target.getParent().toString());
            
            Files.copy(source, target);
            logger.debug("Copied file from {} to {}", sourcePath, targetPath);
        } catch (Exception e) {
            logger.error("Failed to copy file from {} to {}", sourcePath, targetPath, e);
            throw new RuntimeException("Failed to copy file", e);
        }
    }
}