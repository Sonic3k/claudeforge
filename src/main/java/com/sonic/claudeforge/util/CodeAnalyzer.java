package com.sonic.claudeforge.util;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code Analysis Utility
 * Analyzes Java code structure and extracts information
 */
@Component
public class CodeAnalyzer {
    
    private static final Pattern CLASS_PATTERN = Pattern.compile("(public\\s+)?(class|interface|enum)\\s+(\\w+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(public|private|protected)\\s+.*?\\s+(\\w+)\\s*\\([^)]*\\)");
    private static final Pattern FIELD_PATTERN = Pattern.compile("(private|public|protected)\\s+([\\w<>\\[\\]]+)\\s+(\\w+)\\s*[;=]");
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile("@(\\w+)");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.]+);");
    
    /**
     * Extract class information from Java code
     */
    public ClassInfo extractClassInfo(String javaCode) {
        ClassInfo classInfo = new ClassInfo();
        
        // Extract class name
        Matcher classMatcher = CLASS_PATTERN.matcher(javaCode);
        if (classMatcher.find()) {
            classInfo.setClassName(classMatcher.group(3));
            classInfo.setClassType(classMatcher.group(2));
        }
        
        // Extract methods
        List<MethodInfo> methods = new ArrayList<>();
        Matcher methodMatcher = METHOD_PATTERN.matcher(javaCode);
        while (methodMatcher.find()) {
            MethodInfo method = new MethodInfo();
            method.setVisibility(methodMatcher.group(1));
            method.setMethodName(methodMatcher.group(2));
            methods.add(method);
        }
        classInfo.setMethods(methods);
        
        // Extract fields
        List<FieldInfo> fields = new ArrayList<>();
        Matcher fieldMatcher = FIELD_PATTERN.matcher(javaCode);
        while (fieldMatcher.find()) {
            FieldInfo field = new FieldInfo();
            field.setVisibility(fieldMatcher.group(1));
            field.setFieldType(fieldMatcher.group(2));
            field.setFieldName(fieldMatcher.group(3));
            fields.add(field);
        }
        classInfo.setFields(fields);
        
        // Extract annotations
        Set<String> annotations = new HashSet<>();
        Matcher annotationMatcher = ANNOTATION_PATTERN.matcher(javaCode);
        while (annotationMatcher.find()) {
            annotations.add(annotationMatcher.group(1));
        }
        classInfo.setAnnotations(annotations);
        
        // Extract imports
        Set<String> imports = new HashSet<>();
        Matcher importMatcher = IMPORT_PATTERN.matcher(javaCode);
        while (importMatcher.find()) {
            imports.add(importMatcher.group(1));
        }
        classInfo.setImports(imports);
        
        return classInfo;
    }
    
    /**
     * Check if code contains Spring Boot annotations
     */
    public boolean isSpringBootClass(String javaCode) {
        String[] springAnnotations = {
            "@SpringBootApplication", "@RestController", "@Controller", 
            "@Service", "@Repository", "@Component", "@Configuration"
        };
        
        for (String annotation : springAnnotations) {
            if (javaCode.contains(annotation)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extract API endpoints from controller code
     */
    public List<EndpointInfo> extractEndpoints(String controllerCode) {
        List<EndpointInfo> endpoints = new ArrayList<>();
        
        // Pattern for mapping annotations
        Pattern mappingPattern = Pattern.compile(
            "@(Get|Post|Put|Delete|Patch|Request)Mapping\\s*\\(.*?(?:value\\s*=\\s*)?\"([^\"]+)\".*?\\)\\s*" +
            "(?:public\\s+)?([\\w<>\\[\\]]+)\\s+(\\w+)\\s*\\("
        );
        
        Matcher matcher = mappingPattern.matcher(controllerCode);
        while (matcher.find()) {
            EndpointInfo endpoint = new EndpointInfo();
            endpoint.setHttpMethod(matcher.group(1).toUpperCase());
            endpoint.setPath(matcher.group(2));
            endpoint.setReturnType(matcher.group(3));
            endpoint.setMethodName(matcher.group(4));
            endpoints.add(endpoint);
        }
        
        return endpoints;
    }
    
    // Inner classes for structured data
    public static class ClassInfo {
        private String className;
        private String classType;
        private List<MethodInfo> methods = new ArrayList<>();
        private List<FieldInfo> fields = new ArrayList<>();
        private Set<String> annotations = new HashSet<>();
        private Set<String> imports = new HashSet<>();
        
        // @GENERATE_GETTERS_SETTERS
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public String getClassType() { return classType; }
        public void setClassType(String classType) { this.classType = classType; }
        
        public List<MethodInfo> getMethods() { return methods; }
        public void setMethods(List<MethodInfo> methods) { this.methods = methods; }
        
        public List<FieldInfo> getFields() { return fields; }
        public void setFields(List<FieldInfo> fields) { this.fields = fields; }
        
        public Set<String> getAnnotations() { return annotations; }
        public void setAnnotations(Set<String> annotations) { this.annotations = annotations; }
        
        public Set<String> getImports() { return imports; }
        public void setImports(Set<String> imports) { this.imports = imports; }
    }
    
    public static class MethodInfo {
        private String visibility;
        private String methodName;
        private String returnType;
        
        // @GENERATE_GETTERS_SETTERS
        public String getVisibility() { return visibility; }
        public void setVisibility(String visibility) { this.visibility = visibility; }
        
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        
        public String getReturnType() { return returnType; }
        public void setReturnType(String returnType) { this.returnType = returnType; }
    }
    
    public static class FieldInfo {
        private String visibility;
        private String fieldType;
        private String fieldName;
        
        // @GENERATE_GETTERS_SETTERS  
        public String getVisibility() { return visibility; }
        public void setVisibility(String visibility) { this.visibility = visibility; }
        
        public String getFieldType() { return fieldType; }
        public void setFieldType(String fieldType) { this.fieldType = fieldType; }
        
        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    }
    
    public static class EndpointInfo {
        private String httpMethod;
        private String path;
        private String returnType;
        private String methodName;
        
        // @GENERATE_GETTERS_SETTERS
        public String getHttpMethod() { return httpMethod; }
        public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public String getReturnType() { return returnType; }
        public void setReturnType(String returnType) { this.returnType = returnType; }
        
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
    }
}
