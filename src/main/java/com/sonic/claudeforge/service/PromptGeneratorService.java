// src/main/java/com/sonic/claudeforge/service/PromptGeneratorService.java
package com.sonic.claudeforge.service;

import com.sonic.claudeforge.model.ClaudeRequest;
import com.sonic.claudeforge.model.ProjectStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Prompt Generator Service
 * Generates structured prompts for Claude based on project context
 */
@Service
public class PromptGeneratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(PromptGeneratorService.class);
    
    public String generateStructuredPrompt(ClaudeRequest request, ProjectStructure projectStructure) {
        StringBuilder prompt = new StringBuilder();
        
        // User request first
        prompt.append("# User Request\n");
        prompt.append(request.getPrompt()).append("\n\n");
        
        // Project context section (only project structure)
        if (projectStructure != null) {
            prompt.append("# Project Structure\n");
            prompt.append("```\n");
            prompt.append(projectStructure.getProjectTree());
            prompt.append("```\n\n");
            
            // Existing API endpoints (if any)
            if (!projectStructure.getApiEndpoints().isEmpty()) {
                prompt.append("## Existing API Endpoints\n");
                for (ProjectStructure.ApiEndpoint endpoint : projectStructure.getApiEndpoints()) {
                    prompt.append("- ").append(endpoint.getMethod()).append(" ")
                           .append(endpoint.getPath()).append(" (")
                           .append(endpoint.getControllerClass()).append(".")
                           .append(endpoint.getMethodName()).append(")\n");
                }
                prompt.append("\n");
            }
        }
        
        // Framework specific requirements
        prompt.append("# Framework Requirements\n");
        String framework = request.getFramework();
        if ("java".equals(framework) || "both".equals(framework)) {
            prompt.append("## Java Spring Boot Requirements\n");
            prompt.append("- Use Spring Boot 3.3.12 with Java 17\n");
            prompt.append("- Follow existing project structure and naming conventions\n");
            prompt.append("- Use RELATIVE paths from project root (e.g., src/main/java/...)\n");
            prompt.append("- Add file path comments at the top of each file\n");
            prompt.append("- For Model/DTO/Entity classes: NO custom constructors, only default constructor\n");
            prompt.append("- Controllers must have JavaDoc documentation\n");
            prompt.append("- Use @RequestParam(name=\"paramName\", required=true) for Spring Boot 3.3.12 compatibility\n");
            prompt.append("- GlobalExceptionHandler with @RestControllerAdvice is already generated\n");
            prompt.append("- RequestLoggingFilter for HTTP request logging is already generated\n");
            
            if (projectStructure != null && projectStructure.getProjectMetadata() != null) {
                Object dbType = projectStructure.getProjectMetadata().get("databaseType");
                if (dbType != null && !"NONE".equals(dbType.toString())) {
                    prompt.append("- Database: ").append(dbType.toString()).append(" is configured\n");
                    prompt.append("- JPA entities should use appropriate annotations\n");
                }
            }
        }
        
        if ("react".equals(framework) || "both".equals(framework)) {
            prompt.append("## ReactJS Requirements\n");
            prompt.append("- Use functional components with hooks\n");
            prompt.append("- Use TypeScript if specified\n");
            prompt.append("- Follow modern React patterns\n");
            prompt.append("- Include proper error handling\n");
            prompt.append("- Use relative paths for imports\n");
        }
        
        // GitHub knowledge option
        if (request.isUseGithubKnowledge()) {
            prompt.append("\n# GitHub Project Knowledge\n");
            prompt.append("Please refer to the GitHub repository information in your Project Knowledge when generating code.\n");
            prompt.append("Follow the patterns, conventions, and architecture from the existing codebase.\n");
        }
        
        prompt.append("\n");
        
        // Expected format
        prompt.append("# Expected Output Format\n");
        prompt.append("Return code in the following format with RELATIVE paths:\n");
        
        if ("java".equals(framework) || "both".equals(framework)) {
            prompt.append("\n**Java files:**\n");
            prompt.append("```java\n");
            prompt.append("// src/main/java/com/sonic/packagename/ClassName.java\n");
            prompt.append("package com.sonic.packagename;\n");
            prompt.append("\n");
            prompt.append("// Your generated Java code here\n");
            prompt.append("```\n");
        }
        
        if ("react".equals(framework) || "both".equals(framework)) {
            prompt.append("\n**React files:**\n");
            prompt.append("```tsx\n");
            prompt.append("// src/components/ComponentName.tsx\n");
            prompt.append("// Your generated React code here\n");
            prompt.append("```\n");
        }
        
        return prompt.toString();
    }
}