package com.megacreative.coding.functions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🎆 Reference System-Style Function Definition
 * 
 * Represents a reusable function with parameters, return values, and scope management.
 * Functions can be called from scripts and maintain their own execution context.
 * 
 * Features:
 * - Parameter validation and type checking
 * - Local variable scope isolation
 * - Return value management
 * - Recursive call protection
 * - Performance optimization
 * - Access control and permissions
 */
public class FunctionDefinition {
    
    private final String name;
    private final String description;
    private final UUID id;
    private final Player owner;
    private final long createdTime;
    
    
    private final List<FunctionParameter> parameters;
    private final List<CodeBlock> functionBlocks;
    private final ValueType returnType;
    private final FunctionScope scope;
    private final FunctionVisibility visibility;
    
    
    private boolean enabled = true;
    private int maxRecursionDepth = 10;
    private long maxExecutionTime = 5000; 
    private int callCount = 0;
    private long totalExecutionTime = 0;
    
    
    private final Set<UUID> allowedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<String> requiredPermissions = ConcurrentHashMap.newKeySet();
    
    public enum FunctionScope {
        GLOBAL,     
        WORLD,      
        PLAYER,     
        SHARED      
    }
    
    public enum FunctionVisibility {
        PUBLIC,     
        PRIVATE,    
        PROTECTED   
    }
    
