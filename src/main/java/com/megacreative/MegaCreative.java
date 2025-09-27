package com.megacreative;

import com.megacreative.core.DependencyContainer;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.core.CommandRegistry;
import com.megacreative.exceptions.ConfigurationException;
import com.megacreative.config.ConfigurationValidator;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.coding.ScriptTriggerManager;
import com.megacreative.listeners.BukkitPlayerJoinListener;
import com.megacreative.listeners.BukkitPlayerMoveListener;
import com.megacreative.listeners.BukkitPlayerChatListener;
import com.megacreative.listeners.BukkitBlockPlaceListener;
import com.megacreative.listeners.BukkitBlockBreakListener;
import com.megacreative.listeners.BukkitEntityPickupItemListener;
import com.megacreative.listeners.BukkitPlayerDeathListener;
import com.megacreative.listeners.BukkitPlayerQuitListener;
import com.megacreative.listeners.BukkitPlayerRespawnListener;
import com.megacreative.listeners.BukkitPlayerTeleportListener;
import com.megacreative.listeners.BukkitEntityDamageListener;
import com.megacreative.listeners.BukkitInventoryClickListener;
import com.megacreative.listeners.BukkitInventoryOpenListener;
import com.megacreative.listeners.PlayerWorldChangeListener;
import com.megacreative.listeners.DataItemListener;
import com.megacreative.listeners.WorldInteractListener;
import com.megacreative.listeners.DevWorldProtectionListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main plugin class - now lightweight and focused on plugin lifecycle
 * Uses ServiceRegistry for dependency injection to avoid God Object pattern
 *
 * Основной класс плагина - теперь легковесный и ориентированный на жизненный цикл плагина
 * Использует ServiceRegistry для внедрения зависимостей для избежания антипаттерна God Object
 *
 * Haupt-Plugin-Klasse - jetzt schlank und fokussiert auf den Plugin-Lebenszyklus
 * Verwendet ServiceRegistry für Dependency Injection, um das God Object-Muster zu vermeiden
 */
public class MegaCreative extends JavaPlugin {
    
    private static MegaCreative instance;
    
    /**
    /**
     * Gets the singleton instance of the plugin
     * @return The plugin instance
     */
    public static MegaCreative getInstance() {
        return instance;
    }
    private DependencyContainer dependencyContainer;
    private ServiceRegistry serviceRegistry;
    private CommandRegistry commandRegistry;
    private BukkitTask tickTask;
    private BukkitTask autoSaveTask;
    private int tpsCheckCounter = 0;
    private Logger logger;
    
    @Override
    public void onEnable() {
        synchronized (MegaCreative.class) {
            instance = this;
        }
        logger = getLogger();
        
        try {
            initializeDependencyInjection();
            bootstrap();
        } catch (Exception e) {
            getLogger().severe("Failed to enable MegaCreative: " + e.getMessage());
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
            
            // Shutdown all services gracefully through ServiceRegistry
            if (serviceRegistry != null) {
                serviceRegistry.dispose();
            }
        } catch (Exception e) {
            getLogger().severe("Error during plugin disable: " + e.getMessage());
        } finally {
            // Ensure instance is cleared
            synchronized (MegaCreative.class) {
                instance = null;
            }
        }
    }
    
    /**
     * Initialize dependency injection container and service registry
     */
    private void initializeDependencyInjection() {
        // Create dependency injection container
        this.dependencyContainer = new DependencyContainer();
        
        // Register this plugin instance as a singleton in the dependency container
        this.dependencyContainer.registerSingleton(MegaCreative.class, this);
        
        // Initialize DataItemFactory with plugin instance
        com.megacreative.coding.data.DataItemFactory.initialize(this);
        
        // Create service registry
        this.serviceRegistry = new ServiceRegistry(this, dependencyContainer);
        
        // Create command registry
        this.commandRegistry = new CommandRegistry(this, serviceRegistry);
        
        // Initialize all services
        this.serviceRegistry.initializeServices();
    }
    
    /**
     * Bootstrap the application by starting core services
     */
    private void bootstrap() {
        // Connect services after initialization but before use
        serviceRegistry.connectServices();
        
        // Register events
        registerEvents();
        
        // Register commands
        registerCommands();
        
        // Start tick scheduler
        startTickScheduler();
        
        // Start auto-save system
        startAutoSaveSystem();
        
        // Delay world loading until after Bukkit is fully initialized
        getServer().getScheduler().runTaskLater(this, this::loadWorlds, 1L);
    }
    
