package com.megacreative;

import com.megacreative.commands.*;
import com.megacreative.listeners.*;
import com.megacreative.core.DependencyContainer;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.config.ConfigurationValidator;
import com.megacreative.exceptions.ConfigurationException;
import com.megacreative.coding.events.PlayerEventsListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Main plugin class - now lightweight and focused on plugin lifecycle
 * Uses ServiceRegistry to avoid God Object pattern
 */
public class MegaCreative extends JavaPlugin {
    
    private static MegaCreative instance;
    private DependencyContainer dependencyContainer;
    private ServiceRegistry serviceRegistry;
    
    @Override
    public void onEnable() {
        instance = this;
        
        try {
            // Initialize dependency injection
            this.dependencyContainer = new DependencyContainer();
            this.serviceRegistry = new ServiceRegistry(this, dependencyContainer);
            
            // Validate configuration
            validateConfiguration();
            
            // Initialize all services through registry
            serviceRegistry.initializeServices();
            
            // Register commands and events
            registerCommands();
            registerEvents();
            
            getLogger().info("MegaCreative enabled successfully!");
            
        } catch (Exception e) {
            getLogger().severe("Failed to enable MegaCreative: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            // Stop all repeating tasks
            if (serviceRegistry != null) {
                // This would be handled by the appropriate service
                com.megacreative.coding.actions.RepeatTriggerAction.stopAllRepeatingTasks();
            }
            
            // Shutdown all services gracefully
            if (serviceRegistry != null) {
                serviceRegistry.shutdown();
            }
            
            getLogger().info("MegaCreative disabled successfully!");
            
        } catch (Exception e) {
            getLogger().severe("Error during plugin disable: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Validates plugin configuration
     */
    private void validateConfiguration() {
        try {
            ConfigurationValidator validator = new ConfigurationValidator(this);
            validator.validateMainConfig();
            validator.validateCodingBlocksConfig();
        } catch (ConfigurationException e) {
            // Create backup and try to restore
            ConfigurationValidator validator = new ConfigurationValidator(this);
            validator.createBackup();
            try {
                validator.restoreFromBackup();
            } catch (ConfigurationException restoreEx) {
                getLogger().warning("Failed to restore configuration from backup");
            }
        }
    }
    
    /**
     * Registers all plugin commands
     */
    // Getters for various managers
    public com.megacreative.interfaces.IWorldManager getWorldManager() {
        return serviceRegistry.getWorldManager();
    }
    
    public com.megacreative.interfaces.IPlayerManager getPlayerManager() {
        return serviceRegistry.getPlayerManager();
    }
    
    public com.megacreative.interfaces.ICodingManager getCodingManager() {
        return serviceRegistry.getCodingManager();
    }
    
    public com.megacreative.managers.TemplateManager getTemplateManager() {
        return serviceRegistry.getTemplateManager();
    }
    
    public com.megacreative.managers.ScoreboardManager getScoreboardManager() {
        return serviceRegistry.getScoreboardManager();
    }
    
    public com.megacreative.interfaces.ITrustedPlayerManager getTrustedPlayerManager() {
        return serviceRegistry.getTrustedPlayerManager();
    }
    
    public com.megacreative.managers.GUIManager getGuiManager() {
        return serviceRegistry.getGuiManager();
    }
    
    public com.megacreative.managers.BlockConfigManager getBlockConfigManager() {
        return serviceRegistry.getBlockConfigManager();
    }
    
    public com.megacreative.coding.variables.VariableManager getVariableManager() {
        return serviceRegistry.getVariableManager();
    }
    
    public com.megacreative.coding.BlockPlacementHandler getBlockPlacementHandler() {
        return serviceRegistry.getBlockPlacementHandler();
    }
    
    /**
     * Gets the VisualDebugger instance
     * @return VisualDebugger instance
     */
    public com.megacreative.coding.debug.VisualDebugger getScriptDebugger() {
        return serviceRegistry.getScriptDebugger();
    }
    
    /**
     * Checks if a player is currently debugging
     * @param player The player to check
     * @return true if the player is in a debug session
     */
    public boolean isDebugging(Player player) {
        return serviceRegistry.getScriptDebugger().isDebugging(player);
    }
    
    public com.megacreative.coding.monitoring.ScriptPerformanceMonitor getScriptPerformanceMonitor() {
        return serviceRegistry.getScriptPerformanceMonitor();
    }
    
    public com.megacreative.utils.ConfigManager getConfigManager() {
        return serviceRegistry.getConfigManager();
    }
    
    private void registerCommands() {
        getCommand("megacreative").setExecutor(new MainCommand(this));
        getCommand("myworlds").setExecutor(new MyWorldsCommand(this));
        getCommand("worldbrowser").setExecutor(new WorldBrowserCommand(this));
        getCommand("join").setExecutor(new JoinCommand(this, serviceRegistry.getWorldManager()));
        getCommand("play").setExecutor(new PlayCommand(this));
        getCommand("trusted").setExecutor(new TrustedPlayerCommand(this));
        getCommand("build").setExecutor(new BuildCommand(this, serviceRegistry.getWorldManager()));
        getCommand("dev").setExecutor(new DevCommand(this));
        getCommand("hub").setExecutor(new HubCommand(this, serviceRegistry.getPlayerManager()));
        
        getCommand("templates").setExecutor(new TemplatesCommand(this));
        getCommand("worldsettings").setExecutor(new WorldSettingsCommand(this));
        getCommand("debug").setExecutor(new DebugCommand(this));
        
        getCommand("status").setExecutor(new StatusCommand(this));
        getCommand("addfloor").setExecutor(new AddFloorCommand(this));
        getCommand("workspace").setExecutor(new WorkspaceCommand(this));
        getCommand("create").setExecutor(new CreateWorldCommand(this, serviceRegistry.getWorldManager()));
        getCommand("clipboard").setExecutor(new ClipboardCommand(this, serviceRegistry.getCodeBlockClipboard()));
        getCommand("group").setExecutor(new GroupCommand(serviceRegistry));
    }
    
    /**
     * Registers all event listeners
     */
    private void registerEvents() {
        // Core listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new DataItemListener(), this);
        getServer().getPluginManager().registerEvents(new WorldInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        
        // Extended event listeners
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
        // Use DevWorldProtectionListener from ServiceRegistry instead of creating new instance
        getServer().getPluginManager().registerEvents(serviceRegistry.getDevWorldProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(this), this);
        
        // Service-specific listeners
        getServer().getPluginManager().registerEvents(serviceRegistry.getBlockPlacementHandler(), this);
        getServer().getPluginManager().registerEvents(serviceRegistry.getAutoConnectionManager(), this);
        getServer().getPluginManager().registerEvents(serviceRegistry.getDevInventoryManager(), this);
        getServer().getPluginManager().registerEvents(serviceRegistry.getGuiManager(), this);
        getServer().getPluginManager().registerEvents(serviceRegistry.getCustomEventManager(), this);
        getServer().getPluginManager().registerEvents(new BlockGroupListener(serviceRegistry), this);
        
        // Register our new PlayerEventsListener
        getServer().getPluginManager().registerEvents(serviceRegistry.getPlayerEventsListener(), this);
    }
    
    // Static access and service delegation
    
    public static MegaCreative getInstance() {
        return instance;
    }
    
    /**
     * Gets the service registry for accessing services
     */
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
    
    /**
     * Gets the dependency container
     */
    public DependencyContainer getDependencyContainer() {
        return dependencyContainer;
    }
    
    // Legacy methods for backward compatibility - these should be migrated to use GUIManager
    private final Map<java.util.UUID, String> deleteConfirmations = new java.util.HashMap<>();
    private final Map<java.util.UUID, String> commentInputs = new java.util.HashMap<>();
    
    @Deprecated
    public Map<java.util.UUID, String> getDeleteConfirmations() {
        return deleteConfirmations;
    }
    
    @Deprecated
    public Map<java.util.UUID, String> getCommentInputs() {
        return commentInputs;
    }
}