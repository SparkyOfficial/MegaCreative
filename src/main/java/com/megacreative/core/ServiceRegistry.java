package com.megacreative.core;

import com.megacreative.coding.AutoConnectionManager;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.ScriptDebugger;
import com.megacreative.coding.BlockConfiguration;
import com.megacreative.coding.containers.BlockContainerManager;
import com.megacreative.coding.executors.ExecutorEngine;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.events.EventDataExtractorRegistry;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.errors.VisualErrorHandler;
import com.megacreative.coding.groups.BlockGroupManager;
import com.megacreative.coding.monitoring.ScriptPerformanceMonitor;
import com.megacreative.interfaces.*;
import com.megacreative.managers.*;
import com.megacreative.services.BlockConfigService;
import com.megacreative.tools.CodeBlockClipboard;
import com.megacreative.utils.ConfigManager;
import lombok.extern.java.Log;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central service registry that manages all plugin services and dependencies
 * Replaces the God Object pattern with proper dependency injection
 */
public class ServiceRegistry {
    
    private static final Logger log = Logger.getLogger(ServiceRegistry.class.getName());
    private final Plugin plugin;
    private final DependencyContainer dependencyContainer;
    private final ConcurrentHashMap<Class<?>, Object> services = new ConcurrentHashMap<>();
    
    // Core services
    private ConfigManager configManager;
    
    // Interface-based managers
    private IWorldManager worldManager;
    private IPlayerManager playerManager;
    private ICodingManager codingManager;
    
    // Implementation managers
    private DataManager dataManager;
    private TemplateManager templateManager;
    private ScoreboardManager scoreboardManager;
    private TrustedPlayerManager trustedPlayerManager;
    private GUIManager guiManager;
    private BlockConfigManager blockConfigManager;
    
    // Coding system services
    private BlockPlacementHandler blockPlacementHandler;
    private VisualDebugger scriptDebugger;
    private AutoConnectionManager autoConnectionManager;
    private DevInventoryManager devInventoryManager;
    private VariableManager variableManager;
    private BlockContainerManager containerManager;
    private ExecutorEngine executorEngine;
    private BlockConfiguration blockConfiguration;
    private ScriptPerformanceMonitor scriptPerformanceMonitor;
    
    // New architecture services
    private BlockConfigService blockConfigService;
    private CustomEventManager customEventManager;
    private EventDataExtractorRegistry eventDataExtractorRegistry;
    private VisualDebugger visualDebugger;
    private VisualErrorHandler visualErrorHandler;
    private CodeBlockClipboard codeBlockClipboard;
    private BlockGroupManager blockGroupManager;
    
    public ServiceRegistry(Plugin plugin, DependencyContainer dependencyContainer) {
        this.plugin = plugin;
        this.dependencyContainer = dependencyContainer;
    }
    
    /**
     * Initializes all services in the correct order
     */
    public void initializeServices() {
        log.info("Initializing MegaCreative services...");
        
        try {
            // 1. Core services first
            initializeCoreServices();
            
            // 2. Interface-based managers
            initializeManagers();
            
            // 3. Implementation managers that depend on interfaces
            initializeImplementationManagers();
            
            // 4. Coding system services
            initializeCodingServices();
            
            // 5. New architecture services
            initializeNewArchitectureServices();
            
            // 6. Register all services in DI container
            registerServicesInDI();
            
            // 7. Initialize services that need post-construction setup
            postInitialize();
            
            log.info("Successfully initialized " + services.size() + " services");
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to initialize services", e);
            throw new RuntimeException("Service initialization failed", e);
        }
    }
    
    /**
     * Gets a service by type with proper type safety
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceType) {
        T service = (T) services.get(serviceType);
        if (service == null) {
            throw new IllegalArgumentException("Service not found: " + serviceType.getName());
        }
        return service;
    }
    
    /**
     * Checks if a service is registered
     */
    public boolean hasService(Class<?> serviceType) {
        return services.containsKey(serviceType);
    }
    
    /**
     * Registers a service instance
     */
    public <T> void registerService(Class<T> serviceType, T serviceInstance) {
        services.put(serviceType, serviceInstance);
        dependencyContainer.registerSingleton(serviceType, serviceInstance);
        log.fine("Registered service: " + serviceType.getSimpleName());
    }
    
