package com.megacreative.coding.functions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.values.types.ListValue;
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
        
        plugin.getLogger().info(" YYS Advanced Function Manager initialized (ScriptEngine will be set later)");
    }
    
    /**
     * Sets the ScriptEngine after late initialization
     */
    public void setScriptEngine(ScriptEngine scriptEngine) {
        if (this.scriptEngine == null && scriptEngine != null) {
            this.scriptEngine = scriptEngine;
            plugin.getLogger().info(" YYS ScriptEngine set for AdvancedFunctionManager");
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
        
        plugin.getLogger().info(" YYS Registered function: " + function.getName() + " (scope: " + function.getScope() + ")");
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
            // Check if this is a built-in function (no actual blocks)
            if (function.getFunctionBlocks().isEmpty() || 
                (function.getFunctionBlocks().size() == 1 && 
                 "return".equals(function.getFunctionBlocks().get(0).getAction()))) {
                // This is likely a built-in function, execute it directly
                return executeBuiltInFunction(function, context);
            }
            
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
                    // Handle return value and termination
                    Object returnValue = scriptContext.getVariable("return");
                    if (returnValue != null) {
                        ExecutionResult newResult = ExecutionResult.success("Function executed with return value: " + returnValue);
                        newResult.setReturnValue(returnValue);
                        // Preserve termination status
                        newResult.setTerminated(result.isTerminated());
                        return newResult;
                    }
                    return result;
                });
            
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Function execution failed: " + e.getMessage()));
        }
    }
    
    /**
     * Executes a built-in function directly
     */
    private CompletableFuture<ExecutionResult> executeBuiltInFunction(FunctionDefinition function, 
                                                                    FunctionExecutionContext context) {
        try {
            String functionName = function.getName();
            DataValue[] arguments = context.getArguments();
            
            // Determine which library this function belongs to by checking prefixes
            Object result = null;
            
            if (functionName.startsWith("abs") || functionName.startsWith("round") || 
                functionName.startsWith("floor") || functionName.startsWith("ceil") ||
                functionName.startsWith("sqrt") || functionName.startsWith("pow") ||
                functionName.startsWith("min") || functionName.startsWith("max") ||
                functionName.startsWith("sin") || functionName.startsWith("cos") ||
                functionName.startsWith("tan") || functionName.startsWith("log") ||
                functionName.startsWith("exp")) {
                // Math functions
                result = executeMathFunction(functionName, arguments);
            } else if (functionName.startsWith("length") || functionName.startsWith("toUpperCase") ||
                       functionName.startsWith("toLowerCase") || functionName.startsWith("substring") ||
                       functionName.startsWith("contains") || functionName.startsWith("startsWith") ||
                       functionName.startsWith("endsWith") || functionName.startsWith("replace") ||
                       functionName.startsWith("trim") || functionName.startsWith("split")) {
                // String functions
                result = executeStringFunction(functionName, arguments);
            } else if (functionName.startsWith("random") || functionName.startsWith("currentTimeMillis") ||
                       functionName.startsWith("format") || functionName.startsWith("join") ||
                       functionName.startsWith("size")) {
                // Utility functions
                result = executeUtilityFunction(functionName, arguments);
            }
            
            if (result != null) {
                ExecutionResult execResult = ExecutionResult.success("Built-in function executed: " + functionName);
                execResult.setReturnValue(result);
                return CompletableFuture.completedFuture(execResult);
            } else {
                return CompletableFuture.completedFuture(
                    ExecutionResult.error("Unknown built-in function: " + functionName));
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Built-in function execution failed: " + e.getMessage()));
        }
    }
    
    /**
     * Executes a math function
     */
    private Object executeMathFunction(String functionName, DataValue[] arguments) {
        try {
            switch (functionName) {
                case "abs":
                    return Math.abs(arguments[0].asNumber().doubleValue());
                case "round":
                    return (double) Math.round(arguments[0].asNumber().doubleValue());
                case "floor":
                    return Math.floor(arguments[0].asNumber().doubleValue());
                case "ceil":
                    return Math.ceil(arguments[0].asNumber().doubleValue());
                case "sqrt":
                    return Math.sqrt(arguments[0].asNumber().doubleValue());
                case "pow":
                    return Math.pow(arguments[0].asNumber().doubleValue(), arguments[1].asNumber().doubleValue());
                case "min":
                    return Math.min(arguments[0].asNumber().doubleValue(), arguments[1].asNumber().doubleValue());
                case "max":
                    return Math.max(arguments[0].asNumber().doubleValue(), arguments[1].asNumber().doubleValue());
                case "sin":
                    return Math.sin(arguments[0].asNumber().doubleValue());
                case "cos":
                    return Math.cos(arguments[0].asNumber().doubleValue());
                case "tan":
                    return Math.tan(arguments[0].asNumber().doubleValue());
                case "log":
                    return Math.log(arguments[0].asNumber().doubleValue());
                case "exp":
                    return Math.exp(arguments[0].asNumber().doubleValue());
                default:
                    return 0.0;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Math function execution failed: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Executes a string function
     */
    private Object executeStringFunction(String functionName, DataValue[] arguments) {
        try {
            String str = arguments[0].asString();
            
            switch (functionName) {
                case "length":
                    return (double) str.length();
                case "toUpperCase":
                    return str.toUpperCase();
                case "toLowerCase":
                    return str.toLowerCase();
                case "substring":
                    int start = arguments[1].asNumber().intValue();
                    int end = arguments[2].asNumber().intValue();
                    return str.substring(start, end);
                case "contains":
                    String substring = arguments[1].asString();
                    return str.contains(substring);
                case "startsWith":
                    String prefix = arguments[1].asString();
                    return str.startsWith(prefix);
                case "endsWith":
                    String suffix = arguments[1].asString();
                    return str.endsWith(suffix);
                case "replace":
                    String target = arguments[1].asString();
                    String replacement = arguments[2].asString();
                    return str.replace(target, replacement);
                case "trim":
                    return str.trim();
                case "split":
                    String delimiter = arguments[1].asString();
                    String[] parts = str.split(delimiter);
                    List<DataValue> list = new ArrayList<>();
                    for (String part : parts) {
                        list.add(DataValue.of(part));
                    }
                    return new ListValue(list);
                default:
                    return "";
            }
        } catch (Exception e) {
            plugin.getLogger().warning("String function execution failed: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Executes a utility function
     */
    private Object executeUtilityFunction(String functionName, DataValue[] arguments) {
        try {
            switch (functionName) {
                case "random":
                    return Math.random();
                case "randomRange":
                    double min = arguments[0].asNumber().doubleValue();
                    double max = arguments[1].asNumber().doubleValue();
                    return min + Math.random() * (max - min);
                case "currentTimeMillis":
                    return (double) System.currentTimeMillis();
                case "format":
                    // Simple string formatting (first arg is format string, rest are values)
                    if (arguments.length > 0) {
                        String format = arguments[0].asString();
                        Object[] values = new Object[arguments.length - 1];
                        for (int i = 1; i < arguments.length; i++) {
                            values[i - 1] = arguments[i].getValue();
                        }
                        return String.format(format, values);
                    }
                    return "";
                case "join":
                    if (arguments.length >= 2 && arguments[0].getType() == ValueType.LIST) {
                        ListValue list = (ListValue) arguments[0];
                        String separator = arguments[1].asString();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < list.size(); i++) {
                            if (i > 0) sb.append(separator);
                            sb.append(list.get(i).asString());
                        }
                        return sb.toString();
                    }
                    return "";
                case "size":
                    if (arguments[0].getType() == ValueType.LIST) {
                        ListValue list = (ListValue) arguments[0];
                        return (double) list.size();
                    }
                    return 0.0;
                default:
                    return "";
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Utility function execution failed: " + e.getMessage());
            return "";
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
                plugin.getLogger().info(".EVT Removed function: " + name + " by " + player.getName());
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
                    plugin.getLogger().info(".EVT Removed world function: " + name + " by " + player.getName());
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
        
        // Add basic math functions
        try {
            // Math functions
            mathLib.addFunction(createMathFunction("abs", "Returns the absolute value of a number", ValueType.NUMBER, 1));
            mathLib.addFunction(createMathFunction("round", "Rounds a number to the nearest integer", ValueType.NUMBER, 1));
            mathLib.addFunction(createMathFunction("floor", "Returns the largest integer less than or equal to a number", ValueType.NUMBER, 1));
            mathLib.addFunction(createMathFunction("ceil", "Returns the smallest integer greater than or equal to a number", ValueType.NUMBER, 1));
            mathLib.addFunction(createMathFunction("sqrt", "Returns the square root of a number", ValueType.NUMBER, 1));
            mathLib.addFunction(createMathFunction("pow", "Returns the value of the first argument raised to the power of the second argument", ValueType.NUMBER, 2));
            mathLib.addFunction(createMathFunction("min", "Returns the smaller of two numbers", ValueType.NUMBER, 2));
            mathLib.addFunction(createMathFunction("max", "Returns the larger of two numbers", ValueType.NUMBER, 2));
            mathLib.addFunction(createMathFunction("sin", "Returns the trigonometric sine of an angle", ValueType.NUMBER, 1));
            mathLib.addFunction(createMathFunction("cos", "Returns the trigonometric cosine of an angle", ValueType.NUMBER, 1));
            mathLib.addFunction(createMathFunction("tan", "Returns the trigonometric tangent of an angle", ValueType.NUMBER, 1));
            mathLib.addFunction(createMathFunction("log", "Returns the natural logarithm of a number", ValueType.NUMBER, 1));
            mathLib.addFunction(createMathFunction("exp", "Returns Euler's number e raised to the power of a number", ValueType.NUMBER, 1));
            
            libraries.put("math", mathLib);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize math library: " + e.getMessage());
        }
        
        // String library
        FunctionLibrary stringLib = new FunctionLibrary("string", "String manipulation functions");
        
        // Add basic string functions
        try {
            // String functions
            stringLib.addFunction(createStringFunction("length", "Returns the length of a string", ValueType.NUMBER, 1));
            stringLib.addFunction(createStringFunction("toUpperCase", "Converts a string to uppercase", ValueType.TEXT, 1));
            stringLib.addFunction(createStringFunction("toLowerCase", "Converts a string to lowercase", ValueType.TEXT, 1));
            stringLib.addFunction(createStringFunction("substring", "Returns a substring of a string", ValueType.TEXT, 3)); // string, start, end
            stringLib.addFunction(createStringFunction("contains", "Checks if a string contains a substring", ValueType.BOOLEAN, 2));
            stringLib.addFunction(createStringFunction("startsWith", "Checks if a string starts with a prefix", ValueType.BOOLEAN, 2));
            stringLib.addFunction(createStringFunction("endsWith", "Checks if a string ends with a suffix", ValueType.BOOLEAN, 2));
            stringLib.addFunction(createStringFunction("replace", "Replaces all occurrences of a substring with another substring", ValueType.TEXT, 3)); // string, target, replacement
            stringLib.addFunction(createStringFunction("trim", "Removes whitespace from both ends of a string", ValueType.TEXT, 1));
            stringLib.addFunction(createStringFunction("split", "Splits a string into a list by a delimiter", ValueType.LIST, 2));
            
            libraries.put("string", stringLib);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize string library: " + e.getMessage());
        }
        
        // Utility library
        FunctionLibrary utilLib = new FunctionLibrary("util", "Utility functions");
        
        // Add basic utility functions
        try {
            // Utility functions
            utilLib.addFunction(createUtilityFunction("random", "Generates a random number between 0 and 1", ValueType.NUMBER, 0));
            utilLib.addFunction(createUtilityFunction("randomRange", "Generates a random number between min and max", ValueType.NUMBER, 2));
            utilLib.addFunction(createUtilityFunction("currentTimeMillis", "Returns the current time in milliseconds", ValueType.NUMBER, 0));
            utilLib.addFunction(createUtilityFunction("format", "Formats a string with arguments", ValueType.TEXT, -1)); // Variable arguments
            utilLib.addFunction(createUtilityFunction("join", "Joins a list of values with a separator", ValueType.TEXT, 2));
            utilLib.addFunction(createUtilityFunction("size", "Returns the size of a list or map", ValueType.NUMBER, 1));
            
            libraries.put("util", utilLib);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize utility library: " + e.getMessage());
        }
        
        plugin.getLogger().info(" YYS Initialized " + libraries.size() + " built-in function libraries with " + 
            (mathLib.getFunctionCount() + stringLib.getFunctionCount() + utilLib.getFunctionCount()) + " functions");
    }
    
    /**
     * Gets a function from a library by name
     */
    public FunctionDefinition getLibraryFunction(String libraryName, String functionName) {
        FunctionLibrary library = libraries.get(libraryName);
        if (library != null) {
            return library.getFunction(functionName);
        }
        return null;
    }
    
    /**
     * Gets all functions from a library
     */
    public Collection<FunctionDefinition> getLibraryFunctions(String libraryName) {
        FunctionLibrary library = libraries.get(libraryName);
        if (library != null) {
            return library.getAllFunctions();
        }
        return new ArrayList<>();
    }
    
    /**
     * Gets all available libraries
     */
    public Collection<FunctionLibrary> getAllLibraries() {
        return libraries.values();
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
        
        plugin.getLogger().info(" YYS Advanced Function Manager shut down");
    }
    
    // Helper methods for creating built-in functions
    
    /**
     * Creates a math function with the specified parameters
     */
    private FunctionDefinition createMathFunction(String name, String description, ValueType returnType, int paramCount) {
        List<FunctionDefinition.FunctionParameter> parameters = new ArrayList<>();
        
        // Add parameters based on paramCount
        for (int i = 0; i < paramCount; i++) {
            String paramName = "param" + (i + 1);
            parameters.add(new FunctionDefinition.FunctionParameter(paramName, ValueType.NUMBER, true, DataValue.of(0.0), "Parameter " + (i + 1)));
        }
        
        // Create a simple function block that just returns a result
        List<CodeBlock> functionBlocks = new ArrayList<>();
        CodeBlock returnBlock = new CodeBlock();
        returnBlock.setAction("return");
        functionBlocks.add(returnBlock);
        
        return new FunctionDefinition(name, description, null, parameters, functionBlocks, returnType, FunctionDefinition.FunctionScope.GLOBAL);
    }
    
    /**
     * Creates a string function with the specified parameters
     */
    private FunctionDefinition createStringFunction(String name, String description, ValueType returnType, int paramCount) {
        List<FunctionDefinition.FunctionParameter> parameters = new ArrayList<>();
        
        // Add parameters based on paramCount
        for (int i = 0; i < paramCount; i++) {
            String paramName = "param" + (i + 1);
            ValueType paramType = (i == 0) ? ValueType.TEXT : ValueType.TEXT; // First param is usually the string
            parameters.add(new FunctionDefinition.FunctionParameter(paramName, paramType, true, DataValue.of(""), "Parameter " + (i + 1)));
        }
        
        // Create a simple function block that just returns a result
        List<CodeBlock> functionBlocks = new ArrayList<>();
        CodeBlock returnBlock = new CodeBlock();
        returnBlock.setAction("return");
        functionBlocks.add(returnBlock);
        
        return new FunctionDefinition(name, description, null, parameters, functionBlocks, returnType, FunctionDefinition.FunctionScope.GLOBAL);
    }
    
    /**
     * Creates a utility function with the specified parameters
     */
    private FunctionDefinition createUtilityFunction(String name, String description, ValueType returnType, int paramCount) {
        List<FunctionDefinition.FunctionParameter> parameters = new ArrayList<>();
        
        // Add parameters based on paramCount (-1 for variable arguments)
        if (paramCount >= 0) {
            for (int i = 0; i < paramCount; i++) {
                String paramName = "param" + (i + 1);
                parameters.add(new FunctionDefinition.FunctionParameter(paramName, ValueType.TEXT, true, DataValue.of(""), "Parameter " + (i + 1)));
            }
        } else {
            // Variable arguments
            parameters.add(new FunctionDefinition.FunctionParameter("args", ValueType.LIST, true, DataValue.of(new ArrayList<>()), "Variable arguments"));
        }
        
        // Create a simple function block that just returns a result
        List<CodeBlock> functionBlocks = new ArrayList<>();
        CodeBlock returnBlock = new CodeBlock();
        returnBlock.setAction("return");
        functionBlocks.add(returnBlock);
        
        return new FunctionDefinition(name, description, null, parameters, functionBlocks, returnType, FunctionDefinition.FunctionScope.GLOBAL);
    }
}