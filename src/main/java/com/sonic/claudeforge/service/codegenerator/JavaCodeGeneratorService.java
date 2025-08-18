// src/main/java/com/sonic/claudeforge/service/codegenerator/JavaCodeGeneratorService.java
package com.sonic.claudeforge.service.codegenerator;

import com.sonic.claudeforge.model.DatabaseType;
import com.sonic.claudeforge.model.ProjectConfig;
import com.sonic.claudeforge.model.ProjectStructureStyle;
import com.sonic.claudeforge.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Java Code Generator Service
 * Handles Java Spring Boot project generation
 */
@Service
public class JavaCodeGeneratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaCodeGeneratorService.class);
    
    private final FileUtils fileUtils;
    
    public JavaCodeGeneratorService(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }
    
    public String generateJavaProject(ProjectConfig config) {
        logger.info("Generating Java Spring Boot project: {}", config.getProjectName());
        
        String projectPath = config.getBackendProjectPath();
        
        createJavaProjectStructure(projectPath, config);
        generatePomXml(projectPath, config);
        generateApplicationProperties(projectPath, config);
        generateMainApplicationClass(projectPath, config);
        
        // Generate infrastructure components
        generateBaseResponse(projectPath, config);
        
        // Generate database-related components only if database is enabled
        if (!DatabaseType.NONE.equals(config.getDatabaseType())) {
            generateBaseEntity(projectPath, config);
        }
        
        generateGlobalExceptionHandler(projectPath, config);
        generateRequestLoggingFilter(projectPath, config);
        
        // Create data folder for SQLite
        if (DatabaseType.SQLITE.equals(config.getDatabaseType())) {
            fileUtils.createDirectory(projectPath + File.separator + "data");
        }
        
        // Generate static web resources for JAVA_REST_API_WITH_STATIC
        if (config.getProjectStructureStyle() == ProjectStructureStyle.JAVA_REST_API_WITH_STATIC) {
            generateStaticWebResources(projectPath, config);
        }
        
        logger.info("Java project generated successfully at: {}", projectPath);
        return projectPath;
    }
    
    private void createJavaProjectStructure(String projectPath, ProjectConfig config) {
        String fullPackage = config.getFullPackage();
        String fullPackagePath = fullPackage.replace(".", File.separator);
        boolean hasDatabase = !DatabaseType.NONE.equals(config.getDatabaseType());
        
        List<String> directories = new ArrayList<>();
        directories.add("src/main/java/" + fullPackagePath);
        directories.add("src/main/java/" + fullPackagePath + "/web");
        directories.add("src/main/java/" + fullPackagePath + "/web/controller");
        directories.add("src/main/java/" + fullPackagePath + "/web/filter");
        directories.add("src/main/java/" + fullPackagePath + "/web/handler");
        directories.add("src/main/java/" + fullPackagePath + "/model");
        directories.add("src/main/java/" + fullPackagePath + "/model/response");
        directories.add("src/main/java/" + fullPackagePath + "/config");
        directories.add("src/main/resources");
        directories.add("src/test/java/" + fullPackagePath);
        
        if (hasDatabase) {
            directories.add("src/main/java/" + fullPackagePath + "/service");
            directories.add("src/main/java/" + fullPackagePath + "/repository");
            directories.add("src/main/java/" + fullPackagePath + "/model/entity");
        }
        
        // Add static resources for JAVA_REST_API_WITH_STATIC
        if (config.getProjectStructureStyle() == ProjectStructureStyle.JAVA_REST_API_WITH_STATIC) {
            directories.add("src/main/resources/static");
            directories.add("src/main/resources/static/css");
            directories.add("src/main/resources/static/js");
            directories.add("src/main/resources/static/images");
        }
        
        for (String dir : directories) {
            fileUtils.createDirectory(projectPath + File.separator + dir);
        }
    }
    
    private void generatePomXml(String projectPath, ProjectConfig config) {
        String fullPackage = config.getFullPackage();
        boolean hasDatabase = !DatabaseType.NONE.equals(config.getDatabaseType());
        DatabaseType dbType = config.getDatabaseType();
        
        StringBuilder dependencies = new StringBuilder();
        dependencies.append("        <dependency>\n");
        dependencies.append("            <groupId>org.springframework.boot</groupId>\n");
        dependencies.append("            <artifactId>spring-boot-starter-web</artifactId>\n");
        dependencies.append("        </dependency>\n");
        dependencies.append("        <dependency>\n");
        dependencies.append("            <groupId>org.springframework.boot</groupId>\n");
        dependencies.append("            <artifactId>spring-boot-starter-validation</artifactId>\n");
        dependencies.append("        </dependency>\n");
        
        if (hasDatabase) {
            dependencies.append("        <dependency>\n");
            dependencies.append("            <groupId>org.springframework.boot</groupId>\n");
            dependencies.append("            <artifactId>spring-boot-starter-data-jpa</artifactId>\n");
            dependencies.append("        </dependency>\n");
            
            switch (dbType) {
                case H2:
                    dependencies.append("        <dependency>\n");
                    dependencies.append("            <groupId>com.h2database</groupId>\n");
                    dependencies.append("            <artifactId>h2</artifactId>\n");
                    dependencies.append("            <scope>runtime</scope>\n");
                    dependencies.append("        </dependency>\n");
                    break;
                case SQLITE:
                    dependencies.append("        <dependency>\n");
                    dependencies.append("            <groupId>org.xerial</groupId>\n");
                    dependencies.append("            <artifactId>sqlite-jdbc</artifactId>\n");
                    dependencies.append("        </dependency>\n");
                    dependencies.append("        <dependency>\n");
                    dependencies.append("            <groupId>org.hibernate.orm</groupId>\n");
                    dependencies.append("            <artifactId>hibernate-community-dialects</artifactId>\n");
                    dependencies.append("        </dependency>\n");
                    break;
                case POSTGRESQL:
                    dependencies.append("        <dependency>\n");
                    dependencies.append("            <groupId>org.postgresql</groupId>\n");
                    dependencies.append("            <artifactId>postgresql</artifactId>\n");
                    dependencies.append("            <scope>runtime</scope>\n");
                    dependencies.append("        </dependency>\n");
                    break;
                case MYSQL:
                    dependencies.append("        <dependency>\n");
                    dependencies.append("            <groupId>com.mysql</groupId>\n");
                    dependencies.append("            <artifactId>mysql-connector-j</artifactId>\n");
                    dependencies.append("            <scope>runtime</scope>\n");
                    dependencies.append("        </dependency>\n");
                    break;
            }
        }
        
        String projectName = config.getProjectName();
        if (config.getProjectStructureStyle() == ProjectStructureStyle.FULL_STACK_SEPARATED) {
            projectName = config.getProjectName() + "-api";
        }
        
        String pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
            "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0\n" +
            "                             https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <parent>\n" +
            "        <groupId>org.springframework.boot</groupId>\n" +
            "        <artifactId>spring-boot-starter-parent</artifactId>\n" +
            "        <version>3.3.12</version>\n" +
            "        <relativePath/>\n" +
            "    </parent>\n" +
            "    <groupId>" + fullPackage + "</groupId>\n" +
            "    <artifactId>" + projectName + "</artifactId>\n" +
            "    <version>" + config.getVersion() + "</version>\n" +
            "    <name>" + projectName + "</name>\n" +
            "    <description>" + (config.getDescription() != null ? config.getDescription() : "Generated by ClaudeForge") + "</description>\n" +
            "    <properties>\n" +
            "        <java.version>" + config.getJavaVersion() + "</java.version>\n" +
            "        <maven.compiler.source>" + config.getJavaVersion() + "</maven.compiler.source>\n" +
            "        <maven.compiler.target>" + config.getJavaVersion() + "</maven.compiler.target>\n" +
            "    </properties>\n" +
            "    <dependencies>\n" +
            dependencies.toString() +
            "        <dependency>\n" +
            "            <groupId>org.springframework.boot</groupId>\n" +
            "            <artifactId>spring-boot-starter-test</artifactId>\n" +
            "            <scope>test</scope>\n" +
            "        </dependency>\n" +
            "    </dependencies>\n" +
            "\n" +
            "    <build>\n" +
            "        <plugins>\n" +
            "            <plugin>\n" +
            "                <groupId>org.springframework.boot</groupId>\n" +
            "                <artifactId>spring-boot-maven-plugin</artifactId>\n" +
            "            </plugin>\n" +
            "            <plugin>\n" +
            "                <groupId>org.apache.maven.plugins</groupId>\n" +
            "                <artifactId>maven-compiler-plugin</artifactId>\n" +
            "                <version>3.11.0</version>\n" +
            "                <configuration>\n" +
            "                    <source>" + config.getJavaVersion() + "</source>\n" +
            "                    <target>" + config.getJavaVersion() + "</target>\n" +
            "                </configuration>\n" +
            "            </plugin>\n" +
            "        </plugins>\n" +
            "    </build>\n" +
            "</project>\n";
        
        fileUtils.writeFile(projectPath + File.separator + "pom.xml", pomContent);
    }

    
    private void generateApplicationProperties(String projectPath, ProjectConfig config) {
        String fullPackage = config.getFullPackage();
        DatabaseType dbType = config.getDatabaseType();
        String projectName = config.getProjectName();
        
        StringBuilder propsContent = new StringBuilder();
        propsContent.append("server.port=8080\n\n");
        
        if (!DatabaseType.NONE.equals(dbType)) {
            switch (dbType) {
                case H2:
                    propsContent.append(String.format("""
                        # H2 Database configuration
                        spring.datasource.url=jdbc:h2:mem:%s
                        spring.datasource.username=sa
                        spring.datasource.password=
                        spring.h2.console.enabled=true
                        spring.jpa.hibernate.ddl-auto=update
                        spring.jpa.show-sql=true
                        """, projectName));
                    break;
                case SQLITE:
                    propsContent.append(String.format("""
                        # SQLite Database configuration
                        spring.datasource.url=jdbc:sqlite:data/%s.db
                        spring.datasource.driver-class-name=org.sqlite.JDBC
                        spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
                        spring.jpa.hibernate.ddl-auto=update
                        spring.jpa.show-sql=true
                        """, projectName));
                    break;
                case POSTGRESQL:
                    propsContent.append(String.format("""
                        # PostgreSQL Database configuration
                        spring.datasource.url=jdbc:postgresql://localhost:5432/%s
                        spring.datasource.username=postgres
                        spring.datasource.password=password
                        spring.jpa.hibernate.ddl-auto=update
                        spring.jpa.show-sql=true
                        spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
                        """, projectName));
                    break;
                case MYSQL:
                    propsContent.append(String.format("""
                        # MySQL Database configuration
                        spring.datasource.url=jdbc:mysql://localhost:3306/%s
                        spring.datasource.username=root
                        spring.datasource.password=password
                        spring.jpa.hibernate.ddl-auto=update
                        spring.jpa.show-sql=true
                        spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
                        """, projectName));
                    break;
            }
            propsContent.append("\n");
        }
        
        propsContent.append(String.format("""
            # Logging configuration
            logging.level.%s=DEBUG
            logging.level.org.springframework.web=INFO
            """, fullPackage));
        
        fileUtils.writeFile(projectPath + File.separator + "src/main/resources/application.properties", propsContent.toString());
    }
    
    private void generateMainApplicationClass(String projectPath, ProjectConfig config) {
        String fullPackage = config.getFullPackage();
        String fullPackagePath = fullPackage.replace(".", File.separator);
        String className = toPascalCase(config.getProjectName()) + "Application";
        
        String classContent = String.format("""
            // src/main/java/%s/%s.java
            package %s;
            
            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;
            
            /**
             * %s - Main Application Class
             * Generated by ClaudeForge
             */
            @SpringBootApplication
            public class %s {
                public static void main(String[] args) {
                    SpringApplication.run(%s.class, args);
                }
            }
            """, fullPackagePath, className, fullPackage, 
                config.getProjectName(), className, className);
        
        String filePath = projectPath + File.separator + "src/main/java/" + fullPackagePath + File.separator + className + ".java";
        fileUtils.writeFile(filePath, classContent);
    }
    
    private void generateBaseResponse(String projectPath, ProjectConfig config) {
        String fullPackage = config.getFullPackage();
        String fullPackagePath = fullPackage.replace(".", File.separator);
        
        String classContent = String.format("""
            // src/main/java/%s/model/response/BaseResponse.java
            package %s.model.response;
            
            import java.time.LocalDateTime;
            
            /**
             * Base Response class for all API responses
             * Generated by ClaudeForge
             */
            public class BaseResponse {
                
                private boolean success;
                private String message;
                private LocalDateTime timestamp;
                
                public BaseResponse() {
                    this.timestamp = LocalDateTime.now();
                }
                
                public static BaseResponse success(String message) {
                    BaseResponse response = new BaseResponse();
                    response.setSuccess(true);
                    response.setMessage(message);
                    return response;
                }
                
                public static BaseResponse error(String message) {
                    BaseResponse response = new BaseResponse();
                    response.setSuccess(false);
                    response.setMessage(message);
                    return response;
                }
                
                // @GENERATE_GETTERS_SETTERS
                public boolean isSuccess() { return success; }
                public void setSuccess(boolean success) { this.success = success; }
                
                public String getMessage() { return message; }
                public void setMessage(String message) { this.message = message; }
                
                public LocalDateTime getTimestamp() { return timestamp; }
                public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
            }
            """, fullPackagePath, fullPackage);
        
        String filePath = projectPath + File.separator + "src/main/java/" + fullPackagePath + File.separator + "model/response/BaseResponse.java";
        fileUtils.writeFile(filePath, classContent);
    }
    
    private void generateBaseEntity(String projectPath, ProjectConfig config) {
        String fullPackage = config.getFullPackage();
        String fullPackagePath = fullPackage.replace(".", File.separator);
        
        String classContent = String.format("""
            // src/main/java/%s/model/entity/BaseEntity.java
            package %s.model.entity;
            
            import com.fasterxml.jackson.annotation.JsonIgnore;
            import jakarta.persistence.*;
            import java.time.LocalDateTime;
            import java.util.UUID;
            
            /**
             * Base Entity class for all entities
             * Generated by ClaudeForge
             */
            @MappedSuperclass
            public abstract class BaseEntity {
                
                @Id
                @GeneratedValue(strategy = GenerationType.UUID)
                private UUID id;
                
                @Column(name = "created_at", nullable = false, updatable = false)
                private LocalDateTime createdAt;
                
                @Column(name = "updated_at")
                private LocalDateTime updatedAt;
                
                @JsonIgnore
                @Column(name = "deleted", nullable = false)
                private boolean deleted = false;
                
                @PrePersist
                protected void onCreate() {
                    this.createdAt = LocalDateTime.now();
                    this.updatedAt = LocalDateTime.now();
                }
                
                @PreUpdate
                protected void onUpdate() {
                    this.updatedAt = LocalDateTime.now();
                }
                
                // @GENERATE_GETTERS_SETTERS
                public UUID getId() { return id; }
                public void setId(UUID id) { this.id = id; }
                
                public LocalDateTime getCreatedAt() { return createdAt; }
                public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
                
                public LocalDateTime getUpdatedAt() { return updatedAt; }
                public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
                
                public boolean isDeleted() { return deleted; }
                public void setDeleted(boolean deleted) { this.deleted = deleted; }
            }
            """, fullPackagePath, fullPackage);
        
        String filePath = projectPath + File.separator + "src/main/java/" + fullPackagePath + File.separator + "model/entity/BaseEntity.java";
        fileUtils.writeFile(filePath, classContent);
    }
    
    private void generateGlobalExceptionHandler(String projectPath, ProjectConfig config) {
        String fullPackage = config.getFullPackage();
        String fullPackagePath = fullPackage.replace(".", File.separator);
        
        String classContent = String.format("""
            // src/main/java/%s/web/handler/GlobalExceptionHandler.java
            package %s.web.handler;
            
            import %s.model.response.BaseResponse;
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import org.springframework.http.HttpStatus;
            import org.springframework.http.ResponseEntity;
            import org.springframework.validation.FieldError;
            import org.springframework.web.bind.MethodArgumentNotValidException;
            import org.springframework.web.bind.annotation.ExceptionHandler;
            import org.springframework.web.bind.annotation.RestControllerAdvice;
            import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
            
            import java.util.stream.Collectors;
            
            /**
             * Global Exception Handler for %s
             * Auto-generated centralized error handling
             */
            @RestControllerAdvice
            public class GlobalExceptionHandler {
            
                private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
            
                @ExceptionHandler(MethodArgumentNotValidException.class)
                public ResponseEntity<BaseResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
                    String errors = ex.getBindingResult().getAllErrors().stream()
                            .map(error -> {
                                String fieldName = ((FieldError) error).getField();
                                String errorMessage = error.getDefaultMessage();
                                return fieldName + ": " + errorMessage;
                            })
                            .collect(Collectors.joining(", "));
            
                    BaseResponse response = BaseResponse.error("Validation failed: " + errors);
                    return ResponseEntity.badRequest().body(response);
                }
            
                @ExceptionHandler(MethodArgumentTypeMismatchException.class)
                public ResponseEntity<BaseResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
                    String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
            
                    BaseResponse response = BaseResponse.error(message);
                    return ResponseEntity.badRequest().body(response);
                }
            
                @ExceptionHandler(Exception.class)
                public ResponseEntity<BaseResponse> handleGenericError(Exception ex) {
                    logger.error("Unexpected error: {}", ex.getMessage(), ex);
            
                    BaseResponse response = BaseResponse.error("An error occurred: " + ex.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }
            """, fullPackagePath, fullPackage, fullPackage, config.getProjectName());
        
        String filePath = projectPath + File.separator + "src/main/java/" + fullPackagePath + File.separator + "web/handler/GlobalExceptionHandler.java";
        fileUtils.writeFile(filePath, classContent);
    }
    
    private void generateRequestLoggingFilter(String projectPath, ProjectConfig config) {
        String fullPackage = config.getFullPackage();
        String fullPackagePath = fullPackage.replace(".", File.separator);
        
        String classContent = String.format("""
            // src/main/java/%s/web/filter/RequestLoggingFilter.java
            package %s.web.filter;
            
            import jakarta.servlet.Filter;
            import jakarta.servlet.FilterChain;
            import jakarta.servlet.ServletException;
            import jakarta.servlet.ServletRequest;
            import jakarta.servlet.ServletResponse;
            import jakarta.servlet.http.HttpServletRequest;
            import jakarta.servlet.http.HttpServletResponse;
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import org.springframework.stereotype.Component;
            
            import java.io.IOException;
            
            /**
             * Request Logging Filter for %s
             * Auto-generated HTTP request/response logging
             */
            @Component
            public class RequestLoggingFilter implements Filter {
                
                private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
                
                @Override
                public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                        throws IOException, ServletException {
                    
                    HttpServletRequest httpRequest = (HttpServletRequest) request;
                    HttpServletResponse httpResponse = (HttpServletResponse) response;
                    
                    // Skip logging for static resources and console endpoints
                    String requestURI = httpRequest.getRequestURI();
                    if (shouldSkipLogging(requestURI)) {
                        chain.doFilter(request, response);
                        return;
                    }
                    
                    long startTime = System.currentTimeMillis();
                    
                    // Log incoming request
                    logger.info("{} {} - Started", 
                            httpRequest.getMethod(), 
                            httpRequest.getRequestURI());
                    
                    try {
                        // Continue with the filter chain
                        chain.doFilter(request, response);
                        
                        // Log completed request
                        long duration = System.currentTimeMillis() - startTime;
                        
                        logger.info("{} {} - Completed in {}ms [Status: {}]",
                                httpRequest.getMethod(),
                                httpRequest.getRequestURI(),
                                duration,
                                httpResponse.getStatus());
                                
                    } catch (Exception ex) {
                        // Log failed request
                        long duration = System.currentTimeMillis() - startTime;
                        logger.error("{} {} - Failed in {}ms - Error: {}",
                                httpRequest.getMethod(),
                                httpRequest.getRequestURI(),
                                duration,
                                ex.getMessage());
                        throw ex;
                    }
                }
                
                private boolean shouldSkipLogging(String requestURI) {
                    return requestURI.startsWith("/static/") ||
                           requestURI.startsWith("/css/") ||
                           requestURI.startsWith("/js/") ||
                           requestURI.startsWith("/images/") ||
                           requestURI.startsWith("/favicon.ico") ||
                           requestURI.startsWith("/h2-console") ||
                           requestURI.startsWith("/actuator");
                }
            }
            """, fullPackagePath, fullPackage, config.getProjectName());
        
        String filePath = projectPath + File.separator + "src/main/java/" + fullPackagePath + File.separator + "web/filter/RequestLoggingFilter.java";
        fileUtils.writeFile(filePath, classContent);
    }
    
    private void generateStaticWebResources(String projectPath, ProjectConfig config) {
        // Generate a simple index.html for Java Rest API + HTML/CSS/JS
        String indexHtml = String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <link rel="stylesheet" href="/css/style.css">
            </head>
            <body>
                <div class="container">
                    <h1>Welcome to %s</h1>
                    <p>Your Spring Boot application with static web resources is running!</p>
                    <div id="app"></div>
                </div>
                <script src="/js/app.js"></script>
            </body>
            </html>
            """, config.getProjectName(), config.getProjectName());
        
        String basicCss = """
            body {
                font-family: Arial, sans-serif;
                margin: 0;
                padding: 20px;
                background: #f5f5f5;
            }
            
            .container {
                max-width: 800px;
                margin: 0 auto;
                background: white;
                padding: 30px;
                border-radius: 8px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            }
            
            h1 {
                color: #333;
                text-align: center;
            }
            
            p {
                color: #666;
                text-align: center;
                font-size: 18px;
            }
            
            #app {
                margin-top: 30px;
                padding: 20px;
                border: 1px solid #ddd;
                border-radius: 4px;
                background: #f9f9f9;
            }
            """;
        
        String basicJs = String.format("""
            document.addEventListener('DOMContentLoaded', function() {
                console.log('%s application loaded');
                
                const app = document.getElementById('app');
                app.innerHTML = '<p>JavaScript is working! Ready for your custom code.</p>';
                
                // Example API call to your backend
                // fetch('/api/health')
                //     .then(response => response.json())
                //     .then(data => console.log(data))
                //     .catch(error => console.error('Error:', error));
            });
            """, config.getProjectName());
        
        fileUtils.writeFile(projectPath + "/src/main/resources/static/index.html", indexHtml);
        fileUtils.writeFile(projectPath + "/src/main/resources/static/css/style.css", basicCss);
        fileUtils.writeFile(projectPath + "/src/main/resources/static/js/app.js", basicJs);
    }
    
    private String toPascalCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : input.toCharArray()) {
            if (Character.isLetter(c)) {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            } else {
                capitalizeNext = true;
            }
        }
        
        return result.toString();
    }
}