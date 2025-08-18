// src/main/java/com/sonic/claudeforge/service/ProjectAnalyzerService.java
package com.sonic.claudeforge.service;

import com.sonic.claudeforge.model.ProjectStructure;
import com.sonic.claudeforge.util.FileUtils;
import com.sonic.claudeforge.util.MermaidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project Analysis Service
 * Analyzes existing Spring Boot projects and extracts structure information
 */
@Service
public class ProjectAnalyzerService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectAnalyzerService.class);
    
    private final FileUtils fileUtils;
    private final MermaidGenerator mermaidGenerator;
    
    public ProjectAnalyzerService(FileUtils fileUtils, MermaidGenerator mermaidGenerator) {
        this.fileUtils = fileUtils;
        this.mermaidGenerator = mermaidGenerator;
    }
    
    public ProjectStructure analyzeProject(String projectPath) {
        logger.info("Analyzing project at: {}", projectPath);
        
        ProjectStructure structure = new ProjectStructure();
        structure.setProjectPath(projectPath);
        structure.setAnalyzedAt(LocalDateTime.now());
        
        // Extract project name from path
        String projectName = extractProjectName(projectPath);
        structure.setProjectName(projectName);
        
        // Find base package
        String basePackage = findBasePackage(projectPath);
        structure.setBasePackage(basePackage);
        
        // Generate project tree structure
        String projectTree = generateProjectTree(projectPath);
        structure.setProjectTree(projectTree);
        
        // Scan Java files
        List<ProjectStructure.FileInfo> javaFiles = scanJavaFiles(projectPath);
        structure.setJavaFiles(javaFiles);
        
        // Extract API endpoints
        List<ProjectStructure.ApiEndpoint> apiEndpoints = extractApiEndpoints(javaFiles, projectPath);
        structure.setApiEndpoints(apiEndpoints);
        
        // Generate project metadata
        Map<String, Object> metadata = generateProjectMetadata(projectPath);
        structure.setProjectMetadata(metadata);
        
        // Generate Mermaid diagram
        String mermaidDiagram = mermaidGenerator.generateProjectDiagram(structure);
        structure.setMermaidDiagram(mermaidDiagram);
        
        logger.info("Project analysis completed successfully");
        return structure;
    }
    
    private String generateProjectTree(String projectPath) {
        StringBuilder tree = new StringBuilder();
        File projectRoot = new File(projectPath);
        String projectName = projectRoot.getName();
        
        tree.append(projectName).append("/\n");
        generateTreeRecursive(projectRoot, tree, "", true);
        
        return tree.toString();
    }
    
    private void generateTreeRecursive(File dir, StringBuilder tree, String prefix, boolean isRoot) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        // Filter out common ignored directories
        List<File> filteredFiles = new ArrayList<>();
        for (File file : files) {
            String name = file.getName();
            if (!name.equals("target") && !name.equals(".git") && !name.equals(".idea") 
                && !name.equals("node_modules") && !name.startsWith(".")) {
                filteredFiles.add(file);
            }
        }
        
        // Sort: directories first, then files
        filteredFiles.sort((f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) return -1;
            if (!f1.isDirectory() && f2.isDirectory()) return 1;
            return f1.getName().compareToIgnoreCase(f2.getName());
        });
        
        for (int i = 0; i < filteredFiles.size(); i++) {
            File file = filteredFiles.get(i);
            boolean isLast = (i == filteredFiles.size() - 1);
            
            tree.append(prefix).append(isLast ? "└── " : "├── ").append(file.getName());
            if (file.isDirectory()) {
                tree.append("/");
            }
            tree.append("\n");
            
            if (file.isDirectory()) {
                String newPrefix = prefix + (isLast ? "    " : "│   ");
                generateTreeRecursive(file, tree, newPrefix, false);
            }
        }
    }
    
    private String getRelativePath(String filePath, String projectPath) {
        try {
            File projectFile = new File(projectPath);
            File targetFile = new File(filePath);
            
            String projectAbsolute = projectFile.getCanonicalPath();
            String targetAbsolute = targetFile.getCanonicalPath();
            
            if (targetAbsolute.startsWith(projectAbsolute)) {
                String relativePath = targetAbsolute.substring(projectAbsolute.length());
                if (relativePath.startsWith(File.separator)) {
                    relativePath = relativePath.substring(1);
                }
                return relativePath.replace(File.separator, "/");
            }
            
            return filePath;
        } catch (Exception e) {
            return filePath;
        }
    }
    
    private String extractProjectName(String projectPath) {
        File projectDir = new File(projectPath);
        return projectDir.getName();
    }
    
    private String findBasePackage(String projectPath) {
        List<File> javaFiles = fileUtils.findJavaFiles(projectPath);
        
        for (File file : javaFiles) {
            String content = fileUtils.readFileContent(file);
            if (content.contains("@SpringBootApplication")) {
                return extractPackageFromFile(content);
            }
        }
        
        return "com.sonic.claudeforge";
    }
    
    private String extractPackageFromFile(String content) {
        Pattern pattern = Pattern.compile("package\\s+([\\w\\.]+);");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "com.sonic.claudeforge";
    }
    
    private List<ProjectStructure.FileInfo> scanJavaFiles(String projectPath) {
        List<ProjectStructure.FileInfo> fileInfos = new ArrayList<>();
        List<File> javaFiles = fileUtils.findJavaFiles(projectPath);
        
        for (File file : javaFiles) {
            ProjectStructure.FileInfo fileInfo = new ProjectStructure.FileInfo();
            fileInfo.setFileName(file.getName());
            fileInfo.setFilePath(file.getAbsolutePath());
            fileInfo.setRelativePath(getRelativePath(file.getAbsolutePath(), projectPath));
            
            String content = fileUtils.readFileContent(file);
            fileInfo.setPackageName(extractPackageFromFile(content));
            fileInfo.setClassName(extractClassName(file.getName()));
            fileInfo.setFileType(determineFileType(content));
            
            fileInfos.add(fileInfo);
        }
        
        return fileInfos;
    }
    
    private String extractClassName(String fileName) {
        return fileName.replace(".java", "");
    }
    
    private String determineFileType(String content) {
        if (content.contains("@RestController") || content.contains("@Controller")) {
            return "Controller";
        } else if (content.contains("@Service")) {
            return "Service";
        } else if (content.contains("@Repository")) {
            return "Repository";
        } else if (content.contains("@Entity")) {
            return "Entity";
        } else if (content.contains("@Configuration")) {
            return "Configuration";
        }
        return "Class";
    }
    
    private List<ProjectStructure.ApiEndpoint> extractApiEndpoints(List<ProjectStructure.FileInfo> javaFiles, String projectPath) {
        List<ProjectStructure.ApiEndpoint> endpoints = new ArrayList<>();
        
        for (ProjectStructure.FileInfo fileInfo : javaFiles) {
            if ("Controller".equals(fileInfo.getFileType())) {
                String content = fileUtils.readFileContent(new File(fileInfo.getFilePath()));
                endpoints.addAll(parseApiEndpoints(content, fileInfo.getClassName()));
            }
        }
        
        return endpoints;
    }
    
    private List<ProjectStructure.ApiEndpoint> parseApiEndpoints(String content, String controllerClass) {
        List<ProjectStructure.ApiEndpoint> endpoints = new ArrayList<>();
        
        Pattern mappingPattern = Pattern.compile("@(Get|Post|Put|Delete|Request)Mapping\\(.*?value\\s*=\\s*\"([^\"]+)\".*?\\)\\s*\\w+\\s+([\\w]+)\\s*\\(");
        Matcher matcher = mappingPattern.matcher(content);
        
        while (matcher.find()) {
            ProjectStructure.ApiEndpoint endpoint = new ProjectStructure.ApiEndpoint();
            endpoint.setMethod(matcher.group(1).toUpperCase());
            endpoint.setPath(matcher.group(2));
            endpoint.setMethodName(matcher.group(3));
            endpoint.setControllerClass(controllerClass);
            endpoint.setDescription("Auto-detected endpoint");
            
            endpoints.add(endpoint);
        }
        
        return endpoints;
    }
    
    private Map<String, Object> generateProjectMetadata(String projectPath) {
        Map<String, Object> metadata = new HashMap<>();
        
        File pomFile = new File(projectPath, "pom.xml");
        if (pomFile.exists()) {
            metadata.put("buildTool", "Maven");
            metadata.put("hasPom", true);
            
            // Try to detect database type from pom.xml
            String pomContent = fileUtils.readFileContent(pomFile);
            if (pomContent.contains("sqlite-jdbc")) {
                metadata.put("databaseType", "SQLITE");
            } else if (pomContent.contains("postgresql")) {
                metadata.put("databaseType", "POSTGRESQL");
            } else if (pomContent.contains("mysql-connector")) {
                metadata.put("databaseType", "MYSQL");
            } else if (pomContent.contains("spring-boot-starter-data-jpa")) {
                metadata.put("databaseType", "JPA");
            } else {
                metadata.put("databaseType", "NONE");
            }
        }
        
        File propsFile = new File(projectPath, "src/main/resources/application.properties");
        if (propsFile.exists()) {
            metadata.put("hasApplicationProperties", true);
        }
        
        List<File> javaFiles = fileUtils.findJavaFiles(projectPath);
        metadata.put("totalJavaFiles", javaFiles.size());
        
        return metadata;
    }
}