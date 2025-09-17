package com.megacreative;

import com.megacreative.commands.*;
import com.megacreative.configs.WorldCode; // Add this import
import com.megacreative.execution.runCode; // Add this import
import com.megacreative.listeners.*;
import com.megacreative.core.DependencyContainer;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.config.ConfigurationValidator;
import com.megacreative.exceptions.ConfigurationException;
import com.megacreative.coding.events.PlayerEventsListener;
import com.megacreative.coding.debug.VisualDebugger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

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
        getCommand("megacreative").setExecutor(new MainCommand(this));
        getCommand("myworlds").setExecutor(new MyWorldsCommand(this));
        getCommand("worldbrowser").setExecutor(new WorldBrowserCommand(this));
        
        // Add null checks for serviceRegistry before using it
        if (serviceRegistry != null) {
            getCommand("join").setExecutor(new JoinCommand(this, serviceRegistry.getWorldManager()));
            getCommand("build").setExecutor(new BuildCommand(this, serviceRegistry.getWorldManager()));
            getCommand("switch").setExecutor(new com.megacreative.commands.SwitchCommand(this, serviceRegistry.getWorldManager()));
            getCommand("hub").setExecutor(new HubCommand(this, serviceRegistry.getPlayerManager()));
            getCommand("create").setExecutor(new CreateWorldCommand(this, serviceRegistry.getWorldManager()));
            getCommand("clipboard").setExecutor(new ClipboardCommand(this, serviceRegistry.getCodeBlockClipboard()));
            getCommand("group").setExecutor(new GroupCommand(serviceRegistry));
        }
        
        getCommand("play").setExecutor(new PlayCommand(this));
        getCommand("trusted").setExecutor(new TrustedPlayerCommand(this));
        getCommand("dev").setExecutor(new DevCommand(this));
        
        getCommand("templates").setExecutor(new TemplatesCommand(this));
        getCommand("worldsettings").setExecutor(new WorldSettingsCommand(this));
        getCommand("debug").setExecutor(new DebugCommand(this));
        
        getCommand("status").setExecutor(new StatusCommand(this));
        getCommand("addfloor").setExecutor(new AddFloorCommand(this));
        getCommand("workspace").setExecutor(new WorkspaceCommand(this));
        getCommand("delete").setExecutor(new DeleteCommand(this));
        
        // Register function management command
        getCommand("function").setExecutor(new FunctionCommand(this));
        getCommand("function").setTabCompleter(new FunctionCommand(this));
        
        // Register interactive GUI command
        getCommand("interactive").setExecutor(new InteractiveCommand(this));
        getCommand("interactive").setTabCompleter(new InteractiveCommand(this));
        
        // Advanced execution command
        getCommand("execution").setExecutor(new ExecutionCommand(this));
        
        // Test compilation command
        getCommand("testcompile").setExecutor(new TestCompileCommand(this));
        
        // Enemy player management command
        getCommand("enemy").setExecutor(new EnemyPlayerCommand(this));
        getCommand("enemy").setTabCompleter(new EnemyPlayerCommand(this));
        
        // Performance monitoring command
        getCommand("performance").setExecutor(new PerformanceCommand(this));
        getCommand("performance").setTabCompleter(new PerformanceCommand(this));
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
        getServer().getPluginManager().registerEvents(new runCode(this), this);
        
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