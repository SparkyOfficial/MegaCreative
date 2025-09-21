package com.megacreative;

import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.AutoConnectionManager;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.DefaultScriptEngine;
import com.megacreative.coding.ActionFactory;
import com.megacreative.coding.ConditionFactory;
import com.megacreative.coding.events.PlayerEventsListener;
import com.megacreative.coding.containers.BlockContainerManager;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.errors.VisualErrorHandler;
import com.megacreative.coding.groups.BlockGroupManager;
import com.megacreative.coding.monitoring.ScriptPerformanceMonitor;
import com.megacreative.coding.events.EventDataExtractorRegistry;
import com.megacreative.interfaces.*;
import com.megacreative.listeners.DevWorldProtectionListener;
import com.megacreative.managers.*;
import com.megacreative.services.BlockConfigService;
import com.megacreative.services.FunctionManager;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.gui.interactive.InteractiveGUIManager;
import com.megacreative.gui.interactive.ReferenceSystemStyleGUI;
import com.megacreative.gui.coding.EnhancedActionParameterGUI;
import com.megacreative.tools.CodeBlockClipboard;
// 🎆 Reference system-style comprehensive events
import com.megacreative.managers.ReferenceSystemEventManager;
import com.megacreative.listeners.CompilationListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.megacreative.commands.AddFloorCommand;
import com.megacreative.commands.BuildCommand;
import com.megacreative.commands.ClipboardCommand;
import com.megacreative.commands.CreateWorldCommand;
import com.megacreative.commands.DebugCommand;
import com.megacreative.commands.DeleteCommand;
import com.megacreative.commands.DevCommand;
import com.megacreative.commands.EnemyPlayerCommand;
import com.megacreative.commands.ExecutionCommand;
import com.megacreative.commands.FunctionCommand;
import com.megacreative.commands.GroupCommand;
import com.megacreative.commands.HubCommand;
import com.megacreative.commands.InteractiveCommand;
import com.megacreative.commands.JoinCommand;
import com.megacreative.commands.MainCommand;
import com.megacreative.commands.MyWorldsCommand;
import com.megacreative.commands.PerformanceCommand;
import com.megacreative.commands.PlayCommand;
import com.megacreative.commands.StatusCommand;
import com.megacreative.commands.TemplatesCommand;
import com.megacreative.commands.TrustedPlayerCommand;
import com.megacreative.commands.WorkspaceCommand;
import com.megacreative.commands.WorldBrowserCommand;
import com.megacreative.commands.WorldSettingsCommand;
import com.megacreative.config.ConfigurationValidator;
import com.megacreative.configs.WorldCode;
import com.megacreative.core.DependencyContainer;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.exceptions.ConfigurationException;
import com.megacreative.listeners.BlockBreakListener;
import com.megacreative.listeners.BlockGroupListener;
import com.megacreative.listeners.CommandListener;
import com.megacreative.listeners.CompilationListener;
import com.megacreative.listeners.DataItemListener;
import com.megacreative.listeners.GuiListener;
import com.megacreative.listeners.InventoryClickListener;
import com.megacreative.listeners.PlayerDeathListener;
import com.megacreative.listeners.PlayerJoinListener;
import com.megacreative.listeners.PlayerQuitListener;
import com.megacreative.listeners.PlayerWorldChangeListener;
import com.megacreative.listeners.WorldInteractListener;

/**
 * Основной класс плагина - теперь легковесный и ориентированный на жизненный цикл плагина
 * Использует ServiceRegistry для избежания антипаттерна God Object
 *
 * Main plugin class - now lightweight and focused on plugin lifecycle
 * Uses ServiceRegistry to avoid God Object pattern
 *
 * Haupt-Plugin-Klasse - jetzt schlank und fokussiert auf den Plugin-Lebenszyklus
 * Verwendet ServiceRegistry, um das God Object-Muster zu vermeiden
 */
public class MegaCreative extends JavaPlugin {
    
    private static MegaCreative instance;
    private DependencyContainer dependencyContainer;
    private ServiceRegistry serviceRegistry;
    private BukkitTask tickTask;
    private BukkitTask autoSaveTask; // Add auto-save task
    private int tpsCheckCounter = 0;
    private java.util.logging.Logger logger;
    
