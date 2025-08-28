package com.megacreative;

import com.megacreative.commands.*;
import com.megacreative.listeners.*;
import com.megacreative.core.DependencyContainer;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.config.ConfigurationValidator;
import com.megacreative.exceptions.ConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

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
    private void registerCommands() {
        getCommand("megacreative").setExecutor(new MainCommand(serviceRegistry));
        getCommand("myworlds").setExecutor(new MyWorldsCommand(serviceRegistry));
        getCommand("worldbrowser").setExecutor(new WorldBrowserCommand(serviceRegistry));
        getCommand("join").setExecutor(new JoinCommand(serviceRegistry));
        getCommand("play").setExecutor(new PlayCommand(serviceRegistry));
        getCommand("trusted").setExecutor(new TrustedPlayerCommand(serviceRegistry));
        getCommand("build").setExecutor(new BuildCommand(serviceRegistry));
        getCommand("dev").setExecutor(new DevCommand(serviceRegistry));
        getCommand("hub").setExecutor(new HubCommand(serviceRegistry));
        getCommand("savescript").setExecutor(new SaveScriptCommand(serviceRegistry));
        getCommand("templates").setExecutor(new TemplatesCommand(serviceRegistry));
        getCommand("scripts").setExecutor(new ScriptsCommand(serviceRegistry));
        getCommand("worldsettings").setExecutor(new WorldSettingsCommand(serviceRegistry));
        getCommand("debug").setExecutor(new DebugCommand(serviceRegistry));
        getCommand("createscript").setExecutor(new CreateScriptCommand(serviceRegistry));
        getCommand("stoprepeat").setExecutor(new StopRepeatCommand(serviceRegistry));
        getCommand("status").setExecutor(new StatusCommand(serviceRegistry));
        getCommand("addfloor").setExecutor(new AddFloorCommand(serviceRegistry));
        getCommand("workspace").setExecutor(new WorkspaceCommand(serviceRegistry));
        getCommand("create").setExecutor(new CreateWorldCommand(serviceRegistry));
        getCommand("clipboard").setExecutor(new ClipboardCommand(this, serviceRegistry.getCodeBlockClipboard()));
        getCommand("group").setExecutor(new GroupCommand(serviceRegistry));
    }
    
    /**
     * Registers all event listeners
     */
    private void registerEvents() {
        // Core listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(serviceRegistry), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(serviceRegistry), this);
        getServer().getPluginManager().registerEvents(new GuiListener(serviceRegistry), this);
        getServer().getPluginManager().registerEvents(new DataItemListener(), this);
        getServer().getPluginManager().registerEvents(new WorldInteractListener(serviceRegistry), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(serviceRegistry), this);
        
        // Extended event listeners
        getServer().getPluginManager().registerEvents(new BlockBreakListener(serviceRegistry), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(serviceRegistry), this);
        getServer().getPluginManager().registerEvents(new CommandListener(serviceRegistry), this);
        getServer().getPluginManager().registerEvents(new DevWorldProtectionListener(serviceRegistry), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(serviceRegistry), this);
        
        // Service-specific listeners
        getServer().getPluginManager().registerEvents(serviceRegistry.getBlockPlacementHandler(), this);
        getServer().getPluginManager().registerEvents(serviceRegistry.getAutoConnectionManager(), this);
        getServer().getPluginManager().registerEvents(serviceRegistry.getDevInventoryManager(), this);
        getServer().getPluginManager().registerEvents(serviceRegistry.getGuiManager(), this);
        getServer().getPluginManager().registerEvents(serviceRegistry.getCustomEventManager(), this);
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
    
    // Legacy getters for backward compatibility (delegate to ServiceRegistry)
    
    @Deprecated
    public com.megacreative.interfaces.IWorldManager getWorldManager() {
        return serviceRegistry.getWorldManager();
    }
    
    @Deprecated
    public com.megacreative.interfaces.IPlayerManager getPlayerManager() {
        return serviceRegistry.getPlayerManager();
    }
    
    @Deprecated
    public com.megacreative.interfaces.ICodingManager getCodingManager() {
        return serviceRegistry.getCodingManager();
    }
    
    @Deprecated
    public com.megacreative.managers.DataManager getDataManager() {
        return serviceRegistry.getDataManager();
    }
    
    @Deprecated
    public com.megacreative.managers.GUIManager getGuiManager() {
        return serviceRegistry.getGuiManager();
    }
}
    
    private void registerCommands() {
        getCommand("megacreative").setExecutor(new MainCommand(this));
        getCommand("myworlds").setExecutor(new MyWorldsCommand(this));
        getCommand("worldbrowser").setExecutor(new WorldBrowserCommand(this));
        getCommand("join").setExecutor(new JoinCommand(this));
        getCommand("play").setExecutor(new PlayCommand(this));
        getCommand("trusted").setExecutor(new TrustedPlayerCommand(this));
        getCommand("build").setExecutor(new BuildCommand(this));
        getCommand("dev").setExecutor(new DevCommand(this));
        getCommand("hub").setExecutor(new HubCommand(this));
        getCommand("savescript").setExecutor(new SaveScriptCommand(this));
        getCommand("templates").setExecutor(new TemplatesCommand(this));
        getCommand("scripts").setExecutor(new ScriptsCommand(this));
        getCommand("worldsettings").setExecutor(new WorldSettingsCommand(this));
        getCommand("debug").setExecutor(new DebugCommand(this));
        getCommand("createscript").setExecutor(new CreateScriptCommand(this));
        getCommand("stoprepeat").setExecutor(new StopRepeatCommand(this));
        getCommand("status").setExecutor(new StatusCommand(this));
        
        // === НОВЫЕ КОМАНДЫ ===
        getCommand("addfloor").setExecutor(new AddFloorCommand(this));
        getCommand("workspace").setExecutor(new WorkspaceCommand(this));
        getCommand("create").setExecutor(new CreateWorldCommand(this));
    }
    
    private void registerEvents() {
        // Регистрируем слушатели событий
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new DataItemListener(), this);
        getServer().getPluginManager().registerEvents(blockPlacementHandler, this);
        getServer().getPluginManager().registerEvents(new WorldInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        
        // Новые слушатели для расширенных событий
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
        
        // РЕГИСТРАЦИЯ НОВОГО СЛУШАТЕЛЯ ДЛЯ ЗАЩИТЫ ПРЕДМЕТОВ
        getServer().getPluginManager().registerEvents(new DevWorldProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(this), this);
        
        // === РЕГИСТРАЦИЯ НОВЫХ СИСТЕМ ===
        getServer().getPluginManager().registerEvents(autoConnectionManager, this);
        getServer().getPluginManager().registerEvents(devInventoryManager, this);
        getServer().getPluginManager().registerEvents(guiManager, this);
    }
    
    public static MegaCreative getInstance() {
        return instance;
    }
    
    public IWorldManager getWorldManager() {
        return worldManager;
    }
    
    public IPlayerManager getPlayerManager() {
        return playerManager;
    }

    public ICodingManager getCodingManager() {
        return codingManager;
    }

    public BlockPlacementHandler getBlockPlacementHandler() {
        return blockPlacementHandler;
    }


    
    public ScriptDebugger getScriptDebugger() {
        return scriptDebugger;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public TemplateManager getTemplateManager() {
        return templateManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public TrustedPlayerManager getTrustedPlayerManager() {
        return trustedPlayerManager;
    }
    
    public GUIManager getGuiManager() {
        return guiManager;
    }
    
    public BlockConfigManager getBlockConfigManager() {
        return blockConfigManager;
    }
    
    public BlockConfiguration getBlockConfiguration() {
        return blockConfiguration;
    }
    
    // === ГЕТТЕРЫ ДЛЯ НОВЫХ СИСТЕМ ===
    
    public AutoConnectionManager getAutoConnectionManager() {
        return autoConnectionManager;
    }
    
    public DevInventoryManager getDevInventoryManager() {
        return devInventoryManager;
    }
    
    public VariableManager getVariableManager() {
        return variableManager;
    }
    
    public BlockContainerManager getContainerManager() {
        return containerManager;
    }
    
    public ExecutorEngine getExecutorEngine() {
        return executorEngine;
    }
    
    public Map<UUID, CreativeWorld> getCommentInputs() {
        return commentInputs;
    }
    
    public Map<UUID, String> getDeleteConfirmations() {
        return deleteConfirmations;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DependencyContainer getDependencyContainer() {
        return dependencyContainer;
    }
    
    private void startInventoryChecker() {
        // Убираем неэффективную проверку каждые 5 секунд
        // Вместо этого будем проверять при входе в dev-мир через PlayerWorldChangeListener
    }
}
