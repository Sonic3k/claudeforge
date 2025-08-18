// src/main/java/com/sonic/claudeforge/service/CodeGeneratorService.java
package com.sonic.claudeforge.service;

import com.sonic.claudeforge.model.GeneratedCode;
import com.sonic.claudeforge.model.ProjectConfig;
import com.sonic.claudeforge.model.ProjectStructureStyle;
import com.sonic.claudeforge.service.codegenerator.JavaCodeGeneratorService;
import com.sonic.claudeforge.service.codegenerator.ReactCodeGeneratorService;
import com.sonic.claudeforge.service.parser.CodeParseManager;
import com.sonic.claudeforge.service.parser.ParseResult;
import com.sonic.claudeforge.service.parser.ParsedFile;
import com.sonic.claudeforge.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Updated Code Generator Service with modular parser system
 * Uses separate parsers for different code types (Java, React, CSS, HTML)
 */
@Service
public class CodeGeneratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeGeneratorService.class);
    
    private final JavaCodeGeneratorService javaCodeGeneratorService;
    private final ReactCodeGeneratorService reactCodeGeneratorService;
    private final CodeParseManager codeParseManager;
    private final FileUtils fileUtils;
    
    @Value("${claudeforge.workspace.base-path}")
    private String workspaceBasePath;
    
    public CodeGeneratorService(JavaCodeGeneratorService javaCodeGeneratorService,
                               ReactCodeGeneratorService reactCodeGeneratorService,
                               CodeParseManager codeParseManager,
                               FileUtils fileUtils) {
        this.javaCodeGeneratorService = javaCodeGeneratorService;
        this.reactCodeGeneratorService = reactCodeGeneratorService;
        this.codeParseManager = codeParseManager;
        this.fileUtils = fileUtils;
    }
    
    public String generateProject(ProjectConfig config) {
        logger.info("Generating project with style: {} - {}", 
                config.getProjectStructureStyle(), config.getProjectName());
        
        // Set workspace path if not provided
        if (config.getWorkspacePath() == null || config.getWorkspacePath().isEmpty()) {
            config.setWorkspacePath(workspaceBasePath);
        }
        
        String resultPath;
        
        switch (config.getProjectStructureStyle()) {
            case JAVA_REST_API:
            case JAVA_REST_API_WITH_STATIC:
                resultPath = javaCodeGeneratorService.generateJavaProject(config);
                break;
                
            case FULL_STACK_SEPARATED:
                resultPath = generateFullStackSeparated(config);
                break;
                
            case REACT_WEB_ONLY:
                resultPath = reactCodeGeneratorService.generateReactProject(config);
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported project structure style: " + config.getProjectStructureStyle());
        }
        
        logger.info("Project generation completed successfully");
        return resultPath;
    }
    
    private String generateFullStackSeparated(ProjectConfig config) {
        logger.info("Generating full-stack separated project: {}", config.getProjectName());
        
        // Create parent directory
        String parentPath = config.getWorkspacePath() + File.separator + config.getProjectName();
        fileUtils.createDirectory(parentPath);
        
        // Generate backend project
        String backendPath = javaCodeGeneratorService.generateJavaProject(config);
        logger.info("Backend generated at: {}", backendPath);
        
        // Generate frontend project  
        String frontendPath = reactCodeGeneratorService.generateReactProject(config);
        logger.info("Frontend generated at: {}", frontendPath);
        
        // Generate parent README
        generateParentReadme(parentPath, config);
        
        return parentPath;
    }
    
    /**
     * NEW: Parse and generate code using the modular parser system
     */
    public GeneratedCode parseAndGenerateCode(String claudeResponse, String workspacePath) {
        logger.info("Parsing Claude response using modular parser system (content length: {})", claudeResponse.length());
        
        GeneratedCode generatedCode = new GeneratedCode();
        generatedCode.setWorkspacePath(workspacePath);
        generatedCode.setGeneratedAt(LocalDateTime.now());
        generatedCode.setClaudeResponse(claudeResponse);
        
        // Use the new modular parser system
        ParseResult parseResult = codeParseManager.parseAll(claudeResponse);
        
        // Log parsing summary
        logger.info("Parsing summary:\n{}", parseResult.getSummary());
        
        // Convert ParsedFile to GeneratedCode.GeneratedFile
        List<GeneratedCode.GeneratedFile> generatedFiles = new ArrayList<>();
        
        for (ParsedFile parsedFile : parseResult.getValidFiles()) {
            GeneratedCode.GeneratedFile generatedFile = new GeneratedCode.GeneratedFile();
            generatedFile.setFileName(parsedFile.getFileName());
            generatedFile.setFilePath(parsedFile.getFilePath());
            generatedFile.setContent(parsedFile.getContent());
            generatedFile.setFileType(parsedFile.getFileType() + " (" + parsedFile.getParserType() + ")");
            
            generatedFiles.add(generatedFile);
            
            // Write file to workspace
            writeGeneratedFile(workspacePath, parsedFile.getFilePath(), parsedFile.getContent());
        }
        
        // Log invalid files for debugging
        if (!parseResult.getInvalidFiles().isEmpty()) {
            logger.warn("Found {} invalid files:", parseResult.getInvalidFiles().size());
            for (ParsedFile invalidFile : parseResult.getInvalidFiles()) {
                logger.warn("  {}: {}", invalidFile.getFilePath(), invalidFile.getErrorMessage());
            }
        }
        
        // Log errors
        if (parseResult.hasErrors()) {
            logger.warn("Parsing errors occurred:");
            parseResult.getErrors().forEach((parser, error) -> 
                logger.warn("  {}: {}", parser, error));
        }
        
        generatedCode.setFiles(generatedFiles);
        logger.info("Successfully generated {} files using parsers: {}", 
                generatedFiles.size(), parseResult.getSuccessfulParsers());
        
        return generatedCode;
    }
    
    /**
     * NEW: Parse and generate with specific parser
     */
    public GeneratedCode parseAndGenerateCodeWithParser(String claudeResponse, String workspacePath, String parserType) {
        logger.info("Parsing Claude response with specific parser: {}", parserType);
        
        GeneratedCode generatedCode = new GeneratedCode();
        generatedCode.setWorkspacePath(workspacePath);
        generatedCode.setGeneratedAt(LocalDateTime.now());
        generatedCode.setClaudeResponse(claudeResponse);
        
        // Use specific parser
        ParseResult parseResult = codeParseManager.parseWithSpecificParser(claudeResponse, parserType);
        
        // Convert and write files
        List<GeneratedCode.GeneratedFile> generatedFiles = new ArrayList<>();
        
        for (ParsedFile parsedFile : parseResult.getValidFiles()) {
            GeneratedCode.GeneratedFile generatedFile = new GeneratedCode.GeneratedFile();
            generatedFile.setFileName(parsedFile.getFileName());
            generatedFile.setFilePath(parsedFile.getFilePath());
            generatedFile.setContent(parsedFile.getContent());
            generatedFile.setFileType(parsedFile.getFileType() + " (" + parsedFile.getParserType() + ")");
            
            generatedFiles.add(generatedFile);
            writeGeneratedFile(workspacePath, parsedFile.getFilePath(), parsedFile.getContent());
        }
        
        generatedCode.setFiles(generatedFiles);
        logger.info("Parser {} generated {} files", parserType, generatedFiles.size());
        
        return generatedCode;
    }
    
    /**
     * NEW: Get available parsers information
     */
    public List<CodeParseManager.ParserInfo> getAvailableParsers() {
        return codeParseManager.getParserInformation();
    }
    
    /**
     * NEW: Detect which parsers can handle content
     */
    public List<String> detectApplicableParsers(String content) {
        return codeParseManager.detectApplicableParsers(content);
    }
    
    /**
     * NEW: Preview parsing without writing files
     */
    public ParseResult previewParsing(String claudeResponse) {
        logger.info("Previewing parsing for content (length: {})", claudeResponse.length());
        return codeParseManager.parseAll(claudeResponse);
    }
    
    private void writeGeneratedFile(String workspacePath, String filePath, String content) {
        String fullPath = workspacePath + File.separator + filePath;
        
        File file = new File(fullPath);
        fileUtils.createDirectory(file.getParent());
        
        logger.debug("Writing file: {} with {} characters", fullPath, content.length());
        
        fileUtils.writeFile(fullPath, content);
        logger.info("Generated file: {} ({} chars)", fullPath, content.length());
    }
    
    private void generateParentReadme(String parentPath, ProjectConfig config) {
        String readme = String.format("""
            # %s
            
            %s
            
            ## Project Structure
            
            This is a full-stack application with separated backend and frontend:
            
            ```
            %s/
            |-- %s-api/          # Spring Boot Backend (Java %s)
            |   |-- src/
            |   |-- pom.xml
            |   `-- ...
            `-- %s-web/          # React Frontend (TypeScript)
                |-- src/
                |-- package.json
                `-- ...
            ```
            
            ## Getting Started
            
            ### Backend (Spring Boot)
            
            ```bash
            cd %s-api
            mvn spring-boot:run
            ```
            
            The backend will start on http://localhost:8080
            
            ### Frontend (React)
            
            ```bash
            cd %s-web
            npm install
            npm run dev
            ```
            
            The frontend will start on http://localhost:3000
            
            ## Development
            
            ### Backend Development
            - API endpoints: `src/main/java/.../web/controller/`
            - Business logic: `src/main/java/.../service/`
            - Data models: `src/main/java/.../model/entity/`
            - Database config: `src/main/resources/application.properties`
            
            ### Frontend Development
            - Components: `src/components/`
            - Pages: `src/pages/`
            - API services: `src/services/`
            - Styles: `src/styles/`
            
            ## Database
            
            Database Type: **%s**
            
            %s
            
            ## Tech Stack
            
            ### Backend
            - Java %s
            - Spring Boot 3.3.12
            - Spring Data JPA
            - Maven
            
            ### Frontend
            - React 18
            - TypeScript
            - Tailwind CSS
            - Vite
            - React Router
            
            ---
            
            Generated by **ClaudeForge** - AI-Powered Code Generation Tool
            """, 
            config.getProjectName(),
            config.getDescription() != null ? config.getDescription() : "A full-stack application generated by ClaudeForge",
            config.getProjectName(),
            config.getProjectName(), config.getJavaVersion(),
            config.getProjectName(),
            config.getProjectName(),
            config.getProjectName(),
            config.getDatabaseType().getDisplayName(),
            getDatabaseInstructions(config),
            config.getJavaVersion()
        );
        
        fileUtils.writeFile(parentPath + "/README.md", readme);
    }
    
    private String getDatabaseInstructions(ProjectConfig config) {
        switch (config.getDatabaseType()) {
            case H2:
                return """
                    H2 is configured for in-memory database.
                    - Access H2 Console: http://localhost:8080/h2-console
                    - JDBC URL: jdbc:h2:mem:testdb
                    - Username: sa
                    - Password: (empty)
                    """;
            case SQLITE:
                return """
                    SQLite database file will be created in the `data/` folder.
                    - Database file: `data/[projectname].db`
                    - No additional setup required
                    """;
            case POSTGRESQL:
                return """
                    PostgreSQL configuration:
                    - Create database: `createdb [projectname]`
                    - Update username/password in application.properties
                    - Default connection: localhost:5432
                    """;
            case MYSQL:
                return """
                    MySQL configuration:
                    - Create database: `CREATE DATABASE [projectname];`
                    - Update username/password in application.properties
                    - Default connection: localhost:3306
                    """;
            default:
                return "No database configured - REST API only.";
        }
    }
}