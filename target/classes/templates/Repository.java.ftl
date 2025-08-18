// src/main/java/${basePackage?replace(".", "/")}/repository/${className}Repository.java
package ${basePackage}.repository;

import ${basePackage}.model.${className};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ${className} Repository
 * 
 * Data access layer for ${className} entities.
 * Extends JpaRepository for standard CRUD operations.
 */
@Repository
public interface ${className}Repository extends JpaRepository<${className}, Long> {

    // Additional custom query methods can be added here
    
}