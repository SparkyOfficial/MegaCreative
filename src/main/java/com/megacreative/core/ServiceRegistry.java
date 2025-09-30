package com.megacreative.core;

import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.ConnectionVisualizer;
import com.megacreative.coding.ScriptCompiler;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.DefaultScriptEngine;
import com.megacreative.coding.ActionFactory;
import com.megacreative.coding.ConditionFactory;
import com.megacreative.coding.BlockLinker;
import com.megacreative.coding.BlockHierarchyManager;
import com.megacreative.coding.WorldCodeRestorer;
import com.megacreative.coding.CodeBlockSignManager;
import com.megacreative.coding.ScriptTriggerManager;
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
// 🎆 Reference system-style comprehensive events
import com.megacreative.managers.ReferenceSystemEventManager;
import com.megacreative.utils.ConfigManager;
import com.megacreative.services.CodeCompiler;
import com.megacreative.coding.activators.ActivatorManager;
import com.megacreative.services.RepeatingTaskManager;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.ConcurrentHashMap;
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
    
    // Track initialization phases
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
        
        // Register this service registry in the container
        this.dependencyContainer.registerSingleton(ServiceRegistry.class, this);
        
        // Register core services
        registerCoreServices();
        coreServicesRegistered = true;
    }
    
    /**
     * Register core services in the dependency container
     */
    private void registerCoreServices() {
        // Register service registry mappings
        dependencyContainer.registerType(IWorldManager.class, WorldManagerImpl.class);
        dependencyContainer.registerType(IPlayerManager.class, PlayerManagerImpl.class);
        dependencyContainer.registerType(ITrustedPlayerManager.class, TrustedPlayerManager.class);
        
        // Register factory functions for services that need plugin instance
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
        // Register ActivatorManager
        dependencyContainer.registerFactory(ActivatorManager.class, (DependencyContainer.Supplier<ActivatorManager>) () -> new ActivatorManager((MegaCreative) plugin));
    }
    
    /**
     * Initialize all services
     */
    public void initializeServices() {
        log.info("Initializing Service Registry...");
        
        try {
            // Initialize core services first
            initializeCoreServices();
            
            // Initialize managers
            initializeManagers();
            managersInitialized = true;
            
            // Initialize implementation managers
            initializeImplementationManagers();
            
            // Initialize coding services
            initializeCodingServices();
            codingServicesInitialized = true;
            
            // Initialize new architecture services
            initializeNewArchitectureServices();
            newArchitectureServicesInitialized = true;
            
            log.info("Service Registry initialized successfully!");
        } catch (Exception e) {
            log.severe("Failed to initialize Service Registry: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Service registry initialization failed", e);
        }
    }
    
    /**
     * Connect services that depend on each other after they are all created
     * This should be called after all services are registered and initialized
     */
    public void connectServices() {
        if (servicesConnected) {
            return; // Already connected
        }
        
        try {
            log.info("Connecting services...");
            
            // Connect ScriptEngine to AdvancedFunctionManager
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
            
            // Connect WorldManager to CodingManager
            IWorldManager worldManager = dependencyContainer.resolve(IWorldManager.class);
            ICodingManager codingManager = dependencyContainer.resolve(ICodingManager.class);
            
            if (worldManager != null && codingManager != null && worldManager instanceof WorldManagerImpl) {
                ((WorldManagerImpl) worldManager).setCodingManager(codingManager);
                log.info("Connected CodingManager to WorldManager");
            }
            
            // Connect Plugin to WorldManager
            if (worldManager != null && worldManager instanceof WorldManagerImpl) {
                ((WorldManagerImpl) worldManager).setPlugin(plugin);
                log.info("Connected Plugin to WorldManager");
            }
            
            // Initialize WorldManager after all dependencies are connected
            if (worldManager != null) {
                worldManager.initialize();
                log.info("WorldManager initialized");
            }
            
            servicesConnected = true;
            log.info("All services connected successfully!");
        } catch (Exception e) {
            log.severe("Failed to connect services: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeCoreServices() {
        // Core services are registered as factories, so they'll be created on demand
    }
    
    private void initializeManagers() {
        // Register manager service mappings
        dependencyContainer.registerType(TickManager.class, TickManager.class);
        // Register GUIManager as a factory since it needs dependencies
        dependencyContainer.registerFactory(GUIManager.class, (DependencyContainer.Supplier<GUIManager>) () -> {
            IPlayerManager playerManager = dependencyContainer.resolve(IPlayerManager.class);
            VariableManager variableManager = dependencyContainer.resolve(VariableManager.class);
            return new GUIManager((MegaCreative) plugin, playerManager, variableManager);
        });
        // Register WorldManagerImpl as a factory since it needs dependencies
        dependencyContainer.registerFactory(IWorldManager.class, (DependencyContainer.Supplier<IWorldManager>) () -> {
            ConfigManager configManager = dependencyContainer.resolve(ConfigManager.class);
            return new WorldManagerImpl(configManager, (MegaCreative) plugin);
        });
        
        // Register CodingManagerImpl as a factory since it needs dependencies
        dependencyContainer.registerFactory(ICodingManager.class, (DependencyContainer.Supplier<ICodingManager>) () -> {
            IWorldManager worldManager = dependencyContainer.resolve(IWorldManager.class);
            return new CodingManagerImpl((MegaCreative) plugin, worldManager);
        });
        dependencyContainer.registerType(ScoreboardManager.class, ScoreboardManager.class);
        dependencyContainer.registerType(GameScoreboardManager.class, GameScoreboardManager.class);
        dependencyContainer.registerType(PlayerManagerImpl.class, PlayerManagerImpl.class);
        dependencyContainer.registerType(TrustedPlayerManager.class, TrustedPlayerManager.class);
        dependencyContainer.registerType(BlockConfigManager.class, BlockConfigManager.class);
        dependencyContainer.registerType(DevInventoryManager.class, DevInventoryManager.class);
        dependencyContainer.registerType(BlockGroupManager.class, BlockGroupManager.class);
        dependencyContainer.registerType(VisualErrorHandler.class, VisualErrorHandler.class);
        dependencyContainer.registerType(EnemyPlayerRestrictionManager.class, EnemyPlayerRestrictionManager.class);
        dependencyContainer.registerType(PlayerModeManager.class, PlayerModeManager.class);
        dependencyContainer.registerType(MessagingService.class, MessagingService.class);
        dependencyContainer.registerType(ScriptPerformanceMonitor.class, ScriptPerformanceMonitor.class);
    }
    
    private void initializeImplementationManagers() {
        // These will be created on demand through the dependency container
    }
    
    private void initializeCodingServices() {
        // Register coding service mappings
        // BlockPlacementHandler will be created on demand, with lazy initialization of dependencies
        dependencyContainer.registerType(BlockPlacementHandler.class, BlockPlacementHandler.class);
        dependencyContainer.registerType(ConnectionVisualizer.class, ConnectionVisualizer.class);
        dependencyContainer.registerType(BlockLinker.class, BlockLinker.class);
        dependencyContainer.registerType(BlockHierarchyManager.class, BlockHierarchyManager.class);
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
        dependencyContainer.registerType(com.megacreative.interfaces.IScriptEngine.class, DefaultScriptEngine.class);
        
        // Register concrete classes
        dependencyContainer.registerFactory(ActionFactory.class, (DependencyContainer.Supplier<ActionFactory>) () -> 
            new ActionFactory((MegaCreative) plugin));
        dependencyContainer.registerFactory(ConditionFactory.class, (DependencyContainer.Supplier<ConditionFactory>) () -> 
            new ConditionFactory((MegaCreative) plugin));
        
        // Register ScriptCompiler as a factory since it needs BlockLinker as a dependency
        dependencyContainer.registerFactory(ScriptCompiler.class, (DependencyContainer.Supplier<ScriptCompiler>) () -> {
            BlockConfigService blockConfigService = dependencyContainer.resolve(BlockConfigService.class);
            BlockLinker blockLinker = dependencyContainer.resolve(BlockLinker.class);
            return new ScriptCompiler((MegaCreative) plugin, blockConfigService, blockLinker);
        });
        
        // Register ScriptEngine as a factory - this is critical for proper initialization
        dependencyContainer.registerFactory(ScriptEngine.class, (DependencyContainer.Supplier<ScriptEngine>) () -> {
            VariableManager variableManager = dependencyContainer.resolve(VariableManager.class);
            VisualDebugger visualDebugger = dependencyContainer.resolve(VisualDebugger.class);
            BlockConfigService blockConfigService = dependencyContainer.resolve(BlockConfigService.class);
            return new DefaultScriptEngine((MegaCreative) plugin, variableManager, visualDebugger, blockConfigService);
        });
    }
    
    private void initializeNewArchitectureServices() {
        // Register new architecture service mappings
        dependencyContainer.registerType(CustomEventManager.class, CustomEventManager.class);
        dependencyContainer.registerType(CodeBlockClipboard.class, CodeBlockClipboard.class);
        dependencyContainer.registerType(EventDataExtractorRegistry.class, EventDataExtractorRegistry.class);
        dependencyContainer.registerType(ReferenceSystemEventManager.class, ReferenceSystemEventManager.class);
        dependencyContainer.registerType(CodeCompiler.class, CodeCompiler.class);
        // Register InteractiveGUIManager as a factory since it needs the plugin
        dependencyContainer.registerFactory(InteractiveGUIManager.class, (DependencyContainer.Supplier<InteractiveGUIManager>) () -> 
            new InteractiveGUIManager((MegaCreative) plugin));
        // Register DevWorldProtectionListener as a factory since it needs dependencies
        dependencyContainer.registerFactory(DevWorldProtectionListener.class, (DependencyContainer.Supplier<DevWorldProtectionListener>) () -> {
            ITrustedPlayerManager trustedPlayerManager = dependencyContainer.resolve(ITrustedPlayerManager.class);
            BlockConfigService blockConfigService = dependencyContainer.resolve(BlockConfigService.class);
            
            // Ensure BlockConfigService has loaded its configuration before creating DevWorldProtectionListener
            if (blockConfigService != null && blockConfigService.getCodeBlockMaterials().isEmpty()) {
                log.info("BlockConfigService has empty materials, forcing configuration load");
                blockConfigService.reload();
            }
            
            DevWorldProtectionListener listener = new DevWorldProtectionListener((MegaCreative) plugin, trustedPlayerManager, blockConfigService);
            // Initialize dynamic allowed blocks after creation
            listener.initializeDynamicAllowedBlocks();
            return listener;
        });
    }
    
    /**
     * Initialize additional services that depend on core services being available
     */
    public void initializeAdditionalServices() {
        log.info("Initializing additional services...");
        
        try {
            // Additional services will be created on demand
            log.info("Additional services initialized successfully!");
        } catch (Exception e) {
            log.severe("Failed to initialize additional services: " + e.getMessage());
            e.printStackTrace();
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

            
            // Let the dependency container dispose all disposable services
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
    
    // Service getter methods - these will create services on demand through DI
    
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
    
    public IScriptEngine getScriptEngineInterface() {
        return dependencyContainer.resolve(IScriptEngine.class);
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
    
    public ScriptCompiler getScriptCompiler() { 
        return dependencyContainer.resolve(ScriptCompiler.class);
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