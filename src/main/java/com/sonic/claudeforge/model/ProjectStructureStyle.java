// src/main/java/com/sonic/claudeforge/model/ProjectStructureStyle.java
package com.sonic.claudeforge.model;

public enum ProjectStructureStyle {
    JAVA_REST_API("java-rest-api", "Java Rest API", "Single Java Spring Boot project for REST API only"),
    JAVA_REST_API_WITH_STATIC("java-rest-api-static", "Java Rest API + HTML/CSS/JS", "Java Spring Boot with static web resources"),
    FULL_STACK_SEPARATED("fullstack-separated", "Java Rest API + ReactJS (Web)", "Separate backend and frontend projects in parent folder"),
    REACT_WEB_ONLY("react-web-only", "ReactJS (Web)", "Frontend ReactJS project only");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    ProjectStructureStyle(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }
    
    // @GENERATE_GETTERS_SETTERS
    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}