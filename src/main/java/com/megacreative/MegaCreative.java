package com.megacreative;

import com.megacreative.core.CommandRegistry;
import com.megacreative.core.DependencyContainer;
import com.megacreative.core.ServiceRegistry;
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
            logger.log(java.util.logging.Level.SEVERE, "Failed to enable MegaCreative", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            
            if (tickTask != null) {
                tickTask.cancel();
            }
            
            
            if (autoSaveTask != null) {
                autoSaveTask.cancel();
            }
            
            
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
        
        this.dependencyContainer = new DependencyContainer();
        
        
        this.dependencyContainer.registerSingleton(MegaCreative.class, this);
        
        
        com.megacreative.coding.data.DataItemFactory.initialize(this);
        
        
        this.serviceRegistry = new ServiceRegistry(this, dependencyContainer);
        
        
        this.commandRegistry = new CommandRegistry(this, serviceRegistry);
        
        
        this.serviceRegistry.initializeServices();
    }
    
    /**
     * Bootstrap the application by starting core services
     */
    private void bootstrap() {
        
        serviceRegistry.connectServices();
        
        
        registerEvents();
        
        
        registerCommands();
        
        
        startTickScheduler();
        
        
        startAutoSaveSystem();
        
        
        getServer().getScheduler().runTaskLater(this, this::loadWorlds, 1L);
    }
    
    /**
     * Load worlds after Bukkit is fully initialized
     */
    private void loadWorlds() {
        try {
            
            com.megacreative.interfaces.IWorldManager worldManager = serviceRegistry.getWorldManager();
            if (worldManager instanceof com.megacreative.managers.WorldManagerImpl worldManagerImpl) {
                worldManagerImpl.loadWorlds();
            }
            
            
            serviceRegistry.initializeAdditionalServices();
            
            
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error loading worlds", e);
        }
    }
    
    /**
     * Register all event listeners
     */
    private void registerEvents() {
        
        if (serviceRegistry != null) {
            getServer().getPluginManager().registerEvents(serviceRegistry.getBlockLinker(), this);
            getServer().getPluginManager().registerEvents(serviceRegistry.getBlockHierarchyManager(), this);
            getServer().getPluginManager().registerEvents(serviceRegistry.getWorldCodeRestorer(), this);
            getServer().getPluginManager().registerEvents(serviceRegistry.getCodeBlockSignManager(), this);
            getServer().getPluginManager().registerEvents(serviceRegistry.getBlockPlacementHandler(), this);
            
            
            getServer().getPluginManager().registerEvents(serviceRegistry.getDevInventoryManager(), this);
            
            
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
            
            
            getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(this), this);
            
            
            getServer().getPluginManager().registerEvents(serviceRegistry.getGuiManager(), this);
            
            
            getServer().getPluginManager().registerEvents(serviceRegistry.getScriptTriggerManager(), this);
            
            
            getServer().getPluginManager().registerEvents(serviceRegistry.getCustomEventManager(), this);
            
            
            getServer().getPluginManager().registerEvents(new DataItemListener(this), this);
            
            
            getServer().getPluginManager().registerEvents(new WorldInteractListener(this), this);
            
            
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
                
                if (serviceRegistry != null) {
                    TickManager tickManager = serviceRegistry.getTickManager();
                    if (tickManager != null) {
                        tickManager.tick();
                    }
                }
                
                
                if (++tpsCheckCounter >= 20) {
                    tpsCheckCounter = 0;
                    
                }
            }
        }.runTaskTimer(this, 1L, 1L); 
    }
    
    /**
     * Start the auto-save system
     */
    private void startAutoSaveSystem() {
        com.megacreative.utils.ConfigManager cfg = serviceRegistry != null ? serviceRegistry.getConfigManager() : null;
        boolean enabled = cfg == null || cfg.isAutoSaveEnabled();
        int seconds = cfg == null ? 300 : cfg.getAutoSaveInterval();
        long periodTicks = Math.max(20L, seconds * 20L);
        if (!enabled) {
            logger.info("Auto-save disabled via config");
            return;
        }
        autoSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    
                    if (serviceRegistry != null) {
                        serviceRegistry.getWorldManager().saveAllWorlds();
                    }
                    
                    
                    if (serviceRegistry != null) {
                        serviceRegistry.getPlayerManager().saveAllPlayerData();
                        serviceRegistry.getVariableManager().savePersistentData();
                    }
                    
                    logger.info("Auto-save completed successfully");
                } catch (Exception e) {
                    logger.severe("Error during auto-save: " + e.getMessage());
                    logger.log(Level.SEVERE, "Error during auto-save", e);
                }
            }
        }.runTaskTimer(this, periodTicks, periodTicks); 
    }
    
    
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