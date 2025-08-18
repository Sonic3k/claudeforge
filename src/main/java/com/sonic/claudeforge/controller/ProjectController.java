// src/main/java/com/sonic/claudeforge/controller/ProjectController.java
package com.sonic.claudeforge.controller;

import com.sonic.claudeforge.model.ProjectConfig;
import com.sonic.claudeforge.service.CodeGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Project Management Controller
 * Handles project creation operations
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    
    private final CodeGeneratorService codeGeneratorService;
    
    public ProjectController(CodeGeneratorService codeGeneratorService) {
        this.codeGeneratorService = codeGeneratorService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createProject(@Valid @RequestBody ProjectConfig config) {
        logger.info("Creating new project: {}", config.getProjectName());
        
        String workspacePath = codeGeneratorService.generateProject(config);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Project created successfully");
        response.put("projectName", config.getProjectName());
        response.put("workspacePath", workspacePath);
        response.put("basePackage", config.getBasePackage());
        
        return ResponseEntity.ok(response);
    }
}