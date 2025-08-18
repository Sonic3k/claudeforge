// src/main/java/com/sonic/claudeforge/util/MermaidGenerator.java
package com.sonic.claudeforge.util;

import com.sonic.claudeforge.model.ProjectStructure;
import org.springframework.stereotype.Component;

/**
 * Mermaid diagram generator for project structure visualization
 */
@Component
public class MermaidGenerator {
    
    /**
     * Generate Mermaid diagram for project structure
     * @param projectStructure Project structure information
     * @return Mermaid diagram as string
     */
    public String generateProjectDiagram(ProjectStructure projectStructure) {
        StringBuilder mermaid = new StringBuilder();
        
        mermaid.append("graph TD\n");
        mermaid.append("    A[").append(projectStructure.getProjectName()).append("] --> B[Controllers]\n");
        mermaid.append("    A --> C[Services]\n");
        mermaid.append("    A --> D[Repositories]\n");
        mermaid.append("    A --> E[Entities]\n");
        mermaid.append("    A --> F[Configuration]\n");
        
        // Add file type counts
        if (projectStructure.getJavaFiles() != null) {
            long controllers = projectStructure.getJavaFiles().stream()
                    .filter(f -> "Controller".equals(f.getFileType())).count();
            long services = projectStructure.getJavaFiles().stream()
                    .filter(f -> "Service".equals(f.getFileType())).count();
            long repositories = projectStructure.getJavaFiles().stream()
                    .filter(f -> "Repository".equals(f.getFileType())).count();
            long entities = projectStructure.getJavaFiles().stream()
                    .filter(f -> "Entity".equals(f.getFileType())).count();
            long configs = projectStructure.getJavaFiles().stream()
                    .filter(f -> "Configuration".equals(f.getFileType())).count();
            
            if (controllers > 0) {
                mermaid.append("    B --> B1[").append(controllers).append(" Controllers]\n");
            }
            if (services > 0) {
                mermaid.append("    C --> C1[").append(services).append(" Services]\n");
            }
            if (repositories > 0) {
                mermaid.append("    D --> D1[").append(repositories).append(" Repositories]\n");
            }
            if (entities > 0) {
                mermaid.append("    E --> E1[").append(entities).append(" Entities]\n");
            }
            if (configs > 0) {
                mermaid.append("    F --> F1[").append(configs).append(" Configs]\n");
            }
        }
        
        return mermaid.toString();
    }
}