// src/main/java/com/sonic/claudeforge/service/parser/ReactCodeParser.java
package com.sonic.claudeforge.service.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for React/TypeScript code from Claude responses
 * Supports TSX, JSX, and TypeScript files
 */
@Component
public class ReactCodeParser implements CodeParser {
    
    private static final Logger logger = LoggerFactory.getLogger(ReactCodeParser.class);
    
    // Pattern for React code blocks in markdown format
    private static final Pattern REACT_CODE_BLOCK_PATTERN = 
        Pattern.compile("```(tsx?|jsx?)\\s*//\\s*([^\\n]+)\\s*\\n([\\s\\S]*?)```");
    
    // Pattern for raw React code with path comments
    private static final Pattern RAW_REACT_PATTERN = 
        Pattern.compile("//\\s*(src/[^\\r\\n]+\\.(tsx?|jsx?))\\s*[\\r\\n]+([\\s\\S]+?)(?=[\\r\\n]+//\\s*src/[^\\r\\n]+\\.(tsx?|jsx?)|\\Z)", Pattern.MULTILINE);
    
    // Pattern for single React file
    private static final Pattern SINGLE_REACT_PATTERN = 
        Pattern.compile("^\\s*//\\s*(src/[^\\r\\n]+\\.(tsx?|jsx?))\\s*[\\r\\n]+([\\s\\S]+)$", Pattern.MULTILINE);
    
    @Override
    public List<ParsedFile> parse(String content) {
        logger.debug("Parsing React code from content (length: {})", content.length());
        
        List<ParsedFile> files = new ArrayList<>();
        
        // 1. Try parsing markdown code blocks first
        files.addAll(parseMarkdownCodeBlocks(content));
        
        // 2. If no markdown blocks found, try raw React code
        if (files.isEmpty()) {
            files.addAll(parseRawReactCode(content));
        }
        
        // 3. If still no files, try single file detection
        if (files.isEmpty()) {
            files.addAll(parseSingleReactFile(content));
        }
        
        logger.info("React parser found {} files", files.size());
        return files;
    }
    
    private List<ParsedFile> parseMarkdownCodeBlocks(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = REACT_CODE_BLOCK_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String fileExtension = matcher.group(1);
            String filePath = matcher.group(2).trim();
            String code = matcher.group(3).trim();
            
            ParsedFile file = createReactFile(filePath, code, fileExtension);
            files.add(file);
            
            logger.debug("Found React markdown block: {} ({} chars)", filePath, code.length());
        }
        
        return files;
    }
    
    private List<ParsedFile> parseRawReactCode(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = RAW_REACT_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String fileExtension = matcher.group(2);
            String code = matcher.group(3).trim();
            
            ParsedFile file = createReactFile(filePath, code, fileExtension);
            files.add(file);
            
            logger.debug("Found raw React file: {} ({} chars)", filePath, code.length());
        }
        
        return files;
    }
    
    private List<ParsedFile> parseSingleReactFile(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = SINGLE_REACT_PATTERN.matcher(content.trim());
        
        if (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String fileExtension = matcher.group(2);
            String code = matcher.group(3).trim();
            
            ParsedFile file = createReactFile(filePath, code, fileExtension);
            files.add(file);
            
            logger.debug("Found single React file: {} ({} chars)", filePath, code.length());
        }
        
        return files;
    }
    
    private ParsedFile createReactFile(String filePath, String content, String extension) {
        // Validate React content
        if (!isValidReactContent(content)) {
            return ParsedFile.invalid(filePath, "Invalid React content - missing import or export statements");
        }
        
        // Determine file type based on content and extension
        String fileType = determineReactFileType(content, extension);
        
        return new ParsedFile(filePath, content, fileType, "React");
    }
    
    private boolean isValidReactContent(String content) {
        // Basic validation for React files
        return content.contains("import ") || 
               content.contains("export ") || 
               content.contains("function ") ||
               content.contains("const ") ||
               content.contains("React") ||
               content.contains("jsx") ||
               content.contains("tsx") ||
               content.contains("<") && content.contains(">");
    }
    
    private String determineReactFileType(String content, String extension) {
        if (content.contains("export default") && content.contains("function")) {
            return "Component";
        } else if (content.contains("const") && content.contains("=") && content.contains("=>")) {
            return "Component";
        } else if (content.contains("interface ") || content.contains("type ")) {
            return "Types";
        } else if (content.contains("useState") || content.contains("useEffect")) {
            return "Hook";
        } else if (content.contains("api") || content.contains("fetch") || content.contains("axios")) {
            return "Service";
        } else if (content.contains("const") && content.contains("=") && !content.contains("=>")) {
            return "Constants";
        } else if (extension != null && extension.contains("ts") && !extension.contains("tsx")) {
            return "TypeScript";
        }
        return "Component";
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".tsx", ".jsx", ".ts", ".js"};
    }
    
    @Override
    public String getParserType() {
        return "React";
    }
    
    @Override
    public boolean canHandle(String content) {
        // Check if content contains React patterns
        return content.contains("```tsx") ||
               content.contains("```jsx") ||
               content.contains("```ts") ||
               content.contains("// src/") && (content.contains(".tsx") || content.contains(".jsx")) ||
               content.contains("import React") ||
               content.contains("export default") ||
               content.contains("useState") ||
               content.contains("useEffect");
    }
}