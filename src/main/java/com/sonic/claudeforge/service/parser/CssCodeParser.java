// src/main/java/com/sonic/claudeforge/service/parser/CssCodeParser.java
package com.sonic.claudeforge.service.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced Parser for CSS code from Claude responses
 * Supports CSS, SCSS, and SASS files with both // and /*
 */
@Component
public class CssCodeParser implements CodeParser {
    
    private static final Logger logger = LoggerFactory.getLogger(CssCodeParser.class);
    
    // Pattern for CSS code blocks in markdown format
    private static final Pattern CSS_CODE_BLOCK_PATTERN = 
        Pattern.compile("```(css|scss|sass)\\s*(?://\\s*|/\\*\\s*)([^\\n]+?)(?:\\s*\\*/)?\\s*\\n([\\s\\S]*?)```");
    
    // Pattern for raw CSS code with path comments (both // and /* */ style)
    private static final Pattern RAW_CSS_PATTERN = 
        Pattern.compile("(?://\\s*|/\\*\\s*)([^\\r\\n]+\\.(css|scss|sass))(?:\\s*\\*/)?\\s*[\\r\\n]+([\\s\\S]+?)(?=[\\r\\n]+(?://\\s*|/\\*\\s*)[^\\r\\n]+\\.(css|scss|sass)|\\Z)", Pattern.MULTILINE);
    
    // Pattern for single CSS file (both comment styles)
    private static final Pattern SINGLE_CSS_PATTERN = 
        Pattern.compile("^\\s*(?://\\s*|/\\*\\s*)([^\\r\\n]+\\.(css|scss|sass))(?:\\s*\\*/)?\\s*[\\r\\n]+([\\s\\S]+)$", Pattern.MULTILINE);
    
    @Override
    public List<ParsedFile> parse(String content) {
        logger.debug("Parsing CSS code from content (length: {})", content.length());
        
        List<ParsedFile> files = new ArrayList<>();
        
        // 1. Try parsing markdown code blocks first
        files.addAll(parseMarkdownCodeBlocks(content));
        
        // 2. If no markdown blocks found, try raw CSS code
        if (files.isEmpty()) {
            files.addAll(parseRawCssCode(content));
        }
        
        // 3. If still no files, try single file detection
        if (files.isEmpty()) {
            files.addAll(parseSingleCssFile(content));
        }
        
        logger.info("CSS parser found {} files", files.size());
        return files;
    }
    
    private List<ParsedFile> parseMarkdownCodeBlocks(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = CSS_CODE_BLOCK_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String fileType = matcher.group(1);
            String filePath = matcher.group(2).trim();
            String code = matcher.group(3).trim();
            
            ParsedFile file = createCssFile(filePath, code, fileType);
            if (file != null) {
                files.add(file);
                logger.debug("Found CSS markdown block: {} ({} chars)", filePath, code.length());
            }
        }
        
        return files;
    }
    
    private List<ParsedFile> parseRawCssCode(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = RAW_CSS_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String fileExtension = matcher.group(2);
            String code = matcher.group(3).trim();
            
            ParsedFile file = createCssFile(filePath, code, fileExtension);
            if (file != null) {
                files.add(file);
                logger.debug("Found raw CSS file: {} ({} chars)", filePath, code.length());
            }
        }
        
        return files;
    }
    
    private List<ParsedFile> parseSingleCssFile(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = SINGLE_CSS_PATTERN.matcher(content.trim());
        
        if (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String fileExtension = matcher.group(2);
            String code = matcher.group(3).trim();
            
            ParsedFile file = createCssFile(filePath, code, fileExtension);
            if (file != null) {
                files.add(file);
                logger.debug("Found single CSS file: {} ({} chars)", filePath, code.length());
            }
        }
        
        return files;
    }
    
    private ParsedFile createCssFile(String filePath, String content, String extension) {
        // Enhanced validation for CSS content
        if (!isValidCssContent(content)) {
            logger.warn("Invalid CSS content for file: {}", filePath);
            return ParsedFile.invalid(filePath, "Invalid CSS content - no valid CSS rules found");
        }
        
        // Determine file type based on content and extension
        String fileType = determineCssFileType(content, extension);
        
        return new ParsedFile(filePath, content, fileType, "CSS");
    }
    
    private boolean isValidCssContent(String content) {
        // Enhanced validation for CSS files including Tailwind
        return content.contains("{") && content.contains("}") ||
               content.contains(":") ||
               content.contains("@import") ||
               content.contains("@media") ||
               content.contains("@tailwind") ||
               content.contains("@layer") ||
               content.contains("@apply") ||
               content.contains("/*") ||
               content.contains("//") ||
               // CSS properties pattern
               content.matches(".*[a-zA-Z-]+\\s*:\\s*[^;]+;.*") ||
               content.trim().length() > 0; // Allow non-empty CSS files
    }
    
    private String determineCssFileType(String content, String extension) {
        if (content.contains("@tailwind")) {
            return "Tailwind CSS";
        } else if (content.contains("@import")) {
            return "CSS Imports";
        } else if (content.contains("@media")) {
            return "Responsive CSS";
        } else if (content.contains("@keyframes")) {
            return "CSS Animations";
        } else if (extension != null && extension.equals("scss")) {
            return "SCSS";
        } else if (extension != null && extension.equals("sass")) {
            return "SASS";
        } else if (content.contains("$") && content.contains("&")) {
            return "SCSS";
        } else if (content.contains("//") && !content.contains("/*")) {
            return "SASS";
        }
        return "CSS";
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".css", ".scss", ".sass"};
    }
    
    @Override
    public String getParserType() {
        return "CSS";
    }
    
    @Override
    public boolean canHandle(String content) {
        // Enhanced detection for CSS content with both comment styles
        return content.contains("```css") ||
               content.contains("```scss") ||
               content.contains("```sass") ||
               // Check for CSS file paths with both comment styles
               (content.contains(".css") || content.contains(".scss") || content.contains(".sass")) &&
               (content.contains("// ") || content.contains("/* ")) ||
               // Check for CSS-specific patterns
               content.contains("@tailwind") ||
               content.contains("@layer") ||
               content.contains("@apply") ||
               content.contains("@import") ||
               content.contains("@media") ||
               // CSS specific syntax
               (content.contains("{") && content.contains("}") && content.contains(":")) ||
               // Tailwind CSS utilities
               content.contains("bg-") || content.contains("text-") || content.contains("p-") ||
               // SCSS/SASS syntax
               content.contains("$") && content.contains(":") ||
               content.contains("&") && content.contains("{");
    }
}