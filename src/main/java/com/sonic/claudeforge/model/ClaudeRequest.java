// src/main/java/com/sonic/claudeforge/model/ClaudeRequest.java
package com.sonic.claudeforge.model;

public class ClaudeRequest {
    
    private String prompt;
    private String projectContext;
    private String framework = "java"; // java, react, both
    private boolean useGithubKnowledge = false;
    
    // @GENERATE_GETTERS_SETTERS
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    
    public String getProjectContext() { return projectContext; }
    public void setProjectContext(String projectContext) { this.projectContext = projectContext; }
    
    public String getFramework() { return framework; }
    public void setFramework(String framework) { this.framework = framework; }
    
    public boolean isUseGithubKnowledge() { return useGithubKnowledge; }
    public void setUseGithubKnowledge(boolean useGithubKnowledge) { this.useGithubKnowledge = useGithubKnowledge; }
}
