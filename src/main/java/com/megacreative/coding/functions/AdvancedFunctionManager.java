package com.megacreative.coding.functions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 🎆 FrameLand-Style Advanced Function Manager
 * 
 * Manages function definitions, execution, and scope isolation.
 * Provides a complete function system with:
 * - Function registration and discovery
 * - Scope-based execution contexts
 * - Recursive call protection
 * - Performance monitoring
 * - Access control and permissions
 * - Function library management
 */
public class AdvancedFunctionManager {
    
    private final MegaCreative plugin;
    private final ScriptEngine scriptEngine;
    
    // Function storage
    private final Map<String, FunctionDefinition> globalFunctions = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, FunctionDefinition>> playerFunctions = new ConcurrentHashMap<>();
    private final Map<String, Map<String, FunctionDefinition>> worldFunctions = new ConcurrentHashMap<>();
    
    // Execution tracking
    private final Map<UUID, FunctionExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    private final Map<String, FunctionLibrary> libraries = new ConcurrentHashMap<>();
    
    // Performance settings
    private static final int MAX_RECURSION_DEPTH = 50;
    private static final long MAX_EXECUTION_TIME = 10000; // 10 seconds
    private static final int MAX_CONCURRENT_EXECUTIONS = 100;
    
    public AdvancedFunctionManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        
        // Initialize built-in function libraries
        initializeBuiltInLibraries();
        
