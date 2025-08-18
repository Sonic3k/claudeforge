// src/main/java/com/claudeforge/config/ClaudeForgeProperties.java
package com.sonic.claudeforge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClaudeForge Configuration Properties
 */
@Component
@ConfigurationProperties(prefix = "claudeforge")
public class ClaudeForgeProperties {
    
    private Workspace workspace = new Workspace();
    private Templates templates = new Templates();
    private Generated generated = new Generated();
    
    // Getters and Setters
    public Workspace getWorkspace() { return workspace; }
    public void setWorkspace(Workspace workspace) { this.workspace = workspace; }
    
    public Templates getTemplates() { return templates; }
    public void setTemplates(Templates templates) { this.templates = templates; }
    
    public Generated getGenerated() { return generated; }
    public void setGenerated(Generated generated) { this.generated = generated; }
    
    public static class Workspace {
        private String basePath;
        
        public String getBasePath() { return basePath; }
        public void setBasePath(String basePath) { this.basePath = basePath; }
    }
    
    public static class Templates {
        private String path;
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
    
    public static class Generated {
        private Projects projects = new Projects();
        
        public Projects getProjects() { return projects; }
        public void setProjects(Projects projects) { this.projects = projects; }
        
        public static class Projects {
            private String path;
            
            public String getPath() { return path; }
            public void setPath(String path) { this.path = path; }
        }
    }
}