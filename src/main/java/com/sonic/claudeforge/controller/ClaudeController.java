// src/main/java/com/sonic/claudeforge/controller/ClaudeController.java
package com.sonic.claudeforge.controller;

import com.sonic.claudeforge.model.ClaudeRequest;
import com.sonic.claudeforge.model.ProjectStructure;
import com.sonic.claudeforge.service.ProjectAnalyzerService;
import com.sonic.claudeforge.service.PromptGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Claude Prompt Generation Controller
 * Generates structured prompts for manual Claude interaction
 */
@RestController
@RequestMapping("/api/claude")
@CrossOrigin(origins = "*")
public class ClaudeController {
    
    private static final Logger logger = LoggerFactory.getLogger(ClaudeController.class);
    
    private final PromptGeneratorService promptGeneratorService;
    private final ProjectAnalyzerService projectAnalyzerService;
    
    public ClaudeController(PromptGeneratorService promptGeneratorService, 
                           ProjectAnalyzerService projectAnalyzerService) {
        this.promptGeneratorService = promptGeneratorService;
        this.projectAnalyzerService = projectAnalyzerService;
    }
    
    @PostMapping("/generate-prompt")
    public ResponseEntity<Map<String, Object>> generatePrompt(@RequestBody ClaudeRequest request) {
        logger.info("Generating structured prompt for Claude");
        
        ProjectStructure projectStructure = null;
        if (request.getProjectContext() != null && !request.getProjectContext().isEmpty()) {
            projectStructure = projectAnalyzerService.analyzeProject(request.getProjectContext());
        }
        
        String structuredPrompt = promptGeneratorService.generateStructuredPrompt(request, projectStructure);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("structuredPrompt", structuredPrompt);
        response.put("projectStructure", projectStructure);
        response.put("originalPrompt", request.getPrompt());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getPromptTemplates() {
        Map<String, String> templates = new HashMap<>();
        templates.put("controller", "Generate a Spring Boot REST controller with CRUD operations");
        templates.put("service", "Generate a Spring Boot service class with business logic");
        templates.put("entity", "Generate a JPA entity class with database mapping");
        templates.put("repository", "Generate a Spring Data JPA repository interface");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("templates", templates);
        
        return ResponseEntity.ok(response);
    }
}
