package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.containers.BlockContainerManager;
import com.megacreative.coding.debug.VisualDebugger;
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
        
        
        scriptEngine.executeScript(script, player, trigger)
            .whenComplete((result, throwable) -> {
                
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
        
        String worldName = world.getName();
        Path worldScriptsDir = scriptsDirectory.resolve(worldName);
        
        if (!Files.exists(worldScriptsDir)) {
            try {
                Files.createDirectories(worldScriptsDir);
            } catch (IOException e) {
                logger.severe(WORLD_SCRIPTS_DIRECTORY_CREATION_FAILED + worldName + ": " + e.getMessage());
                
                if (world.getScripts() == null) {
                    world.setScripts(new java.util.ArrayList<>());
                }
                return;
            }
        }
        
        
        if (world.getScripts() == null) {
            world.setScripts(new java.util.ArrayList<>());
        } else {
            world.getScripts().clear();
        }
        
        
        try (java.util.stream.Stream<Path> paths = Files.list(worldScriptsDir)) {
            paths
                .filter(path -> path.toString().endsWith(SCRIPT_FILE_EXTENSION))
                .forEach(path -> {
                    try {
                        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                        CodeScript script = com.megacreative.utils.JsonSerializer.deserializeScript(content);
                        if (script != null) {
                            
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
        
        if (world.getScripts() != null) {
            
            for (CodeScript script : world.getScripts()) {
                saveScript(script);
            }
            world.getScripts().clear();
        }
        logger.info("Unloaded scripts for world: " + world.getName());
    }
    
    @Override
    public CodeScript getScript(String name) {
        
        
        if (worldManager == null) {
            logger.warning(WORLD_MANAGER_NOT_AVAILABLE);
            return null;
        }
        
        return findScriptInWorlds(name);
    }
    
    /**
     * Finds a script by name in all worlds
     * @param name The name of the script to find
     * @return The found script or null if not found
     */
    private CodeScript findScriptInWorlds(String name) {
        for (CreativeWorld world : worldManager.getCreativeWorlds()) {
            CodeScript script = findScriptInWorld(world, name);
            if (script != null) {
                return script;
            }
        }
        return null;
    }
    
    /**
     * Finds a script by name in a specific world
     * @param world The world to search in
     * @param name The name of the script to find
     * @return The found script or null if not found
     */
    private CodeScript findScriptInWorld(CreativeWorld world, String name) {
        if (world.getScripts() == null) {
            return null;
        }
        
        for (CodeScript script : world.getScripts()) {
            
            if (script.getName().equals(name)) {
                return script;
            }
            
            
            if (script.getName().equalsIgnoreCase(name)) {
                return script;
            }
            
            
            if (script.getName().toLowerCase().contains(name.toLowerCase())) {
                return script;
            }
        }
        
        return null;
    }
    
    @Override
    public java.util.List<CodeScript> getWorldScripts(CreativeWorld world) {
        
        return world.getScripts() != null ? world.getScripts() : new java.util.ArrayList<>();
    }
    
    @Override
    public void saveScript(CodeScript script) {
        
        if (script.getWorldName() == null || script.getWorldName().isEmpty()) {
            logger.warning(SCRIPT_WITHOUT_WORLD_NAME + script.getName());
            return;
        }
        
        
        Path worldScriptsDir = scriptsDirectory.resolve(script.getWorldName());
        if (!Files.exists(worldScriptsDir)) {
            try {
                Files.createDirectories(worldScriptsDir);
            } catch (IOException e) {
                logger.severe(WORLD_SCRIPTS_DIRECTORY_CREATION_FAILED + script.getWorldName() + ": " + e.getMessage());
                return;
            }
        }
        
        
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
        
        logger.info("Cancelled script execution: " + scriptId);
        
        ScriptEngine engine = getScriptEngine();
        if (engine != null) {
            engine.stopExecution(scriptId);
        }
    }
    
    @Override
    public void deleteScript(String scriptName) {
        
        logger.info("Deleting script: " + scriptName);
        
        
        if (worldManager == null) {
            logger.warning(WORLD_MANAGER_NOT_AVAILABLE);
            return;
        }
        
        deleteScriptFromAllWorlds(scriptName);
    }
    
    /**
     * Deletes a script from all worlds
     * @param scriptName The name of the script to delete
     */
    private void deleteScriptFromAllWorlds(String scriptName) {
        for (CreativeWorld world : worldManager.getCreativeWorlds()) {
            deleteScriptFromWorld(world, scriptName);
        }
    }
    
    /**
     * Deletes a script from a specific world
     * @param world The world to delete the script from
     * @param scriptName The name of the script to delete
     */
    private void deleteScriptFromWorld(CreativeWorld world, String scriptName) {
        if (world.getScripts() == null) {
            return;
        }
        
        
        CodeScript scriptToRemove = findScriptByName(world.getScripts(), scriptName);
        if (scriptToRemove != null) {
            world.getScripts().remove(scriptToRemove);
            deleteScriptFile(world, scriptName);
        }
    }
    
    /**
     * Finds a script by name in a list of scripts
     * @param scripts The list of scripts to search
     * @param scriptName The name of the script to find
     * @return The found script or null if not found
     */
    private CodeScript findScriptByName(java.util.List<CodeScript> scripts, String scriptName) {
        for (CodeScript script : scripts) {
            if (script.getName().equals(scriptName)) {
                return script;
            }
        }
        return null;
    }
    
    /**
     * Deletes a script file from storage
     * @param world The world the script belongs to
     * @param scriptName The name of the script file to delete
     */
    private void deleteScriptFile(CreativeWorld world, String scriptName) {
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
    
    @Override
    public Object getGlobalVariable(String name) {
        
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
        return serviceRegistry.getScriptEngine();
    }
    
    @Override
    public void shutdown() {
        
        logger.info(CODING_MANAGER_SHUTDOWN_COMPLETED);
    }
    
    private void logError(Player player, String message) {
        logger.severe(message);
        // Argument player might be null
        // The check has been noted but left as is since it's part of the method signature
        if (player != null && player.isOnline()) {
            player.sendMessage("§cError: " + message);
        }
        
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry != null) {
            VisualDebugger debugger = serviceRegistry.getScriptDebugger();
            if (debugger != null) {
                debugger.logError(player, message);
            }
        }
    }
    
    
    private static class Paths {
        public static Path get(String first, String... more) {
            return java.nio.file.Paths.get(first, more);
        }
    }
}