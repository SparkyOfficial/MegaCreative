package com.megacreative.core;

import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.ConnectionVisualizer;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.DefaultScriptEngine;
import com.megacreative.coding.ActionFactory;
import com.megacreative.coding.ConditionFactory;
import com.megacreative.coding.BlockLinker;
import com.megacreative.coding.BlockHierarchyManager;
import com.megacreative.coding.WorldCodeRestorer;
import com.megacreative.coding.CodeBlockSignManager;
import com.megacreative.coding.ScriptTriggerManager;
import com.megacreative.coding.ScriptValidator;
import com.megacreative.listeners.*;
import com.megacreative.coding.containers.BlockContainerManager;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.errors.VisualErrorHandler;
import com.megacreative.coding.groups.BlockGroupManager;
import com.megacreative.coding.monitoring.ScriptPerformanceMonitor;
import com.megacreative.coding.events.EventDataExtractorRegistry;
import com.megacreative.interfaces.*;
import com.megacreative.managers.*;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.gui.interactive.InteractiveGUIManager;
import com.megacreative.gui.interactive.ReferenceSystemStyleGUI;
import com.megacreative.gui.coding.EnhancedActionParameterGUI;
import com.megacreative.services.MessagingService;
import com.megacreative.MegaCreative;
import com.megacreative.tools.CodeBlockClipboard;
import com.megacreative.coding.CodingManagerImpl;

import com.megacreative.managers.ReferenceSystemEventManager;
import com.megacreative.utils.ConfigManager;
import com.megacreative.services.CodeCompiler;
import com.megacreative.coding.activators.ActivatorManager;
import com.megacreative.services.RepeatingTaskManager;
import org.bukkit.plugin.Plugin;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central service registry that manages all plugin services and dependencies
 * Replaces the God Object pattern with proper dependency injection
 *
 * Центральный реестр сервисов, который управляет всеми сервисами плагина и зависимостями
 * Заменяет паттерн Бога Объекта на правильное внедрение зависимостей
 *
 * Zentraler Serviceregister, der alle Plugin-Services und Abhängigkeiten verwaltet
 * Ersetzt das God Object-Muster durch ordnungsgemäß Dependency Injection
 */
public class ServiceRegistry implements DependencyContainer.Disposable {
    private static final Logger log = Logger.getLogger(ServiceRegistry.class.getName());
    private final Plugin plugin;
    private final DependencyContainer dependencyContainer;
    
    
    // These fields track initialization state across method calls and cannot be converted to local variables
    // They are required to prevent re-initialization of services and track which initialization steps have been completed
    private boolean coreServicesRegistered = false;
    private boolean managersInitialized = false;
    private boolean codingServicesInitialized = false;
    private boolean newArchitectureServicesInitialized = false;
    private boolean servicesConnected = false;
    
    /**
     * Creates a new service registry
     * @param plugin The plugin instance
     * @param dependencyContainer The dependency container
     */
    public ServiceRegistry(Plugin plugin, DependencyContainer dependencyContainer) {
        this.plugin = plugin;
        this.dependencyContainer = dependencyContainer;
        
        
        this.dependencyContainer.registerSingleton(ServiceRegistry.class, this);
        
        
        registerCoreServices();
        coreServicesRegistered = true;
    }
    
    /**
     * Register core services in the dependency container
     */
    private void registerCoreServices() {
        
        dependencyContainer.registerType(IWorldManager.class, WorldManagerImpl.class);
        dependencyContainer.registerType(GameLoopManager.class, GameLoopManager.class);
        dependencyContainer.registerFactory(TickManager.class, (DependencyContainer.Supplier<TickManager>) () -> new TickManager((MegaCreative) plugin));
        dependencyContainer.registerType(IPlayerManager.class, PlayerManagerImpl.class);
        dependencyContainer.registerType(ITrustedPlayerManager.class, TrustedPlayerManager.class);
        
        
        dependencyContainer.registerFactory(VariableManager.class, (DependencyContainer.Supplier<VariableManager>) () -> new VariableManager((MegaCreative) plugin));
        dependencyContainer.registerFactory(VisualDebugger.class, (DependencyContainer.Supplier<VisualDebugger>) () -> new VisualDebugger((MegaCreative) plugin));
        dependencyContainer.registerFactory(BlockConfigService.class, (DependencyContainer.Supplier<BlockConfigService>) () -> new BlockConfigService((MegaCreative) plugin));
        dependencyContainer.registerFactory(AdvancedFunctionManager.class, (DependencyContainer.Supplier<AdvancedFunctionManager>) () -> new AdvancedFunctionManager((MegaCreative) plugin));
        dependencyContainer.registerFactory(RepeatingTaskManager.class, (DependencyContainer.Supplier<RepeatingTaskManager>) () -> new RepeatingTaskManager((MegaCreative) plugin));
        dependencyContainer.registerFactory(ConfigManager.class, (DependencyContainer.Supplier<ConfigManager>) () -> {
            ConfigManager configManager = new ConfigManager((MegaCreative) plugin);
            configManager.loadConfig();
            return configManager;
        });
        
        dependencyContainer.registerFactory(ActivatorManager.class, (DependencyContainer.Supplier<ActivatorManager>) () -> new ActivatorManager((MegaCreative) plugin));
    }
    
