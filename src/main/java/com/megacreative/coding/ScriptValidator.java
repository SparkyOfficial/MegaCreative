package com.megacreative.coding;

import com.megacreative.coding.validation.BlockGraphValidator;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import java.util.*;

/**
 * Advanced script validator for comprehensive script validation and error detection
 */
public class ScriptValidator {
    
    private static final String ERROR_SCRIPT_IS_NULL = "Script is null";
    private static final String ERROR_SCRIPT_NAME_REQUIRED = "Script name is required";
    private static final String WARNING_SCRIPT_NAME_LONG = "Script name is very long";
    private static final String ERROR_SCRIPT_ROOT_BLOCK_REQUIRED = "Script must have a root block";
    private static final String ERROR_BLOCK_MATERIAL_REQUIRED = "Block material is required";
    private static final String ERROR_BLOCK_ACTION_REQUIRED = "Block action is required";
    private static final String ERROR_UNKNOWN_BLOCK_ACTION = "Unknown block action: ";
    private static final String ERROR_REQUIRED_PARAMETER_MISSING = "Required parameter missing: ";
    private static final String WARNING_PARAMETER_NULL_VALUE = "Parameter has null value: ";
    private static final String ERROR_REQUIRED_PARAMETER_EMPTY = "Required parameter is empty: ";
    private static final String ERROR_BLOCK_TYPE_MISMATCH = "Block type mismatch. Expected: ";
    private static final String ERROR_BRACKET_TYPE_REQUIRED = "Bracket block must have a bracket type";
    private static final String WARNING_ACTION_DEPRECATED = "Action is deprecated: ";
    private static final String WARNING_SCRIPT_NO_EXECUTABLE_BLOCKS = "Script has no executable blocks";
    private static final String WARNING_WHILE_LOOP_PERFORMANCE = "While loops can cause performance issues if not properly bounded";
    
    private final BlockConfigService blockConfigService;
    private final BlockGraphValidator blockGraphValidator;
    
    public ScriptValidator(BlockConfigService blockConfigService) {
        this.blockConfigService = blockConfigService;
        this.blockGraphValidator = new BlockGraphValidator();
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
            return createNullScriptResult();
        }
        
        List<ValidationError> errors = new ArrayList<>();
        List<ValidationError> warnings = new ArrayList<>();
        
        validateScriptName(script, errors, warnings);
        
        validateRootBlock(script, errors, warnings);
        
