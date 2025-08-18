// src/main/java/com/sonic/claudeforge/model/ProjectStructure.java
package com.sonic.claudeforge.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ProjectStructure {
    
    private String projectPath;
    private String projectName;
    private String basePackage;
    private String projectTree;
    private List<FileInfo> javaFiles;
    private List<ApiEndpoint> apiEndpoints;
    private Map<String, Object> projectMetadata;
    private String mermaidDiagram;
    private LocalDateTime analyzedAt;
    
    // @GENERATE_GETTERS_SETTERS
    public String getProjectPath() { return projectPath; }
    public void setProjectPath(String projectPath) { this.projectPath = projectPath; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getBasePackage() { return basePackage; }
    public void setBasePackage(String basePackage) { this.basePackage = basePackage; }
    
    public String getProjectTree() { return projectTree; }
    public void setProjectTree(String projectTree) { this.projectTree = projectTree; }
    
    public List<FileInfo> getJavaFiles() { return javaFiles; }
    public void setJavaFiles(List<FileInfo> javaFiles) { this.javaFiles = javaFiles; }
    
    public List<ApiEndpoint> getApiEndpoints() { return apiEndpoints; }
    public void setApiEndpoints(List<ApiEndpoint> apiEndpoints) { this.apiEndpoints = apiEndpoints; }
    
    public Map<String, Object> getProjectMetadata() { return projectMetadata; }
    public void setProjectMetadata(Map<String, Object> projectMetadata) { this.projectMetadata = projectMetadata; }
    
    public String getMermaidDiagram() { return mermaidDiagram; }
    public void setMermaidDiagram(String mermaidDiagram) { this.mermaidDiagram = mermaidDiagram; }
    
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
    
    public static class FileInfo {
        private String fileName;
        private String filePath;
        private String relativePath;
        private String fileType;
        private String className;
        private String packageName;
        
        // @GENERATE_GETTERS_SETTERS
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getRelativePath() { return relativePath; }
        public void setRelativePath(String relativePath) { this.relativePath = relativePath; }
        
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
    }
    
    public static class ApiEndpoint {
        private String method;
        private String path;
        private String controllerClass;
        private String methodName;
        private String description;
        
        // @GENERATE_GETTERS_SETTERS  
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public String getControllerClass() { return controllerClass; }
        public void setControllerClass(String controllerClass) { this.controllerClass = controllerClass; }
        
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}