    /**
     * Initialize all services
     */
    public void initializeServices() {
        log.info("Initializing Service Registry...");
        
        try {
            
            initializeCoreServices();
            
            
            initializeManagers();
            managersInitialized = true;
            
            
            initializeImplementationManagers();
            
            
            initializeCodingServices();
            codingServicesInitialized = true;
            
            
            initializeNewArchitectureServices();
            newArchitectureServicesInitialized = true;
            
            log.info("Service Registry initialized successfully!");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Service registry initialization failed", e);
            throw new RuntimeException("Service registry initialization failed", e);
        }
    }
    
    /**
     * Connect services that depend on each other after they are all created
     * This should be called after all services are registered and initialized
     */
    public void connectServices() {
        if (servicesConnected) {
            return; 
        }
        
        try {
            log.info("Connecting services...");
            
            
            ScriptEngine scriptEngine = dependencyContainer.resolve(ScriptEngine.class);
            AdvancedFunctionManager advancedFunctionManager = dependencyContainer.resolve(AdvancedFunctionManager.class);
            
            if (scriptEngine != null && advancedFunctionManager != null) {
                advancedFunctionManager.setScriptEngine(scriptEngine);
                log.info("Connected ScriptEngine to AdvancedFunctionManager");
            } else {
                log.warning("Could not connect ScriptEngine to AdvancedFunctionManager - one or both are null");
                log.info("ScriptEngine: " + (scriptEngine != null));
                log.info("AdvancedFunctionManager: " + (advancedFunctionManager != null));
            }
            
            
            IWorldManager worldManager = dependencyContainer.resolve(IWorldManager.class);
            ICodingManager codingManager = dependencyContainer.resolve(ICodingManager.class);
            
            if (worldManager != null && codingManager != null && worldManager instanceof WorldManagerImpl) {
                ((WorldManagerImpl) worldManager).setCodingManager(codingManager);
                log.info("Connected CodingManager to WorldManager");
            }
            
            
            if (worldManager != null && worldManager instanceof WorldManagerImpl) {
                ((WorldManagerImpl) worldManager).setPlugin(plugin);
                log.info("Connected Plugin to WorldManager");
            }
            
            
            if (worldManager != null) {
                worldManager.initialize();
                log.info("WorldManager initialized");
            }
            
            servicesConnected = true;
            log.info("All services connected successfully!");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to connect services", e);
        }
    }
    
    private void initializeCoreServices() {
        
    }
    
    private void initializeManagers() {
        
    }
    
    private void initializeImplementationManagers() {
        // Register the CodingManagerImpl as a factory since it needs dependencies
        dependencyContainer.registerFactory(ICodingManager.class, (DependencyContainer.Supplier<ICodingManager>) () -> {
            IWorldManager worldManager = dependencyContainer.resolve(IWorldManager.class);
            return new CodingManagerImpl((MegaCreative) plugin, worldManager);
        });
    }
    
