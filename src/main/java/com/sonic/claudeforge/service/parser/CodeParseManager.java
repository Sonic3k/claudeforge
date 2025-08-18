// src/main/java/com/sonic/claudeforge/service/parser/CodeParseManager.java
package com.sonic.claudeforge.service.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager for coordinating different code parsers
 * Automatically detects and applies appropriate parsers based on content
 */
@Service
public class CodeParseManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeParseManager.class);
    
    private final List<CodeParser> parsers;
    
    public CodeParseManager(JavaCodeParser javaParser,
                           ReactCodeParser reactParser,
                           CssCodeParser cssParser,
                           HtmlCodeParser htmlParser) {
        this.parsers = List.of(javaParser, reactParser, cssParser, htmlParser);
        logger.info("Initialized CodeParseManager with {} parsers: {}", 
                   parsers.size(), 
                   parsers.stream().map(CodeParser::getParserType).collect(Collectors.joining(", ")));
    }
    
    /**
     * Parse content using all applicable parsers
     * @param content Raw content from Claude response
     * @return List of parsed files from all parsers
     */
    public ParseResult parseAll(String content) {
        logger.info("Parsing content with {} parsers (content length: {})", parsers.size(), content.length());
        
        ParseResult result = new ParseResult();
        List<ParsedFile> allFiles = new ArrayList<>();
        
        for (CodeParser parser : parsers) {
            try {
                if (parser.canHandle(content)) {
                    logger.debug("Parser {} can handle this content", parser.getParserType());
                    
                    List<ParsedFile> files = parser.parse(content);
                    if (!files.isEmpty()) {
                        logger.info("Parser {} found {} files", parser.getParserType(), files.size());
                        allFiles.addAll(files);
                        result.addParserResult(parser.getParserType(), files);
                    } else {
                        logger.debug("Parser {} found no files", parser.getParserType());
                    }
                } else {
                    logger.debug("Parser {} cannot handle this content", parser.getParserType());
                }
            } catch (Exception e) {
                logger.error("Error in parser {}: {}", parser.getParserType(), e.getMessage(), e);
                result.addError(parser.getParserType(), e.getMessage());
            }
        }
        
        result.setAllFiles(allFiles);
        result.setSuccess(!allFiles.isEmpty());
        
        logger.info("Parsing completed: {} total files found", allFiles.size());
        return result;
    }
    
    /**
     * Parse content using specific parser type
     * @param content Raw content from Claude response
     * @param parserType Specific parser type to use
     * @return List of parsed files from the specified parser
     */
    public ParseResult parseWithSpecificParser(String content, String parserType) {
        logger.info("Parsing content with specific parser: {}", parserType);
        
        CodeParser targetParser = parsers.stream()
                .filter(parser -> parser.getParserType().equalsIgnoreCase(parserType))
                .findFirst()
                .orElse(null);
        
        if (targetParser == null) {
            logger.error("Parser type '{}' not found", parserType);
            ParseResult result = new ParseResult();
            result.addError("Manager", "Parser type '" + parserType + "' not found");
            return result;
        }
        
        ParseResult result = new ParseResult();
        try {
            List<ParsedFile> files = targetParser.parse(content);
            result.setAllFiles(files);
            result.addParserResult(parserType, files);
            result.setSuccess(!files.isEmpty());
            
            logger.info("Parser {} found {} files", parserType, files.size());
        } catch (Exception e) {
            logger.error("Error in parser {}: {}", parserType, e.getMessage(), e);
            result.addError(parserType, e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get all available parser types
     * @return List of available parser types
     */
    public List<String> getAvailableParserTypes() {
        return parsers.stream()
                .map(CodeParser::getParserType)
                .collect(Collectors.toList());
    }
    
    /**
     * Get parser information
     * @return List of parser information
     */
    public List<ParserInfo> getParserInformation() {
        return parsers.stream()
                .map(parser -> new ParserInfo(
                        parser.getParserType(),
                        String.join(", ", parser.getSupportedExtensions()),
                        parser.getClass().getSimpleName()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Auto-detect which parsers can handle the content
     * @param content Content to analyze
     * @return List of parser types that can handle the content
     */
    public List<String> detectApplicableParsers(String content) {
        return parsers.stream()
                .filter(parser -> parser.canHandle(content))
                .map(CodeParser::getParserType)
                .collect(Collectors.toList());
    }
    
    /**
     * Information about a parser
     */
    public static class ParserInfo {
        private final String type;
        private final String supportedExtensions;
        private final String className;
        
        public ParserInfo(String type, String supportedExtensions, String className) {
            this.type = type;
            this.supportedExtensions = supportedExtensions;
            this.className = className;
        }
        
        // @GENERATE_GETTERS_SETTERS
        public String getType() { return type; }
        public String getSupportedExtensions() { return supportedExtensions; }
        public String getClassName() { return className; }
    }
}