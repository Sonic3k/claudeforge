// src/main/java/com/sonic/claudeforge/service/parser/HtmlCodeParser.java
package com.sonic.claudeforge.service.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for HTML code from Claude responses
 * Supports HTML files and templates
 */
@Component
public class HtmlCodeParser implements CodeParser {
    
    private static final Logger logger = LoggerFactory.getLogger(HtmlCodeParser.class);
    
    // Pattern for HTML code blocks in markdown format
    private static final Pattern HTML_CODE_BLOCK_PATTERN = 
        Pattern.compile("```html\\s*//\\s*([^\\n]+)\\s*\\n([\\s\\S]*?)```");
    
    // Pattern for raw HTML code with path comments
    private static final Pattern RAW_HTML_PATTERN = 
        Pattern.compile("//\\s*([^\\r\\n]+\\.html?)\\s*[\\r\\n]+([\\s\\S]+?)(?=[\\r\\n]+//\\s*[^\\r\\n]+\\.html?|\\Z)", Pattern.MULTILINE);
    
    // Pattern for single HTML file
    private static final Pattern SINGLE_HTML_PATTERN = 
        Pattern.compile("^\\s*//\\s*([^\\r\\n]+\\.html?)\\s*[\\r\\n]+([\\s\\S]+)$", Pattern.MULTILINE);
    
    // Alternative patterns for HTML without file path
    private static final Pattern HTML_DOCTYPE_PATTERN = 
        Pattern.compile("```html\\s*\\n([\\s\\S]*?<!DOCTYPE html[\\s\\S]*?)```");
    
    @Override
    public List<ParsedFile> parse(String content) {
        logger.debug("Parsing HTML code from content (length: {})", content.length());
        
        List<ParsedFile> files = new ArrayList<>();
        
        // 1. Try parsing markdown code blocks first
        files.addAll(parseMarkdownCodeBlocks(content));
        
        // 2. If no markdown blocks found, try raw HTML code
        if (files.isEmpty()) {
            files.addAll(parseRawHtmlCode(content));
        }
        
        // 3. If still no files, try single file detection
        if (files.isEmpty()) {
            files.addAll(parseSingleHtmlFile(content));
        }
        
        // 4. Try parsing HTML blocks without file paths
        if (files.isEmpty()) {
            files.addAll(parseHtmlWithoutPath(content));
        }
        
        logger.info("HTML parser found {} files", files.size());
        return files;
    }
    
    private List<ParsedFile> parseMarkdownCodeBlocks(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = HTML_CODE_BLOCK_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String code = matcher.group(2).trim();
            
            ParsedFile file = createHtmlFile(filePath, code);
            files.add(file);
            
            logger.debug("Found HTML markdown block: {} ({} chars)", filePath, code.length());
        }
        
        return files;
    }
    
    private List<ParsedFile> parseRawHtmlCode(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = RAW_HTML_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String code = matcher.group(2).trim();
            
            ParsedFile file = createHtmlFile(filePath, code);
            files.add(file);
            
            logger.debug("Found raw HTML file: {} ({} chars)", filePath, code.length());
        }
        
        return files;
    }
    
    private List<ParsedFile> parseSingleHtmlFile(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = SINGLE_HTML_PATTERN.matcher(content.trim());
        
        if (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String code = matcher.group(2).trim();
            
            ParsedFile file = createHtmlFile(filePath, code);
            files.add(file);
            
            logger.debug("Found single HTML file: {} ({} chars)", filePath, code.length());
        }
        
        return files;
    }
    
    private List<ParsedFile> parseHtmlWithoutPath(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = HTML_DOCTYPE_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String code = matcher.group(1).trim();
            
            // Generate default file path
            String filePath = "src/main/resources/static/index.html";
            if (code.contains("<title>")) {
                Pattern titlePattern = Pattern.compile("<title>([^<]+)</title>");
                Matcher titleMatcher = titlePattern.matcher(code);
                if (titleMatcher.find()) {
                    String title = titleMatcher.group(1).toLowerCase()
                        .replaceAll("[^a-z0-9]", "-")
                        .replaceAll("-+", "-")
                        .replaceAll("^-|-$", "");
                    if (!title.isEmpty()) {
                        filePath = "src/main/resources/static/" + title + ".html";
                    }
                }
            }
            
            ParsedFile file = createHtmlFile(filePath, code);
            files.add(file);
            
            logger.debug("Found HTML without path, using: {} ({} chars)", filePath, code.length());
        }
        
        return files;
    }
    
    private ParsedFile createHtmlFile(String filePath, String content) {
        // Validate HTML content
        if (!isValidHtmlContent(content)) {
            return ParsedFile.invalid(filePath, "Invalid HTML content - missing basic HTML structure");
        }
        
        // Determine file type based on content
        String fileType = determineHtmlFileType(content);
        
        return new ParsedFile(filePath, content, fileType, "HTML");
    }
    
    private boolean isValidHtmlContent(String content) {
        // Basic validation for HTML files
        String lowerContent = content.toLowerCase();
        return lowerContent.contains("<html") ||
               lowerContent.contains("<!doctype") ||
               lowerContent.contains("<head") ||
               lowerContent.contains("<body") ||
               lowerContent.contains("<div") ||
               lowerContent.contains("<span") ||
               lowerContent.contains("<p") ||
               (lowerContent.contains("<") && lowerContent.contains(">"));
    }
    
    private String determineHtmlFileType(String content) {
        String lowerContent = content.toLowerCase();
        
        if (lowerContent.contains("<!doctype html")) {
            return "HTML5 Document";
        } else if (lowerContent.contains("<template")) {
            return "HTML Template";
        } else if (lowerContent.contains("@{") || lowerContent.contains("th:")) {
            return "Thymeleaf Template";
        } else if (lowerContent.contains("${") || lowerContent.contains("<%")) {
            return "JSP Template";
        } else if (lowerContent.contains("<form")) {
            return "HTML Form";
        } else if (lowerContent.contains("<table")) {
            return "HTML Table";
        } else if (lowerContent.contains("bootstrap") || lowerContent.contains("tailwind")) {
            return "Styled HTML";
        } else if (lowerContent.contains("<script")) {
            return "Interactive HTML";
        }
        
        return "HTML";
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".html", ".htm"};
    }
    
    @Override
    public String getParserType() {
        return "HTML";
    }
    
    @Override
    public boolean canHandle(String content) {
        // Check if content contains HTML patterns
        String lowerContent = content.toLowerCase();
        return content.contains("```html") ||
               content.contains(".html") ||
               lowerContent.contains("<!doctype") ||
               lowerContent.contains("<html") ||
               lowerContent.contains("<head") ||
               lowerContent.contains("<body") ||
               (lowerContent.contains("<") && lowerContent.contains(">"));
    }
}