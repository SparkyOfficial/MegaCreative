package com.megacreative.core;

import com.megacreative.coding.AutoConnectionManager;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.DefaultScriptEngine;
import com.megacreative.coding.ActionFactory;
import com.megacreative.coding.ConditionFactory;
import com.megacreative.coding.events.PlayerEventsListener;
import com.megacreative.coding.containers.BlockContainerManager;
import com.megacreative.coding.executors.ExecutorEngine;
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
import com.megacreative.gui.interactive.FrameLandStyleGUI;
import com.megacreative.gui.coding.EnhancedActionParameterGUI;
import com.megacreative.MegaCreative;
import com.megacreative.tools.CodeBlockClipboard;
// 🎆 FrameLand-style comprehensive events
import com.megacreative.managers.FrameLandEventManager;
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
    private GameScoreboardManager gameScoreboardManager;
    private TrustedPlayerManager trustedPlayerManager;
    private ITrustedPlayerManager trustedPlayerManagerInterface;
    private GUIManager guiManager;
    private BlockConfigManager blockConfigManager;
    private ExecutorEngine executorEngine;
    
    // Coding system services
    private BlockPlacementHandler blockPlacementHandler;
    private VisualDebugger visualDebugger;
    private AutoConnectionManager autoConnectionManager;
    private DevInventoryManager devInventoryManager;
    private VariableManager variableManager;
    private BlockContainerManager containerManager;
    private final ScriptEngine scriptEngine;
    
    // New architecture services
    private BlockConfigService blockConfigService;
    private ActionFactory actionFactory;
    private ConditionFactory conditionFactory;
    private CustomEventManager customEventManager;
    private EventDataExtractorRegistry eventDataExtractorRegistry;
    private VisualErrorHandler visualErrorHandler;
    private CodeBlockClipboard codeBlockClipboard;
    private BlockGroupManager blockGroupManager;
    private DevWorldProtectionListener devWorldProtectionListener;
    private PlayerEventsListener playerEventsListener;
    private ScriptPerformanceMonitor scriptPerformanceMonitor;
    private FunctionManager functionManager;
    private AdvancedFunctionManager advancedFunctionManager;
    
    // 🎆 FrameLand: Interactive GUI System
    private InteractiveGUIManager interactiveGUIManager;
    private FrameLandStyleGUI frameLandStyleGUI;
    private EnhancedActionParameterGUI enhancedActionParameterGUI;
    
    // 🎆 FrameLand-style comprehensive event system
    private FrameLandEventManager frameLandEventManager;

    public ServiceRegistry(Plugin plugin, DependencyContainer dependencyContainer) {
        this.plugin = plugin;
        this.dependencyContainer = dependencyContainer;
        
        // Initialize core services first
        this.variableManager = new VariableManager((MegaCreative) plugin);
        this.visualDebugger = new VisualDebugger((MegaCreative) plugin);
        this.blockConfigService = new BlockConfigService((MegaCreative) plugin);
        
        // Initialize factories with dependency container
        this.actionFactory = new ActionFactory(dependencyContainer);
        this.conditionFactory = new ConditionFactory();
        
        // Initialize ScriptEngine with its dependencies
        this.scriptEngine = new DefaultScriptEngine(
            (MegaCreative) plugin, 
            variableManager, 
            visualDebugger,
            blockConfigService
        );
        
        // Register services
        registerService(BlockConfigService.class, blockConfigService);
        registerService(ActionFactory.class, actionFactory);
        registerService(ConditionFactory.class, conditionFactory);
        initializeScriptEngine();
    }
    
    private void initializeScriptEngine() {
        // Initialize FunctionManager
        this.functionManager = new FunctionManager((MegaCreative) plugin);
        registerService(FunctionManager.class, functionManager);
        
        // 🎆 FrameLand: Initialize Advanced Function Manager
        this.advancedFunctionManager = new AdvancedFunctionManager((MegaCreative) plugin);
        registerService(AdvancedFunctionManager.class, advancedFunctionManager);
        
        // Register core services
        registerService(VariableManager.class, variableManager);
        registerService(VisualDebugger.class, visualDebugger);
        
        // Initialize ScriptEngine with required dependencies
        if (scriptEngine instanceof DefaultScriptEngine) {
            DefaultScriptEngine defaultEngine = (DefaultScriptEngine) scriptEngine;
            // Initialize the engine
            defaultEngine.initialize();
            
            log.info("ScriptEngine initialized with " + 
                    defaultEngine.getActionCount() + " actions and " +
                    defaultEngine.getConditionCount() + " conditions");
        }
        
        // Register ScriptEngine services
        registerService(ScriptEngine.class, scriptEngine);
        registerService(DefaultScriptEngine.class, (DefaultScriptEngine) scriptEngine);
    }
    
    /**
     * Initializes all services in the correct order
     */
    public void initializeServices() {
        log.info("Initializing MegaCreative services...");
        
        try {
            // 1. Core services first (config, database, etc.)
            initializeCoreServices();
            
            // 2. Interface-based managers (world, player, coding)
            initializeManagers();
            
            // 3. Implementation managers (templates, scoreboard, etc.)
            initializeImplementationManagers();
            
            // 4. Coding system services (block placement, debugger, etc.)
            initializeCodingServices();
            
            // 5. New architecture services (visual programming, collaboration, etc.)
            initializeNewArchitectureServices();
            
            // 6. Register all services in DI container for dependency injection
            registerServicesInDI();
            
            // 7. Initialize services that need post-construction setup
            postInitialize();
            
            log.info(String.format("Successfully initialized %d services", services.size()));
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to initialize services: " + e.getMessage(), e);
            throw new RuntimeException("Service initialization failed: " + e.getMessage(), e);
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
        
        // 🎆 FRAMELAND: Shutdown comprehensive event manager
        if (frameLandEventManager != null) {
            frameLandEventManager.shutdown();
        }
        
        // 🎆 FRAMELAND: Shutdown interactive GUI system
        if (interactiveGUIManager != null) {
            interactiveGUIManager.shutdown();
        }
        
        // 🎆 FRAMELAND: Shutdown advanced function manager
        if (advancedFunctionManager != null) {
            advancedFunctionManager.shutdown();
        }
        
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
    
    public IWorldManager getWorldManager() { 
        return worldManager != null ? worldManager : 
            (worldManager = dependencyContainer.resolve(IWorldManager.class));
    }
    
    public IPlayerManager getPlayerManager() { 
        return playerManager != null ? playerManager : 
            (playerManager = dependencyContainer.resolve(IPlayerManager.class));
    }
    
    public ICodingManager getCodingManager() {
        if (codingManager == null) {
            // Get required dependencies
            IWorldManager worldManager = getWorldManager();
            ScriptEngine scriptEngine = getService(ScriptEngine.class);
            
            // Create and initialize CodingManager
            this.codingManager = new com.megacreative.coding.CodingManagerImpl((MegaCreative) plugin, worldManager);
            registerService(ICodingManager.class, codingManager);
            
            // Verify ScriptEngine is properly set
            if (scriptEngine == null) {
                log.warning("ScriptEngine is not available when initializing CodingManager");
            } else {
                log.info("CodingManager initialized with ScriptEngine: " + scriptEngine.getClass().getSimpleName());
            }
        }
        return codingManager;
    }
    
    public TemplateManager getTemplateManager() { 
        return templateManager != null ? templateManager : 
            (templateManager = dependencyContainer.resolve(TemplateManager.class));
    }
    
    public ScoreboardManager getScoreboardManager() { 
        return scoreboardManager != null ? scoreboardManager : 
            (scoreboardManager = dependencyContainer.resolve(ScoreboardManager.class));
    }
    
    public GameScoreboardManager getGameScoreboardManager() {
        return gameScoreboardManager;
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
        return variableManager;
    }
    
    public BlockPlacementHandler getBlockPlacementHandler() {
        return blockPlacementHandler != null ? blockPlacementHandler :
            (blockPlacementHandler = dependencyContainer.resolve(BlockPlacementHandler.class));
    }
    
    /**
     * Gets the VisualDebugger instance
     * @return VisualDebugger instance
     */
    public VisualDebugger getScriptDebugger() {
        return visualDebugger != null ? visualDebugger :
            (visualDebugger = dependencyContainer.resolve(VisualDebugger.class));
    }
    
    public ScriptPerformanceMonitor getScriptPerformanceMonitor() {
        return scriptPerformanceMonitor != null ? scriptPerformanceMonitor :
            (scriptPerformanceMonitor = dependencyContainer.resolve(ScriptPerformanceMonitor.class));
    }
    
    public EventDataExtractorRegistry getEventDataExtractorRegistry() {
        if (eventDataExtractorRegistry == null) {
            eventDataExtractorRegistry = new EventDataExtractorRegistry();
            registerService(EventDataExtractorRegistry.class, eventDataExtractorRegistry);
        }
        return eventDataExtractorRegistry;
    }
    
    public BlockContainerManager getBlockContainerManager() {
        return containerManager != null ? containerManager :
            (containerManager = dependencyContainer.resolve(BlockContainerManager.class));
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
    
    public com.megacreative.utils.ConfigManager getConfigManager() { 
        if (configManager == null) {
            configManager = dependencyContainer.resolve(com.megacreative.utils.ConfigManager.class);
            registerService(com.megacreative.utils.ConfigManager.class, configManager);
        }
        return configManager;
    }
    
    // Getters for new services
    public BlockConfigService getBlockConfigService() {
        return blockConfigService;
    }
    
    public ActionFactory getActionFactory() {
        return actionFactory;
    }
    
    public ConditionFactory getConditionFactory() {
        return conditionFactory;
    }
    
    public PlayerEventsListener getPlayerEventsListener() {
        if (playerEventsListener == null) {
            playerEventsListener = new PlayerEventsListener((MegaCreative) plugin);
            registerService(PlayerEventsListener.class, playerEventsListener);
        }
        return playerEventsListener;
    }
    
    public CustomEventManager getCustomEventManager() {
        if (customEventManager == null) {
            customEventManager = new CustomEventManager((MegaCreative) plugin);
            registerService(CustomEventManager.class, customEventManager);
        }
        return customEventManager;
    }
    
    public DevWorldProtectionListener getDevWorldProtectionListener() {
        if (devWorldProtectionListener == null) {
            // Get required dependencies
            ITrustedPlayerManager trustedPlayerManager = getTrustedPlayerManager();
            BlockConfigService blockConfigService = getBlockConfigService();
            
            this.devWorldProtectionListener = new DevWorldProtectionListener((MegaCreative) plugin, 
                (com.megacreative.managers.TrustedPlayerManager) trustedPlayerManager, 
                blockConfigService);
            registerService(DevWorldProtectionListener.class, devWorldProtectionListener);
        }
        return devWorldProtectionListener;
    }
    
    public CodeBlockClipboard getCodeBlockClipboard() {
        if (codeBlockClipboard == null) {
            this.codeBlockClipboard = new CodeBlockClipboard();
            registerService(CodeBlockClipboard.class, codeBlockClipboard);
        }
        return codeBlockClipboard;
    }
    
    public com.megacreative.services.FunctionManager getFunctionManager() {
        if (functionManager == null) {
            this.functionManager = new com.megacreative.services.FunctionManager((MegaCreative) plugin);
            registerService(com.megacreative.services.FunctionManager.class, functionManager);
        }
        return functionManager;
    }
    
    // 🎆 FrameLand: Get Advanced Function Manager
    public AdvancedFunctionManager getAdvancedFunctionManager() {
        if (advancedFunctionManager == null) {
            this.advancedFunctionManager = new AdvancedFunctionManager((MegaCreative) plugin);
            registerService(AdvancedFunctionManager.class, advancedFunctionManager);
        }
        return advancedFunctionManager;
    }
    
    // 🎆 FRAMELAND: Get comprehensive event manager
    public FrameLandEventManager getFrameLandEventManager() {
        if (frameLandEventManager == null) {
            this.frameLandEventManager = new FrameLandEventManager((MegaCreative) plugin);
            registerService(FrameLandEventManager.class, frameLandEventManager);
        }
        return frameLandEventManager;
    }
    
    // 🎆 FrameLand: Get Interactive GUI Manager
    public InteractiveGUIManager getInteractiveGUIManager() {
        if (interactiveGUIManager == null) {
            this.interactiveGUIManager = new InteractiveGUIManager((MegaCreative) plugin);
            registerService(InteractiveGUIManager.class, interactiveGUIManager);
        }
        return interactiveGUIManager;
    }
    
    // 🎆 FrameLand: Get FrameLand Style GUI
    public FrameLandStyleGUI getFrameLandStyleGUI() {
        if (frameLandStyleGUI == null) {
            this.frameLandStyleGUI = new FrameLandStyleGUI((MegaCreative) plugin);
            registerService(FrameLandStyleGUI.class, frameLandStyleGUI);
        }
        return frameLandStyleGUI;
    }
    
    // 🎆 FrameLand: Get Enhanced Action Parameter GUI
    public EnhancedActionParameterGUI getEnhancedActionParameterGUI() {
        if (enhancedActionParameterGUI == null) {
            this.enhancedActionParameterGUI = new EnhancedActionParameterGUI((MegaCreative) plugin);
            registerService(EnhancedActionParameterGUI.class, enhancedActionParameterGUI);
        }
        return enhancedActionParameterGUI;
    }
    
    private void initializeCoreServices() {
        // Initialize core services like ConfigManager
        if (configManager == null) {
            configManager = new com.megacreative.utils.ConfigManager((MegaCreative) plugin);
            configManager.loadConfig(); // Load the configuration immediately after creation
            registerService(com.megacreative.utils.ConfigManager.class, configManager);
        }
    }
    
    private void initializeManagers() {
        // Initialize interface-based managers
        if (playerManager == null) {
            this.playerManager = new com.megacreative.managers.PlayerManagerImpl((MegaCreative) plugin);
            registerService(IPlayerManager.class, playerManager);
        }
        
        if (worldManager == null) {
            // Use the constructor that accepts ConfigManager and set codingManager later
            this.worldManager = new com.megacreative.managers.WorldManagerImpl(getConfigManager());
            // Set the coding manager after it's available
            if (worldManager instanceof com.megacreative.managers.WorldManagerImpl) {
                ((com.megacreative.managers.WorldManagerImpl) worldManager).setCodingManager(getCodingManager());
            }
            registerService(IWorldManager.class, worldManager);
        }
        
        if (trustedPlayerManager == null) {
            this.trustedPlayerManager = new com.megacreative.managers.TrustedPlayerManager((MegaCreative) plugin);
            registerService(ITrustedPlayerManager.class, trustedPlayerManager);
            // Keep reference to implementation for type casting
            this.trustedPlayerManagerInterface = trustedPlayerManager;
        }
    }
    
    private void initializeImplementationManagers() {
        // Initialize implementation managers
        if (gameScoreboardManager == null) {
            this.gameScoreboardManager = new GameScoreboardManager((MegaCreative) plugin);
            registerService(GameScoreboardManager.class, gameScoreboardManager);
        }
    }
    
    private void initializeCodingServices() {
        // Initialize coding system services
        if (blockPlacementHandler == null) {
            this.blockPlacementHandler = new BlockPlacementHandler((MegaCreative) plugin);
            registerService(BlockPlacementHandler.class, blockPlacementHandler);
        }
    }
    
    private void initializeNewArchitectureServices() {
        // Initialize BlockConfigService first as it's a core dependency
        if (blockConfigService == null) {
            this.blockConfigService = new BlockConfigService((MegaCreative) plugin);
            registerService(BlockConfigService.class, blockConfigService);
        }
        
        // Load block configurations
        blockConfigService.reload();
        
        // Initialize GUI Manager with required dependencies
        if (guiManager == null) {
            this.guiManager = new GUIManager(getPlayerManager(), getVariableManager());
            registerService(GUIManager.class, guiManager);
        }
        
        // Initialize PlayerEventsListener
        if (playerEventsListener == null) {
            this.playerEventsListener = new PlayerEventsListener((MegaCreative) plugin);
            registerService(PlayerEventsListener.class, playerEventsListener);
        }
        
        // Initialize CustomEventManager
        if (customEventManager == null) {
            this.customEventManager = new CustomEventManager((MegaCreative) plugin);
            registerService(CustomEventManager.class, customEventManager);
        }
        
        // Initialize DevWorldProtectionListener
        if (devWorldProtectionListener == null) {
            this.devWorldProtectionListener = new DevWorldProtectionListener(
                (MegaCreative) plugin,
                (com.megacreative.managers.TrustedPlayerManager) getTrustedPlayerManager(),
                getBlockConfigService()
            );
            registerService(DevWorldProtectionListener.class, devWorldProtectionListener);
        }
        
        // Initialize CodeBlockClipboard
        if (codeBlockClipboard == null) {
            this.codeBlockClipboard = new CodeBlockClipboard();
            registerService(CodeBlockClipboard.class, codeBlockClipboard);
        }
        
        // Initialize EventDataExtractorRegistry
        if (eventDataExtractorRegistry == null) {
            this.eventDataExtractorRegistry = new EventDataExtractorRegistry();
            registerService(EventDataExtractorRegistry.class, eventDataExtractorRegistry);
        }
        
        // 🎆 FRAMELAND: Initialize comprehensive event manager
        if (frameLandEventManager == null) {
            this.frameLandEventManager = new FrameLandEventManager((MegaCreative) plugin);
            registerService(FrameLandEventManager.class, frameLandEventManager);
        }
        
        // 🎆 FRAMELAND: Initialize interactive GUI system
        if (interactiveGUIManager == null) {
            this.interactiveGUIManager = new InteractiveGUIManager((MegaCreative) plugin);
            registerService(InteractiveGUIManager.class, interactiveGUIManager);
        }
        
        if (frameLandStyleGUI == null) {
            this.frameLandStyleGUI = new FrameLandStyleGUI((MegaCreative) plugin);
            registerService(FrameLandStyleGUI.class, frameLandStyleGUI);
        }
        
        if (enhancedActionParameterGUI == null) {
            this.enhancedActionParameterGUI = new EnhancedActionParameterGUI((MegaCreative) plugin);
            registerService(EnhancedActionParameterGUI.class, enhancedActionParameterGUI);
        }
        
        log.info("BlockConfigService initialized with " + blockConfigService.getAllBlockConfigs().size() + " block configurations");
        log.info("🎆 FrameLand Event Manager initialized with comprehensive event coverage");
        log.info("🎆 FrameLand Advanced Execution Engine integrated with DefaultScriptEngine");
        log.info("🎆 FrameLand Interactive GUI System initialized with 6 element types");
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
        if (worldManager instanceof com.megacreative.managers.WorldManagerImpl) {
            ((com.megacreative.managers.WorldManagerImpl) worldManager).initialize();
        }
    }
}