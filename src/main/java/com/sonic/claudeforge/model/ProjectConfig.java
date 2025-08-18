// src/main/java/com/sonic/claudeforge/model/ProjectConfig.java
package com.sonic.claudeforge.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ProjectConfig {
    
    @NotBlank(message = "Project name is required")
    private String projectName;
    
    @NotBlank(message = "Parent package is required")
    @Pattern(regexp = "^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$", message = "Invalid package name format")
    private String basePackage;
    
    private String description;
    private String version = "1.0.0";
    private String javaVersion = "17";
    private DatabaseType databaseType = DatabaseType.NONE;
    private ProjectStructureStyle projectStructureStyle = ProjectStructureStyle.JAVA_REST_API;
    private String workspacePath;
    
    // @GENERATE_GETTERS_SETTERS
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getBasePackage() { return basePackage; }
    public void setBasePackage(String basePackage) { this.basePackage = basePackage; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getJavaVersion() { return javaVersion; }
    public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }
    
    public DatabaseType getDatabaseType() { return databaseType; }
    public void setDatabaseType(DatabaseType databaseType) { this.databaseType = databaseType; }
    
    public ProjectStructureStyle getProjectStructureStyle() { return projectStructureStyle; }
    public void setProjectStructureStyle(ProjectStructureStyle projectStructureStyle) { this.projectStructureStyle = projectStructureStyle; }
    
    public String getWorkspacePath() { return workspacePath; }
    public void setWorkspacePath(String workspacePath) { this.workspacePath = workspacePath; }
    
    /**
     * Get the full package name: basePackage + projectName
     * Example: basePackage="com.sonic", projectName="smugmugdemo" => "com.sonic.smugmugdemo"
     */
    public String getFullPackage() {
        String cleanProjectName = projectName.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .replaceAll("[-_]", "");
        return basePackage + "." + cleanProjectName;
    }
    
    /**
     * Get workspace path based on project structure style
     */
    public String getEffectiveWorkspacePath() {
        if (projectStructureStyle == ProjectStructureStyle.FULL_STACK_SEPARATED) {
            // For fullstack separated, return parent folder
            return workspacePath;
        }
        return workspacePath + "/" + projectName;
    }
    
    /**
     * Get backend project path for fullstack separated structure
     */
    public String getBackendProjectPath() {
        if (projectStructureStyle == ProjectStructureStyle.FULL_STACK_SEPARATED) {
            return workspacePath + "/" + projectName + "-api";
        }
        return getEffectiveWorkspacePath();
    }
    
    /**
     * Get frontend project path for fullstack separated structure  
     */
    public String getFrontendProjectPath() {
        if (projectStructureStyle == ProjectStructureStyle.FULL_STACK_SEPARATED) {
            return workspacePath + "/" + projectName + "-web";
        }
        return getEffectiveWorkspacePath() + "/src/main/resources/static";
    }
}