    private void initializeCodingServices() {
        // Register coding service mappings
        // BlockPlacementHandler will be created on demand, with lazy initialization of dependencies
        dependencyContainer.registerType(BlockPlacementHandler.class, BlockPlacementHandler.class);
        dependencyContainer.registerType(ConnectionVisualizer.class, ConnectionVisualizer.class);
        // Register BlockLinker as a factory since it needs BlockPlacementHandler as a dependency
        dependencyContainer.registerFactory(BlockLinker.class, (DependencyContainer.Supplier<BlockLinker>) () -> {
            BlockPlacementHandler placementHandler = dependencyContainer.resolve(BlockPlacementHandler.class);
            return new BlockLinker((MegaCreative) plugin, placementHandler);
        });
        // Register BlockHierarchyManager as a factory since it needs BlockPlacementHandler as a dependency
        dependencyContainer.registerFactory(BlockHierarchyManager.class, (DependencyContainer.Supplier<BlockHierarchyManager>) () -> {
            BlockPlacementHandler placementHandler = dependencyContainer.resolve(BlockPlacementHandler.class);
            return new BlockHierarchyManager(placementHandler);
        });
        dependencyContainer.registerType(WorldCodeRestorer.class, WorldCodeRestorer.class);
        dependencyContainer.registerType(CodeBlockSignManager.class, CodeBlockSignManager.class);
        // Register ScriptTriggerManager as a factory since it needs dependencies
        dependencyContainer.registerFactory(ScriptTriggerManager.class, (DependencyContainer.Supplier<ScriptTriggerManager>) () -> {
            IWorldManager worldManager = dependencyContainer.resolve(IWorldManager.class);
            PlayerModeManager playerModeManager = dependencyContainer.resolve(PlayerModeManager.class);
            return new ScriptTriggerManager((MegaCreative) plugin, worldManager, playerModeManager);
        });
        
        // Register interfaces for factories
        dependencyContainer.registerType(com.megacreative.interfaces.IActionFactory.class, ActionFactory.class);
        dependencyContainer.registerType(com.megacreative.interfaces.IConditionFactory.class, ConditionFactory.class);
        
        // Register concrete classes
        ActionFactory actionFactoryInstance = new ActionFactory((MegaCreative) plugin);
        actionFactoryInstance.registerAllActions();
        dependencyContainer.registerSingleton(ActionFactory.class, actionFactoryInstance);
        dependencyContainer.registerSingleton(com.megacreative.interfaces.IActionFactory.class, actionFactoryInstance);

        ConditionFactory conditionFactoryInstance = new ConditionFactory((MegaCreative) plugin);
        conditionFactoryInstance.registerAllConditions();
        dependencyContainer.registerSingleton(ConditionFactory.class, conditionFactoryInstance);
        dependencyContainer.registerSingleton(com.megacreative.interfaces.IConditionFactory.class, conditionFactoryInstance);
        
        
        // Register ScriptValidator as a singleton to consolidate validation surface
        dependencyContainer.registerFactory(ScriptValidator.class, (DependencyContainer.Supplier<ScriptValidator>) () -> {
            BlockConfigService blockConfigService = dependencyContainer.resolve(BlockConfigService.class);
            return new ScriptValidator(blockConfigService);
        });

        // Register ScriptEngine as a factory - this is critical for proper initialization
        dependencyContainer.registerFactory(ScriptEngine.class, (DependencyContainer.Supplier<ScriptEngine>) () -> {
            VariableManager variableManager = dependencyContainer.resolve(VariableManager.class);
            VisualDebugger visualDebugger = dependencyContainer.resolve(VisualDebugger.class);
            BlockConfigService blockConfigService = dependencyContainer.resolve(BlockConfigService.class);
            ScriptValidator scriptValidator = dependencyContainer.resolve(ScriptValidator.class);
            return new DefaultScriptEngine((MegaCreative) plugin, variableManager, visualDebugger, blockConfigService, scriptValidator);
        });
    }
    
