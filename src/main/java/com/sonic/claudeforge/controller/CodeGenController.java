// src/main/java/com/sonic/claudeforge/controller/CodeGenController.java
package com.sonic.claudeforge.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sonic.claudeforge.model.GeneratedCode;
import com.sonic.claudeforge.service.CodeGeneratorService;
import com.sonic.claudeforge.service.parser.CodeParseManager;
import com.sonic.claudeforge.service.parser.ParseResult;

/**
 * Enhanced Code Generation Controller with modular parser support
 * Supports multiple code types: Java, React, CSS, HTML
 */
@RestController
@RequestMapping("/api/codegen")
@CrossOrigin(origins = "*")
public class CodeGenController {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeGenController.class);
    
    private final CodeGeneratorService codeGeneratorService;
    
    public CodeGenController(CodeGeneratorService codeGeneratorService) {
        this.codeGeneratorService = codeGeneratorService;
    }
    
    @PostMapping("/parse-and-generate")
    public ResponseEntity<Map<String, Object>> parseAndGenerateCode(@RequestBody Map<String, String> request) {
        String claudeResponse = request.get("claudeResponse");
        String workspacePath = request.get("workspacePath");
        
        logger.info("Parsing Claude response and generating code files at: {}", workspacePath);
        
        GeneratedCode generatedCode = codeGeneratorService.parseAndGenerateCode(claudeResponse, workspacePath);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Code files generated successfully using modular parser system");
        response.put("generatedCode", generatedCode);
        response.put("totalFiles", generatedCode.getFiles().size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * NEW: Debug parser conflicts and priorities
     */
    @PostMapping("/debug-parsers")
    public ResponseEntity<Map<String, Object>> debugParsers(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        
        logger.info("Debugging parser conflicts for content (length: {})", content.length());
        
        List<CodeParseManager.ParserInfo> allParsers = codeGeneratorService.getAvailableParsers();
        List<String> applicableParsers = codeGeneratorService.detectApplicableParsers(content);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Parser debug completed");
        response.put("contentLength", content.length());
        response.put("contentPreview", content.length() > 150 ? content.substring(0, 150) + "..." : content);
        
        response.put("allParsers", allParsers);
        response.put("applicableParsers", applicableParsers);
        response.put("totalApplicableParsers", applicableParsers.size());
        
        // Test each parser individually
        Map<String, Object> parserResults = new HashMap<>();
        
        for (CodeParseManager.ParserInfo parserInfo : allParsers) {
            String parserType = parserInfo.getType();
            try {
                GeneratedCode result = codeGeneratorService.parseAndGenerateCodeWithParser(
                    content, "/tmp/test", parserType, false);  // Don't write files for debug
                
                Map<String, Object> parserResult = new HashMap<>();
                parserResult.put("filesFound", result.getFiles().size());
                parserResult.put("canHandle", applicableParsers.contains(parserType));
                
                if (!result.getFiles().isEmpty()) {
                    parserResult.put("fileTypes", result.getFiles().stream()
                        .map(f -> f.getFileType()).distinct().toList());
                    parserResult.put("filePaths", result.getFiles().stream()
                        .map(f -> f.getFilePath()).toList());
                }
                
                parserResults.put(parserType, parserResult);
                
            } catch (Exception e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", e.getMessage());
                errorResult.put("canHandle", applicableParsers.contains(parserType));
                parserResults.put(parserType, errorResult);
            }
        }
        
        response.put("parserResults", parserResults);
        
        // Detect potential conflicts
        List<String> conflicts = new ArrayList<>();
        if (applicableParsers.size() > 1) {
            conflicts.add("Multiple parsers can handle this content: " + String.join(", ", applicableParsers));
        }
        
        response.put("conflicts", conflicts);
        response.put("hasConflicts", !conflicts.isEmpty());
        
        return ResponseEntity.ok(response);
    }
    
    
    /**
     * NEW: Parse with specific parser type
     */
    @PostMapping("/parse-with-parser")
    public ResponseEntity<Map<String, Object>> parseWithSpecificParser(@RequestBody Map<String, String> request) {
        String claudeResponse = request.get("claudeResponse");
        String workspacePath = request.get("workspacePath");
        String parserType = request.get("parserType");
        
        logger.info("Parsing Claude response with {} parser at: {}", parserType, workspacePath);
        
        GeneratedCode generatedCode = codeGeneratorService.parseAndGenerateCodeWithParser(claudeResponse, workspacePath, parserType, true);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Code files generated successfully using " + parserType + " parser");
        response.put("generatedCode", generatedCode);
        response.put("totalFiles", generatedCode.getFiles().size());
        response.put("parserUsed", parserType);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * NEW: Preview parsing without writing files
     */
    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewGeneratedCode(@RequestBody Map<String, String> request) {
        String claudeResponse = request.get("claudeResponse");
        
        logger.info("Previewing generated code from Claude response");
        
        ParseResult parseResult = codeGeneratorService.previewParsing(claudeResponse);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", parseResult.isSuccess());
        response.put("message", "Code preview generated successfully");
        response.put("totalFiles", parseResult.getAllFiles().size());
        response.put("validFiles", parseResult.getTotalValidFiles());
        response.put("invalidFiles", parseResult.getTotalInvalidFiles());
        response.put("parsersUsed", parseResult.getSuccessfulParsers());
        response.put("errors", parseResult.getErrors());
        
        // Convert ParsedFile to simpler format for frontend
        List<Map<String, Object>> files = parseResult.getValidFiles().stream()
                .map(file -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("fileName", file.getFileName());
                    fileInfo.put("filePath", file.getFilePath());
                    fileInfo.put("fileType", file.getFileType());
                    fileInfo.put("parserType", file.getParserType());
                    fileInfo.put("contentLength", file.getContent().length());
                    return fileInfo;
                })
                .toList();
        
        response.put("files", files);
        response.put("summary", parseResult.getSummary());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * NEW: Get available parsers
     */
    @GetMapping("/parsers")
    public ResponseEntity<Map<String, Object>> getAvailableParsers() {
        logger.info("Getting available parsers information");
        
        List<CodeParseManager.ParserInfo> parsers = codeGeneratorService.getAvailableParsers();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Available parsers retrieved successfully");
        response.put("parsers", parsers);
        response.put("totalParsers", parsers.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * NEW: Detect applicable parsers for content
     */
    @PostMapping("/detect-parsers")
    public ResponseEntity<Map<String, Object>> detectApplicableParsers(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        
        logger.info("Detecting applicable parsers for content (length: {})", content.length());
        
        List<String> applicableParsers = codeGeneratorService.detectApplicableParsers(content);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Parser detection completed");
        response.put("applicableParsers", applicableParsers);
        response.put("totalApplicableParsers", applicableParsers.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * NEW: Test parsing with debug information
     */
    @PostMapping("/test-parse")
    public ResponseEntity<Map<String, Object>> testParsing(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        
        logger.info("Testing parsing for content (length: {})", content.length());
        
        ParseResult parseResult = codeGeneratorService.previewParsing(content);
        
        // Get detailed parser information
        List<String> applicableParsers = codeGeneratorService.detectApplicableParsers(content);
        List<CodeParseManager.ParserInfo> allParsers = codeGeneratorService.getAvailableParsers();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Test parsing completed");
        response.put("contentLength", content.length());
        response.put("contentPreview", content.length() > 200 ? content.substring(0, 200) + "..." : content);
        
        response.put("applicableParsers", applicableParsers);
        response.put("allParsers", allParsers);
        response.put("totalFiles", parseResult.getAllFiles().size());
        response.put("validFiles", parseResult.getTotalValidFiles());
        response.put("invalidFiles", parseResult.getTotalInvalidFiles());
        response.put("parsersUsed", parseResult.getSuccessfulParsers());
        response.put("errors", parseResult.getErrors());
        response.put("summary", parseResult.getSummary());
        
        // Detailed file information
        List<Map<String, Object>> fileDetails = parseResult.getAllFiles().stream()
                .map(file -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("fileName", file.getFileName());
                    fileInfo.put("filePath", file.getFilePath());
                    fileInfo.put("fileType", file.getFileType());
                    fileInfo.put("parserType", file.getParserType());
                    fileInfo.put("valid", file.isValid());
                    fileInfo.put("errorMessage", file.getErrorMessage());
                    fileInfo.put("contentLength", file.getContent() != null ? file.getContent().length() : 0);
                    fileInfo.put("contentPreview", file.getContent() != null && file.getContent().length() > 100 
                        ? file.getContent().substring(0, 100) + "..." 
                        : file.getContent());
                    return fileInfo;
                })
                .toList();
        
        response.put("fileDetails", fileDetails);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/workspace-info")
    public ResponseEntity<Map<String, Object>> getWorkspaceInfo(
            @RequestParam(name = "workspacePath", required = true) String workspacePath) {
        logger.info("Getting workspace information for: {}", workspacePath);
        
        Map<String, Object> workspaceInfo = new HashMap<>();
        workspaceInfo.put("path", workspacePath);
        workspaceInfo.put("exists", java.nio.file.Files.exists(java.nio.file.Paths.get(workspacePath)));
        
        if (java.nio.file.Files.exists(java.nio.file.Paths.get(workspacePath))) {
            java.io.File workspaceDir = new java.io.File(workspacePath);
            java.io.File[] files = workspaceDir.listFiles();
            workspaceInfo.put("fileCount", files != null ? files.length : 0);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("workspaceInfo", workspaceInfo);
        
        return ResponseEntity.ok(response);
    }
}