    /**
     * Shuts down all services gracefully
     */
    public void shutdown() {
        log.info("Shutting down services...");
        
        // Shutdown in reverse order
        shutdownNewArchitectureServices();
        shutdownCodingServices();
        shutdownImplementationManagers();
        shutdownManagers();
        shutdownCoreServices();
        
        services.clear();
        log.info("All services shut down");
    }
    
    // Service getters with proper types
    public ConfigManager getConfigManager() { return configManager; }
    public IWorldManager getWorldManager() { return worldManager; }
    public IPlayerManager getPlayerManager() { return playerManager; }
    public ICodingManager getCodingManager() { return codingManager; }
    public DataManager getDataManager() { return dataManager; }
    public TemplateManager getTemplateManager() { return templateManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public TrustedPlayerManager getTrustedPlayerManager() { return trustedPlayerManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public BlockConfigManager getBlockConfigManager() { return blockConfigManager; }
    public BlockPlacementHandler getBlockPlacementHandler() { return blockPlacementHandler; }
    public VisualDebugger getScriptDebugger() { return scriptDebugger; }
    public AutoConnectionManager getAutoConnectionManager() { return autoConnectionManager; }
    public DevInventoryManager getDevInventoryManager() { return devInventoryManager; }
    public VariableManager getVariableManager() { return variableManager; }
    public BlockContainerManager getContainerManager() { return containerManager; }
    public ExecutorEngine getExecutorEngine() { return executorEngine; }
    public BlockConfiguration getBlockConfiguration() { return blockConfiguration; }
    public ScriptPerformanceMonitor getScriptPerformanceMonitor() { return scriptPerformanceMonitor; }
    public BlockConfigService getBlockConfigService() { return blockConfigService; }
    public CustomEventManager getCustomEventManager() { return customEventManager; }
    public EventDataExtractorRegistry getEventDataExtractorRegistry() { return eventDataExtractorRegistry; }
    public VisualDebugger getVisualDebugger() { return visualDebugger; }
    public VisualErrorHandler getVisualErrorHandler() { return visualErrorHandler; }
    public CodeBlockClipboard getCodeBlockClipboard() { return codeBlockClipboard; }
    public BlockGroupManager getBlockGroupManager() { return blockGroupManager; }
    
    // Private initialization methods
    
    private void initializeCoreServices() {
        // Config manager first
        configManager = new ConfigManager((com.megacreative.MegaCreative) plugin);
        configManager.loadConfig();
        registerService(ConfigManager.class, configManager);
    }
    
    private void initializeManagers() {
        // Interface-based managers with proper dependency injection
        worldManager = dependencyContainer.resolve(IWorldManager.class);
        if (worldManager == null) {
            worldManager = new WorldManagerImpl((com.megacreative.MegaCreative) plugin, null, configManager); // CodingManager will be set later
            registerService(IWorldManager.class, worldManager);
        }
        
        playerManager = dependencyContainer.resolve(IPlayerManager.class);
        if (playerManager == null) {
            playerManager = new PlayerManagerImpl((com.megacreative.MegaCreative) plugin);
            registerService(IPlayerManager.class, playerManager);
        }
        
        codingManager = dependencyContainer.resolve(ICodingManager.class);
        if (codingManager == null) {
            codingManager = new com.megacreative.coding.CodingManagerImpl((com.megacreative.MegaCreative) plugin);
            registerService(ICodingManager.class, codingManager);
        }
        
        // Now update WorldManager with CodingManager dependency
        if (worldManager instanceof WorldManagerImpl) {
            ((WorldManagerImpl) worldManager).setCodingManager(codingManager);
        }
    }
    
    private void initializeImplementationManagers() {
        // Services that depend on the interface managers
        dataManager = new DataManager((com.megacreative.MegaCreative) plugin);
        registerService(DataManager.class, dataManager);
        
        templateManager = new TemplateManager((com.megacreative.MegaCreative) plugin);
        registerService(TemplateManager.class, templateManager);
        
        scoreboardManager = new ScoreboardManager((com.megacreative.MegaCreative) plugin);
        registerService(ScoreboardManager.class, scoreboardManager);
        
        trustedPlayerManager = new TrustedPlayerManager((com.megacreative.MegaCreative) plugin);
        registerService(TrustedPlayerManager.class, trustedPlayerManager);
        
        blockConfigManager = new BlockConfigManager((com.megacreative.MegaCreative) plugin);
        registerService(BlockConfigManager.class, blockConfigManager);
    }
    
    private void initializeCodingServices() {
        // Block configuration service (new architecture)
        blockConfigService = new BlockConfigService((com.megacreative.MegaCreative) plugin);
        registerService(BlockConfigService.class, blockConfigService);
        
        // Coding system components
        blockPlacementHandler = new BlockPlacementHandler((com.megacreative.MegaCreative) plugin);
        registerService(BlockPlacementHandler.class, blockPlacementHandler);
        
        scriptDebugger = new VisualDebugger((com.megacreative.MegaCreative) plugin);
        registerService(VisualDebugger.class, scriptDebugger);
        
        autoConnectionManager = new AutoConnectionManager((com.megacreative.MegaCreative) plugin, blockConfigService);
        registerService(AutoConnectionManager.class, autoConnectionManager);
        
        devInventoryManager = new DevInventoryManager((com.megacreative.MegaCreative) plugin);
        registerService(DevInventoryManager.class, devInventoryManager);
        
        variableManager = new VariableManager((com.megacreative.MegaCreative) plugin);
        registerService(VariableManager.class, variableManager);
        
        containerManager = new BlockContainerManager((com.megacreative.MegaCreative) plugin);
        registerService(BlockContainerManager.class, containerManager);
        
        executorEngine = new ExecutorEngine((com.megacreative.MegaCreative) plugin);
        registerService(ExecutorEngine.class, executorEngine);
        
        blockConfiguration = new BlockConfiguration((com.megacreative.MegaCreative) plugin);
        registerService(BlockConfiguration.class, blockConfiguration);
        
        // Performance monitoring system
        scriptPerformanceMonitor = new ScriptPerformanceMonitor(plugin);
        registerService(ScriptPerformanceMonitor.class, scriptPerformanceMonitor);
    }
    
    private void initializeNewArchitectureServices() {
        // Event data extraction system
        eventDataExtractorRegistry = new EventDataExtractorRegistry();
        registerService(EventDataExtractorRegistry.class, eventDataExtractorRegistry);
        
        // Advanced debugging and error handling
        visualDebugger = new VisualDebugger((com.megacreative.MegaCreative) plugin);
        registerService(VisualDebugger.class, visualDebugger);
        
        visualErrorHandler = new VisualErrorHandler((com.megacreative.MegaCreative) plugin);
        registerService(VisualErrorHandler.class, visualErrorHandler);
        
        // Custom events system
        customEventManager = new CustomEventManager((com.megacreative.MegaCreative) plugin);
        registerService(CustomEventManager.class, customEventManager);
        
        // Code manipulation tools
        codeBlockClipboard = new CodeBlockClipboard();
        registerService(CodeBlockClipboard.class, codeBlockClipboard);
        
        // Block grouping system
        blockGroupManager = new BlockGroupManager((com.megacreative.MegaCreative) plugin, playerManager);
        registerService(BlockGroupManager.class, blockGroupManager);
        
        // GUI manager with proper dependencies
        guiManager = new GUIManager(playerManager, dataManager);
        registerService(GUIManager.class, guiManager);
    }
    
    private void registerServicesInDI() {
        // Register all services in the dependency container for auto-injection
        for (var entry : services.entrySet()) {
            if (!dependencyContainer.isRegistered(entry.getKey())) {
                @SuppressWarnings("unchecked")
                Class<Object> keyClass = (Class<Object>) entry.getKey();
                dependencyContainer.registerSingleton(keyClass, entry.getValue());
            }
        }
    }
    
    private void postInitialize() {
        // Services that need initialization after all dependencies are available
        if (worldManager instanceof WorldManagerImpl) {
            ((WorldManagerImpl) worldManager).initialize();
        }
    }
    
    // Shutdown methods
    
    private void shutdownCoreServices() {
        if (dataManager != null) {
            dataManager.saveAllData();
        }
        if (worldManager != null) {
            worldManager.saveAllWorlds();
        }
    }
    
    private void shutdownManagers() {
        // Shutdown managers that might need cleanup
    }
    
    private void shutdownImplementationManagers() {
        // Shutdown implementation-specific managers
    }
    
    private void shutdownCodingServices() {
        // Stop coding-related services
        if (executorEngine != null) {
            // Stop any running executions
        }
    }
    
    private void shutdownNewArchitectureServices() {
        if (visualDebugger != null) {
            visualDebugger.cleanup();
        }
        if (visualErrorHandler != null) {
            visualErrorHandler.cleanup();
        }
        if (blockGroupManager != null) {
            blockGroupManager.cleanup();
        }
    }
}