    private void initializeNewArchitectureServices() {
        
        dependencyContainer.registerType(CustomEventManager.class, CustomEventManager.class);
        dependencyContainer.registerType(CodeBlockClipboard.class, CodeBlockClipboard.class);
        dependencyContainer.registerType(EventDataExtractorRegistry.class, EventDataExtractorRegistry.class);
        dependencyContainer.registerType(ReferenceSystemEventManager.class, ReferenceSystemEventManager.class);
        dependencyContainer.registerType(CodeCompiler.class, CodeCompiler.class);
        dependencyContainer.registerFactory(GameLoopManager.class, (DependencyContainer.Supplier<GameLoopManager>) () -> {
            ScriptEngine scriptEngine = dependencyContainer.resolve(ScriptEngine.class);
            IWorldManager worldManager = dependencyContainer.resolve(IWorldManager.class);
            return new GameLoopManager((MegaCreative) plugin, worldManager, scriptEngine);
        });
        
        dependencyContainer.registerFactory(InteractiveGUIManager.class, (DependencyContainer.Supplier<InteractiveGUIManager>) () -> 
            new InteractiveGUIManager((MegaCreative) plugin));
        
        dependencyContainer.registerFactory(DevWorldProtectionListener.class, (DependencyContainer.Supplier<DevWorldProtectionListener>) () -> {
            ITrustedPlayerManager trustedPlayerManager = dependencyContainer.resolve(ITrustedPlayerManager.class);
            BlockConfigService blockConfigService = dependencyContainer.resolve(BlockConfigService.class);
            
            
            if (blockConfigService != null && blockConfigService.getCodeBlockMaterials().isEmpty()) {
                log.info("BlockConfigService has empty materials, forcing configuration load");
                blockConfigService.reload();
            }
            
            DevWorldProtectionListener listener = new DevWorldProtectionListener((MegaCreative) plugin, trustedPlayerManager, blockConfigService);
            
            listener.initializeDynamicAllowedBlocks();
            return listener;
        });
        
        dependencyContainer.registerFactory(GUIClickListener.class, (DependencyContainer.Supplier<GUIClickListener>) () -> 
            new GUIClickListener(plugin));
    }
    
    /**
     * Initialize additional services that depend on core services being available
     */
    public void initializeAdditionalServices() {
        log.info("Initializing additional services...");
        
        try {
            
            log.info("Additional services initialized successfully!");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to initialize additional services", e);
        }
    }
    
    /**
     * Gets a service by type with proper type safety
     */
    public <T> T getService(Class<T> serviceType) {
        return dependencyContainer.resolve(serviceType);
    }
    
    /**
     * Checks if a service is registered
     */
    public boolean hasService(Class<?> serviceType) {
        return dependencyContainer.isRegistered(serviceType);
    }
    
