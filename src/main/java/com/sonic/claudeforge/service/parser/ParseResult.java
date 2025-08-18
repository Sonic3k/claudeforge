// src/main/java/com/sonic/claudeforge/service/parser/ParseResult.java
package com.sonic.claudeforge.service.parser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of code parsing operation
 * Contains parsed files, errors, and metadata from all parsers
 */
public class ParseResult {
    
    private List<ParsedFile> allFiles = new ArrayList<>();
    private Map<String, List<ParsedFile>> parserResults = new HashMap<>();
    private Map<String, String> errors = new HashMap<>();
    private boolean success = false;
    private LocalDateTime parsedAt = LocalDateTime.now();
    private String originalContent;
    private int totalValidFiles = 0;
    private int totalInvalidFiles = 0;
    
    /**
     * Add parser result
     * @param parserType Type of parser
     * @param files Files found by this parser
     */
    public void addParserResult(String parserType, List<ParsedFile> files) {
        parserResults.put(parserType, new ArrayList<>(files));
        updateCounts();
    }
    
    /**
     * Add error from parser
     * @param parserType Type of parser that had error
     * @param errorMessage Error message
     */
    public void addError(String parserType, String errorMessage) {
        errors.put(parserType, errorMessage);
    }
    
    /**
     * Get files by parser type
     * @param parserType Parser type
     * @return List of files from that parser
     */
    public List<ParsedFile> getFilesByParser(String parserType) {
        return parserResults.getOrDefault(parserType, new ArrayList<>());
    }
    
    /**
     * Get all valid files (excluding invalid ones)
     * @return List of valid parsed files
     */
    public List<ParsedFile> getValidFiles() {
        return allFiles.stream()
                .filter(ParsedFile::isValid)
                .toList();
    }
    
    /**
     * Get all invalid files
     * @return List of invalid parsed files
     */
    public List<ParsedFile> getInvalidFiles() {
        return allFiles.stream()
                .filter(file -> !file.isValid())
                .toList();
    }
    
    /**
     * Get summary of parsing results
     * @return Summary string
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Parsing completed at %s\n", parsedAt));
        summary.append(String.format("Total files: %d (Valid: %d, Invalid: %d)\n", 
                allFiles.size(), totalValidFiles, totalInvalidFiles));
        
        if (!parserResults.isEmpty()) {
            summary.append("Files by parser:\n");
            parserResults.forEach((parser, files) -> {
                long validCount = files.stream().filter(ParsedFile::isValid).count();
                summary.append(String.format("  %s: %d files (%d valid)\n", 
                        parser, files.size(), validCount));
            });
        }
        
        if (!errors.isEmpty()) {
            summary.append("Errors:\n");
            errors.forEach((parser, error) -> 
                    summary.append(String.format("  %s: %s\n", parser, error)));
        }
        
        return summary.toString();
    }
    
    /**
     * Check if any parser found files
     * @return true if at least one file was found
     */
    public boolean hasFiles() {
        return !allFiles.isEmpty();
    }
    
    /**
     * Check if any parser had errors
     * @return true if there were errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Get parser types that found files
     * @return List of parser types with results
     */
    public List<String> getSuccessfulParsers() {
        return parserResults.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .toList();
    }
    
    /**
     * Get parser types that had errors
     * @return List of parser types with errors
     */
    public List<String> getFailedParsers() {
        return new ArrayList<>(errors.keySet());
    }
    
    private void updateCounts() {
        totalValidFiles = (int) allFiles.stream().filter(ParsedFile::isValid).count();
        totalInvalidFiles = allFiles.size() - totalValidFiles;
    }
    
    // @GENERATE_GETTERS_SETTERS
    public List<ParsedFile> getAllFiles() { return allFiles; }
    public void setAllFiles(List<ParsedFile> allFiles) { 
        this.allFiles = allFiles;
        updateCounts();
    }
    
    public Map<String, List<ParsedFile>> getParserResults() { return parserResults; }
    public void setParserResults(Map<String, List<ParsedFile>> parserResults) { this.parserResults = parserResults; }
    
    public Map<String, String> getErrors() { return errors; }
    public void setErrors(Map<String, String> errors) { this.errors = errors; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public LocalDateTime getParsedAt() { return parsedAt; }
    public void setParsedAt(LocalDateTime parsedAt) { this.parsedAt = parsedAt; }
    
    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }
    
    public int getTotalValidFiles() { return totalValidFiles; }
    public int getTotalInvalidFiles() { return totalInvalidFiles; }
}