# Server Configuration
server.port=8080

# Database Configuration
<#if databaseType == "H2">
spring.datasource.url=jdbc:h2:mem:${projectName}
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
<#elseif databaseType == "MYSQL">
spring.datasource.url=jdbc:mysql://localhost:3306/${projectName}
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
<#elseif databaseType == "POSTGRESQL">
spring.datasource.url=jdbc:postgresql://localhost:5432/${projectName}
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
</#if>

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Jackson Configuration
spring.jackson.serialization.indent_output=true

# Logging
logging.level.${basePackage}=DEBUG