        checkScriptStructure(script, errors, warnings);
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Creates a validation result for a null script
     */
    private ValidationResult createNullScriptResult() {
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError(ValidationError.Severity.ERROR, ERROR_SCRIPT_IS_NULL, null, null));
        return new ValidationResult(false, errors, new ArrayList<>());
    }
    
    /**
     * Validates the script name
     */
    private void validateScriptName(CodeScript script, List<ValidationError> errors, List<ValidationError> warnings) {
        if (script.getName() == null || script.getName().trim().isEmpty()) {
            errors.add(new ValidationError(ValidationError.Severity.ERROR, ERROR_SCRIPT_NAME_REQUIRED, null, "name"));
        } else if (script.getName().length() > 64) {
            warnings.add(new ValidationError(ValidationError.Severity.WARNING, WARNING_SCRIPT_NAME_LONG, null, "name"));
        }
    }
    
    /**
     * Validates the root block of a script
     */
    private void validateRootBlock(CodeScript script, List<ValidationError> errors, List<ValidationError> warnings) {
        if (script.getRootBlock() == null) {
            errors.add(new ValidationError(ValidationError.Severity.ERROR, ERROR_SCRIPT_ROOT_BLOCK_REQUIRED, null, "rootBlock"));
        } else {
            
            BlockGraphValidator.ValidationResult graphResult = blockGraphValidator.validate(script.getRootBlock());
            if (!graphResult.isValid()) {
                for (String error : graphResult.getErrors()) {
                    errors.add(new ValidationError(ValidationError.Severity.ERROR, error, null, null));
                }
            }
            for (String warning : graphResult.getWarnings()) {
                warnings.add(new ValidationError(ValidationError.Severity.WARNING, warning, null, null));
            }
        }
    }
    
    /**
     * Validates a single block and its chain
     */
    private void validateBlockChain(CodeBlock block, List<ValidationError> errors, List<ValidationError> warnings) {
        Set<UUID> visitedBlocks = new HashSet<>();
        CodeBlock current = block;
        
        while (current != null) {
            
            if (!visitedBlocks.add(current.getId())) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                    "Infinite loop detected in block chain", current, null));
                break;
            }
            
            validateBlock(current, errors, warnings);
            
            current = current.getNextBlock();
        }
    }
    
    /**
     * Validates a single block
     */
    private void validateBlock(CodeBlock block, List<ValidationError> errors, List<ValidationError> warnings) {
        if (block == null) return;
        
        validateBlockFields(block, errors);
        
        validateBlockAction(block, errors, warnings);
        
        validateBlockChildren(block, errors, warnings);
        
        validateBracketConsistency(block, errors);
        
        checkBlockIssues(block, errors, warnings);
    }
    
    /**
     * Validates block required fields
     */
    private void validateBlockFields(CodeBlock block, List<ValidationError> errors) {
        if (block.getMaterial() == null) {
            errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                ERROR_BLOCK_MATERIAL_REQUIRED, block, "material"));
        }
        
        if (block.getAction() == null || block.getAction().trim().isEmpty()) {
            errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                ERROR_BLOCK_ACTION_REQUIRED, block, "action"));
        }
    }
    
    /**
     * Validates block action and parameters
     */
    private void validateBlockAction(CodeBlock block, List<ValidationError> errors, List<ValidationError> warnings) {
        String action = block.getAction();
        if (action == null || action.trim().isEmpty()) {
            return; 
        }
        
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(action);
        if (config == null) {
            errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                ERROR_UNKNOWN_BLOCK_ACTION + action, block, "action"));
        } else {
            
            validateBlockParameters(block, config, errors, warnings);
            
            validateBlockType(block, config, errors);
        }
    }
    
    /**
     * Validates block parameters against configuration
     */
    private void validateBlockParameters(CodeBlock block, BlockConfigService.BlockConfig config, 
                                       List<ValidationError> errors, List<ValidationError> warnings) {
        
        Map<String, BlockConfigService.ParameterConfig> paramConfigs = config.getActionParameters(); 
        if (paramConfigs != null) {
            checkRequiredParameters(block, paramConfigs, errors);
        }
        
        validateParameterValues(block, paramConfigs, errors, warnings);
    }
    
    /**
     * Checks required parameters
     */
    private void checkRequiredParameters(CodeBlock block, Map<String, BlockConfigService.ParameterConfig> paramConfigs, 
                                       List<ValidationError> errors) {
        for (Map.Entry<String, BlockConfigService.ParameterConfig> entry : paramConfigs.entrySet()) {
            String paramName = entry.getKey();
            BlockConfigService.ParameterConfig paramConfig = entry.getValue();
            
            if (paramConfig.isRequired() && !block.hasParameter(paramName)) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                    ERROR_REQUIRED_PARAMETER_MISSING + paramName, block, paramName));
            }
        }
    }
    
    /**
     * Validates parameter values
     */
    private void validateParameterValues(CodeBlock block, Map<String, BlockConfigService.ParameterConfig> paramConfigs, 
                                       List<ValidationError> errors, List<ValidationError> warnings) {
        for (Map.Entry<String, DataValue> entry : block.getParameters().entrySet()) {
            String paramName = entry.getKey();
            DataValue value = entry.getValue();
            
            if (value == null) {
                warnings.add(new ValidationError(ValidationError.Severity.WARNING, 
                    WARNING_PARAMETER_NULL_VALUE + paramName, block, paramName));
                continue;
            }
            
            BlockConfigService.ParameterConfig paramConfig = paramConfigs != null ? paramConfigs.get(paramName) : null;
            if (paramConfig != null && paramConfig.isRequired()) {
                if (value.isEmpty()) {
                    errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                        ERROR_REQUIRED_PARAMETER_EMPTY + paramName, block, paramName));
                }
            }
        }
    }
    
    /**
     * Validates block type matches configuration
     */
    private void validateBlockType(CodeBlock block, BlockConfigService.BlockConfig config, List<ValidationError> errors) {
        String expectedType = config.getType();
        if (!isValidBlockType(block, expectedType)) {
            errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                ERROR_BLOCK_TYPE_MISMATCH + expectedType, block, "type"));
        }
    }
    
    /**
     * Validates block children
     */
    private void validateBlockChildren(CodeBlock block, List<ValidationError> errors, List<ValidationError> warnings) {
        for (CodeBlock child : block.getChildren()) {
            validateBlock(child, errors, warnings);
        }
    }
    
    /**
     * Validates bracket consistency
     */
    private void validateBracketConsistency(CodeBlock block, List<ValidationError> errors) {
        if (block.isBracket()) {
            if (block.getBracketType() == null) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, 
                    ERROR_BRACKET_TYPE_REQUIRED, block, "bracketType"));
            }
        }
    }
    
    /**
     * Checks for potential issues in a block
     */
    private void checkBlockIssues(CodeBlock block, List<ValidationError> errors, List<ValidationError> warnings) {
        
        if (isDeprecatedAction(block.getAction())) {
            warnings.add(new ValidationError(ValidationError.Severity.WARNING, 
                WARNING_ACTION_DEPRECATED + block.getAction(), block, "action"));
        }
        
        checkParameterCombinations(block, warnings);
        
        checkPerformanceIssues(block, warnings);
    }
    
    /**
     * Checks script structure for common issues
     */
    private void checkScriptStructure(CodeScript script, List<ValidationError> errors, List<ValidationError> warnings) {
        
        if (script.getRootBlock() == null) {
            return; 
        }
        
        if (!hasExecutableBlocks(script.getRootBlock())) {
            warnings.add(new ValidationError(ValidationError.Severity.WARNING, 
                WARNING_SCRIPT_NO_EXECUTABLE_BLOCKS, null, null));
        }
        
        checkUnreachableBlocks(script, warnings);
    }
    
    /**
     * Checks if a script has any executable blocks
     */
    private boolean hasExecutableBlocks(CodeBlock rootBlock) {
        CodeBlock current = rootBlock;
        while (current != null) {
            
            BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(current.getAction());
            if (config != null && ("EVENT".equals(config.getType()) || "ACTION".equals(config.getType()))) {
                return true;
            }
            current = current.getNextBlock();
        }
        return false;
    }
    
    /**
     * Checks for unreachable blocks in the script
     */
    private void checkUnreachableBlocks(CodeScript script, List<ValidationError> warnings) {
        if (script == null || script.getRootBlock() == null) {
            return;
        }
        
        Set<CodeBlock> allBlocks = new HashSet<>();
        collectAllBlocks(script.getRootBlock(), allBlocks);
        
        Set<CodeBlock> reachableBlocks = new HashSet<>();
        findReachableBlocks(script.getRootBlock(), reachableBlocks);
        
        Set<CodeBlock> unreachableBlocks = new HashSet<>(allBlocks);
        unreachableBlocks.removeAll(reachableBlocks);
        
        for (CodeBlock block : unreachableBlocks) {
            warnings.add(new ValidationError(ValidationError.Severity.WARNING, 
                "Unreachable block: " + (block.getAction() != null ? block.getAction() : "unnamed"), 
                block, null));
        }
    }
    
    /**
     * Collects all blocks in the script recursively
     */
    private void collectAllBlocks(CodeBlock block, Set<CodeBlock> allBlocks) {
        if (block == null || allBlocks.contains(block)) {
            return;
        }
        
        allBlocks.add(block);
        
        if (block.getNextBlock() != null) {
            collectAllBlocks(block.getNextBlock(), allBlocks);
        }
        
        for (CodeBlock child : block.getChildren()) {
            collectAllBlocks(child, allBlocks);
        }
        
        if ("conditionalBranch".equals(block.getAction())) {
            
            CodeBlock trueBlock = block.getNextBlock();
            if (trueBlock != null) {
                CodeBlock current = trueBlock;
                while (current != null && current.getNextBlock() != null) {
                    current = current.getNextBlock();
                }
                if (current != null && "ELSE".equals(current.getAction())) {
                    collectAllBlocks(current.getNextBlock(), allBlocks);
                }
            }
        }
    }
    
    /**
     * Finds all reachable blocks from the root block
     */
    private void findReachableBlocks(CodeBlock block, Set<CodeBlock> reachableBlocks) {
        if (block == null || reachableBlocks.contains(block)) {
            return;
        }
        
        reachableBlocks.add(block);
        
        if (block.getNextBlock() != null) {
            findReachableBlocks(block.getNextBlock(), reachableBlocks);
        }
        
        for (CodeBlock child : block.getChildren()) {
            findReachableBlocks(child, reachableBlocks);
        }
        
        if ("conditionalBranch".equals(block.getAction())) {
            
            CodeBlock trueBlock = block.getNextBlock();
            if (trueBlock != null) {
                findReachableBlocks(trueBlock, reachableBlocks);
                
                CodeBlock current = trueBlock;
                while (current != null && current.getNextBlock() != null) {
                    current = current.getNextBlock();
                }
                if (current != null && "ELSE".equals(current.getAction())) {
                    findReachableBlocks(current.getNextBlock(), reachableBlocks);
                }
            }
        } else if ("whileLoop".equals(block.getAction())) {
            
            if (block.getNextBlock() != null) {
                findReachableBlocks(block.getNextBlock(), reachableBlocks);
            }
        } else if ("forEach".equals(block.getAction())) {
            
            if (block.getNextBlock() != null) {
                findReachableBlocks(block.getNextBlock(), reachableBlocks);
            }
        }
    }
    
    /**
     * Checks for problematic parameter combinations
     */
    private void checkParameterCombinations(CodeBlock block, List<ValidationError> warnings) {
        // Static analysis warning fix - empty method body
        // This method is a placeholder for future implementation
        // TODO: Implement logic for checking problematic parameter combinations
    }
    
    /**
     * Checks for potential performance issues
     */
    private void checkPerformanceIssues(CodeBlock block, List<ValidationError> warnings) {
        
        String action = block.getAction();
        if ("whileLoop".equals(action)) {
            warnings.add(new ValidationError(ValidationError.Severity.WARNING, 
                WARNING_WHILE_LOOP_PERFORMANCE, block, "action"));
        } else if ("forEach".equals(action)) {
            DataValue listParam = block.getParameter("list");
            if (listParam != null && listParam.getType().getName().equals("List")) {
                // Static analysis warning fix - empty if body
                // This check is a placeholder for future implementation
                // TODO: Implement logic for checking list parameter in forEach loop
            }
        }
    }
    
    /**
     * Checks if a block type is valid for the configuration
     */
    private boolean isValidBlockType(CodeBlock block, String expectedType) {
        
        if (block == null || expectedType == null) {
            return false;
        }
        
        if (blockConfigService == null) {
            return false;
        }
        
        List<BlockConfigService.BlockConfig> configs = blockConfigService.getBlockConfigsForMaterial(block.getMaterial());
        if (configs == null || configs.isEmpty()) {
            return false;
        }
        
        for (BlockConfigService.BlockConfig config : configs) {
            List<String> actions = config.getActions();
            if (actions != null && actions.contains(block.getAction())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if an action is deprecated
     */
    private boolean isDeprecatedAction(String action) {
        
        if (action == null) return false;
        
        Set<String> deprecatedActions = Set.of(
            "oldSendMessage", 
            "deprecatedTeleport",
            "outdatedGiveItem",
            "legacySetBlock",
            "removedPlaySound"
        );
        
        return deprecatedActions.contains(action);
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
        
        appendErrorDetails(result, report);
        appendWarningDetails(result, report);
        
        return report.toString();
    }
    
    /**
     * Appends error details to the validation report
     */
    private void appendErrorDetails(ValidationResult result, StringBuilder report) {
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
    }
    
    /**
     * Appends warning details to the validation report
     */
    private void appendWarningDetails(ValidationResult result, StringBuilder report) {
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
    }
}