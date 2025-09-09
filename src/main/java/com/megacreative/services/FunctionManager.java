package com.megacreative.services;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing custom functions in the scripting engine.
 * Allows defining and calling reusable blocks of code.
 */
public class FunctionManager {
    
    private final MegaCreative plugin;
    
    // Map of worlds to their function definitions
    // Key: world name, Value: map of function name to first block of function
    private final Map<String, Map<String, CodeBlock>> worldFunctions = new ConcurrentHashMap<>();
    
    public FunctionManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Registers a function definition for a specific world
     * @param worldName The name of the world
     * @param functionName The name of the function
     * @param firstBlock The first block in the function (entry point)
     */
    public void registerFunction(String worldName, String functionName, CodeBlock firstBlock) {
        worldFunctions.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>())
                     .put(functionName, firstBlock);
        plugin.getLogger().info("Registered function '" + functionName + "' in world '" + worldName + "'");
    }
    
    /**
     * Gets a function definition for a specific world
     * @param worldName The name of the world
     * @param functionName The name of the function
     * @return The first block of the function, or null if not found
     */
    public CodeBlock getFunction(String worldName, String functionName) {
        Map<String, CodeBlock> functions = worldFunctions.get(worldName);
        if (functions != null) {
            return functions.get(functionName);
        }
        return null;
    }
    
    /**
     * Checks if a function exists in a specific world
     * @param worldName The name of the world
     * @param functionName The name of the function
     * @return true if the function exists, false otherwise
     */
    public boolean functionExists(String worldName, String functionName) {
        Map<String, CodeBlock> functions = worldFunctions.get(worldName);
        return functions != null && functions.containsKey(functionName);
    }
    
    /**
     * Removes all functions for a specific world
     * @param worldName The name of the world
     */
    public void clearWorldFunctions(String worldName) {
        worldFunctions.remove(worldName);
        plugin.getLogger().info("Cleared all functions for world '" + worldName + "'");
    }
    
    /**
     * Gets all function names for a specific world
     * @param worldName The name of the world
     * @return Array of function names
     */
    public String[] getFunctionNames(String worldName) {
        Map<String, CodeBlock> functions = worldFunctions.get(worldName);
        if (functions != null) {
            return functions.keySet().toArray(new String[0]);
        }
        return new String[0];
    }
    
    /**
     * Clears all functions (used when plugin is disabled)
     */
    public void clearAllFunctions() {
        worldFunctions.clear();
        plugin.getLogger().info("Cleared all functions");
    }
}