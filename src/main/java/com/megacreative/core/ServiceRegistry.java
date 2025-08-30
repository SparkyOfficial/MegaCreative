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
import com.megacreative.listeners.DevWorldProtectionListener;
import com.megacreative.managers.*;
import com.megacreative.services.BlockConfigService;
import com.megacreative.tools.CodeBlockClipboard;
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
    private com.megacreative.utils.ConfigManager configManager;
    
    // Interface-based managers
    private IWorldManager worldManager;
    private IPlayerManager playerManager;
    private ICodingManager codingManager;
    
    // Implementation managers
    private TemplateManager templateManager;
    private ScoreboardManager scoreboardManager;
    private TrustedPlayerManager trustedPlayerManager;
    private ITrustedPlayerManager trustedPlayerManagerInterface; // Interface reference
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
    private DevWorldProtectionListener devWorldProtectionListener; // Add this field
    
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
        log.info("Shutting down MegaCreative services...");
        
        // Shutdown services in reverse order of initialization
        if (scriptPerformanceMonitor != null) {
            scriptPerformanceMonitor.shutdown();
        }
        
        if (visualDebugger != null) {
            visualDebugger.shutdown();
        }
        
        if (executorEngine != null) {
            executorEngine.shutdown();
        }
        
        if (autoConnectionManager != null) {
            autoConnectionManager.shutdown();
        }
        
        if (codingManager != null) {
            codingManager.shutdown();
        }
        
        if (guiManager != null) {
            guiManager.shutdown();
        }
        
        if (trustedPlayerManager != null) {
            trustedPlayerManager.shutdown();
        }
        
        if (scoreboardManager != null) {
            scoreboardManager.shutdown();
        }
        
        if (templateManager != null) {
            templateManager.shutdown();
        }
        
        if (blockConfigManager != null) {
            blockConfigManager.shutdown();
        }
        
        if (worldManager != null) {
            worldManager.shutdown();
        }
        
        if (configManager != null) {
            configManager.shutdown();
        }
        
        log.info("All services shut down successfully");
    }
    
    // Service getters with proper types and null safety
    /**
     * Gets the BlockConfiguration service
     * @return The BlockConfiguration instance
     */
    public com.megacreative.coding.BlockConfiguration getBlockConfiguration() {
        if (blockConfiguration == null) {
            blockConfiguration = new com.megacreative.coding.BlockConfiguration(
                (com.megacreative.MegaCreative) plugin
            );
            registerService(com.megacreative.coding.BlockConfiguration.class, blockConfiguration);
        }
        return blockConfiguration;
    }
    
    public com.megacreative.utils.ConfigManager getConfigManager() { 
        return configManager; 
    }
    
    public IWorldManager getWorldManager() { 
        return worldManager != null ? worldManager : 
            (worldManager = dependencyContainer.resolve(IWorldManager.class));
    }
    
    public IPlayerManager getPlayerManager() { 
        return playerManager != null ? playerManager : 
            (playerManager = dependencyContainer.resolve(IPlayerManager.class));
    }
    
    public ICodingManager getCodingManager() { 
        return codingManager != null ? codingManager : 
            (codingManager = dependencyContainer.resolve(ICodingManager.class));
    }
    
    public TemplateManager getTemplateManager() { 
        return templateManager != null ? templateManager : 
            (templateManager = dependencyContainer.resolve(TemplateManager.class));
    }
    
    public ScoreboardManager getScoreboardManager() { 
        return scoreboardManager != null ? scoreboardManager : 
            (scoreboardManager = dependencyContainer.resolve(ScoreboardManager.class));
    }
    
    public ITrustedPlayerManager getTrustedPlayerManager() { 
        return trustedPlayerManagerInterface != null ? trustedPlayerManagerInterface : 
            (trustedPlayerManagerInterface = dependencyContainer.resolve(ITrustedPlayerManager.class));
    }
    
    public GUIManager getGuiManager() {
        return guiManager != null ? guiManager :
            (guiManager = dependencyContainer.resolve(GUIManager.class));
    }
    
    public VariableManager getVariableManager() {
        return variableManager != null ? variableManager :
            (variableManager = dependencyContainer.resolve(VariableManager.class));
    }
    
    public BlockPlacementHandler getBlockPlacementHandler() {
        return blockPlacementHandler != null ? blockPlacementHandler :
            (blockPlacementHandler = dependencyContainer.resolve(BlockPlacementHandler.class));
    }
    
    public VisualDebugger getScriptDebugger() {
        return visualDebugger != null ? visualDebugger :
            (visualDebugger = dependencyContainer.resolve(VisualDebugger.class));
    }
    
    public ScriptPerformanceMonitor getScriptPerformanceMonitor() {
        return scriptPerformanceMonitor != null ? scriptPerformanceMonitor :
            (scriptPerformanceMonitor = dependencyContainer.resolve(ScriptPerformanceMonitor.class));
    }
    
    public BlockConfigManager getBlockConfigManager() { 
        return blockConfigManager != null ? blockConfigManager :
            (blockConfigManager = dependencyContainer.resolve(BlockConfigManager.class));
    }
    
    public AutoConnectionManager getAutoConnectionManager() {
        return autoConnectionManager != null ? autoConnectionManager :
            (autoConnectionManager = dependencyContainer.resolve(AutoConnectionManager.class));
    }
    
    public DevInventoryManager getDevInventoryManager() {
        return devInventoryManager != null ? devInventoryManager :
            (devInventoryManager = dependencyContainer.resolve(DevInventoryManager.class));
    }
    
    public BlockContainerManager getContainerManager() {
        return containerManager != null ? containerManager :
            (containerManager = dependencyContainer.resolve(BlockContainerManager.class));
    }
    
    public ExecutorEngine getExecutorEngine() {
        return executorEngine != null ? executorEngine :
            (executorEngine = dependencyContainer.resolve(ExecutorEngine.class));
    }
    
    public BlockConfigService getBlockConfigService() {
        return blockConfigService != null ? blockConfigService :
            (blockConfigService = dependencyContainer.resolve(BlockConfigService.class));
    }
    
    public CustomEventManager getCustomEventManager() {
        return customEventManager != null ? customEventManager :
            (customEventManager = dependencyContainer.resolve(CustomEventManager.class));
    }
    
    public EventDataExtractorRegistry getEventDataExtractorRegistry() {
        return eventDataExtractorRegistry != null ? eventDataExtractorRegistry :
            (eventDataExtractorRegistry = dependencyContainer.resolve(EventDataExtractorRegistry.class));
    }
    
    public VisualErrorHandler getVisualErrorHandler() {
        return visualErrorHandler != null ? visualErrorHandler :
            (visualErrorHandler = dependencyContainer.resolve(VisualErrorHandler.class));
    }
    
    public CodeBlockClipboard getCodeBlockClipboard() {
        return codeBlockClipboard != null ? codeBlockClipboard :
            (codeBlockClipboard = dependencyContainer.resolve(CodeBlockClipboard.class));
    }
    
    public BlockGroupManager getBlockGroupManager() {
        return blockGroupManager != null ? blockGroupManager :
            (blockGroupManager = dependencyContainer.resolve(BlockGroupManager.class));
    }
    
    public DevWorldProtectionListener getDevWorldProtectionListener() {
        return devWorldProtectionListener != null ? devWorldProtectionListener :
            (devWorldProtectionListener = dependencyContainer.resolve(DevWorldProtectionListener.class));
    }
    
    // Private initialization methods
    
    private void initializeCoreServices() {
        // Config manager first
        configManager = new com.megacreative.utils.ConfigManager((com.megacreative.MegaCreative) plugin);
        configManager.loadConfig();
        registerService(com.megacreative.utils.ConfigManager.class, configManager);
        
        // Register the existing plugin instance to prevent circular dependency issues
        dependencyContainer.registerSingleton(com.megacreative.MegaCreative.class, (com.megacreative.MegaCreative) plugin);
        
        // Register type mappings for interfaces to prevent DI issues
        dependencyContainer.registerType(com.megacreative.utils.ConfigManager.class, com.megacreative.utils.ConfigManager.class);
        dependencyContainer.registerType(IWorldManager.class, WorldManagerImpl.class);
        dependencyContainer.registerType(IPlayerManager.class, PlayerManagerImpl.class);
        dependencyContainer.registerType(ICodingManager.class, com.megacreative.coding.CodingManagerImpl.class);
        
        // Initialize BlockConfigService early as it's a core service
        this.blockConfigService = new BlockConfigService((com.megacreative.MegaCreative) plugin);
        registerService(BlockConfigService.class, blockConfigService);
        
        // Initialize VariableManager early as it's a core service
        this.variableManager = new VariableManager((com.megacreative.MegaCreative) plugin);
        registerService(VariableManager.class, variableManager);
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
            codingManager = new com.megacreative.coding.CodingManagerImpl((com.megacreative.MegaCreative) plugin, worldManager);
            registerService(ICodingManager.class, codingManager);
        }
        
        // Now update WorldManager with CodingManager dependency
        if (worldManager instanceof WorldManagerImpl) {
            ((WorldManagerImpl) worldManager).setCodingManager(codingManager);
        }
    }
    
    private void initializeImplementationManagers() {
        log.info("Initializing implementation managers...");
        
        // Cast plugin to MegaCreative once
        com.megacreative.MegaCreative megaCreative = (com.megacreative.MegaCreative) plugin;
        
        // Initialize BlockConfiguration with required dependencies
        this.blockConfiguration = new BlockConfiguration(megaCreative);
        registerService(BlockConfiguration.class, blockConfiguration);
        
        // Initialize CodeBlockClipboard
        this.codeBlockClipboard = new CodeBlockClipboard();
        registerService(CodeBlockClipboard.class, codeBlockClipboard);
        
        // Initialize BlockGroupManager with required dependencies
        this.blockGroupManager = new BlockGroupManager(megaCreative, playerManager);
        registerService(BlockGroupManager.class, blockGroupManager);
        
        // Initialize TemplateManager with required dependencies
        this.templateManager = new TemplateManager(megaCreative);
        registerService(TemplateManager.class, templateManager);
        
        // Initialize ScoreboardManager
        this.scoreboardManager = new ScoreboardManager(megaCreative);
        registerService(ScoreboardManager.class, scoreboardManager);
        
        // Initialize TrustedPlayerManager
        this.trustedPlayerManager = new TrustedPlayerManager(megaCreative);
        this.trustedPlayerManagerInterface = trustedPlayerManager; // Store both concrete and interface references
        registerService(TrustedPlayerManager.class, trustedPlayerManager);
        registerService(ITrustedPlayerManager.class, trustedPlayerManager); // Use the same instance for both
        
        // Initialize BlockConfigManager
        this.blockConfigManager = new BlockConfigManager(megaCreative);
        registerService(BlockConfigManager.class, blockConfigManager);
        
        // Initialize DevWorldProtectionListener
        this.devWorldProtectionListener = new DevWorldProtectionListener(
            megaCreative, 
            trustedPlayerManager,
            blockConfigService
        );
        registerService(DevWorldProtectionListener.class, devWorldProtectionListener);
        
        log.info("All implementation managers initialized");
    }
    
    private void initializeCodingServices() {
        // Initialize coding system components
        // Initialize BlockPlacementHandler with just the plugin instance
        this.blockPlacementHandler = new BlockPlacementHandler((com.megacreative.MegaCreative) plugin);
        registerService(BlockPlacementHandler.class, blockPlacementHandler);
        
        scriptDebugger = new VisualDebugger((com.megacreative.MegaCreative) plugin);
        registerService(VisualDebugger.class, scriptDebugger);
        
        autoConnectionManager = new AutoConnectionManager((com.megacreative.MegaCreative) plugin, blockConfigService);
        registerService(AutoConnectionManager.class, autoConnectionManager);
        
        devInventoryManager = new DevInventoryManager((com.megacreative.MegaCreative) plugin);
        registerService(DevInventoryManager.class, devInventoryManager);
        
        // VariableManager was already initialized in core services
        registerService(VariableManager.class, variableManager);
        
        containerManager = new BlockContainerManager((com.megacreative.MegaCreative) plugin);
        registerService(BlockContainerManager.class, containerManager);
        
        executorEngine = new ExecutorEngine((com.megacreative.MegaCreative) plugin, variableManager);
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
        
        // Initialize GUI Manager with required dependencies
        this.guiManager = new GUIManager(playerManager, variableManager);
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
        
        // Initialize DevWorldProtectionListener after BlockConfigService is fully loaded
        if (devWorldProtectionListener != null) {
            devWorldProtectionListener.initializeDynamicAllowedBlocks();
            plugin.getLogger().info("DevWorldProtectionListener initialized with dynamic block list");
        }
        
        // Connect CodeBlockClipboard with BlockPlacementHandler and AutoConnectionManager
        if (codeBlockClipboard != null && blockPlacementHandler != null && autoConnectionManager != null) {
            codeBlockClipboard.setPlacementHandler(blockPlacementHandler);
            codeBlockClipboard.setConnectionManager(autoConnectionManager);
            plugin.getLogger().info("CodeBlockClipboard connected to BlockPlacementHandler and AutoConnectionManager");
        }
    }
    
    // Shutdown methods
    
    private void shutdownCoreServices() {
        log.info("Shutting down core services...");
        
        // Shutdown world manager first to save all worlds
        if (worldManager != null) {
            try {
                worldManager.saveAllWorlds();
                log.info("World manager shut down successfully");
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error shutting down world manager", e);
            }
        }
        
        // Shutdown config manager last to ensure all services can save their configs
        if (configManager != null) {
            try {
                configManager.shutdown();
                log.info("ConfigManager shut down successfully");
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error shutting down ConfigManager", e);
            }
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