        plugin.getLogger().info("🎆 Advanced Function Manager initialized with built-in libraries");
    }
    
    /**
     * Registers a new function definition
     */
    public boolean registerFunction(FunctionDefinition function) {
        if (function == null) {
            return false;
        }
        
        // Check for name conflicts
        if (functionExists(function.getName(), function.getOwner(), function.getScope())) {
            return false;
        }
        
        // Register based on scope
        switch (function.getScope()) {
            case GLOBAL:
                globalFunctions.put(function.getName(), function);
                break;
            case PLAYER:
                playerFunctions.computeIfAbsent(function.getOwner().getUniqueId(), k -> new ConcurrentHashMap<>())
                    .put(function.getName(), function);
                break;
            case WORLD:
                String worldId = getPlayerWorldId(function.getOwner());
                if (worldId != null) {
                    worldFunctions.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>())
                        .put(function.getName(), function);
                }
                break;
            case SHARED:
                // Store as player function but with shared access
                playerFunctions.computeIfAbsent(function.getOwner().getUniqueId(), k -> new ConcurrentHashMap<>())
                    .put(function.getName(), function);
                break;
        }
        
        plugin.getLogger().info("🎆 Registered function: " + function.getName() + " (scope: " + function.getScope() + ")");
        return true;
    }
    
    /**
     * Finds a function definition by name and context
     */
    public FunctionDefinition findFunction(String name, Player caller) {
        if (name == null || caller == null) {
            return null;
        }
        
        // Search order: player -> world -> global
        
        // 1. Check player functions
        Map<String, FunctionDefinition> playerFuncs = playerFunctions.get(caller.getUniqueId());
        if (playerFuncs != null && playerFuncs.containsKey(name)) {
            return playerFuncs.get(name);
        }
        
        // 2. Check shared functions from other players
        for (Map<String, FunctionDefinition> funcs : playerFunctions.values()) {
            FunctionDefinition func = funcs.get(name);
            if (func != null && func.getScope() == FunctionDefinition.FunctionScope.SHARED && func.canCall(caller)) {
                return func;
            }
        }
        
        // 3. Check world functions
        String worldId = getPlayerWorldId(caller);
        if (worldId != null) {
            Map<String, FunctionDefinition> worldFuncs = worldFunctions.get(worldId);
            if (worldFuncs != null && worldFuncs.containsKey(name)) {
                FunctionDefinition func = worldFuncs.get(name);
                if (func.canCall(caller)) {
                    return func;
                }
            }
        }
        
        // 4. Check global functions
        FunctionDefinition globalFunc = globalFunctions.get(name);
        if (globalFunc != null && globalFunc.canCall(caller)) {
            return globalFunc;
        }
        
        return null;
    }
    
    /**
     * Executes a function with given arguments
     */
    public CompletableFuture<ExecutionResult> executeFunction(String functionName, Player caller, DataValue[] arguments) {
        return executeFunction(functionName, caller, arguments, new HashMap<>());
    }
    
    /**
     * Executes a function with given arguments and execution context
     */
    public CompletableFuture<ExecutionResult> executeFunction(String functionName, Player caller, 
                                                             DataValue[] arguments, Map<String, Object> contextData) {
        // Find function
        FunctionDefinition function = findFunction(functionName, caller);
        if (function == null) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Function not found: " + functionName));
        }
        
        // Check if function is enabled
        if (!function.isEnabled()) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Function is disabled: " + functionName));
        }
        
        // Validate arguments
        FunctionDefinition.ValidationResult validation = function.validateArguments(arguments);
        if (!validation.isValid()) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Invalid arguments: " + validation.getMessage()));
        }
        
        // Check recursion depth
        UUID executionId = UUID.randomUUID();
        FunctionExecutionContext execContext = activeExecutions.get(caller.getUniqueId());
        if (execContext != null && execContext.getRecursionDepth() >= function.getMaxRecursionDepth()) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Maximum recursion depth exceeded: " + function.getMaxRecursionDepth()));
        }
        
        // Check concurrent execution limit
        if (activeExecutions.size() >= MAX_CONCURRENT_EXECUTIONS) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Maximum concurrent executions exceeded"));
        }
        
        // Create execution context
        FunctionExecutionContext newContext = new FunctionExecutionContext(
            executionId, function, caller, arguments, contextData);
        newContext.setRecursionDepth(execContext != null ? execContext.getRecursionDepth() + 1 : 0);
        
        activeExecutions.put(caller.getUniqueId(), newContext);
        
        long startTime = System.currentTimeMillis();
        
        // Execute function blocks
        return executeFunctionBlocks(function, newContext)\n            .whenComplete((result, throwable) -> {\n                long executionTime = System.currentTimeMillis() - startTime;\n                \n                // Record execution statistics\n                function.recordExecution(executionTime, throwable == null && result.isSuccess());\n                \n                // Remove from active executions\n                activeExecutions.remove(caller.getUniqueId());\n                \n                // Restore previous context if nested\n                if (execContext != null) {\n                    activeExecutions.put(caller.getUniqueId(), execContext);\n                }\n            });\n    }\n    \n    /**\n     * Executes the code blocks of a function\n     */\n    private CompletableFuture<ExecutionResult> executeFunctionBlocks(FunctionDefinition function, \n                                                                     FunctionExecutionContext context) {\n        try {\n            // Create local scope with function parameters\n            Map<String, DataValue> localScope = function.createLocalScope(context.getArguments());\n            \n            // Create execution context for script engine\n            ExecutionContext scriptContext = new ExecutionContext.Builder()\n                .plugin(plugin)\n                .player(context.getCaller())\n                .creativeWorld(plugin.getWorldManager().findCreativeWorldByBukkit(context.getCaller().getWorld()))\n                .currentBlock(function.getFunctionBlocks().get(0))\n                .build();\n            \n            // Add local variables to context\n            for (Map.Entry<String, DataValue> entry : localScope.entrySet()) {\n                scriptContext.setVariable(entry.getKey(), entry.getValue().getValue());\n            }\n            \n            // Add context data\n            for (Map.Entry<String, Object> entry : context.getContextData().entrySet()) {\n                scriptContext.setVariable(entry.getKey(), entry.getValue());\n            }\n            \n            // Create temporary script from function blocks\n            CodeScript functionScript = new CodeScript(\n                \"Function: \" + function.getName(), true, function.getFunctionBlocks().get(0));\n            \n            // Link function blocks\n            for (int i = 0; i < function.getFunctionBlocks().size() - 1; i++) {\n                function.getFunctionBlocks().get(i).setNextBlock(function.getFunctionBlocks().get(i + 1));\n            }\n            \n            // Execute function script\n            return scriptEngine.executeScript(functionScript, context.getCaller(), \"function_call\")\n                .thenApply(result -> {\n                    // Handle return value\n                    Object returnValue = scriptContext.getVariable(\"return\");\n                    if (returnValue != null) {\n                        result = ExecutionResult.success(\"Function executed with return value: \" + returnValue);\n                    }\n                    return result;\n                });\n            \n        } catch (Exception e) {\n            return CompletableFuture.completedFuture(\n                ExecutionResult.error(\"Function execution failed: \" + e.getMessage()));\n        }\n    }\n    \n    /**\n     * Gets all functions available to a player\n     */\n    public List<FunctionDefinition> getAvailableFunctions(Player player) {\n        List<FunctionDefinition> available = new ArrayList<>();\n        \n        // Add player functions\n        Map<String, FunctionDefinition> playerFuncs = playerFunctions.get(player.getUniqueId());\n        if (playerFuncs != null) {\n            available.addAll(playerFuncs.values());\n        }\n        \n        // Add shared functions\n        for (Map<String, FunctionDefinition> funcs : playerFunctions.values()) {\n            for (FunctionDefinition func : funcs.values()) {\n                if (func.getScope() == FunctionDefinition.FunctionScope.SHARED && func.canCall(player)) {\n                    available.add(func);\n                }\n            }\n        }\n        \n        // Add world functions\n        String worldId = getPlayerWorldId(player);\n        if (worldId != null) {\n            Map<String, FunctionDefinition> worldFuncs = worldFunctions.get(worldId);\n            if (worldFuncs != null) {\n                available.addAll(worldFuncs.values().stream()\n                    .filter(func -> func.canCall(player))\n                    .collect(Collectors.toList()));\n            }\n        }\n        \n        // Add global functions\n        available.addAll(globalFunctions.values().stream()\n            .filter(func -> func.canCall(player))\n            .collect(Collectors.toList()));\n        \n        return available;\n    }\n    \n    /**\n     * Removes a function definition\n     */\n    public boolean removeFunction(String name, Player owner) {\n        boolean removed = false;\n        \n        // Remove from player functions\n        Map<String, FunctionDefinition> playerFuncs = playerFunctions.get(owner.getUniqueId());\n        if (playerFuncs != null) {\n            removed = playerFuncs.remove(name) != null;\n        }\n        \n        // Remove from world functions\n        String worldId = getPlayerWorldId(owner);\n        if (worldId != null) {\n            Map<String, FunctionDefinition> worldFuncs = worldFunctions.get(worldId);\n            if (worldFuncs != null) {\n                removed = worldFuncs.remove(name) != null || removed;\n            }\n        }\n        \n        // Remove from global functions (only if owner)\n        FunctionDefinition globalFunc = globalFunctions.get(name);\n        if (globalFunc != null && globalFunc.getOwner().getUniqueId().equals(owner.getUniqueId())) {\n            removed = globalFunctions.remove(name) != null || removed;\n        }\n        \n        return removed;\n    }\n    \n    /**\n     * Gets function execution statistics\n     */\n    public Map<String, Object> getFunctionStatistics() {\n        Map<String, Object> stats = new HashMap<>();\n        \n        int totalFunctions = globalFunctions.size();\n        totalFunctions += playerFunctions.values().stream().mapToInt(Map::size).sum();\n        totalFunctions += worldFunctions.values().stream().mapToInt(Map::size).sum();\n        \n        stats.put(\"total_functions\", totalFunctions);\n        stats.put(\"global_functions\", globalFunctions.size());\n        stats.put(\"player_functions\", playerFunctions.size());\n        stats.put(\"world_functions\", worldFunctions.size());\n        stats.put(\"active_executions\", activeExecutions.size());\n        stats.put(\"libraries\", libraries.size());\n        \n        return stats;\n    }\n    \n    /**\n     * Initializes built-in function libraries\n     */\n    private void initializeBuiltInLibraries() {\n        // Math library\n        FunctionLibrary mathLib = new FunctionLibrary(\"math\", \"Mathematical functions\");\n        libraries.put(\"math\", mathLib);\n        \n        // String library\n        FunctionLibrary stringLib = new FunctionLibrary(\"string\", \"String manipulation functions\");\n        libraries.put(\"string\", stringLib);\n        \n        // Utility library\n        FunctionLibrary utilLib = new FunctionLibrary(\"util\", \"Utility functions\");\n        libraries.put(\"util\", utilLib);\n        \n        plugin.getLogger().info(\"🎆 Initialized \" + libraries.size() + \" built-in function libraries\");\n    }\n    \n    /**\n     * Helper methods\n     */\n    \n    private boolean functionExists(String name, Player owner, FunctionDefinition.FunctionScope scope) {\n        switch (scope) {\n            case GLOBAL:\n                return globalFunctions.containsKey(name);\n            case PLAYER:\n            case SHARED:\n                Map<String, FunctionDefinition> playerFuncs = playerFunctions.get(owner.getUniqueId());\n                return playerFuncs != null && playerFuncs.containsKey(name);\n            case WORLD:\n                String worldId = getPlayerWorldId(owner);\n                if (worldId != null) {\n                    Map<String, FunctionDefinition> worldFuncs = worldFunctions.get(worldId);\n                    return worldFuncs != null && worldFuncs.containsKey(name);\n                }\n                return false;\n            default:\n                return false;\n        }\n    }\n    \n    private String getPlayerWorldId(Player player) {\n        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());\n        return world != null ? world.getId() : null;\n    }\n    \n    /**\n     * Function execution context\n     */\n    private static class FunctionExecutionContext {\n        private final UUID executionId;\n        private final FunctionDefinition function;\n        private final Player caller;\n        private final DataValue[] arguments;\n        private final Map<String, Object> contextData;\n        private final long startTime;\n        private int recursionDepth = 0;\n        \n        public FunctionExecutionContext(UUID executionId, FunctionDefinition function, Player caller,\n                                      DataValue[] arguments, Map<String, Object> contextData) {\n            this.executionId = executionId;\n            this.function = function;\n            this.caller = caller;\n            this.arguments = arguments;\n            this.contextData = contextData;\n            this.startTime = System.currentTimeMillis();\n        }\n        \n        // Getters\n        public UUID getExecutionId() { return executionId; }\n        public FunctionDefinition getFunction() { return function; }\n        public Player getCaller() { return caller; }\n        public DataValue[] getArguments() { return arguments; }\n        public Map<String, Object> getContextData() { return contextData; }\n        public long getStartTime() { return startTime; }\n        public int getRecursionDepth() { return recursionDepth; }\n        \n        public void setRecursionDepth(int recursionDepth) { this.recursionDepth = recursionDepth; }\n    }\n    \n    /**\n     * Function library for organizing related functions\n     */\n    public static class FunctionLibrary {\n        private final String name;\n        private final String description;\n        private final Map<String, FunctionDefinition> functions = new ConcurrentHashMap<>();\n        \n        public FunctionLibrary(String name, String description) {\n            this.name = name;\n            this.description = description;\n        }\n        \n        public void addFunction(FunctionDefinition function) {\n            functions.put(function.getName(), function);\n        }\n        \n        public void removeFunction(String name) {\n            functions.remove(name);\n        }\n        \n        public FunctionDefinition getFunction(String name) {\n            return functions.get(name);\n        }\n        \n        public Collection<FunctionDefinition> getAllFunctions() {\n            return functions.values();\n        }\n        \n        // Getters\n        public String getName() { return name; }\n        public String getDescription() { return description; }\n        public int getFunctionCount() { return functions.size(); }\n    }\n    \n    /**\n     * Cleanup and shutdown\n     */\n    public void shutdown() {\n        // Cancel all active executions\n        activeExecutions.clear();\n        \n        // Clear all function definitions\n        globalFunctions.clear();\n        playerFunctions.clear();\n        worldFunctions.clear();\n        libraries.clear();\n        \n        plugin.getLogger().info(\"🎆 Advanced Function Manager shut down\");\n    }\n}