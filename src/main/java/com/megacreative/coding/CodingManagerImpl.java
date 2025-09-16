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

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class CodingManagerImpl implements ICodingManager {

    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    private final Logger logger;
    
    public CodingManagerImpl(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.logger = plugin.getLogger();
    }
    
    @Override
    public void executeScript(CodeScript script, Player player, String trigger) {
        // Get the script engine from service registry
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry == null) {
            logger.warning("Service registry not available");
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
                    logger.warning("Script execution failed with exception: " + throwable.getMessage());
                    logError(player, "Script execution failed: " + throwable.getMessage());
                } else if (result != null) {
                    if (!result.isSuccess()) {
                        logger.warning("Script execution failed: " + result.getMessage());
                        logError(player, "Script execution failed: " + result.getMessage());
                    }
                }
            });
    }
    
    @Override
    public void loadScriptsForWorld(CreativeWorld world) {
        // In a real implementation, this would load scripts from storage
        // For now, we'll just ensure the scripts list is initialized
        if (world.getScripts() == null) {
            world.setScripts(new java.util.ArrayList<>());
        }
        logger.info("Loaded scripts for world: " + world.getName());
    }
    
    @Override
    public void unloadScriptsForWorld(CreativeWorld world) {
        // In a real implementation, this would save scripts to storage
        // For now, we'll just clear the scripts list
        if (world.getScripts() != null) {
            // Save scripts before clearing
            for (CodeScript script : world.getScripts()) {
                saveScript(script);
            }
            world.getScripts().clear();
        }
        logger.info("Unloaded scripts for world: " + world.getName());
    }
    
    @Override
    public CodeScript getScript(String name) {
        // In a real implementation, this would search for a script by name
        // Search through all worlds for a script with the given name
        for (CreativeWorld world : plugin.getWorldManager().getCreativeWorlds()) {
            if (world.getScripts() != null) {
                for (CodeScript script : world.getScripts()) {
                    if (script.getName().equals(name)) {
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
        // In a real implementation, this would save a script to storage
        logger.info("Saved script: " + script.getName());
        // In a more complete implementation, we would serialize the script to a file or database
    }
    
    @Override
    public void cancelScriptExecution(String scriptId) {
        // In a real implementation, this would cancel a running script
        logger.info("Cancelled script execution: " + scriptId);
        // Get the script engine and cancel the execution
        ScriptEngine engine = getScriptEngine();
        if (engine != null) {
            engine.stopExecution(scriptId);
        }
    }
    
    @Override
    public void deleteScript(String scriptName) {
        // In a real implementation, this would delete a script from storage
        logger.info("Deleted script: " + scriptName);
        // Find and remove the script from all worlds
        for (CreativeWorld world : plugin.getWorldManager().getCreativeWorlds()) {
            if (world.getScripts() != null) {
                world.getScripts().removeIf(script -> script.getName().equals(scriptName));
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
        logger.info("CodingManager shutdown completed");
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
}