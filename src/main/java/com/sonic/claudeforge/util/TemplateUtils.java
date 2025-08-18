package com.sonic.claudeforge.util;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Template Utility Class
 * Handles template processing and code formatting
 */
@Component
public class TemplateUtils {
    
    /**
     * Extract expected format from user prompt
     */
    public String extractExpectedFormat(String prompt) {
        // Look for format indicators in prompt
        if (prompt.toLowerCase().contains("json")) {
            return getJsonFormat();
        } else if (prompt.toLowerCase().contains("controller")) {
            return getControllerFormat();
        } else if (prompt.toLowerCase().contains("service")) {
            return getServiceFormat();
        } else if (prompt.toLowerCase().contains("entity") || prompt.toLowerCase().contains("model")) {
            return getEntityFormat();
        }
        
        return getDefaultFormat();
    }
    
    private String getJsonFormat() {
        return """
            Return response in JSON format:
            {
              "files": [
                {
                  "filePath": "src/main/java/...",
                  "content": "// File content here"
                }
              ]
            }
            """;
    }
    
    private String getControllerFormat() {
        return """
            Return Java Controller with:
            - File path comment at top
            - JavaDoc documentation for class and methods
            - Proper Spring annotations
            - REST endpoints with appropriate HTTP methods
            """;
    }
    
    private String getServiceFormat() {
        return """
            Return Java Service with:
            - File path comment at top
            - @Service annotation
            - Business logic methods
            - Exception handling
            """;
    }
    
    private String getEntityFormat() {
        return """
            Return Java Entity/Model with:
            - File path comment at top
            - JPA annotations if needed
            - Field declarations
            - @GENERATE_GETTERS_SETTERS comment instead of actual getters/setters
            """;
    }
    
    private String getDefaultFormat() {
        return """
            Return Java code with:
            - File path comment at top of each file
            - Proper package declarations
            - Appropriate Spring Boot annotations
            - For models: use @GENERATE_GETTERS_SETTERS comment
            - For controllers: include JavaDoc
            """;
    }
    
    /**
     * Generate getter/setter methods for a field
     */
    public String generateGetterSetter(String fieldName, String fieldType) {
        String capitalizedName = capitalize(fieldName);
        
        StringBuilder sb = new StringBuilder();
        
        // Getter
        sb.append("    public ").append(fieldType).append(" get").append(capitalizedName).append("() {\n");
        sb.append("        return ").append(fieldName).append(";\n");
        sb.append("    }\n\n");
        
        // Setter
        sb.append("    public void set").append(capitalizedName).append("(").append(fieldType).append(" ").append(fieldName).append(") {\n");
        sb.append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n");
        sb.append("    }\n");
        
        return sb.toString();
    }
    
    /**
     * Parse fields from model class content
     */
    public String processGetterSetterGeneration(String classContent) {
        if (!classContent.contains("@GENERATE_GETTERS_SETTERS")) {
            return classContent;
        }
        
        StringBuilder result = new StringBuilder();
        Pattern fieldPattern = Pattern.compile("private\\s+(\\w+)\\s+(\\w+);");
        Matcher matcher = fieldPattern.matcher(classContent);
        
        StringBuilder gettersSetters = new StringBuilder();
        while (matcher.find()) {
            String fieldType = matcher.group(1);
            String fieldName = matcher.group(2);
            gettersSetters.append("\n").append(generateGetterSetter(fieldName, fieldType));
        }
        
        result.append(classContent.replace("@GENERATE_GETTERS_SETTERS", gettersSetters.toString()));
        return result.toString();
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
