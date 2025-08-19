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
 * Enhanced Parser for React/TypeScript code from Claude responses
 * Supports TSX, JSX, TS, and JS files including pure TypeScript types/interfaces
 */
@Component
public class ReactCodeParser implements CodeParser {
    
    private static final Logger logger = LoggerFactory.getLogger(ReactCodeParser.class);
    
    // Pattern for React/TS code blocks in markdown format
    private static final Pattern REACT_CODE_BLOCK_PATTERN = 
        Pattern.compile("```(tsx?|jsx?|typescript|javascript)\\s*//\\s*([^\\n]+)\\s*\\n([\\s\\S]*?)```");
    
    // Pattern for raw React/TS code with path comments
    private static final Pattern RAW_REACT_PATTERN = 
        Pattern.compile("//\\s*(src/[^\\r\\n]+\\.(tsx?|jsx?|js))\\s*[\\r\\n]+([\\s\\S]+?)(?=[\\r\\n]+//\\s*src/[^\\r\\n]+\\.(tsx?|jsx?|js)|\\Z)", Pattern.MULTILINE);
    
    // Pattern for single React/TS file
    private static final Pattern SINGLE_REACT_PATTERN = 
        Pattern.compile("^\\s*//\\s*(src/[^\\r\\n]+\\.(tsx?|jsx?|js))\\s*[\\r\\n]+([\\s\\S]+)$", Pattern.MULTILINE);
    
    @Override
    public List<ParsedFile> parse(String content) {
        logger.debug("Parsing React/TypeScript code from content (length: {})", content.length());
        
        List<ParsedFile> files = new ArrayList<>();
        
        // 1. Try parsing markdown code blocks first
        files.addAll(parseMarkdownCodeBlocks(content));
        
        // 2. If no markdown blocks found, try raw code
        if (files.isEmpty()) {
            files.addAll(parseRawCode(content));
        }
        
        // 3. If still no files, try single file detection
        if (files.isEmpty()) {
            files.addAll(parseSingleFile(content));
        }
        
        logger.info("React/TypeScript parser found {} files", files.size());
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
            if (file != null) {
                files.add(file);
                logger.debug("Found React/TS markdown block: {} ({} chars)", filePath, code.length());
            }
        }
        
        return files;
    }
    
    private List<ParsedFile> parseRawCode(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = RAW_REACT_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String fileExtension = matcher.group(2);
            String code = matcher.group(3).trim();
            
            ParsedFile file = createReactFile(filePath, code, fileExtension);
            if (file != null) {
                files.add(file);
                logger.debug("Found raw React/TS file: {} ({} chars)", filePath, code.length());
            }
        }
        
        return files;
    }
    
    private List<ParsedFile> parseSingleFile(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = SINGLE_REACT_PATTERN.matcher(content.trim());
        
        if (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String fileExtension = matcher.group(2);
            String code = matcher.group(3).trim();
            
            ParsedFile file = createReactFile(filePath, code, fileExtension);
            if (file != null) {
                files.add(file);
                logger.debug("Found single React/TS file: {} ({} chars)", filePath, code.length());
            }
        }
        
        return files;
    }
    
    private ParsedFile createReactFile(String filePath, String content, String extension) {
        // Enhanced validation for React/TypeScript content
        if (!isValidReactOrTypeScriptContent(content)) {
            logger.warn("Invalid React/TypeScript content for file: {}", filePath);
            return ParsedFile.invalid(filePath, "Invalid React/TypeScript content");
        }
        
        // Determine file type based on content and extension
        String fileType = determineFileType(content, extension);
        
        return new ParsedFile(filePath, content, fileType, "React/TypeScript");
    }
    
    /**
     * Enhanced validation that accepts both React components and pure TypeScript
     */
    private boolean isValidReactOrTypeScriptContent(String content) {
        // Check for TypeScript/JavaScript keywords and patterns
        return content.contains("export ") || 
               content.contains("import ") || 
               content.contains("interface ") ||
               content.contains("type ") ||
               content.contains("enum ") ||
               content.contains("function ") ||
               content.contains("const ") ||
               content.contains("let ") ||
               content.contains("var ") ||
               content.contains("class ") ||
               content.contains("React") ||
               content.contains("jsx") ||
               content.contains("tsx") ||
               (content.contains("<") && content.contains(">")) ||
               content.trim().length() > 0; // Allow non-empty files
    }
    
    /**
     * Enhanced file type detection
     */
    private String determineFileType(String content, String extension) {
        // Check for TypeScript-specific patterns first
        if (content.contains("interface ") && content.contains("export interface")) {
            return "TypeScript Interfaces";
        } else if (content.contains("enum ") && content.contains("export enum")) {
            return "TypeScript Enums";
        } else if (content.contains("type ") && content.contains("export type")) {
            return "TypeScript Types";
        } else if (content.contains("export ") && (content.contains("interface ") || content.contains("type ") || content.contains("enum "))) {
            return "TypeScript Definitions";
        }
        
        // Check for React patterns
        else if (content.contains("export default") && content.contains("function")) {
            return "React Component";
        } else if (content.contains("const") && content.contains("=") && content.contains("=>") && content.contains("jsx")) {
            return "React Component";
        } else if (content.contains("useState") || content.contains("useEffect")) {
            return "React Hook";
        } else if (content.contains("api") || content.contains("fetch") || content.contains("axios")) {
            return "API Service";
        } else if (content.contains("const") && content.contains("=") && !content.contains("=>")) {
            return "Constants";
        }
        
        // Fallback based on extension
        else if (extension != null) {
            switch (extension.toLowerCase()) {
                case "ts":
                    return "TypeScript";
                case "tsx":
                    return "React TSX Component";
                case "jsx":
                    return "React JSX Component";
                case "js":
                    return "JavaScript";
                default:
                    return "JavaScript/TypeScript";
            }
        }
        
        return "TypeScript/React";
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".tsx", ".jsx", ".ts", ".js"};
    }
    
    @Override
    public String getParserType() {
        return "React/TypeScript";
    }
    
    @Override
    public boolean canHandle(String content) {
        // Focus on React/JSX content, avoid pure TypeScript files
        return content.contains("```tsx") ||
               content.contains("```jsx") ||
               // Check for React file paths only (.tsx, .jsx)
               content.contains("// src/") && (
                   content.contains(".tsx") || 
                   content.contains(".jsx")
               ) ||
               // Check for React-specific patterns
               content.contains("import React") ||
               content.contains("useState") ||
               content.contains("useEffect") ||
               content.contains("useContext") ||
               content.contains("useReducer") ||
               content.contains("JSX.Element") ||
               content.contains("React.FC") ||
               content.contains("React.Component") ||
               // React component patterns
               (content.contains("export default") && 
                (content.contains("function") || content.contains("const")) && 
                (content.contains("<") && content.contains("/>"))) ||
               // JSX syntax
               (content.contains("return (") && content.contains("<") && content.contains("/>"));
    }
}