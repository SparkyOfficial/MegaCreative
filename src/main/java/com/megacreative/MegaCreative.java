package com.megacreative;

import com.megacreative.commands.*;
import com.megacreative.config.ConfigurationValidator;
import com.megacreative.core.CommandRegistry;
import com.megacreative.core.DependencyContainer;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.exceptions.ConfigurationException;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.listeners.*;
import com.megacreative.managers.TickManager;
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
 * 
 * @author Андрій Будильников
 */
public class MegaCreative extends JavaPlugin {
    
    private DependencyContainer dependencyContainer;
    private ServiceRegistry serviceRegistry;
    private CommandRegistry commandRegistry;
    private BukkitTask tickTask;
    private BukkitTask autoSaveTask;
    private int tpsCheckCounter = 0;
    private Logger logger;
    
    @Override
    public void onEnable() {
        logger = getLogger();
        
        try {
            initializeDependencyInjection();
            bootstrap();
            logger.info("MegaCreative initialized successfully!");
        } catch (Exception e) {
            logger.severe("Failed to enable MegaCreative: " + e.getMessage());
            e.printStackTrace();
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
            getServer().getPluginManager().registerEvents(serviceRegistry.getBlockPlacementHandler(), this);
            
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
            getServer().getPluginManager().registerEvents(new DataItemListener(this), this);
            
            // Register WorldInteractListener to handle starter item interactions
            getServer().getPluginManager().registerEvents(new WorldInteractListener(this), this);
            
            // Register GUIClickListener to handle GUI click events
            getServer().getPluginManager().registerEvents(serviceRegistry.getService(GUIClickListener.class), this);
        }
    }
    
    /**
     * Register all commands
     */
    private void registerCommands() {
        if (commandRegistry != null) {
            commandRegistry.registerCommands();
        }
    }
    
    /**
     * Start the tick scheduler for our custom tick events
     */
    private void startTickScheduler() {
        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Get the TickManager from the service registry and call tick()
                if (serviceRegistry != null) {
                    TickManager tickManager = serviceRegistry.getTickManager();
                    if (tickManager != null) {
                        tickManager.tick();
                    }
                }
                
                // Check TPS every 20 ticks (1 second)
                if (++tpsCheckCounter >= 20) {
                    tpsCheckCounter = 0;
                    // TPS monitoring logic would go here if needed
                }
            }
        }.runTaskTimer(this, 1L, 1L); // Run every tick
    }
    
    /**
     * Start the auto-save system
     */
    private void startAutoSaveSystem() {
        autoSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Save all creative worlds
                    if (serviceRegistry != null) {
                        serviceRegistry.getWorldManager().saveAllWorlds();
                    }
                    
                    // Save player data
                    if (serviceRegistry != null) {
                        serviceRegistry.getPlayerManager().saveAllPlayerData();
                        serviceRegistry.getVariableManager().savePersistentData();
                    }
                    
                    logger.info("Auto-save completed successfully");
                } catch (Exception e) {
                    logger.severe("Error during auto-save: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(this, 6000L, 6000L); // Run every 5 minutes (6000 ticks)
    }
    
    // Getters for services
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
    
    public DependencyContainer getDependencyContainer() {
        return dependencyContainer;
    }
    
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }
}