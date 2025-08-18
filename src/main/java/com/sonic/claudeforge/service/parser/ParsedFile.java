// src/main/java/com/sonic/claudeforge/service/parser/ParsedFile.java
package com.sonic.claudeforge.service.parser;

/**
 * Represents a parsed file from Claude response
 */
public class ParsedFile {
    
    private String fileName;
    private String filePath;
    private String content;
    private String fileType;
    private String parserType;
    private boolean isValid;
    private String errorMessage;
    
    public ParsedFile() {}
    
    public ParsedFile(String filePath, String content, String fileType, String parserType) {
        this.filePath = filePath;
        this.content = content;
        this.fileType = fileType;
        this.parserType = parserType;
        this.fileName = extractFileNameFromPath(filePath);
        this.isValid = true;
    }
    
    /**
     * Create an invalid file with error message
     */
    public static ParsedFile invalid(String filePath, String errorMessage) {
        ParsedFile file = new ParsedFile();
        file.filePath = filePath;
        file.fileName = extractFileNameFromPath(filePath);
        file.isValid = false;
        file.errorMessage = errorMessage;
        return file;
    }
    
    private static String extractFileNameFromPath(String filePath) {
        if (filePath == null) return null;
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }
    
    // @GENERATE_GETTERS_SETTERS
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { 
        this.filePath = filePath;
        this.fileName = extractFileNameFromPath(filePath);
    }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public String getParserType() { return parserType; }
    public void setParserType(String parserType) { this.parserType = parserType; }
    
    public boolean isValid() { return isValid; }
    public void setValid(boolean valid) { this.isValid = valid; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}