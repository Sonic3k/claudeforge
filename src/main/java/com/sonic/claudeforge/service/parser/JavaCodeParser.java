// src/main/java/com/sonic/claudeforge/service/parser/JavaCodeParser.java
package com.sonic.claudeforge.service.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Java code from Claude responses
 * Supports both markdown format and raw Java code
 */
@Component
public class JavaCodeParser implements CodeParser {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaCodeParser.class);
    
    // Pattern for Java code blocks in markdown format
    private static final Pattern JAVA_CODE_BLOCK_PATTERN = 
        Pattern.compile("```java\\s*//\\s*([^\\n]+)\\s*\\n([\\s\\S]*?)```");
    
    // Pattern for raw Java code with path comments
    private static final Pattern RAW_JAVA_PATTERN = 
        Pattern.compile("//\\s*(src/[^\\r\\n]+\\.java)\\s*[\\r\\n]+([\\s\\S]+?)(?=[\\r\\n]+//\\s*src/[^\\r\\n]+\\.java|\\Z)", Pattern.MULTILINE);
    
    // Pattern for single Java file
    private static final Pattern SINGLE_JAVA_PATTERN = 
        Pattern.compile("^\\s*//\\s*(src/[^\\r\\n]+\\.java)\\s*[\\r\\n]+([\\s\\S]+)$", Pattern.MULTILINE);
    
    @Override
    public List<ParsedFile> parse(String content) {
        logger.debug("Parsing Java code from content (length: {})", content.length());
        
        List<ParsedFile> files = new ArrayList<>();
        
        // 1. Try parsing markdown code blocks first
        files.addAll(parseMarkdownCodeBlocks(content));
        
        // 2. If no markdown blocks found, try raw Java code
        if (files.isEmpty()) {
            files.addAll(parseRawJavaCode(content));
        }
        
        // 3. If still no files, try single file detection
        if (files.isEmpty()) {
            files.addAll(parseSingleJavaFile(content));
        }
        
        logger.info("Java parser found {} files", files.size());
        return files;
    }
    
    private List<ParsedFile> parseMarkdownCodeBlocks(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = JAVA_CODE_BLOCK_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String code = matcher.group(2).trim();
            
            ParsedFile file = createJavaFile(filePath, code);
            files.add(file);
            
            logger.debug("Found Java markdown block: {} ({} chars)", filePath, code.length());
        }
        
        return files;
    }
    
    private List<ParsedFile> parseRawJavaCode(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = RAW_JAVA_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String code = matcher.group(2).trim();
            
            ParsedFile file = createJavaFile(filePath, code);
            files.add(file);
            
            logger.debug("Found raw Java file: {} ({} chars)", filePath, code.length());
        }
        
        return files;
    }
    
    private List<ParsedFile> parseSingleJavaFile(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = SINGLE_JAVA_PATTERN.matcher(content.trim());
        
        if (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String code = matcher.group(2).trim();
            
            ParsedFile file = createJavaFile(filePath, code);
            files.add(file);
            
            logger.debug("Found single Java file: {} ({} chars)", filePath, code.length());
        }
        
        return files;
    }
    
    private ParsedFile createJavaFile(String filePath, String content) {
        // Validate and clean file path
        String cleanPath = cleanFilePath(filePath);
        
        // Validate Java content
        if (!isValidJavaContent(content)) {
            return ParsedFile.invalid(cleanPath, "Invalid Java content - missing package declaration or class definition");
        }
        
        // Determine file type based on content
        String fileType = determineJavaFileType(content);
        
        return new ParsedFile(cleanPath, content, fileType, "Java");
    }
    
    private String cleanFilePath(String filePath) {
        // Convert to relative path if needed
        if (filePath.startsWith("src/")) {
            return filePath;
        } else if (filePath.contains("src/main/java/")) {
            int srcIndex = filePath.indexOf("src/main/java/");
            return filePath.substring(srcIndex);
        }
        return filePath;
    }
    
    private boolean isValidJavaContent(String content) {
        // Basic validation - should contain package declaration or import
        return content.contains("package ") || content.contains("import ") || 
               content.contains("class ") || content.contains("interface ") || content.contains("enum ");
    }
    
    private String determineJavaFileType(String content) {
        if (content.contains("@RestController") || content.contains("@Controller")) {
            return "Controller";
        } else if (content.contains("@Service")) {
            return "Service";
        } else if (content.contains("@Repository")) {
            return "Repository";
        } else if (content.contains("@Entity")) {
            return "Entity";
        } else if (content.contains("@Configuration")) {
            return "Configuration";
        } else if (content.contains("@Component") && content.contains("Filter")) {
            return "Filter";
        } else if (content.contains("interface ")) {
            return "Interface";
        } else if (content.contains("enum ")) {
            return "Enum";
        }
        return "Class";
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".java"};
    }
    
    @Override
    public String getParserType() {
        return "Java";
    }
    
    @Override
    public boolean canHandle(String content) {
        // Check if content contains Java patterns
        return content.contains("```java") || 
               content.contains("// src/main/java/") ||
               content.contains("package ") ||
               content.contains("public class ") ||
               content.contains("@SpringBootApplication") ||
               content.contains("@RestController");
    }
}