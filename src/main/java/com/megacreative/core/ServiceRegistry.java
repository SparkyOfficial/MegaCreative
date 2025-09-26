package com.megacreative.core;

import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.ConnectionVisualizer;
import com.megacreative.coding.ScriptCompiler;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.DefaultScriptEngine;
import com.megacreative.coding.ActionFactory;
import com.megacreative.coding.ConditionFactory;
import com.megacreative.coding.GUIRegistry;
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
import com.megacreative.services.FunctionManager;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.gui.interactive.InteractiveGUIManager;
import com.megacreative.gui.interactive.ReferenceSystemStyleGUI;
import com.megacreative.gui.coding.EnhancedActionParameterGUI;
import com.megacreative.services.MessagingService;
import com.megacreative.MegaCreative;
import com.megacreative.tools.CodeBlockClipboard;
// 🎆 Reference system-style comprehensive events
import com.megacreative.managers.ReferenceSystemEventManager;
import com.megacreative.utils.ConfigManager;
import com.megacreative.services.CodeCompiler;
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
        dependencyContainer.registerFactory(FunctionManager.class, (DependencyContainer.Supplier<FunctionManager>) () -> new FunctionManager((MegaCreative) plugin));
        dependencyContainer.registerFactory(AdvancedFunctionManager.class, (DependencyContainer.Supplier<AdvancedFunctionManager>) () -> new AdvancedFunctionManager((MegaCreative) plugin));
        dependencyContainer.registerFactory(ConfigManager.class, (DependencyContainer.Supplier<ConfigManager>) () -> {
            ConfigManager configManager = new ConfigManager((MegaCreative) plugin);
            configManager.loadConfig();
            return configManager;
        });
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
            
            // Initialize implementation managers
            initializeImplementationManagers();
            
            // Initialize coding services
            initializeCodingServices();
            
            // Initialize new architecture services
            initializeNewArchitectureServices();
            
            log.info("Service Registry initialized successfully!");
        } catch (Exception e) {
            log.severe("Failed to initialize Service Registry: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Service registry initialization failed", e);
        }
    }
    
    private void initializeCoreServices() {
        // Core services are registered as factories, so they'll be created on demand
    }
    
    private void initializeManagers() {
        // These will be created on demand through the dependency container
    }
    
    private void initializeImplementationManagers() {
        // These will be created on demand through the dependency container
    }
    
    private void initializeCodingServices() {
        // Register coding service mappings
        dependencyContainer.registerType(BlockPlacementHandler.class, BlockPlacementHandler.class);
        dependencyContainer.registerType(ConnectionVisualizer.class, ConnectionVisualizer.class);
        dependencyContainer.registerType(BlockLinker.class, BlockLinker.class);
        dependencyContainer.registerType(BlockHierarchyManager.class, BlockHierarchyManager.class);
        dependencyContainer.registerType(WorldCodeRestorer.class, WorldCodeRestorer.class);
        dependencyContainer.registerType(CodeBlockSignManager.class, CodeBlockSignManager.class);
        dependencyContainer.registerType(ScriptTriggerManager.class, ScriptTriggerManager.class);
        
        // Register ScriptCompiler as a factory since it needs BlockLinker as a dependency
        dependencyContainer.registerFactory(ScriptCompiler.class, (DependencyContainer.Supplier<ScriptCompiler>) () -> {
            BlockConfigService blockConfigService = dependencyContainer.resolve(BlockConfigService.class);
            BlockLinker blockLinker = dependencyContainer.resolve(BlockLinker.class);
            return new ScriptCompiler((MegaCreative) plugin, blockConfigService, blockLinker);
        });
        
        // Register ScriptEngine as a factory
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
            dependencyContainer.dispose();
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
    
    public TemplateManager getTemplateManager() { 
        return dependencyContainer.resolve(TemplateManager.class);
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
    
    public GUIManager getGuiManager() {
        return dependencyContainer.resolve(GUIManager.class);
    }
    
    public GUIRegistry getGuiRegistry() {
        return dependencyContainer.resolve(GUIRegistry.class);
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
    
    public ActionFactory getActionFactory() {
        return dependencyContainer.resolve(ActionFactory.class);
    }
    
    public ConditionFactory getConditionFactory() {
        return dependencyContainer.resolve(ConditionFactory.class);
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
    
    public FunctionManager getFunctionManager() {
        return dependencyContainer.resolve(FunctionManager.class);
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
}