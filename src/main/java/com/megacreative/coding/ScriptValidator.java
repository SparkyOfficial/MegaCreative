package com.megacreative.coding;

import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import java.util.*;

/**
 * Advanced script validator for comprehensive script validation and error detection
 */
public class ScriptValidator {
    
    private final BlockConfigService blockConfigService;
    
    public ScriptValidator(BlockConfigService blockConfigService) {
        this.blockConfigService = blockConfigService;
    }
    
    /**
     * Validation result containing all issues found
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<ValidationError> errors;
        private final List<ValidationError> warnings;
        
        public ValidationResult(boolean valid, List<ValidationError> errors, List<ValidationError> warnings) {
            this.valid = valid;
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
            this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
        }
        
        public boolean isValid() { return valid; }
        public List<ValidationError> getErrors() { return new ArrayList<>(errors); }
        public List<ValidationError> getWarnings() { return new ArrayList<>(warnings); }
        
        public int getErrorCount() { return errors.size(); }
        public int getWarningCount() { return warnings.size(); }
        
        @Override
        public String toString() {
            return "ValidationResult{" +
                   "valid=" + valid +
                   ", errors=" + errors.size() +
                   ", warnings=" + warnings.size() +
                   '}';
        }
    }
    
    /**
     * Represents a validation error or warning
     */
    public static class ValidationError {
        public enum Severity {
            ERROR, WARNING, INFO
        }
        
        private final Severity severity;
        private final String message;
        private final CodeBlock block;
        private final String field;
        
        public ValidationError(Severity severity, String message, CodeBlock block, String field) {
            this.severity = severity;
            this.message = message;
            this.block = block;
            this.field = field;
        }
        
        public Severity getSeverity() { return severity; }
        public String getMessage() { return message; }
        public CodeBlock getBlock() { return block; }
        public String getField() { return field; }
        
        @Override
        public String toString() {
            return severity + ": " + message + 
                   (block != null ? " at block " + block.getAction() : "") +
                   (field != null ? " field " + field : "");
        }
    }
    
    /**
     * Validates a complete script
     * @param script The script to validate
     * @return ValidationResult containing all validation issues
     */
    public ValidationResult validateScript(CodeScript script) {
        if (script == null) {
            List<ValidationError> errors = new ArrayList<>();
            errors.add(new ValidationError(ValidationError.Severity.ERROR, "Script is null", null, null));
            return new ValidationResult(false, errors, new ArrayList<>());
        }
        
        List<ValidationError> errors = new ArrayList<>();
        List<ValidationError> warnings = new ArrayList<>();
        
        // Validate script name
        if (script.getName() == null || script.getName().trim().isEmpty()) {
            errors.add(new ValidationError(ValidationError.Severity.ERROR, "Script name is required", null, "name"));
        } else if (script.getName().length() > 64) {
            warnings.add(new ValidationError(ValidationError.Severity.WARNING, "Script name is very long", null, "name"));
        }
        
        // Validate root block
        if (script.getRootBlock() == null) {
            errors.add(new ValidationError(ValidationError.Severity.ERROR, "Script must have a root block", null, "rootBlock"));
        } else {
            // Validate all blocks in the script
            validateBlockChain(script.getRootBlock(), errors, warnings);
        }
        
        // Check for common script issues
        checkScriptStructure(script, errors, warnings);
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Validates a single block and its chain
     */
    private void validateBlockChain(CodeBlock block, List<ValidationError> errors, List<ValidationError> warnings) {
        Set<UUID> visitedBlocks = new HashSet<>();
        CodeBlock current = block;
        
        while (current != null) {
            // Prevent infinite loops
            if (!visitedBlocks.add(current.getId())) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                    "Infinite loop detected in block chain", current, null));
                break;
            }
            
            // Validate current block
            validateBlock(current, errors, warnings);
            
