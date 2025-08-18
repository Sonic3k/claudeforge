package com.sonic.claudeforge.config;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * FreeMarker Configuration for template processing
 */
@Component
public class FreemarkerConfig {

    @Bean
    public Configuration freemarkerConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        
        try {
            // Set template directory
            cfg.setClassForTemplateLoading(this.getClass(), "/templates/");
            
            // Set default encoding
            cfg.setDefaultEncoding("UTF-8");
            
            // Set template exception handler
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            
            // Don't log exceptions inside FreeMarker
            cfg.setLogTemplateExceptions(false);
            
            // Wrap unchecked exceptions thrown during template processing
            cfg.setWrapUncheckedExceptions(true);
            
            // Don't fall back to higher scopes when reading a null loop variable
            cfg.setFallbackOnNullLoopVariable(false);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure FreeMarker", e);
        }
        
        return cfg;
    }
}