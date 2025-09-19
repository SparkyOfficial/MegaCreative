package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.containers.BlockContainerManager;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.events.PlayerEventsListener;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.interfaces.ICodingManager;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.managers.TrustedPlayerManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class CodingManagerImpl implements ICodingManager {

    // Constants for script execution messages
    private static final String SCRIPT_EXECUTION_FAILED_EXCEPTION = "Script execution failed with exception: ";
    private static final String SCRIPT_EXECUTION_FAILED = "Script execution failed: ";
    private static final String WORLD_SCRIPTS_DIRECTORY_CREATION_FAILED = "Failed to create world scripts directory for ";
    private static final String SCRIPT_FILE_LOADING_FAILED = "Failed to load script from ";
    private static final String WORLD_SCRIPTS_LISTING_FAILED = "Failed to list script files for world ";
    private static final String SCRIPT_SAVED = "Saved script: ";
    private static final String SCRIPT_SAVE_FAILED = "Failed to save script ";
    private static final String SCRIPT_DELETED = "Deleted script file: ";
    private static final String SCRIPT_DELETION_FAILED = "Failed to delete script file ";
    private static final String WORLD_MANAGER_NOT_AVAILABLE = "World manager not available";
    private static final String SCRIPT_WITHOUT_WORLD_NAME = "Cannot save script without world name: ";
    private static final String CODING_MANAGER_SHUTDOWN_COMPLETED = "CodingManager shutdown completed";
    
    // Constants for file extensions
    private static final String SCRIPT_FILE_EXTENSION = ".json";
    private static final String SCRIPTS_DIRECTORY_NAME = "scripts";

    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    private final Logger logger;
    private final Path scriptsDirectory;
    
    public CodingManagerImpl(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.logger = plugin.getLogger();
        this.scriptsDirectory = Paths.get(plugin.getDataFolder().getAbsolutePath(), SCRIPTS_DIRECTORY_NAME);
        
        // Create scripts directory if it doesn't exist
        if (!Files.exists(scriptsDirectory)) {
            try {
                Files.createDirectories(scriptsDirectory);
            } catch (IOException e) {
                logger.severe("Failed to create scripts directory: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void executeScript(CodeScript script, Player player, String trigger) {
        // Get the script engine from service registry
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry == null) {
            logger.warning(WORLD_MANAGER_NOT_AVAILABLE);
            return;
        }
        
        ScriptEngine scriptEngine = serviceRegistry.getService(ScriptEngine.class);
        if (scriptEngine == null) {
            logger.warning("Script engine not available");
            return;
        }
        
        // Execute the script using the script engine
        scriptEngine.executeScript(script, player, trigger)
            .whenComplete((result, throwable) -> {
                // Handle the result
                if (throwable != null) {
                    logger.warning(SCRIPT_EXECUTION_FAILED_EXCEPTION + throwable.getMessage());
                    logError(player, SCRIPT_EXECUTION_FAILED + throwable.getMessage());
                } else if (result != null) {
                    if (!result.isSuccess()) {
                        logger.warning(SCRIPT_EXECUTION_FAILED + result.getMessage());
                        logError(player, SCRIPT_EXECUTION_FAILED + result.getMessage());
                    }
                }
            });
    }
    
    @Override
    public void loadScriptsForWorld(CreativeWorld world) {
        // Load scripts from storage
        String worldName = world.getName();
        Path worldScriptsDir = scriptsDirectory.resolve(worldName);
        
        if (!Files.exists(worldScriptsDir)) {
            try {
                Files.createDirectories(worldScriptsDir);
            } catch (IOException e) {
                logger.severe(WORLD_SCRIPTS_DIRECTORY_CREATION_FAILED + worldName + ": " + e.getMessage());
                // Initialize empty scripts list
                if (world.getScripts() == null) {
                    world.setScripts(new java.util.ArrayList<>());
                }
                return;
            }
        }
        
        // Initialize scripts list
        if (world.getScripts() == null) {
            world.setScripts(new java.util.ArrayList<>());
        } else {
            world.getScripts().clear();
        }
        
        // Load all script files from the world directory
        try {
            Files.list(worldScriptsDir)
                .filter(path -> path.toString().endsWith(SCRIPT_FILE_EXTENSION))
                .forEach(path -> {
                    try {
                        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                        CodeScript script = com.megacreative.utils.JsonSerializer.deserializeScript(content);
                        if (script != null) {
                            // Set the world name for the script
                            script.setWorldName(world.getName());
                            world.getScripts().add(script);
                            logger.info("Loaded script: " + script.getName() + " for world: " + worldName);
                        }
                    } catch (Exception e) {
                        logger.severe(SCRIPT_FILE_LOADING_FAILED + path.toString() + ": " + e.getMessage());
                    }
                });
        } catch (IOException e) {
            logger.severe(WORLD_SCRIPTS_LISTING_FAILED + worldName + ": " + e.getMessage());
        }
        
        logger.info("Loaded " + world.getScripts().size() + " scripts for world: " + worldName);
    }
    
    @Override
    public void unloadScriptsForWorld(CreativeWorld world) {
        // Save scripts to storage before clearing
        if (world.getScripts() != null) {
            // Save all scripts
            for (CodeScript script : world.getScripts()) {
                saveScript(script);
            }
            world.getScripts().clear();
        }
        logger.info("Unloaded scripts for world: " + world.getName());
    }
    
    @Override
    public CodeScript getScript(String name) {
        // Search for a script by name with enhanced matching
        // Search through all worlds for a script with the given name
        if (worldManager == null) {
            logger.warning(WORLD_MANAGER_NOT_AVAILABLE);
            return null;
        }
        
        for (CreativeWorld world : worldManager.getCreativeWorlds()) {
            if (world.getScripts() != null) {
                for (CodeScript script : world.getScripts()) {
                    // Exact match first
                    if (script.getName().equals(name)) {
                        return script;
                    }
                    // Case-insensitive match
                    if (script.getName().equalsIgnoreCase(name)) {
                        return script;
                    }
                    // Partial match (contains)
                    if (script.getName().toLowerCase().contains(name.toLowerCase())) {
                        return script;
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public java.util.List<CodeScript> getWorldScripts(CreativeWorld world) {
        // Return the scripts list for the world
        return world.getScripts() != null ? world.getScripts() : new java.util.ArrayList<>();
    }
    
    @Override
    public void saveScript(CodeScript script) {
        // Save a script to storage
        if (script.getWorldName() == null || script.getWorldName().isEmpty()) {
            logger.warning(SCRIPT_WITHOUT_WORLD_NAME + script.getName());
            return;
        }
        
        // Create world directory if it doesn't exist
        Path worldScriptsDir = scriptsDirectory.resolve(script.getWorldName());
        if (!Files.exists(worldScriptsDir)) {
            try {
                Files.createDirectories(worldScriptsDir);
            } catch (IOException e) {
                logger.severe(WORLD_SCRIPTS_DIRECTORY_CREATION_FAILED + script.getWorldName() + ": " + e.getMessage());
                return;
            }
        }
        
        // Save script to file
        Path scriptFile = worldScriptsDir.resolve(script.getName() + SCRIPT_FILE_EXTENSION);
        try {
            String json = com.megacreative.utils.JsonSerializer.serializeScript(script);
            Files.write(scriptFile, json.getBytes(StandardCharsets.UTF_8));
            logger.info(SCRIPT_SAVED + script.getName() + " to " + scriptFile.toString());
        } catch (Exception e) {
            logger.severe(SCRIPT_SAVE_FAILED + script.getName() + ": " + e.getMessage());
        }
    }
    
    @Override
    public void cancelScriptExecution(String scriptId) {
        // Cancel a running script
        logger.info("Cancelled script execution: " + scriptId);
        // Get the script engine and cancel the execution
        ScriptEngine engine = getScriptEngine();
        if (engine != null) {
            engine.stopExecution(scriptId);
        }
    }
    
    @Override
    public void deleteScript(String scriptName) {
        // Delete a script from storage
        logger.info("Deleting script: " + scriptName);
        
        // Find and remove the script from all worlds
        if (worldManager == null) {
            logger.warning(WORLD_MANAGER_NOT_AVAILABLE);
            return;
        }
        
        for (CreativeWorld world : worldManager.getCreativeWorlds()) {
            if (world.getScripts() != null) {
                // Remove from memory
                CodeScript scriptToRemove = null;
                for (CodeScript script : world.getScripts()) {
                    if (script.getName().equals(scriptName)) {
                        scriptToRemove = script;
                        break;
                    }
                }
                
                if (scriptToRemove != null) {
                    world.getScripts().remove(scriptToRemove);
                    
                    // Delete from storage
                    Path scriptFile = scriptsDirectory.resolve(world.getName()).resolve(scriptName + SCRIPT_FILE_EXTENSION);
                    try {
                        if (Files.exists(scriptFile)) {
                            Files.delete(scriptFile);
                            logger.info(SCRIPT_DELETED + scriptFile.toString());
                        }
                    } catch (IOException e) {
                        logger.severe(SCRIPT_DELETION_FAILED + scriptFile.toString() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    @Override
    public Object getGlobalVariable(String name) {
        // Implementation for global variables
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry != null) {
            VariableManager variableManager = serviceRegistry.getVariableManager();
            if (variableManager != null) {
                DataValue value = variableManager.getGlobalVariable(name);
                return value != null ? value.getValue() : null;
            }
        }
        return null;
    }
    
    @Override
    public void setGlobalVariable(String name, Object value) {
        // Implementation for setting global variables
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry != null) {
            VariableManager variableManager = serviceRegistry.getVariableManager();
            if (variableManager != null) {
                variableManager.setGlobalVariable(name, DataValue.fromObject(value));
            }
        }
    }
    
    @Override
    public Object getServerVariable(String name) {
        // Implementation for server variables
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry != null) {
            VariableManager variableManager = serviceRegistry.getVariableManager();
            if (variableManager != null) {
                DataValue value = variableManager.getServerVariable(name);
                return value != null ? value.getValue() : null;
            }
        }
        return null;
    }
    
    @Override
    public void setServerVariable(String name, Object value) {
        // Implementation for setting server variables
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry != null) {
            VariableManager variableManager = serviceRegistry.getVariableManager();
            if (variableManager != null) {
                variableManager.setServerVariable(name, DataValue.fromObject(value));
            }
        }
    }
    
    @Override
    public java.util.Map<String, Object> getGlobalVariables() {
        // Implementation for getting all global variables
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry != null) {
            VariableManager variableManager = serviceRegistry.getVariableManager();
            if (variableManager != null) {
                Map<String, DataValue> dataValues = variableManager.getAllGlobalVariables();
                Map<String, Object> objects = new HashMap<>();
                for (Map.Entry<String, DataValue> entry : dataValues.entrySet()) {
                    objects.put(entry.getKey(), entry.getValue().getValue());
                }
                return objects;
            }
        }
        return new java.util.HashMap<>();
    }
    
    @Override
    public java.util.Map<String, Object> getServerVariables() {
        // Implementation for getting all server variables
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry != null) {
            VariableManager variableManager = serviceRegistry.getVariableManager();
            if (variableManager != null) {
                Map<String, DataValue> dataValues = variableManager.getServerVariables();
                Map<String, Object> objects = new HashMap<>();
                for (Map.Entry<String, DataValue> entry : dataValues.entrySet()) {
                    objects.put(entry.getKey(), entry.getValue().getValue());
                }
                return objects;
            }
        }
        return new java.util.HashMap<>();
    }
    
    @Override
    public void clearVariables() {
        // Implementation for clearing all variables
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry != null) {
            VariableManager variableManager = serviceRegistry.getVariableManager();
            if (variableManager != null) {
                variableManager.clearGlobalVariables();
                variableManager.clearServerVariables();
            }
        }
    }
    
    @Override
    public ScriptEngine getScriptEngine() {
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getService(ScriptEngine.class);
    }
    
    @Override
    public void shutdown() {
        // Cleanup resources if needed
        logger.info(CODING_MANAGER_SHUTDOWN_COMPLETED);
    }
    
    private void logError(Player player, String message) {
        logger.severe(message);
        if (player != null && player.isOnline()) {
            player.sendMessage("§cError: " + message);
        }
        
        // Log to visual debugger if available
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry != null) {
            VisualDebugger debugger = serviceRegistry.getScriptDebugger();
            if (debugger != null) {
                debugger.logError(player, message);
            }
        }
    }
    
    // Utility class for Paths (since we're using it in the constructor)
    private static class Paths {
        public static Path get(String first, String... more) {
            return java.nio.file.Paths.get(first, more);
        }
    }
}