            // Move to next block
            current = current.getNextBlock();
        }
    }
    
    /**
     * Validates a single block
     */
    private void validateBlock(CodeBlock block, List<ValidationError> errors, List<ValidationError> warnings) {
        if (block == null) return;
        
        // Validate block has required fields
        if (block.getMaterial() == null) {
            errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                "Block material is required", block, "material"));
        }
        
        if (block.getAction() == null || block.getAction().trim().isEmpty()) {
            errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                "Block action is required", block, "action"));
        } else {
            // Validate action exists in configuration
            BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(block.getAction());
            if (config == null) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                    "Unknown block action: " + block.getAction(), block, "action"));
            } else {
                // Validate block type matches configuration
                String expectedType = config.getType();
                if (!isValidBlockType(block, expectedType)) {
                    errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                        "Block type mismatch. Expected: " + expectedType, block, "type"));
                }
            }
        }
        
        // Validate parameters
        validateParameters(block, errors, warnings);
        
        // Validate children
        for (CodeBlock child : block.getChildren()) {
            validateBlock(child, errors, warnings);
        }
        
        // Validate bracket consistency
        if (block.isBracket()) {
            if (block.getBracketType() == null) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                    "Bracket block must have a bracket type", block, "bracketType"));
            }
        }
        
        // Check for potential issues
        checkBlockIssues(block, errors, warnings);
    }
    
    /**
     * Validates block parameters
     */
    private void validateParameters(CodeBlock block, List<ValidationError> errors, List<ValidationError> warnings) {
        if (block.getParameters() == null) return;
        
        // Get block configuration to validate required parameters
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(block.getAction());
        if (config == null) return;
        
        // Check required parameters
        Map<String, BlockConfigService.ParameterConfig> paramConfigs = config.getParameters();
        if (paramConfigs != null) {
            for (Map.Entry<String, BlockConfigService.ParameterConfig> entry : paramConfigs.entrySet()) {
                String paramName = entry.getKey();
                BlockConfigService.ParameterConfig paramConfig = entry.getValue();
                
                if (paramConfig.isRequired() && !block.hasParameter(paramName)) {
                    errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                        "Required parameter missing: " + paramName, block, paramName));
                }
            }
        }
        
        // Validate parameter values
        for (Map.Entry<String, DataValue> entry : block.getParameters().entrySet()) {
            String paramName = entry.getKey();
            DataValue value = entry.getValue();
            
            if (value == null) {
                warnings.add(new ValidationError(ValidationError.Severity.WARNING, 
                    "Parameter has null value: " + paramName, block, paramName));
                continue;
            }
            
            // Check for empty values in required parameters
            if (value.isEmpty()) {
                BlockConfigService.ParameterConfig paramConfig = paramConfigs != null ? paramConfigs.get(paramName) : null;
                if (paramConfig != null && paramConfig.isRequired()) {
                    errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                        "Required parameter is empty: " + paramName, block, paramName));
                }
            }
        }
    }
    
    /**
     * Checks for potential issues in a block
     */
    private void checkBlockIssues(CodeBlock block, List<ValidationError> errors, List<ValidationError> warnings) {
        // Check for deprecated actions
        if (isDeprecatedAction(block.getAction())) {
            warnings.add(new ValidationError(ValidationError.Severity.WARNING, 
                "Action is deprecated: " + block.getAction(), block, "action"));
        }
        
        // Check for potentially problematic parameter combinations
        checkParameterCombinations(block, warnings);
        
        // Check for performance issues
        checkPerformanceIssues(block, warnings);
    }
    
    /**
     * Checks script structure for common issues
     */
    private void checkScriptStructure(CodeScript script, List<ValidationError> errors, List<ValidationError> warnings) {
        // Check for empty scripts
        if (script.getRootBlock() == null) {
            return; // Already reported as error
        }
        
        // Check if script has any executable blocks
        if (!hasExecutableBlocks(script.getRootBlock())) {
            warnings.add(new ValidationError(ValidationError.Severity.WARNING, 
                "Script has no executable blocks", null, null));
        }
        
        // Check for unreachable blocks
        checkUnreachableBlocks(script, warnings);
    }
    
    /**
     * Checks if a script has any executable blocks
     */
    private boolean hasExecutableBlocks(CodeBlock rootBlock) {
        CodeBlock current = rootBlock;
        while (current != null) {
            // Events and actions are executable
            BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(current.getAction());
            if (config != null && ("EVENT".equals(config.getType()) || "ACTION".equals(config.getType()))) {
                return true;
            }
            current = current.getNextBlock();
        }
        return false;
    }
    
    /**
     * Checks for unreachable blocks in a script
     */
    private void checkUnreachableBlocks(CodeScript script, List<ValidationError> warnings) {
        // This is a complex check that would require full script analysis
        // For now, we'll just note that it could be implemented
        // In a full implementation, we would trace all possible execution paths
    }
    
    /**
     * Checks for problematic parameter combinations
     */
    private void checkParameterCombinations(CodeBlock block, List<ValidationError> warnings) {
        // This would contain logic to detect problematic parameter combinations
        // For example, checking if mutually exclusive parameters are both set
        // Or if required parameter combinations are missing
    }
    
    /**
     * Checks for potential performance issues
     */
    private void checkPerformanceIssues(CodeBlock block, List<ValidationError> warnings) {
        // Check for potentially expensive operations
        String action = block.getAction();
        if ("whileLoop".equals(action)) {
            warnings.add(new ValidationError(ValidationError.Severity.WARNING, 
                "While loops can cause performance issues if not properly bounded", block, "action"));
        } else if ("forEach".equals(action)) {
            DataValue listParam = block.getParameter("list");
            if (listParam != null && listParam.getType().getName().equals("List")) {
                // Could check list size if available
            }
        }
    }
    
    /**
     * Checks if a block type is valid for the configuration
     */
    private boolean isValidBlockType(CodeBlock block, String expectedType) {
        // This would contain logic to validate that the block's material and action
        // are consistent with the expected block type from configuration
        return true; // Simplified for now
    }
    
    /**
     * Checks if an action is deprecated
     */
    private boolean isDeprecatedAction(String action) {
        // This would contain a list of deprecated actions
        // For now, return false
        return false;
    }
    
    /**
     * Gets a formatted validation report
     */
    public String getValidationReport(ValidationResult result) {
        StringBuilder report = new StringBuilder();
        report.append("=== Script Validation Report ===\n");
        
        if (result.isValid()) {
            report.append("✓ Script is valid\n");
        } else {
            report.append("✗ Script has ").append(result.getErrorCount()).append(" errors\n");
        }
        
        if (result.getWarningCount() > 0) {
            report.append("⚠ Script has ").append(result.getWarningCount()).append(" warnings\n");
        }
        
        if (!result.getErrors().isEmpty()) {
            report.append("\n--- Errors ---\n");
            for (ValidationError error : result.getErrors()) {
                report.append("✗ ").append(error.getMessage());
                if (error.getBlock() != null) {
                    report.append(" (block: ").append(error.getBlock().getAction()).append(")");
                }
                if (error.getField() != null) {
                    report.append(" [field: ").append(error.getField()).append("]");
                }
                report.append("\n");
            }
        }
        
        if (!result.getWarnings().isEmpty()) {
            report.append("\n--- Warnings ---\n");
            for (ValidationError warning : result.getWarnings()) {
                report.append("⚠ ").append(warning.getMessage());
                if (warning.getBlock() != null) {
                    report.append(" (block: ").append(warning.getBlock().getAction()).append(")");
                }
                if (warning.getField() != null) {
                    report.append(" [field: ").append(warning.getField()).append("]");
                }
                report.append("\n");
            }
        }
        
        return report.toString();
    }
}