    @Override
    public void onEnable() {
        synchronized (MegaCreative.class) {
            instance = this;
        }
        logger = getLogger();
        
        // Initialize WorldCode configuration system
        WorldCode.setup(this);
        
        try {
            // Initialize dependency injection
            this.dependencyContainer = new DependencyContainer();
            // Register this plugin instance as a singleton in the dependency container
            this.dependencyContainer.registerSingleton(MegaCreative.class, this);
            this.serviceRegistry = new ServiceRegistry(this, dependencyContainer);
            
            validateConfiguration();

            serviceRegistry.initializeServices();
            
            registerCommands();
            registerEvents();
            
            startTickScheduler();
            
            // Start auto-save system
            startAutoSaveSystem();
            
            getLogger().info("MegaCreative enabled successfully!");
            
        } catch (Exception e) {
            getLogger().severe("Failed to enable MegaCreative: " + e.getMessage());
            getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            // Stop the tick scheduler
            if (tickTask != null) {
                tickTask.cancel();
            }
            
            // Stop auto-save task
            if (autoSaveTask != null) {
                autoSaveTask.cancel();
            }
            
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
            getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }
    
    /**
     * Проверяет конфигурацию плагина
     *
     * Validates plugin configuration
     *
     * Validiert die Plugin-Konfiguration
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
     * Запускает систему автоматического сохранения для всех миров
     *
     * Starts auto-save system for all worlds
     *
     * Startet das automatische Speichersystem für alle Welten
     */
    private void startAutoSaveSystem() {
        // Auto-save every 5 minutes (6000 ticks)
        autoSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (serviceRegistry != null && serviceRegistry.getWorldManager() != null) {
                    try {
                        // Save all worlds asynchronously
                        serviceRegistry.getWorldManager().saveAllWorlds();
                        getLogger().info("Auto-saved all creative worlds");
                    } catch (Exception e) {
                        getLogger().warning("Error during auto-save: " + e.getMessage());
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 6000L, 6000L); // 5 minutes interval
    }
    
    /**
     * Запускает планировщик тиков для событий onTick
     *
     * Starts the tick scheduler for onTick events
     *
     * Startet den Tick-Planer für onTick-Ereignisse
     */
    private void startTickScheduler() {
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Trigger onTick event for all creative worlds
                // This will be handled by the PlayerEventsListener
                if (serviceRegistry != null) {
                    PlayerEventsListener listener = serviceRegistry.getPlayerEventsListener();
                    if (listener != null) {
                        listener.onTick();
                        
                        // Check TPS every 20 ticks (1 second)
                        tpsCheckCounter++;
                        if (tpsCheckCounter >= 20) {
                            listener.onServerTPS();
                            tpsCheckCounter = 0;
                        }
                    }
                }
            }
        }.runTaskTimer(this, 1L, 1L); // Run every tick
    }
    
    /**
     * Регистрирует все команды плагина
     *
     * Registers all plugin commands
     *
     * Registriert alle Plugin-Befehle
     */
    private void registerCommands() {
        // Add null checks for all commands before setting executor
        if (getCommand("megacreative") != null) {
            getCommand("megacreative").setExecutor(new MainCommand(this));
        }
        if (getCommand("myworlds") != null) {
            getCommand("myworlds").setExecutor(new MyWorldsCommand(this));
        }
        if (getCommand("worldbrowser") != null) {
            getCommand("worldbrowser").setExecutor(new WorldBrowserCommand(this));
        }
        
        // Add null checks for serviceRegistry before using it
        if (serviceRegistry != null) {
            if (getCommand("join") != null) {
                getCommand("join").setExecutor(new JoinCommand(this, serviceRegistry.getWorldManager()));
            }
            if (getCommand("build") != null) {
                getCommand("build").setExecutor(new BuildCommand(this, serviceRegistry.getWorldManager()));
            }
            if (getCommand("switch") != null) {
                getCommand("switch").setExecutor(new com.megacreative.commands.SwitchCommand(this, serviceRegistry.getWorldManager()));
            }
            if (getCommand("hub") != null) {
                getCommand("hub").setExecutor(new HubCommand(this, serviceRegistry.getPlayerManager()));
            }
            if (getCommand("create") != null) {
                getCommand("create").setExecutor(new CreateWorldCommand(this, serviceRegistry.getWorldManager()));
            }
            if (getCommand("clipboard") != null) {
                getCommand("clipboard").setExecutor(new ClipboardCommand(this, serviceRegistry.getCodeBlockClipboard()));
            }
            if (getCommand("group") != null) {
                getCommand("group").setExecutor(new GroupCommand(serviceRegistry));
            }
        }
        
        if (getCommand("play") != null) {
            getCommand("play").setExecutor(new PlayCommand(this));
        }
        if (getCommand("trusted") != null) {
            getCommand("trusted").setExecutor(new TrustedPlayerCommand(this));
        }
        if (getCommand("dev") != null) {
            getCommand("dev").setExecutor(new DevCommand(this));
        }
        
        if (getCommand("templates") != null) {
            getCommand("templates").setExecutor(new TemplatesCommand(this));
        }
        if (getCommand("worldsettings") != null) {
            getCommand("worldsettings").setExecutor(new WorldSettingsCommand(this));
        }
        if (getCommand("debug") != null) {
            getCommand("debug").setExecutor(new DebugCommand(this));
        }
        
        if (getCommand("status") != null) {
            getCommand("status").setExecutor(new StatusCommand(this));
        }
        if (getCommand("addfloor") != null) {
            getCommand("addfloor").setExecutor(new AddFloorCommand(this));
        }
        if (getCommand("workspace") != null) {
            getCommand("workspace").setExecutor(new WorkspaceCommand(this));
        }
        if (getCommand("delete") != null) {
            getCommand("delete").setExecutor(new DeleteCommand(this));
        }
        
        // Register function management command
        if (getCommand("function") != null) {
            getCommand("function").setExecutor(new FunctionCommand(this));
            getCommand("function").setTabCompleter(new FunctionCommand(this));
        }
        
        // Register interactive GUI command
        if (getCommand("interactive") != null) {
            getCommand("interactive").setExecutor(new InteractiveCommand(this));
            getCommand("interactive").setTabCompleter(new InteractiveCommand(this));
        }
        
        // Advanced execution command
        if (getCommand("execution") != null) {
            getCommand("execution").setExecutor(new ExecutionCommand(this));
        }
        
        // Enemy player management command
        if (getCommand("enemy") != null) {
            getCommand("enemy").setExecutor(new EnemyPlayerCommand(this));
            getCommand("enemy").setTabCompleter(new EnemyPlayerCommand(this));
        }
        
        // Performance monitoring command
        if (getCommand("performance") != null) {
            getCommand("performance").setExecutor(new PerformanceCommand(this));
            getCommand("performance").setTabCompleter(new PerformanceCommand(this));
        }
        
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
        
        // Add null checks for serviceRegistry before using it
        if (serviceRegistry != null) {
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
            
            // 🎆 ENHANCED: Register comprehensive world protection listener
            getServer().getPluginManager().registerEvents(new com.megacreative.listeners.WorldProtectionListener(this, serviceRegistry.getWorldManager()), this);
            
            // Register enemy player restriction manager
            getServer().getPluginManager().registerEvents(serviceRegistry.getEnemyPlayerRestrictionManager(), this);
        }
        
        // Register CodeMoverListener for advanced code manipulation
        getServer().getPluginManager().registerEvents(new com.megacreative.listeners.CodeMoverListener(this), this);
        
        // Register our new CompilationListener for automatic code compilation
        getServer().getPluginManager().registerEvents(new CompilationListener(this), this);
        
        // Register runCode execution engine
        // getServer().getPluginManager().registerEvents(new runCode(this), this);
        
        // 🎆 ENHANCED: Register world load listener for code block hydration
        getServer().getPluginManager().registerEvents(new com.megacreative.listeners.WorldLoadListener(this), this);
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
     * Gets the VariableManager instance
     */
    public com.megacreative.coding.variables.VariableManager getVariableManager() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getVariableManager();
    }
    