    public FunctionDefinition(String name, String description, Player owner, List<FunctionParameter> parameters,
                            List<CodeBlock> functionBlocks, ValueType returnType, FunctionScope scope) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.parameters = new ArrayList<>(parameters);
        this.functionBlocks = new ArrayList<>(functionBlocks);
        this.returnType = returnType;
        this.scope = scope;
        this.visibility = FunctionVisibility.PUBLIC;
        this.createdTime = System.currentTimeMillis();
        
        
        validateFunction();
    }
    
    /**
     * Validates the function definition for correctness
     */
    private void validateFunction() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be empty");
        }
        
        if (!name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Function name must be a valid identifier: " + name);
        }
        
        if (functionBlocks == null || functionBlocks.isEmpty()) {
            throw new IllegalArgumentException("Function must have at least one code block");
        }
        
        
        Set<String> paramNames = new HashSet<>();
        for (FunctionParameter param : parameters) {
            if (!paramNames.add(param.getName())) {
                throw new IllegalArgumentException("Duplicate parameter name: " + param.getName());
            }
        }
    }
    
    /**
     * Checks if a player can call this function
     */
    public boolean canCall(Player player) {
        if (!enabled) {
            return false;
        }
        
        
        switch (scope) {
            case PLAYER:
                return player.getUniqueId().equals(owner.getUniqueId());
            case SHARED:
                return allowedPlayers.contains(player.getUniqueId()) || player.getUniqueId().equals(owner.getUniqueId());
            case WORLD:
            case GLOBAL:
                
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Validates function call arguments
     */
    public ValidationResult validateArguments(DataValue[] arguments) {
        if (arguments.length != parameters.size()) {
            return ValidationResult.error("Expected " + parameters.size() + " arguments, got " + arguments.length);
        }
        
        for (int i = 0; i < parameters.size(); i++) {
            FunctionParameter param = parameters.get(i);
            DataValue arg = arguments[i];
            
            
            if (param.isRequired() && (arg == null || arg.isEmpty())) {
                return ValidationResult.error("Required parameter '" + param.getName() + "' is missing");
            }
            
            
            if (arg != null && !arg.isEmpty()) {
                if (!param.getType().isCompatible(arg.getType())) {
                    return ValidationResult.error("Parameter '" + param.getName() + "' expects " + 
                        param.getType().getName() + " but got " + arg.getType().getName());
                }
            }
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Creates a local variable scope for function execution
     */
    public Map<String, DataValue> createLocalScope(DataValue[] arguments) {
        Map<String, DataValue> localScope = new HashMap<>();
        
        
        for (int i = 0; i < parameters.size(); i++) {
            FunctionParameter param = parameters.get(i);
            DataValue value = i < arguments.length ? arguments[i] : param.getDefaultValue();
            
            if (value == null && param.isRequired()) {
                throw new IllegalArgumentException("Required parameter '" + param.getName() + "' has no value");
            }
            
            localScope.put(param.getName(), value);
        }
        
        return localScope;
    }
    
    /**
     * Records function execution statistics
     */
    public void recordExecution(long executionTime, boolean success) {
        callCount++;
        if (success) {
            totalExecutionTime += executionTime;
        }
    }
    
    /**
     * Gets function execution statistics
     */
    public FunctionStatistics getStatistics() {
        long avgExecutionTime = callCount > 0 ? totalExecutionTime / callCount : 0;
        return new FunctionStatistics(callCount, avgExecutionTime, totalExecutionTime);
    }
    
    
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public UUID getId() { return id; }
    public Player getOwner() { return owner; }
    public long getCreatedTime() { return createdTime; }
    public List<FunctionParameter> getParameters() { return new ArrayList<>(parameters); }
    public List<CodeBlock> getFunctionBlocks() { return new ArrayList<>(functionBlocks); }
    public ValueType getReturnType() { return returnType; }
    public FunctionScope getScope() { return scope; }
    public FunctionVisibility getVisibility() { return visibility; }
    public boolean isEnabled() { return enabled; }
    public int getMaxRecursionDepth() { return maxRecursionDepth; }
    public long getMaxExecutionTime() { return maxExecutionTime; }
    
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setMaxRecursionDepth(int maxRecursionDepth) { this.maxRecursionDepth = maxRecursionDepth; }
    public void setMaxExecutionTime(long maxExecutionTime) { this.maxExecutionTime = maxExecutionTime; }
    
    
    
    public void addAllowedPlayer(UUID playerId) {
        allowedPlayers.add(playerId);
    }
    
    public void removeAllowedPlayer(UUID playerId) {
        allowedPlayers.remove(playerId);
    }
    
    public Set<UUID> getAllowedPlayers() {
        return new HashSet<>(allowedPlayers);
    }
    
    public void addRequiredPermission(String permission) {
        requiredPermissions.add(permission);
    }
    
    public void removeRequiredPermission(String permission) {
        requiredPermissions.remove(permission);
    }
    
    public Set<String> getRequiredPermissions() {
        return new HashSet<>(requiredPermissions);
    }
    
    /**
     * Function parameter definition
     */
    public static class FunctionParameter {
        private final String name;
        private final ValueType type;
        private final boolean required;
        private final DataValue defaultValue;
        private final String description;
        
        public FunctionParameter(String name, ValueType type, boolean required, DataValue defaultValue, String description) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.defaultValue = defaultValue;
            this.description = description;
        }
        
        
        public String getName() { return name; }
        public ValueType getType() { return type; }
        public boolean isRequired() { return required; }
        public DataValue getDefaultValue() { return defaultValue; }
        public String getDescription() { return description; }
    }
    
    /**
     * Function validation result
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
    
    /**
     * Function execution statistics
     */
    public static class FunctionStatistics {
        private final int callCount;
        private final long averageExecutionTime;
        private final long totalExecutionTime;
        
        public FunctionStatistics(int callCount, long averageExecutionTime, long totalExecutionTime) {
            this.callCount = callCount;
            this.averageExecutionTime = averageExecutionTime;
            this.totalExecutionTime = totalExecutionTime;
        }
        
        public int getCallCount() { return callCount; }
        public long getAverageExecutionTime() { return averageExecutionTime; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
    }
    
    @Override
    public String toString() {
        return "Function{" +
            "name='" + name + '\'' +
            ", parameters=" + parameters.size() +
            ", returnType=" + returnType +
            ", scope=" + scope +
            ", calls=" + callCount +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionDefinition)) return false;
        FunctionDefinition that = (FunctionDefinition) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}