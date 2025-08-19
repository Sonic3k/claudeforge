// src/main/java/com/sonic/claudeforge/service/parser/TypeScriptCodeParser.java
package com.sonic.claudeforge.service.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dedicated Parser for TypeScript code (interfaces, types, enums, etc.)
 * Optimized for pure TypeScript files without React components
 */
@Component
public class TypeScriptCodeParser implements CodeParser {
    
    private static final Logger logger = LoggerFactory.getLogger(TypeScriptCodeParser.class);
    
    // Pattern for TypeScript code blocks in markdown format
    private static final Pattern TS_CODE_BLOCK_PATTERN = 
        Pattern.compile("```(ts|typescript)\\s*//\\s*([^\\n]+)\\s*\\n([\\s\\S]*?)```");
    
    // Pattern for raw TypeScript code with path comments (including .ts files)
    private static final Pattern RAW_TS_PATTERN = 
        Pattern.compile("//\\s*(src/[^\\r\\n]+\\.ts)\\s*[\\r\\n]+([\\s\\S]+?)(?=[\\r\\n]+//\\s*src/[^\\r\\n]+\\.ts|\\Z)", Pattern.MULTILINE);
    
    // Pattern for single TypeScript file
    private static final Pattern SINGLE_TS_PATTERN = 
        Pattern.compile("^\\s*//\\s*(src/[^\\r\\n]+\\.ts)\\s*[\\r\\n]+([\\s\\S]+)$", Pattern.MULTILINE);
    
    @Override
    public List<ParsedFile> parse(String content) {
        logger.debug("Parsing TypeScript code from content (length: {})", content.length());
        
        List<ParsedFile> files = new ArrayList<>();
        
        // 1. Try parsing markdown code blocks first
        files.addAll(parseMarkdownCodeBlocks(content));
        
        // 2. If no markdown blocks found, try raw TypeScript code
        if (files.isEmpty()) {
            files.addAll(parseRawTypeScriptCode(content));
        }
        
        // 3. If still no files, try single file detection
        if (files.isEmpty()) {
            files.addAll(parseSingleTypeScriptFile(content));
        }
        
        logger.info("TypeScript parser found {} files", files.size());
        return files;
    }
    
    private List<ParsedFile> parseMarkdownCodeBlocks(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = TS_CODE_BLOCK_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String filePath = matcher.group(2).trim();
            String code = matcher.group(3).trim();
            
            ParsedFile file = createTypeScriptFile(filePath, code);
            if (file != null) {
                files.add(file);
                logger.debug("Found TypeScript markdown block: {} ({} chars)", filePath, code.length());
            }
        }
        
        return files;
    }
    
    private List<ParsedFile> parseRawTypeScriptCode(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = RAW_TS_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String code = matcher.group(2).trim();
            
            ParsedFile file = createTypeScriptFile(filePath, code);
            if (file != null) {
                files.add(file);
                logger.debug("Found raw TypeScript file: {} ({} chars)", filePath, code.length());
            }
        }
        
        return files;
    }
    
    private List<ParsedFile> parseSingleTypeScriptFile(String content) {
        List<ParsedFile> files = new ArrayList<>();
        Matcher matcher = SINGLE_TS_PATTERN.matcher(content.trim());
        
        if (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String code = matcher.group(2).trim();
            
            ParsedFile file = createTypeScriptFile(filePath, code);
            if (file != null) {
                files.add(file);
                logger.debug("Found single TypeScript file: {} ({} chars)", filePath, code.length());
            }
        }
        
        return files;
    }
    
    private ParsedFile createTypeScriptFile(String filePath, String content) {
        // Validate TypeScript content
        if (!isValidTypeScriptContent(content)) {
            logger.warn("Invalid TypeScript content for file: {}", filePath);
            return ParsedFile.invalid(filePath, "Invalid TypeScript content - missing valid TS syntax");
        }
        
        // Determine file type based on content
        String fileType = determineTypeScriptFileType(content);
        
        return new ParsedFile(filePath, content, fileType, "TypeScript");
    }
    
    /**
     * Validate TypeScript content
     */
    private boolean isValidTypeScriptContent(String content) {
        // Must contain TypeScript-specific syntax
        return content.contains("export ") || 
               content.contains("import ") || 
               content.contains("interface ") ||
               content.contains("type ") ||
               content.contains("enum ") ||
               content.contains("namespace ") ||
               content.contains("module ") ||
               content.contains("declare ") ||
               content.contains("function ") ||
               content.contains("const ") ||
               content.contains("let ") ||
               content.contains("class ") ||
               // Generic TypeScript patterns
               content.contains(": ") || // Type annotations
               content.contains("<") && content.contains(">") || // Generics
               content.trim().length() > 0;
    }
    
    /**
     * Determine specific TypeScript file type
     */
    private String determineTypeScriptFileType(String content) {
        // Count different TypeScript constructs
        boolean hasInterfaces = content.contains("interface ");
        boolean hasEnums = content.contains("enum ");
        boolean hasTypes = content.contains("type ");
        boolean hasConstants = content.contains("const ");
        boolean hasFunctions = content.contains("function ");
        boolean hasClasses = content.contains("class ");
        
        // Determine primary purpose
        if (hasInterfaces && hasEnums && hasTypes) {
            return "TypeScript Definitions"; // Mixed types file
        } else if (hasInterfaces && content.contains("export interface")) {
            long interfaceCount = content.split("export interface").length - 1;
            if (interfaceCount > 1) {
                return "TypeScript Interfaces (" + interfaceCount + " interfaces)";
            }
            return "TypeScript Interface";
        } else if (hasEnums && content.contains("export enum")) {
            long enumCount = content.split("export enum").length - 1;
            if (enumCount > 1) {
                return "TypeScript Enums (" + enumCount + " enums)";
            }
            return "TypeScript Enum";
        } else if (hasTypes && content.contains("export type")) {
            return "TypeScript Types";
        } else if (hasConstants && content.contains("export const")) {
            return "TypeScript Constants";
        } else if (hasFunctions) {
            return "TypeScript Functions";
        } else if (hasClasses) {
            return "TypeScript Class";
        } else if (content.contains("declare ")) {
            return "TypeScript Declarations";
        } else if (content.contains("namespace ")) {
            return "TypeScript Namespace";
        }
        
        return "TypeScript";
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return new String[]{".ts", ".d.ts"};
    }
    
    @Override
    public String getParserType() {
        return "TypeScript";
    }
    
    @Override
    public boolean canHandle(String content) {
        // Focus only on pure TypeScript files, not JavaScript or React
        return content.contains("```ts") ||
               content.contains("```typescript") ||
               // Check for .ts file paths (excluding .tsx)
               (content.contains("// src/") && content.contains(".ts") && !content.contains(".tsx")) ||
               // Check for TypeScript-specific syntax without React or JS-only content
               (content.contains("export interface") ||
                content.contains("export enum") ||
                content.contains("export type") ||
                content.contains("declare ") ||
                content.contains("namespace ")) &&
               // Exclude React content
               !content.contains("React") &&
               !content.contains("jsx") &&
               !content.contains("tsx") &&
               !content.contains("useState") &&
               !content.contains("useEffect") &&
               // Exclude plain JavaScript (.js files without TS syntax)
               !(!content.contains("interface ") && !content.contains("enum ") && 
                 !content.contains("type ") && content.contains(".js"));
    }
}