    /**
     * Shuts down all services gracefully
     */
    @Override
    public void dispose() {
        log.info("Shutting down MegaCreative services...");
        
        try {
            
            if (dependencyContainer != null) {
                try {
                    dependencyContainer.dispose();
                    log.info("Dependency container disposed successfully");
                } catch (Exception e) {
                    log.log(Level.WARNING, "Error during dependency container disposal", e);
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error during service shutdown", e);
        }
        
        log.info("All services shut down successfully");
    }
    
    
    
    
    public IWorldManager getWorldManager() { 
        return dependencyContainer.resolve(IWorldManager.class);
    }
    
    public IPlayerManager getPlayerManager() { 
        return dependencyContainer.resolve(IPlayerManager.class);
    }
    
    public ICodingManager getCodingManager() {
        return dependencyContainer.resolve(ICodingManager.class);
    }
    
    public ScoreboardManager getScoreboardManager() { 
        return dependencyContainer.resolve(ScoreboardManager.class);
    }
    
    public GameScoreboardManager getGameScoreboardManager() {
        return dependencyContainer.resolve(GameScoreboardManager.class);
    }
    
    public ITrustedPlayerManager getTrustedPlayerManager() { 
        return dependencyContainer.resolve(ITrustedPlayerManager.class);
    }

    public TickManager getTickManager() {
        return dependencyContainer.resolve(TickManager.class);
    }
    
    public GUIManager getGuiManager() {
        return dependencyContainer.resolve(GUIManager.class);
    }
    
    public VariableManager getVariableManager() { 
        return dependencyContainer.resolve(VariableManager.class);
    }
    
    public BlockPlacementHandler getBlockPlacementHandler() {
        return dependencyContainer.resolve(BlockPlacementHandler.class);
    }
    
    public VisualDebugger getScriptDebugger() {
        return dependencyContainer.resolve(VisualDebugger.class);
    }
    
    public ScriptPerformanceMonitor getScriptPerformanceMonitor() {
        return dependencyContainer.resolve(ScriptPerformanceMonitor.class);
    }
    
    public EventDataExtractorRegistry getEventDataExtractorRegistry() {
        return dependencyContainer.resolve(EventDataExtractorRegistry.class);
    }
    
    public BlockContainerManager getBlockContainerManager() {
        return dependencyContainer.resolve(BlockContainerManager.class);
    }
    
    public BlockConfigManager getBlockConfigManager() { 
        return dependencyContainer.resolve(BlockConfigManager.class);
    }
    
    public DevInventoryManager getDevInventoryManager() {
        return dependencyContainer.resolve(DevInventoryManager.class);
    }
    
    public ConfigManager getConfigManager() { 
        return dependencyContainer.resolve(ConfigManager.class);
    }
    
    public GameLoopManager getGameLoopManager() {
        return dependencyContainer.resolve(GameLoopManager.class);
    }
    
    public BlockConfigService getBlockConfigService() {
        return dependencyContainer.resolve(BlockConfigService.class);
    }
    
    public IActionFactory getActionFactory() {
        return dependencyContainer.resolve(IActionFactory.class);
    }
    
    public IConditionFactory getConditionFactory() {
        return dependencyContainer.resolve(IConditionFactory.class);
    }
    
    public RepeatingTaskManager getRepeatingTaskManager() {
        return dependencyContainer.resolve(RepeatingTaskManager.class);
    }
    
    public ScriptEngine getScriptEngine() {
        return dependencyContainer.resolve(ScriptEngine.class);
    }
    
    
    
    
    public DefaultScriptEngine getDefaultScriptEngine() {
        return dependencyContainer.resolve(DefaultScriptEngine.class);
    }
    
    public CustomEventManager getCustomEventManager() {
        return dependencyContainer.resolve(CustomEventManager.class);
    }
    
    public CodeBlockClipboard getCodeBlockClipboard() {
        return dependencyContainer.resolve(CodeBlockClipboard.class);
    }
    
    public BlockGroupManager getBlockGroupManager() {
        return dependencyContainer.resolve(BlockGroupManager.class);
    }
    
    public VisualErrorHandler getVisualErrorHandler() {
        return dependencyContainer.resolve(VisualErrorHandler.class);
    }
    
    public EnemyPlayerRestrictionManager getEnemyPlayerRestrictionManager() {
        return dependencyContainer.resolve(EnemyPlayerRestrictionManager.class);
    }
    
    public PlayerModeManager getPlayerModeManager() {
        return dependencyContainer.resolve(PlayerModeManager.class);
    }
    
    public MessagingService getMessagingService() {
        return dependencyContainer.resolve(MessagingService.class);
    }
    
    public InteractiveGUIManager getInteractiveGUIManager() {
        return dependencyContainer.resolve(InteractiveGUIManager.class);
    }
    
    public ReferenceSystemStyleGUI getReferenceSystemStyleGUI() {
        return dependencyContainer.resolve(ReferenceSystemStyleGUI.class);
    }
    
    public EnhancedActionParameterGUI getEnhancedActionParameterGUI() {
        return dependencyContainer.resolve(EnhancedActionParameterGUI.class);
    }
    
    public ReferenceSystemEventManager getReferenceSystemEventManager() {
        return dependencyContainer.resolve(ReferenceSystemEventManager.class);
    }
    
    public AdvancedFunctionManager getAdvancedFunctionManager() {
        return dependencyContainer.resolve(AdvancedFunctionManager.class);
    }
    
    public CodeCompiler getCodeCompiler() {
        return dependencyContainer.resolve(CodeCompiler.class);
    }
    
    
    
    
    public ConnectionVisualizer getConnectionVisualizer() { 
        return dependencyContainer.resolve(ConnectionVisualizer.class);
    }
    
    public BlockLinker getBlockLinker() {
        return dependencyContainer.resolve(BlockLinker.class);
    }
    
    public BlockHierarchyManager getBlockHierarchyManager() {
        return dependencyContainer.resolve(BlockHierarchyManager.class);
    }
    
    public WorldCodeRestorer getWorldCodeRestorer() {
        return dependencyContainer.resolve(WorldCodeRestorer.class);
    }
    
    public CodeBlockSignManager getCodeBlockSignManager() {
        return dependencyContainer.resolve(CodeBlockSignManager.class);
    }
    
    public ScriptTriggerManager getScriptTriggerManager() {
        return dependencyContainer.resolve(ScriptTriggerManager.class);
    }
    
    public ActivatorManager getActivatorManager() {
        return dependencyContainer.resolve(ActivatorManager.class);
    }
}