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
 * 🎆 Reference System-Style Advanced Function Manager
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
    private ScriptEngine scriptEngine; // Remove final to allow late initialization
    
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
        
        // ScriptEngine will be set later via setScriptEngine()
        // Cannot access ServiceRegistry during construction as it's still being built
        this.scriptEngine = null;
        
        // Initialize built-in function libraries
        initializeBuiltInLibraries();
        
        plugin.getLogger().info("🎆 Advanced Function Manager initialized (ScriptEngine will be set later)");
    }
    
    /**
     * Sets the ScriptEngine after late initialization
     */
    public void setScriptEngine(ScriptEngine scriptEngine) {
        if (this.scriptEngine == null && scriptEngine != null) {
            this.scriptEngine = scriptEngine;
            plugin.getLogger().info("🎆 ScriptEngine set for AdvancedFunctionManager");
        }
    }
    
    /**
     * Checks if ScriptEngine is available
     */
    public boolean isScriptEngineAvailable() {
        return scriptEngine != null;
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
        // Check if ScriptEngine is available
        if (scriptEngine == null) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("ScriptEngine not available for function execution"));
        }
        
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
        return executeFunctionBlocks(function, newContext)
            .whenComplete((result, throwable) -> {
                long executionTime = System.currentTimeMillis() - startTime;
                
                // Record execution statistics
                function.recordExecution(executionTime, throwable == null && result.isSuccess());
                
                // Remove from active executions
                activeExecutions.remove(caller.getUniqueId());
                
                // Restore previous context if nested
                if (execContext != null) {
                    activeExecutions.put(caller.getUniqueId(), execContext);
                }
            });
    }
    
    /**
     * Executes the code blocks of a function
     */
    private CompletableFuture<ExecutionResult> executeFunctionBlocks(FunctionDefinition function, 
                                                                     FunctionExecutionContext context) {
        try {
            // Create local scope with function parameters
            Map<String, DataValue> localScope = function.createLocalScope(context.getArguments());
            
            // Create execution context for script engine
            ExecutionContext scriptContext = new ExecutionContext.Builder()
                .plugin(plugin)
                .player(context.getCaller())
                .creativeWorld(plugin.getWorldManager().findCreativeWorldByBukkit(context.getCaller().getWorld()))
                .currentBlock(function.getFunctionBlocks().get(0))
                .build();
            
            // Add local variables to context
            for (Map.Entry<String, DataValue> entry : localScope.entrySet()) {
                scriptContext.setVariable(entry.getKey(), entry.getValue().getValue());
            }
            
            // Add context data
            for (Map.Entry<String, Object> entry : context.getContextData().entrySet()) {
                scriptContext.setVariable(entry.getKey(), entry.getValue());
            }
            
            // Create temporary script from function blocks
            CodeScript functionScript = new CodeScript(
                "Function: " + function.getName(), true, function.getFunctionBlocks().get(0));
            
            // Link function blocks
            for (int i = 0; i < function.getFunctionBlocks().size() - 1; i++) {
                function.getFunctionBlocks().get(i).setNextBlock(function.getFunctionBlocks().get(i + 1));
            }
            
            // Execute function script
            return scriptEngine.executeScript(functionScript, context.getCaller(), "function_call")
                .thenApply(result -> {
                    // Handle return value
                    Object returnValue = scriptContext.getVariable("return");
                    if (returnValue != null) {
                        result = ExecutionResult.success("Function executed with return value: " + returnValue);
                    }
                    return result;
                });
            
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Function execution failed: " + e.getMessage()));
        }
    }
    
    /**
     * Gets all functions available to a player
     */
    public List<FunctionDefinition> getAvailableFunctions(Player player) {
        List<FunctionDefinition> available = new ArrayList<>();
        
        // Add player functions
        Map<String, FunctionDefinition> playerFuncs = playerFunctions.get(player.getUniqueId());
        if (playerFuncs != null) {
            available.addAll(playerFuncs.values());
        }
        
        // Add shared functions
        for (Map<String, FunctionDefinition> funcs : playerFunctions.values()) {
            for (FunctionDefinition func : funcs.values()) {
                if (func.getScope() == FunctionDefinition.FunctionScope.SHARED && func.canCall(player)) {
                    available.add(func);
                }
            }
        }
        
        // Add world functions
        String worldId = getPlayerWorldId(player);
        if (worldId != null) {
            Map<String, FunctionDefinition> worldFuncs = worldFunctions.get(worldId);
            if (worldFuncs != null) {
                available.addAll(worldFuncs.values().stream()
                    .filter(func -> func.canCall(player))
                    .collect(Collectors.toList()));
            }
        }
        
        // Add global functions
        available.addAll(globalFunctions.values().stream()
            .filter(func -> func.canCall(player))
            .collect(Collectors.toList()));
        
        return available;
    }
    
    /**
     * Removes a function by name and player (for player/shared functions)
     */
    public boolean removeFunction(String name, Player player) {
        // Try to remove from player functions
        Map<String, FunctionDefinition> playerFuncs = playerFunctions.get(player.getUniqueId());
        if (playerFuncs != null && playerFuncs.containsKey(name)) {
            FunctionDefinition func = playerFuncs.get(name);
            if (func.getOwner().getUniqueId().equals(player.getUniqueId())) {
                playerFuncs.remove(name);
                plugin.getLogger().info("🎆 Removed function: " + name + " by " + player.getName());
                return true;
            }
        }
        
        // Try to remove from world functions (if player is owner)
        String worldId = getPlayerWorldId(player);
        if (worldId != null) {
            Map<String, FunctionDefinition> worldFuncs = worldFunctions.get(worldId);
            if (worldFuncs != null && worldFuncs.containsKey(name)) {
                FunctionDefinition func = worldFuncs.get(name);
                if (func.getOwner().getUniqueId().equals(player.getUniqueId())) {
                    worldFuncs.remove(name);
                    plugin.getLogger().info("🎆 Removed world function: " + name + " by " + player.getName());
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Gets function system statistics
     */
    public Map<String, Object> getFunctionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        int totalFunctions = globalFunctions.size();
        int playerFunctionCount = 0;
        int worldFunctionCount = 0;
        
        for (Map<String, FunctionDefinition> playerMap : playerFunctions.values()) {
            playerFunctionCount += playerMap.size();
        }
        
        for (Map<String, FunctionDefinition> worldMap : worldFunctions.values()) {
            worldFunctionCount += worldMap.size();
        }
        
        totalFunctions += playerFunctionCount + worldFunctionCount;
        
        stats.put("total_functions", totalFunctions);
        stats.put("global_functions", globalFunctions.size());
        stats.put("player_functions", playerFunctionCount);
        stats.put("world_functions", worldFunctionCount);
        stats.put("active_executions", activeExecutions.size());
        stats.put("libraries", libraries.size());
        
        return stats;
    }
    
    /**
     * Helper methods
     */
    
    private boolean functionExists(String name, Player owner, FunctionDefinition.FunctionScope scope) {
        switch (scope) {
            case GLOBAL:
                return globalFunctions.containsKey(name);
            case PLAYER:
            case SHARED:
                Map<String, FunctionDefinition> playerFuncs = playerFunctions.get(owner.getUniqueId());
                return playerFuncs != null && playerFuncs.containsKey(name);
            case WORLD:
                String worldId = getPlayerWorldId(owner);
                if (worldId != null) {
                    Map<String, FunctionDefinition> worldFuncs = worldFunctions.get(worldId);
                    return worldFuncs != null && worldFuncs.containsKey(name);
                }
                return false;
            default:
                return false;
        }
    }
    
    private String getPlayerWorldId(Player player) {
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        return world != null ? world.getId() : null;
    }
    
    /**
     * Initializes built-in function libraries
     */
    private void initializeBuiltInLibraries() {
        // Math library
        FunctionLibrary mathLib = new FunctionLibrary("math", "Mathematical functions");
        libraries.put("math", mathLib);
        
        // String library
        FunctionLibrary stringLib = new FunctionLibrary("string", "String manipulation functions");
        libraries.put("string", stringLib);
        
        // Utility library
        FunctionLibrary utilLib = new FunctionLibrary("util", "Utility functions");
        libraries.put("util", utilLib);
        
        plugin.getLogger().info("🎆 Initialized " + libraries.size() + " built-in function libraries");
    }
    
    /**
     * Function execution context
     */
    private static class FunctionExecutionContext {
        private final UUID executionId;
        private final FunctionDefinition function;
        private final Player caller;
        private final DataValue[] arguments;
        private final Map<String, Object> contextData;
        private final long startTime;
        private int recursionDepth = 0;
        
        public FunctionExecutionContext(UUID executionId, FunctionDefinition function, Player caller,
                                      DataValue[] arguments, Map<String, Object> contextData) {
            this.executionId = executionId;
            this.function = function;
            this.caller = caller;
            this.arguments = arguments;
            this.contextData = contextData;
            this.startTime = System.currentTimeMillis();
        }
        
        // Getters
        public UUID getExecutionId() { return executionId; }
        public FunctionDefinition getFunction() { return function; }
        public Player getCaller() { return caller; }
        public DataValue[] getArguments() { return arguments; }
        public Map<String, Object> getContextData() { return contextData; }
        public long getStartTime() { return startTime; }
        public int getRecursionDepth() { return recursionDepth; }
        
        public void setRecursionDepth(int recursionDepth) { this.recursionDepth = recursionDepth; }
    }
    
    /**
     * Function library for organizing related functions
     */
    public static class FunctionLibrary {
        private final String name;
        private final String description;
        private final Map<String, FunctionDefinition> functions = new ConcurrentHashMap<>();
        
        public FunctionLibrary(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public void addFunction(FunctionDefinition function) {
            functions.put(function.getName(), function);
        }
        
        public void removeFunction(String name) {
            functions.remove(name);
        }
        
        public FunctionDefinition getFunction(String name) {
            return functions.get(name);
        }
        
        public Collection<FunctionDefinition> getAllFunctions() {
            return functions.values();
        }
        
        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getFunctionCount() { return functions.size(); }
    }
    
    /**
     * Cleanup and shutdown
     */
    public void shutdown() {
        // Cancel all active executions
        activeExecutions.clear();
        
        // Clear all function definitions
        globalFunctions.clear();
        playerFunctions.clear();
        worldFunctions.clear();
        libraries.clear();
        
        plugin.getLogger().info("🎆 Advanced Function Manager shut down");
    }
}