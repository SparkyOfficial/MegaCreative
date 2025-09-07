package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.containers.BlockContainerManager;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.events.PlayerEventsListener;
import com.megacreative.coding.executors.ExecutionResult;
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
        // Implementation not needed for now
    }
    
    @Override
    public void unloadScriptsForWorld(CreativeWorld world) {
        // Implementation not needed for now
    }
    
    @Override
    public CodeScript getScript(String name) {
        // Implementation not needed for now
        return null;
    }
    
    @Override
    public java.util.List<CodeScript> getWorldScripts(CreativeWorld world) {
        // Implementation not needed for now
        return new java.util.ArrayList<>();
    }
    
    @Override
    public void saveScript(CodeScript script) {
        // Implementation not needed for now
    }
    
    @Override
    public void cancelScriptExecution(String scriptId) {
        // Implementation not needed for now
    }
    
    @Override
    public void deleteScript(String scriptName) {
        // Implementation not needed for now
    }
    
    @Override
    public Object getGlobalVariable(String name) {
        // Implementation not needed for now
        return null;
    }
    
    @Override
    public void setGlobalVariable(String name, Object value) {
        // Implementation not needed for now
    }
    
    @Override
    public Object getServerVariable(String name) {
        // Implementation not needed for now
        return null;
    }
    
    @Override
    public void setServerVariable(String name, Object value) {
        // Implementation not needed for now
    }
    
    @Override
    public java.util.Map<String, Object> getGlobalVariables() {
        // Implementation not needed for now
        return new java.util.HashMap<>();
    }
    
    @Override
    public java.util.Map<String, Object> getServerVariables() {
        // Implementation not needed for now
        return new java.util.HashMap<>();
    }
    
    @Override
    public void clearVariables() {
        // Implementation not needed for now
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