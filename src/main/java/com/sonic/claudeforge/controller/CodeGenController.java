// src/main/java/com/sonic/claudeforge/controller/CodeGenController.java
package com.sonic.claudeforge.controller;

import com.sonic.claudeforge.model.GeneratedCode;
import com.sonic.claudeforge.service.CodeGeneratorService;
import com.sonic.claudeforge.service.parser.CodeParseManager;
import com.sonic.claudeforge.service.parser.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * NEW: Parse with specific parser type
     */
    @PostMapping("/parse-with-parser")
    public ResponseEntity<Map<String, Object>> parseWithSpecificParser(@RequestBody Map<String, String> request) {
        String claudeResponse = request.get("claudeResponse");
        String workspacePath = request.get("workspacePath");
        String parserType = request.get("parserType");
        
        logger.info("Parsing Claude response with {} parser at: {}", parserType, workspacePath);
        
        GeneratedCode generatedCode = codeGeneratorService.parseAndGenerateCodeWithParser(claudeResponse, workspacePath, parserType);
        
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