package com.megacreative.coding.validation;

import com.megacreative.coding.CodeBlock;
import java.util.*;

/**
 * Validates block connections to detect circular references and other graph-related issues.
 */
public class BlockGraphValidator {
    
    private final Set<UUID> visitedBlocks = new HashSet<>();
    private final Set<UUID> currentPath = new HashSet<>();
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    
    /**
     * Validates the block graph starting from the root block.
     * @param rootBlock The root block of the script
     * @return ValidationResult containing any errors or warnings found
     */
    public ValidationResult validate(CodeBlock rootBlock) {
        if (rootBlock == null) {
            return new ValidationResult(false, List.of("Root block cannot be null"));
        }
        
        visitedBlocks.clear();
        currentPath.clear();
        errors.clear();
        warnings.clear();
        
        validateBlock(rootBlock);
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    private void validateBlock(CodeBlock block) {
        if (block == null) return;
        
        UUID blockId = block.getId();
        
        
        if (currentPath.contains(blockId)) {
            errors.add("Circular reference detected in block chain");
            return;
        }
        
        
        if (visitedBlocks.contains(blockId)) {
            return;
        }
        
        
        visitedBlocks.add(blockId);
        currentPath.add(blockId);
        
        try {
            
            validateBlock(block.getNextBlock());
            
            
            for (CodeBlock child : block.getChildren()) {
                validateBlock(child);
            }
            
        } finally {
            
            currentPath.remove(blockId);
        }
    }
    
    /**
     * Result of block graph validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this(valid, errors, List.of());
        }
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
    }
}
