// src/main/java/${basePackage?replace(".", "/")}/controller/${className}Controller.java
package ${basePackage}.controller;

import ${basePackage}.model.${className};
import ${basePackage}.service.${className}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * ${className} REST Controller
 * 
 * Provides RESTful API endpoints for ${className} management.
 * Supports CRUD operations and follows REST best practices.
 * 
 * @author ClaudeForge
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/${className?lower_case}s")
@CrossOrigin(origins = "*")
public class ${className}Controller {

    private final ${className}Service ${className?uncap_first}Service;

    @Autowired
    public ${className}Controller(${className}Service ${className?uncap_first}Service) {
        this.${className?uncap_first}Service = ${className?uncap_first}Service;
    }

    /**
     * Get all ${className?lower_case}s
     * 
     * @return List of all ${className?lower_case}s
     */
    @GetMapping
    public ResponseEntity<List<${className}>> getAll${className}s() {
        List<${className}> ${className?uncap_first}s = ${className?uncap_first}Service.findAll();
        return ResponseEntity.ok(${className?uncap_first}s);
    }

    /**
     * Get ${className?lower_case} by ID
     * 
     * @param id ${className} ID
     * @return ${className} if found, 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<${className}> get${className}ById(@PathVariable Long id) {
        Optional<${className}> ${className?uncap_first} = ${className?uncap_first}Service.findById(id);
        return ${className?uncap_first}.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new ${className?lower_case}
     * 
     * @param ${className?uncap_first} ${className} data to create
     * @return Created ${className}
     */
    @PostMapping
    public ResponseEntity<${className}> create${className}(@Valid @RequestBody ${className} ${className?uncap_first}) {
        ${className} created${className} = ${className?uncap_first}Service.save(${className?uncap_first});
        return ResponseEntity.ok(created${className});
    }

    /**
     * Update existing ${className?lower_case}
     * 
     * @param id ${className} ID to update
     * @param ${className?uncap_first} Updated ${className} data
     * @return Updated ${className} or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<${className}> update${className}(@PathVariable Long id, @Valid @RequestBody ${className} ${className?uncap_first}) {
        if (!${className?uncap_first}Service.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ${className?uncap_first}.setId(id);
        ${className} updated${className} = ${className?uncap_first}Service.save(${className?uncap_first});
        return ResponseEntity.ok(updated${className});
    }

    /**
     * Delete ${className?lower_case} by ID
     * 
     * @param id ${className} ID to delete
     * @return 204 No Content if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete${className}(@PathVariable Long id) {
        if (!${className?uncap_first}Service.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ${className?uncap_first}Service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
