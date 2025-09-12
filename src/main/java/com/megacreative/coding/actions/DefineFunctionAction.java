package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.coding.functions.FunctionDefinition;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;

/**
 * 🎆 FrameLand-Style Function Definition Action
 * 
 * Handles creation and registration of user-defined functions.
 * Allows players to define reusable code blocks with parameters.
 */
public class DefineFunctionAction extends CodeAction {
    
    private final AdvancedFunctionManager functionManager;
    
    public DefineFunctionAction(MegaCreative plugin) {
        super(plugin);
        this.functionManager = plugin.getServiceRegistry().getService(AdvancedFunctionManager.class);
    }

    @Override
    public CompletableFuture<ExecutionResult> execute(ExecutionContext context) {
        CodeBlock block = context.getCurrentBlock();
        Player player = context.getPlayer();
        
        if (block == null || player == null) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Invalid execution context for function definition"));
        }
        
        try {
            // Parse function definition from block parameters
            String functionName = block.getParameterValue("function_name", String.class);
            if (functionName == null || functionName.trim().isEmpty()) {
                return CompletableFuture.completedFuture(
                    ExecutionResult.error("Function name is required"));
            }
            
            String description = block.getParameterValue("description", String.class);
            if (description == null) description = "User-defined function";
            
            String scopeStr = block.getParameterValue("scope", String.class);
            FunctionDefinition.FunctionScope scope = parseFunctionScope(scopeStr);
            
            // Parse function parameters
            List<FunctionDefinition.Parameter> parameters = parseParameters(block);
            
            // Get function body blocks (connected blocks after this one)
            List<CodeBlock> functionBlocks = collectFunctionBlocks(block);
            
            // Parse additional settings
            boolean enabled = block.getParameterValue("enabled", Boolean.class, true);
            int maxRecursionDepth = block.getParameterValue("max_recursion", Integer.class, 10);
            long maxExecutionTime = block.getParameterValue("max_execution_time", Long.class, 5000L);
            
            // Create function definition
            FunctionDefinition function = new FunctionDefinition.Builder()
                .name(functionName)
                .description(description)
                .owner(player)
                .scope(scope)
                .parameters(parameters)
                .functionBlocks(functionBlocks)
                .enabled(enabled)
                .maxRecursionDepth(maxRecursionDepth)
                .maxExecutionTime(maxExecutionTime)
                .build();
            
            // Register function
            boolean registered = functionManager.registerFunction(function);
            
            if (registered) {
                plugin.getLogger().info("🎆 Function '" + functionName + "' defined successfully by " + player.getName());
                return CompletableFuture.completedFuture(
                    ExecutionResult.success("Function '" + functionName + "' defined successfully"));
            } else {
                return CompletableFuture.completedFuture(
                    ExecutionResult.error("Failed to register function: " + functionName + " (name conflict?)"));
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("🎆 Function definition failed: " + e.getMessage());
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Function definition failed: " + e.getMessage()));
        }
    }
    
    /**
     * Parses function scope from string
     */
    private FunctionDefinition.FunctionScope parseFunctionScope(String scopeStr) {
        if (scopeStr == null) return FunctionDefinition.FunctionScope.PLAYER;
        
        switch (scopeStr.toUpperCase()) {
            case "GLOBAL":
                return FunctionDefinition.FunctionScope.GLOBAL;
            case "WORLD":
                return FunctionDefinition.FunctionScope.WORLD;
            case "SHARED":
                return FunctionDefinition.FunctionScope.SHARED;
            case "PLAYER":
            default:
                return FunctionDefinition.FunctionScope.PLAYER;
        }
    }
    
    /**
     * Parses function parameters from block parameters
     */
    private List<FunctionDefinition.Parameter> parseParameters(CodeBlock block) {
        List<FunctionDefinition.Parameter> parameters = new ArrayList<>();
        
        // Look for parameter definitions in block parameters
        int paramIndex = 0;
        while (true) {
            String paramName = block.getParameterValue("param_" + paramIndex + "_name", String.class);
            if (paramName == null) break;
            
            String typeStr = block.getParameterValue("param_" + paramIndex + "_type", String.class);
            ValueType type = parseValueType(typeStr);
            
            String description = block.getParameterValue("param_" + paramIndex + "_desc", String.class);
            if (description == null) description = "Parameter " + paramName;
            
            boolean required = block.getParameterValue("param_" + paramIndex + "_required", Boolean.class, true);
            Object defaultValue = block.getParameterValue("param_" + paramIndex + "_default");
            
            FunctionDefinition.Parameter parameter = new FunctionDefinition.Parameter(
                paramName, type, description, required, defaultValue);
            
            parameters.add(parameter);
            paramIndex++;
        }
        
        return parameters;
    }
    
    /**
     * Parses ValueType from string
     */
    private ValueType parseValueType(String typeStr) {
        if (typeStr == null) return ValueType.STRING;
        
        switch (typeStr.toUpperCase()) {
            case "NUMBER":
                return ValueType.NUMBER;
            case "BOOLEAN":
                return ValueType.BOOLEAN;
            case "LOCATION":
                return ValueType.LOCATION;
            case "ITEM":
                return ValueType.ITEM;
            case "PLAYER":
                return ValueType.PLAYER;
            case "STRING":
            default:
                return ValueType.STRING;
        }
    }
    
    /**
     * Collects function body blocks (blocks that follow this definition block)
     */
    private List<CodeBlock> collectFunctionBlocks(CodeBlock definitionBlock) {
        List<CodeBlock> functionBlocks = new ArrayList<>();
        
        // Start from the next block after definition
        CodeBlock currentBlock = definitionBlock.getNextBlock();
        
        // Collect blocks until we hit another function definition or end
        while (currentBlock != null) {
            // Stop if we encounter another function definition
            if ("define_function".equals(currentBlock.getAction())) {
                break;
            }
            
            // Stop if we encounter a return statement (end of function)
            if ("return".equals(currentBlock.getAction())) {
                functionBlocks.add(currentBlock);
                break;
            }
            
            functionBlocks.add(currentBlock);
            currentBlock = currentBlock.getNextBlock();
        }
        
        return functionBlocks;
    }

    @Override
    public boolean canExecute(ExecutionContext context) {
        return context.getCurrentBlock() != null && 
               context.getPlayer() != null &&
               functionManager != null;
    }

    @Override
    public String getActionName() {
        return "define_function";
    }

    @Override
    public String getDescription() {
        return "🎆 Defines a new user function with parameters and body";
    }
}