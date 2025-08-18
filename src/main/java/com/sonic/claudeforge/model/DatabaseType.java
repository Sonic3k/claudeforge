// src/main/java/com/sonic/claudeforge/model/DatabaseType.java
package com.sonic.claudeforge.model;

public enum DatabaseType {
    NONE("none", "", "", "", "No Database"),
    H2("com.h2database", "h2", "org.h2.Driver", "jdbc:h2:mem:", "H2 Database"),
    SQLITE("org.xerial", "sqlite-jdbc", "org.sqlite.JDBC", "jdbc:sqlite:", "SQLite"),
    POSTGRESQL("org.postgresql", "postgresql", "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/", "PostgreSQL"),
    MYSQL("com.mysql", "mysql-connector-j", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/", "MySQL");
    
    private final String groupId;
    private final String artifactId;
    private final String driverClassName;
    private final String urlPrefix;
    private final String displayName;
    
    DatabaseType(String groupId, String artifactId, String driverClassName, String urlPrefix, String displayName) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.driverClassName = driverClassName;
        this.urlPrefix = urlPrefix;
        this.displayName = displayName;
    }
    
    // @GENERATE_GETTERS_SETTERS
    public String getGroupId() { return groupId; }
    public String getArtifactId() { return artifactId; }
    public String getDriverClassName() { return driverClassName; }
    public String getUrlPrefix() { return urlPrefix; }
    public String getDisplayName() { return displayName; }
}