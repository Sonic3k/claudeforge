// src/main/java/com/sonic/claudeforge/service/parser/CodeParser.java
package com.sonic.claudeforge.service.parser;

import java.util.List;

/**
 * Interface for parsing different types of code from Claude responses
 */
public interface CodeParser {
    
    /**
     * Parse content and extract files of specific type
     * @param content The raw content from Claude response
     * @return List of parsed files
     */
    List<ParsedFile> parse(String content);
    
    /**
     * Get the supported file extensions
     * @return Array of supported extensions (e.g., [".java", ".kt"])
     */
    String[] getSupportedExtensions();
    
    /**
     * Get the parser type name
     * @return Parser type (e.g., "Java", "React", "CSS")
     */
    String getParserType();
    
    /**
     * Check if this parser can handle the given content
     * @param content The content to check
     * @return true if this parser can handle the content
     */
    boolean canHandle(String content);
}