    /**
     * Gets the IWorldManager instance
     */
    public com.megacreative.interfaces.IWorldManager getWorldManager() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getWorldManager();
    }
    
    /**
     * Gets the GUIManager instance
     */
    public com.megacreative.managers.GUIManager getGuiManager() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getGuiManager();
    }
    
    /**
     * Gets the BlockPlacementHandler instance
     */
    public com.megacreative.coding.BlockPlacementHandler getBlockPlacementHandler() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getBlockPlacementHandler();
    }
    
    /**
     * Gets the TemplateManager instance
     */
    public com.megacreative.managers.TemplateManager getTemplateManager() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getTemplateManager();
    }
    
    /**
     * Gets the TrustedPlayerManager instance
     */
    public com.megacreative.interfaces.ITrustedPlayerManager getTrustedPlayerManager() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getTrustedPlayerManager();
    }
    
    /**
     * Gets the CodeBlockClipboard instance
     */
    public com.megacreative.tools.CodeBlockClipboard getCodeBlockClipboard() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getCodeBlockClipboard();
    }
    
    /**
     * Gets the dependency container
     */
    public DependencyContainer getDependencyContainer() {
        return dependencyContainer;
    }
    
    /**
     * Gets the player manager
     */
    public com.megacreative.managers.PlayerManagerImpl getPlayerManager() {
        if (serviceRegistry == null) {
            return null;
        }
        return (com.megacreative.managers.PlayerManagerImpl) serviceRegistry.getPlayerManager();
    }
    
    /**
     * Gets the config manager
     */
    public com.megacreative.utils.ConfigManager getConfigManager() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getConfigManager();
    }
    
    /**
     * Gets the scoreboard manager
     */
    public com.megacreative.managers.ScoreboardManager getScoreboardManager() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getScoreboardManager();
    }
    
    /**
     * Gets the coding manager
     */
    public com.megacreative.interfaces.ICodingManager getCodingManager() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getCodingManager();
    }
    
    /**
     * Gets the script debugger
     */
    public com.megacreative.coding.debug.VisualDebugger getScriptDebugger() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getScriptDebugger();
    }
    
    /**
     * Gets the script performance monitor
     */
    public com.megacreative.coding.monitoring.ScriptPerformanceMonitor getScriptPerformanceMonitor() {
        if (serviceRegistry == null) {
            return null;
        }
        return serviceRegistry.getScriptPerformanceMonitor();
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