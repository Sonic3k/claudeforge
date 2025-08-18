// src/main/java/com/sonic/claudeforge/model/GeneratedCode.java
package com.sonic.claudeforge.model;

import java.time.LocalDateTime;
import java.util.List;

public class GeneratedCode {
    
    private List<GeneratedFile> files;
    private String workspacePath;
    private LocalDateTime generatedAt;
    private String claudeResponse;
    
    // @GENERATE_GETTERS_SETTERS
    public List<GeneratedFile> getFiles() { return files; }
    public void setFiles(List<GeneratedFile> files) { this.files = files; }
    
    public String getWorkspacePath() { return workspacePath; }
    public void setWorkspacePath(String workspacePath) { this.workspacePath = workspacePath; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public String getClaudeResponse() { return claudeResponse; }
    public void setClaudeResponse(String claudeResponse) { this.claudeResponse = claudeResponse; }
    
    public static class GeneratedFile {
        private String fileName;
        private String filePath;
        private String content;
        private String fileType;
        
        // @GENERATE_GETTERS_SETTERS
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
    }
}