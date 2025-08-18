// src/main/java/com/sonic/claudeforge/ClaudeForgeApplication.java
package com.sonic.claudeforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClaudeForge - AI-Powered Code Generation Tool
 * 
 * Main application class for ClaudeForge, a development productivity tool
 * that generates structured prompts for Claude AI and parses responses
 * to automatically generate Spring Boot code files.
 * 
 * @author ClaudeForge Team
 * @version 1.0.0
 */
@SpringBootApplication
public class ClaudeForgeApplication {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    🚀 ClaudeForge - AI Code Generation Tool    ");
        System.out.println("=================================================");
        System.out.println("Starting ClaudeForge Application...");
        
        SpringApplication.run(ClaudeForgeApplication.class, args);
        
        System.out.println("✅ ClaudeForge is running!");
        System.out.println("🌐 Web Interface: http://localhost:8099");
        System.out.println("🗄️  H2 Console: http://localhost:8099/h2-console");
        System.out.println("=================================================");
    }
}