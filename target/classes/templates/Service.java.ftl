// src/main/java/${basePackage?replace(".", "/")}/service/${className}Service.java
package ${basePackage}.service;

import ${basePackage}.model.${className};
import ${basePackage}.repository.${className}Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ${className} Service
 * 
 * Business logic layer for ${className} operations.
 * Handles data processing and business rules.
 */
@Service
public class ${className}Service {

    private final ${className}Repository ${className?uncap_first}Repository;

    @Autowired
    public ${className}Service(${className}Repository ${className?uncap_first}Repository) {
        this.${className?uncap_first}Repository = ${className?uncap_first}Repository;
    }

    public List<${className}> findAll() {
        return ${className?uncap_first}Repository.findAll();
    }

    public Optional<${className}> findById(Long id) {
        return ${className?uncap_first}Repository.findById(id);
    }

    public ${className} save(${className} ${className?uncap_first}) {
        return ${className?uncap_first}Repository.save(${className?uncap_first});
    }

    public boolean existsById(Long id) {
        return ${className?uncap_first}Repository.existsById(id);
    }

    public void deleteById(Long id) {
        ${className?uncap_first}Repository.deleteById(id);
    }
}