    /**
     * Load worlds after Bukkit is fully initialized
     */
    private void loadWorlds() {
        try {
            // Load worlds after Bukkit is ready
            IWorldManager worldManager = serviceRegistry.getWorldManager();
            if (worldManager instanceof com.megacreative.managers.WorldManagerImpl worldManagerImpl) {
                worldManagerImpl.loadWorlds();
            }
            
            // Initialize additional services that depend on worlds being loaded
            serviceRegistry.initializeAdditionalServices();
            
            // Now that worlds are loaded, rebuild event handler maps
            // PlayerEventsListener is deprecated, we're using ScriptTriggerManager instead
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error loading worlds", e);
        }
    }
    
    /**
     * Register all event listeners
     */
    private void registerEvents() {
        // Register new architecture listeners
        if (serviceRegistry != null) {
            getServer().getPluginManager().registerEvents(serviceRegistry.getBlockLinker(), this);
            getServer().getPluginManager().registerEvents(serviceRegistry.getBlockHierarchyManager(), this);
            getServer().getPluginManager().registerEvents(serviceRegistry.getWorldCodeRestorer(), this);
            getServer().getPluginManager().registerEvents(serviceRegistry.getCodeBlockSignManager(), this);
            
            // Register DevInventoryManager to handle world change events
            getServer().getPluginManager().registerEvents(serviceRegistry.getDevInventoryManager(), this);
            
            // Register new Bukkit listeners for clean event bus
            getServer().getPluginManager().registerEvents(new BukkitPlayerJoinListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitPlayerMoveListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitPlayerChatListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitBlockPlaceListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitBlockBreakListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitEntityPickupItemListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitPlayerDeathListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitPlayerQuitListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitPlayerRespawnListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitPlayerTeleportListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitEntityDamageListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitInventoryClickListener(this), this);
            getServer().getPluginManager().registerEvents(new BukkitInventoryOpenListener(this), this);
            
            // Register PlayerWorldChangeListener to handle player world changes and give coding items
            getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(this), this);
            
            // Register GUIManager to handle GUI events
            getServer().getPluginManager().registerEvents(serviceRegistry.getGuiManager(), this);
            
            // Register ScriptTriggerManager to listen to our custom events
            getServer().getPluginManager().registerEvents(serviceRegistry.getScriptTriggerManager(), this);
            
            // Register DataItemListener to handle data item events
            getServer().getPluginManager().registerEvents(new DataItemListener(), this);
            
            // Register WorldInteractListener to handle starter item interactions
            getServer().getPluginManager().registerEvents(new WorldInteractListener(this), this);
            
            // Register DevWorldProtectionListener to handle dev world protection
            getServer().getPluginManager().registerEvents(serviceRegistry.getService(DevWorldProtectionListener.class), this);
        }
    }
    
    /**
     * Register all commands
     */
    private void registerCommands() {
        commandRegistry.registerCommands();
    }
    
    /**
     * Start the tick scheduler for onTick events
     */
    private void startTickScheduler() {
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Use TickManager to handle tick events
                if (serviceRegistry != null) {
                    serviceRegistry.getTickManager().tick();
                }
                
                tpsCheckCounter++;
                if (tpsCheckCounter >= 20) {
                    // Check TPS every 20 ticks (1 second)
                    tpsCheckCounter = 0;
                }
            }
        }.runTaskTimer(this, 1L, 1L); // Run every tick
    }
    
    /**
     * Start auto-save system for all worlds
     */
    private void startAutoSaveSystem() {
        // Auto-save every 5 minutes (6000 ticks)
        autoSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                if (worldManager != null) {
                    try {
                        // Save all worlds asynchronously
                        worldManager.saveAllWorlds();
                    } catch (Exception e) {
                        getLogger().warning("Error during auto-save: " + e.getMessage());
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 6000L, 6000L); // 5 minutes interval
    }
    
    // Service access methods (dependency injection preferred over static access)
    
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
    
    /**
     * Gets the command registry
     */
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }
}