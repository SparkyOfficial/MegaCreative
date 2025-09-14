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
import com.megacreative.gui.interactive.ReferenceSystemStyleGUI;
import com.megacreative.gui.coding.EnhancedActionParameterGUI;
import com.megacreative.MegaCreative;
import com.megacreative.tools.CodeBlockClipboard;
// 🎆 Reference system-style comprehensive events
import com.megacreative.managers.ReferenceSystemEventManager;
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
    private com.megacreative.services.CodeCompiler codeCompiler;
    
    // 🎆 Reference system: Interactive GUI System
    private InteractiveGUIManager interactiveGUIManager;
    private ReferenceSystemStyleGUI referenceSystemStyleGUI;
    private EnhancedActionParameterGUI enhancedActionParameterGUI;
    
    // 🎆 Reference system-style comprehensive event system
    private ReferenceSystemEventManager referenceSystemEventManager;
    
    // Enemy player restriction system
    private EnemyPlayerRestrictionManager enemyPlayerRestrictionManager;

    public ServiceRegistry(Plugin plugin, DependencyContainer dependencyContainer) {
        this.plugin = plugin;
        this.dependencyContainer = dependencyContainer;
        
        // Initialize core services first (services without dependencies)
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
        
        // IMPORTANT: Register key services BEFORE creating dependent services
        registerService(BlockConfigService.class, blockConfigService);
        registerService(VariableManager.class, variableManager);
        registerService(VisualDebugger.class, visualDebugger);
        registerService(ActionFactory.class, actionFactory);
        registerService(ConditionFactory.class, conditionFactory);
        registerService(ScriptEngine.class, scriptEngine);
        registerService(DefaultScriptEngine.class, (DefaultScriptEngine) scriptEngine);
        
        // Now initialize services that depend on the above
        initializeDependentServices();
    }
    
    private void initializeDependentServices() {
        // Initialize FunctionManager first
        this.functionManager = new FunctionManager((MegaCreative) plugin);
        registerService(FunctionManager.class, functionManager);
        
        // 🎆 Reference system: Now it's safe to initialize Advanced Function Manager
        // because ScriptEngine is already registered
        this.advancedFunctionManager = new AdvancedFunctionManager((MegaCreative) plugin);
        registerService(AdvancedFunctionManager.class, advancedFunctionManager);
        
        // Set ScriptEngine in AdvancedFunctionManager if it wasn't available during construction
        if (!advancedFunctionManager.isScriptEngineAvailable()) {
            advancedFunctionManager.setScriptEngine(scriptEngine);
        }
        
        // Initialize ScriptEngine
        if (scriptEngine instanceof DefaultScriptEngine) {
            DefaultScriptEngine defaultEngine = (DefaultScriptEngine) scriptEngine;
            // Initialize the engine
            defaultEngine.initialize();
            
            log.info("ScriptEngine initialized with " + 
                    defaultEngine.getActionCount() + " actions and " +
                    defaultEngine.getConditionCount() + " conditions");
        }
        
        log.info(" YYS Advanced Function Manager initialized after ScriptEngine registration");
    }
    
    /**
     * 🎆 Reference system: Initialize all core services in correct order
     * This method sets up the dependency injection container and registers all services
     * Services are initialized in dependency order to prevent circular dependencies
     */
    public void initializeServices() {
        plugin.getLogger().info(" YYS Initializing Service Registry...");
        
        try {
            // Initialize core services first (services without dependencies)
            // Initialize ConfigManager first as it's needed by WorldManagerImpl
            if (configManager == null) {
                configManager = new com.megacreative.utils.ConfigManager((MegaCreative) plugin);
                configManager.loadConfig(); // Load the configuration immediately after creation
                registerService(com.megacreative.utils.ConfigManager.class, configManager);
            }
            
            // Core managers first (minimal dependencies)
            // Use the constructor that accepts ConfigManager to avoid circular dependency
            WorldManagerImpl worldManagerImpl = new com.megacreative.managers.WorldManagerImpl(configManager);
            worldManagerImpl.setPlugin(plugin); // Set the plugin instance
            this.worldManager = worldManagerImpl;
            registerService(com.megacreative.managers.WorldManagerImpl.class, worldManagerImpl);
            registerService(com.megacreative.interfaces.IWorldManager.class, worldManagerImpl);
            
            // Register interface-to-implementation mapping in DependencyContainer
            dependencyContainer.registerType(com.megacreative.interfaces.IWorldManager.class, com.megacreative.managers.WorldManagerImpl.class);
            
            PlayerManagerImpl playerManagerImpl = new com.megacreative.managers.PlayerManagerImpl((MegaCreative) plugin);
            this.playerManager = playerManagerImpl;
            registerService(com.megacreative.managers.PlayerManagerImpl.class, playerManagerImpl);
            registerService(com.megacreative.interfaces.IPlayerManager.class, playerManagerImpl);
            
            // Register interface-to-implementation mapping in DependencyContainer
            dependencyContainer.registerType(com.megacreative.interfaces.IPlayerManager.class, com.megacreative.managers.PlayerManagerImpl.class);
            
            this.trustedPlayerManager = new com.megacreative.managers.TrustedPlayerManager((MegaCreative) plugin);
            registerService(com.megacreative.managers.TrustedPlayerManager.class, trustedPlayerManager);
            // Keep reference to implementation for type casting
            this.trustedPlayerManagerInterface = trustedPlayerManager;
            
            // Register interface-to-implementation mapping in DependencyContainer
            dependencyContainer.registerType(com.megacreative.interfaces.ITrustedPlayerManager.class, com.megacreative.managers.TrustedPlayerManager.class);
            
            // Initialize world manager early
            if (worldManagerImpl != null) {
                // Set the coding manager after it's available through lazy initialization
                worldManagerImpl.setCodingManager(getCodingManager());
                worldManagerImpl.initialize();
            }
            
            // Managers with minimal dependencies
            this.devInventoryManager = new DevInventoryManager((MegaCreative) plugin);
            registerService(DevInventoryManager.class, devInventoryManager);
            
            // 🎆 Reference system: Now it's safe to initialize Advanced Function Manager
            this.advancedFunctionManager = new AdvancedFunctionManager((MegaCreative) plugin);
            registerService(AdvancedFunctionManager.class, advancedFunctionManager);
            
            // Core handlers (depend on managers)
            this.blockPlacementHandler = new BlockPlacementHandler((MegaCreative) plugin);
            registerService(BlockPlacementHandler.class, blockPlacementHandler);
            
            this.autoConnectionManager = new AutoConnectionManager((MegaCreative) plugin, blockConfigService);
            registerService(AutoConnectionManager.class, autoConnectionManager);
            
            // 🎆 Reference system-style comprehensive event system
            this.referenceSystemEventManager = new ReferenceSystemEventManager((MegaCreative) plugin);
            registerService(ReferenceSystemEventManager.class, referenceSystemEventManager);
            
            // Compiler service
            this.codeCompiler = new com.megacreative.services.CodeCompiler((MegaCreative) plugin);
            registerService(com.megacreative.services.CodeCompiler.class, codeCompiler);
            
            plugin.getLogger().info(" YYS Service Registry initialized successfully!");
            
        } catch (Exception e) {
            plugin.getLogger().severe(" YYS Failed to initialize Service Registry: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Service registry initialization failed", e);
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
        if (referenceSystemEventManager != null) {
            referenceSystemEventManager.shutdown();
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
            
            // Set the coding manager in WorldManagerImpl
            if (worldManager instanceof WorldManagerImpl) {
                ((WorldManagerImpl) worldManager).setCodingManager(codingManager);
            }
            
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
    
    // 🎆 Reference system: Get Interactive GUI Manager
    public InteractiveGUIManager getInteractiveGUIManager() {
        if (interactiveGUIManager == null) {
            this.interactiveGUIManager = new InteractiveGUIManager((MegaCreative) plugin);
            registerService(InteractiveGUIManager.class, interactiveGUIManager);
        }
        return interactiveGUIManager;
    }
    
    // 🎆 Reference system: Get Reference System Style GUI
    public ReferenceSystemStyleGUI getReferenceSystemStyleGUI() {
        if (referenceSystemStyleGUI == null) {
            this.referenceSystemStyleGUI = new ReferenceSystemStyleGUI((MegaCreative) plugin);
            registerService(ReferenceSystemStyleGUI.class, referenceSystemStyleGUI);
        }
        return referenceSystemStyleGUI;
    }
    
    // 🎆 Reference system: Get Enhanced Action Parameter GUI
    public EnhancedActionParameterGUI getEnhancedActionParameterGUI() {
        if (enhancedActionParameterGUI == null) {
            this.enhancedActionParameterGUI = new EnhancedActionParameterGUI((MegaCreative) plugin);
            registerService(EnhancedActionParameterGUI.class, enhancedActionParameterGUI);
        }
        return enhancedActionParameterGUI;
    }
    
    // Get CodeCompiler service
    public com.megacreative.services.CodeCompiler getCodeCompiler() {
        if (codeCompiler == null) {
            this.codeCompiler = new com.megacreative.services.CodeCompiler((MegaCreative) plugin);
            registerService(com.megacreative.services.CodeCompiler.class, codeCompiler);
        }
        return codeCompiler;
    }
    
    // Get EnemyPlayerRestrictionManager service
    public EnemyPlayerRestrictionManager getEnemyPlayerRestrictionManager() {
        if (enemyPlayerRestrictionManager == null) {
            this.enemyPlayerRestrictionManager = new EnemyPlayerRestrictionManager((MegaCreative) plugin);
            registerService(EnemyPlayerRestrictionManager.class, enemyPlayerRestrictionManager);
        }
        return enemyPlayerRestrictionManager;
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
            PlayerManagerImpl playerManagerImpl = new com.megacreative.managers.PlayerManagerImpl((MegaCreative) plugin);
            this.playerManager = playerManagerImpl;
            registerService(IPlayerManager.class, playerManagerImpl);
            dependencyContainer.registerType(com.megacreative.interfaces.IPlayerManager.class, com.megacreative.managers.PlayerManagerImpl.class);
        }
        
        if (worldManager == null) {
            // Use the constructor that accepts ConfigManager and set codingManager later
            WorldManagerImpl worldManagerImpl = new com.megacreative.managers.WorldManagerImpl(getConfigManager());
            this.worldManager = worldManagerImpl;
            // Set the coding manager after it's available
            if (worldManagerImpl != null) {
                worldManagerImpl.setCodingManager(getCodingManager());
            }
            registerService(IWorldManager.class, worldManagerImpl);
            dependencyContainer.registerType(com.megacreative.interfaces.IWorldManager.class, com.megacreative.managers.WorldManagerImpl.class);
        }
        
        if (trustedPlayerManager == null) {
            this.trustedPlayerManager = new com.megacreative.managers.TrustedPlayerManager((MegaCreative) plugin);
            registerService(ITrustedPlayerManager.class, trustedPlayerManager);
            dependencyContainer.registerType(com.megacreative.interfaces.ITrustedPlayerManager.class, com.megacreative.managers.TrustedPlayerManager.class);
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
        if (referenceSystemEventManager == null) {
            this.referenceSystemEventManager = new ReferenceSystemEventManager((MegaCreative) plugin);
            registerService(ReferenceSystemEventManager.class, referenceSystemEventManager);
        }
        
        // Initialize Enemy Player Restriction Manager
        if (enemyPlayerRestrictionManager == null) {
            this.enemyPlayerRestrictionManager = new EnemyPlayerRestrictionManager((MegaCreative) plugin);
            registerService(EnemyPlayerRestrictionManager.class, enemyPlayerRestrictionManager);
        }
        
        log.info("BlockConfigService initialized with " + blockConfigService.getAllBlockConfigs().size() + " block configurations");
        log.info(" YYS Reference System Event Manager initialized with comprehensive event coverage");
        log.info(" YYS Reference System Advanced Execution Engine integrated with DefaultScriptEngine");
        log.info(" YYS Reference System Interactive GUI System initialized with 6 element types");
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