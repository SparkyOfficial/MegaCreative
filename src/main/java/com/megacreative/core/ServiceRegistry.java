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
import com.megacreative.testing.ScriptTestRunner;
// 🎆 Reference system-style comprehensive events
import com.megacreative.managers.ReferenceSystemEventManager;
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
 * Ersetzt das God Object-Muster durch ordnungsgemäße Dependency Injection
 */
public class ServiceRegistry {
    private static final Logger log = Logger.getLogger(ServiceRegistry.class.getName());
    private final Plugin plugin;
    private final DependencyContainer dependencyContainer;
    private final ConcurrentHashMap<Class<?>, Object> services = new ConcurrentHashMap<>();
    
    /**
     * Core services
     *
     * Основные сервисы
     *
     * Kernservices
     */
    private com.megacreative.utils.ConfigManager configManager;
    
    /**
     * Interface-based managers
     *
     * Менеджеры на основе интерфейсов
     *
     * Schnittstellenbasierte Manager
     */
    private IWorldManager worldManager;
    private IPlayerManager playerManager;
    private ICodingManager codingManager;
    
    /**
     * Implementation managers
     *
     * Менеджеры реализации
     *
     * Implementierungsmanager
     */
    private TemplateManager templateManager;
    private ScoreboardManager scoreboardManager;
    private GameScoreboardManager gameScoreboardManager;
    private TrustedPlayerManager trustedPlayerManager;
    private ITrustedPlayerManager trustedPlayerManagerInterface;
    private GUIManager guiManager;
    private BlockConfigManager blockConfigManager;
    private ExecutorEngine executorEngine;
    
    /**
     * Coding system services
     *
     * Сервисы системы кодирования
     *
     * Kodiersystem-Services
     */
    private BlockPlacementHandler blockPlacementHandler;
    private VisualDebugger visualDebugger;
    private AutoConnectionManager autoConnectionManager;
    private DevInventoryManager devInventoryManager;
    private VariableManager variableManager;
    private BlockContainerManager containerManager;
    private final ScriptEngine scriptEngine;
    
    /**
     * New architecture services
     *
     * Сервисы новой архитектуры
     *
     * Neue Architekturservices
     */
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
    private ScriptTestRunner scriptTestRunner;
    
    /**
     * 🎆 Reference system: Interactive GUI System
     *
     * 🎆 Reference system: Интерактивная система GUI
     *
     * 🎆 Reference system: Interaktives GUI-System
     */
    private InteractiveGUIManager interactiveGUIManager;
    private ReferenceSystemStyleGUI referenceSystemStyleGUI;
    private EnhancedActionParameterGUI enhancedActionParameterGUI;
    
    /**
     * 🎆 Reference system-style comprehensive event system
     *
     * 🎆 Reference system-style: Комплексная система событий
     *
     * 🎆 Reference system-style: Umfassendes Ereignissystem
     */
    private ReferenceSystemEventManager referenceSystemEventManager;
    
    /**
     * Enemy player restriction system
     *
     * Система ограничения враждебных игроков
     *
     * Feindliches Spielerbeschränkungssystem
     */
    private EnemyPlayerRestrictionManager enemyPlayerRestrictionManager;

    /**
     * Creates a new service registry
     * @param plugin The plugin instance
     * @param dependencyContainer The dependency container
     *
     * Создает новый реестр сервисов
     * @param plugin Экземпляр плагина
     * @param dependencyContainer Контейнер зависимостей
     *
     * Erstellt einen neuen Serviceregister
     * @param plugin Die Plugin-Instanz
     * @param dependencyContainer Der Abhängigkeitscontainer
     */
    public ServiceRegistry(Plugin plugin, DependencyContainer dependencyContainer) {
        this.plugin = plugin;
        this.dependencyContainer = dependencyContainer;
        
        // Initialize core services first (services without dependencies)
        // Инициализировать основные сервисы первыми (сервисы без зависимостей)
        // Kernservices zuerst initialisieren (Services ohne Abhängigkeiten)
        this.variableManager = new VariableManager((MegaCreative) plugin);
        this.visualDebugger = new VisualDebugger((MegaCreative) plugin);
        this.blockConfigService = new BlockConfigService((MegaCreative) plugin);
        
        // Initialize factories with dependency container
        // Инициализировать фабрики с контейнером зависимостей
        // Fabriken mit Abhängigkeitscontainer initialisieren
        this.actionFactory = new ActionFactory(dependencyContainer);
        this.conditionFactory = new ConditionFactory();
        
        // Initialize ScriptEngine with its dependencies
        // Инициализировать ScriptEngine с его зависимостями
        // ScriptEngine mit seinen Abhängigkeiten initialisieren
        this.scriptEngine = new DefaultScriptEngine(
            (MegaCreative) plugin, 
            variableManager, 
            visualDebugger,
            blockConfigService
        );
        
        // Initialize ScriptTestRunner with its dependencies
        this.scriptTestRunner = new ScriptTestRunner(
            (MegaCreative) plugin,
            scriptEngine,
            variableManager,
            visualDebugger
        );
        
        // IMPORTANT: Register key services BEFORE creating dependent services
        // ВАЖНО: Зарегистрировать ключевые сервисы ДО создания зависимых сервисов
        // WICHTIG: Schlüsselservices REGISTRIEREN, BEVOR abhängige Services erstellt werden
        registerService(BlockConfigService.class, blockConfigService);
        registerService(VariableManager.class, variableManager);
        registerService(VisualDebugger.class, visualDebugger);
        registerService(ScriptTestRunner.class, scriptTestRunner);
        registerService(ActionFactory.class, actionFactory);
        registerService(ConditionFactory.class, conditionFactory);
        registerService(ScriptEngine.class, scriptEngine);
        registerService(DefaultScriptEngine.class, (DefaultScriptEngine) scriptEngine);
        
        // Now initialize services that depend on the above
        // Теперь инициализировать сервисы, которые зависят от вышеуказанных
        // Jetzt Services initialisieren, die von den oben genannten abhängen
        initializeDependentServices();
    }
    
    /**
     * Initialize dependent services
     *
     * Инициализировать зависимые сервисы
     *
     * Abhängige Services initialisieren
     */
    private void initializeDependentServices() {
        // Initialize FunctionManager first
        // Инициализировать FunctionManager первым
        // FunctionManager zuerst initialisieren
        this.functionManager = new FunctionManager((MegaCreative) plugin);
        registerService(FunctionManager.class, functionManager);
        
        // 🎆 Reference system: Now it's safe to initialize Advanced Function Manager
        // because ScriptEngine is already registered
        // 🎆 Reference system: Теперь безопасно инициализировать Advanced Function Manager
        // потому что ScriptEngine уже зарегистрирован
        // 🎆 Reference system: Jetzt ist es sicher, den Advanced Function Manager zu initialisieren
        // weil ScriptEngine bereits registriert ist
        this.advancedFunctionManager = new AdvancedFunctionManager((MegaCreative) plugin);
        registerService(AdvancedFunctionManager.class, advancedFunctionManager);
        
        // Set ScriptEngine in AdvancedFunctionManager if it wasn't available during construction
        // Установить ScriptEngine в AdvancedFunctionManager, если он не был доступен во время конструкции
        // ScriptEngine im AdvancedFunctionManager setzen, wenn er während der Konstruktion nicht verfügbar war
        if (!advancedFunctionManager.isScriptEngineAvailable()) {
            advancedFunctionManager.setScriptEngine(scriptEngine);
        }
        
        // Initialize ScriptEngine
        // Инициализировать ScriptEngine
        // ScriptEngine initialisieren
        if (scriptEngine instanceof DefaultScriptEngine) {
            DefaultScriptEngine defaultEngine = (DefaultScriptEngine) scriptEngine;
            // Initialize the engine
            // Инициализировать движок
            // Die Engine initialisieren
            defaultEngine.initialize();
            
            log.info("ScriptEngine initialized with " + 
                    defaultEngine.getActionCount() + " actions and " +
                    defaultEngine.getConditionCount() + " conditions");
            // ScriptEngine инициализирован с действиями и условиями
            // ScriptEngine initialisiert mit Aktionen und Bedingungen
        }
        
        log.info(" YYS Advanced Function Manager initialized after ScriptEngine registration");
        // YYS Advanced Function Manager инициализирован после регистрации ScriptEngine
        // YYS Advanced Function Manager nach ScriptEngine-Registrierung initialisiert
    }
    
    /**
     * 🎆 Reference system: Initialize all core services in correct order
     * This method sets up the dependency injection container and registers all services
     * Services are initialized in dependency order to prevent circular dependencies
     *
     * 🎆 Reference system: Инициализировать все основные сервисы в правильном порядке
     * Этот метод настраивает контейнер внедрения зависимостей и регистрирует все сервисы
     * Сервисы инициализируются в порядке зависимостей для предотвращения циклических зависимостей
     *
     * 🎆 Reference system: Alle Kernservices in korrekter Reihenfolge initialisieren
     * Diese Methode richtet den Dependency Injection Container ein und registriert alle Services
     * Services werden in Abhängigkeitsreihenfolge initialisiert, um zirkuläre Abhängigkeiten zu vermeiden
     */
    public void initializeServices() {
        plugin.getLogger().info(" YYS Initializing Service Registry...");
        // YYS Инициализация реестра сервисов...
        // YYS Serviceregister wird initialisiert...
        
        try {
            // Initialize core services first (services without dependencies)
            // Initialize ConfigManager first as it's needed by WorldManagerImpl
            // Инициализировать основные сервисы первыми (сервисы без зависимостей)
            // Инициализировать ConfigManager первым, так как он нужен для WorldManagerImpl
            // Kernservices zuerst initialisieren (Services ohne Abhängigkeiten)
            // ConfigManager zuerst initialisieren, da er von WorldManagerImpl benötigt wird
            if (configManager == null) {
                configManager = new com.megacreative.utils.ConfigManager((MegaCreative) plugin);
                configManager.loadConfig(); // Load the configuration immediately after creation
                // Загрузить конфигурацию сразу после создания
                // Die Konfiguration sofort nach der Erstellung laden
                registerService(com.megacreative.utils.ConfigManager.class, configManager);
            }
            
            // Core managers first (minimal dependencies)
            // Use the constructor that accepts ConfigManager to avoid circular dependency
            // Основные менеджеры первыми (минимальные зависимости)
            // Использовать конструктор, который принимает ConfigManager, чтобы избежать циклической зависимости
            // Kernmanager zuerst (minimale Abhängigkeiten)
            // Den Konstruktor verwenden, der ConfigManager akzeptiert, um zirkuläre Abhängigkeit zu vermeiden
            WorldManagerImpl worldManagerImpl = new com.megacreative.managers.WorldManagerImpl(configManager);
            worldManagerImpl.setPlugin(plugin); // Set the plugin instance
            // Установить экземпляр плагина
            // Die Plugin-Instanz setzen
            this.worldManager = worldManagerImpl;
            registerService(com.megacreative.managers.WorldManagerImpl.class, worldManagerImpl);
            registerService(com.megacreative.interfaces.IWorldManager.class, worldManagerImpl);
            
            // Register interface-to-implementation mapping in DependencyContainer
            // Зарегистрировать сопоставление интерфейса с реализацией в DependencyContainer
            // Schnittstellen-zu-Implementierungs-Zuordnung im DependencyContainer registrieren
            dependencyContainer.registerType(com.megacreative.interfaces.IWorldManager.class, com.megacreative.managers.WorldManagerImpl.class);
            
            PlayerManagerImpl playerManagerImpl = new com.megacreative.managers.PlayerManagerImpl((MegaCreative) plugin);
            this.playerManager = playerManagerImpl;
            registerService(com.megacreative.managers.PlayerManagerImpl.class, playerManagerImpl);
            registerService(com.megacreative.interfaces.IPlayerManager.class, playerManagerImpl);
            
            // Register interface-to-implementation mapping in DependencyContainer
            // Зарегистрировать сопоставление интерфейса с реализацией в DependencyContainer
            // Schnittstellen-zu-Implementierungs-Zuordnung im DependencyContainer registrieren
            dependencyContainer.registerType(com.megacreative.interfaces.IPlayerManager.class, com.megacreative.managers.PlayerManagerImpl.class);
            
            this.trustedPlayerManager = new com.megacreative.managers.TrustedPlayerManager((MegaCreative) plugin);
            registerService(com.megacreative.managers.TrustedPlayerManager.class, trustedPlayerManager);
            // Keep reference to implementation for type casting
            // Сохранить ссылку на реализацию для приведения типов
            // Referenz auf Implementierung für Typumwandlung behalten
            this.trustedPlayerManagerInterface = trustedPlayerManager;
            
            // Register interface-to-implementation mapping in DependencyContainer
            // Зарегистрировать сопоставление интерфейса с реализацией в DependencyContainer
            // Schnittstellen-zu-Implementierungs-Zuordnung im DependencyContainer registrieren
            dependencyContainer.registerType(com.megacreative.interfaces.ITrustedPlayerManager.class, com.megacreative.managers.TrustedPlayerManager.class);
            
            // Initialize world manager early
            // Инициализировать менеджер мира рано
            // Weltmanager früh initialisieren
            if (worldManagerImpl != null) {
                // Set the coding manager after it's available through lazy initialization
                // Установить менеджер кодирования после того, как он станет доступен через ленивую инициализацию
                // Den Coding-Manager setzen, nachdem er durch Lazy Initialization verfügbar ist
                worldManagerImpl.setCodingManager(getCodingManager());
                worldManagerImpl.initialize();
            }
            
            // Managers with minimal dependencies
            // Менеджеры с минимальными зависимостями
            // Manager mit minimalen Abhängigkeiten
            this.devInventoryManager = new DevInventoryManager((MegaCreative) plugin);
            registerService(DevInventoryManager.class, devInventoryManager);
            
            // 🎆 Reference system: Now it's safe to initialize Advanced Function Manager
            // 🎆 Reference system: Теперь безопасно инициализировать Advanced Function Manager
            // 🎆 Reference system: Jetzt ist es sicher, den Advanced Function Manager zu initialisieren
            this.advancedFunctionManager = new AdvancedFunctionManager((MegaCreative) plugin);
            registerService(AdvancedFunctionManager.class, advancedFunctionManager);
            
            // Core handlers (depend on managers)
            // Основные обработчики (зависят от менеджеров)
            // Kernhandler (abhängen von Managern)
            this.blockPlacementHandler = new BlockPlacementHandler((MegaCreative) plugin);
            registerService(BlockPlacementHandler.class, blockPlacementHandler);
            
            this.autoConnectionManager = new AutoConnectionManager((MegaCreative) plugin, blockConfigService);
            registerService(AutoConnectionManager.class, autoConnectionManager);
            
            // 🎆 Reference system-style comprehensive event system
            // 🎆 Reference system-style: Комплексная система событий
            // 🎆 Reference system-style: Umfassendes Ereignissystem
            this.referenceSystemEventManager = new ReferenceSystemEventManager((MegaCreative) plugin);
            registerService(ReferenceSystemEventManager.class, referenceSystemEventManager);
            
            // Compiler service
            // Сервис компилятора
            // Compiler-Service
            this.codeCompiler = new com.megacreative.services.CodeCompiler((MegaCreative) plugin);
            registerService(com.megacreative.services.CodeCompiler.class, codeCompiler);
            
            plugin.getLogger().info(" YYS Service Registry initialized successfully!");
            // YYS Реестр сервисов успешно инициализирован!
            // YYS Serviceregister erfolgreich initialisiert!
            
        } catch (Exception e) {
            plugin.getLogger().severe(" YYS Failed to initialize Service Registry: " + e.getMessage());
            // YYS Не удалось инициализировать реестр сервисов:
            // YYS Fehler beim Initialisieren des Serviceregisters:
            e.printStackTrace();
            throw new RuntimeException("Service registry initialization failed", e);
            // Инициализация реестра сервисов не удалась
            // Serviceregister-Initialisierung fehlgeschlagen
        }
    }
    
    /**
     * Gets a service by type with proper type safety
     *
     * Получает сервис по типу с правильной безопасностью типов
     *
     * Ruft einen Service nach Typ mit ordnungsgemäßer Typsicherheit ab
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceType) {
        T service = (T) services.get(serviceType);
        if (service == null) {
            throw new IllegalArgumentException("Service not found: " + serviceType.getName());
            // Сервис не найден:
            // Service nicht gefunden:
        }
        return service;
    }
    
    /**
     * Checks if a service is registered
     *
     * Проверяет, зарегистрирован ли сервис
     *
     * Prüft, ob ein Service registriert ist
     */
    public boolean hasService(Class<?> serviceType) {
        return services.containsKey(serviceType);
    }
    
    /**
     * Registers a service instance
     *
     * Регистрирует экземпляр сервиса
     *
     * Registriert eine Service-Instanz
     */
    public <T> void registerService(Class<T> serviceType, T serviceInstance) {
        services.put(serviceType, serviceInstance);
        dependencyContainer.registerSingleton(serviceType, serviceInstance);
        log.fine("Registered service: " + serviceType.getSimpleName());
        // Зарегистрированный сервис:
        // Registrierter Service:
    }
    
    /**
     * Shuts down all services gracefully
     *
     * Корректно завершает работу всех сервисов
     *
     * Fährt alle Services ordnungsgemäß herunter
     */
    public void shutdown() {
        log.info("Shutting down MegaCreative services...");
        // Завершение работы сервисов MegaCreative...
        // MegaCreative-Services werden heruntergefahren...
        
        // Shutdown services in reverse order of initialization
        // Завершить работу сервисов в обратном порядке инициализации
        // Services in umgekehrter Reihenfolge der Initialisierung herunterfahren
        
        // 🎆 FRAMELAND: Shutdown comprehensive event manager
        // 🎆 FRAMELAND: Завершить работу комплексного менеджера событий
        // 🎆 FRAMELAND: Umfassenden Ereignismanager herunterfahren
        if (referenceSystemEventManager != null) {
            referenceSystemEventManager.shutdown();
        }
        
        // 🎆 FRAMELAND: Shutdown interactive GUI system
        // 🎆 FRAMELAND: Завершить работу интерактивной системы GUI
        // 🎆 FRAMELAND: Interaktives GUI-System herunterfahren
        if (interactiveGUIManager != null) {
            interactiveGUIManager.shutdown();
        }
        
        // 🎆 FRAMELAND: Shutdown advanced function manager
        // 🎆 FRAMELAND: Завершить работу продвинутого менеджера функций
        // 🎆 FRAMELAND: Erweiterten Funktionsmanager herunterfahren
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
        // Все сервисы успешно завершили работу
        // Alle Services erfolgreich heruntergefahren
    }
    
    /**
     * Gets the world manager
     * @return World manager instance
     *
     * Получает менеджер мира
     * @return Экземпляр менеджера мира
     *
     * Ruft den Weltmanager ab
     * @return Weltmanager-Instanz
     */
    public IWorldManager getWorldManager() { 
        return worldManager != null ? worldManager : 
            (worldManager = dependencyContainer.resolve(IWorldManager.class));
    }
    
    /**
     * Gets the player manager
     * @return Player manager instance
     *
     * Получает менеджер игроков
     * @return Экземпляр менеджера игроков
     *
     * Ruft den Spieler-Manager ab
     * @return Spieler-Manager-Instanz
     */
    public IPlayerManager getPlayerManager() { 
        return playerManager != null ? playerManager : 
            (playerManager = dependencyContainer.resolve(IPlayerManager.class));
    }
    
    /**
     * Gets the coding manager
     * @return Coding manager instance
     *
     * Получает менеджер кодирования
     * @return Экземпляр менеджера кодирования
     *
     * Ruft den Coding-Manager ab
     * @return Coding-Manager-Instanz
     */
    public ICodingManager getCodingManager() {
        if (codingManager == null) {
            // Get required dependencies
            // Получить необходимые зависимости
            // Erforderliche Abhängigkeiten abrufen
            IWorldManager worldManager = getWorldManager();
            ScriptEngine scriptEngine = getService(ScriptEngine.class);
            
            // Create and initialize CodingManager
            // Создать и инициализировать CodingManager
            // CodingManager erstellen und initialisieren
            this.codingManager = new com.megacreative.coding.CodingManagerImpl((MegaCreative) plugin, worldManager);
            registerService(ICodingManager.class, codingManager);
            
            // Set the coding manager in WorldManagerImpl
            // Установить менеджер кодирования в WorldManagerImpl
            // Den Coding-Manager im WorldManagerImpl setzen
            if (worldManager instanceof WorldManagerImpl) {
                ((WorldManagerImpl) worldManager).setCodingManager(codingManager);
            }
            
            // Verify ScriptEngine is properly set
            // Проверить, что ScriptEngine правильно установлен
            // Überprüfen, ob ScriptEngine ordnungsgemäß gesetzt ist
            if (scriptEngine == null) {
                log.warning("ScriptEngine is not available when initializing CodingManager");
                // ScriptEngine недоступен при инициализации CodingManager
                // ScriptEngine ist beim Initialisieren von CodingManager nicht verfügbar
            } else {
                log.info("CodingManager initialized with ScriptEngine: " + scriptEngine.getClass().getSimpleName());
                // CodingManager инициализирован с ScriptEngine:
                // CodingManager initialisiert mit ScriptEngine:
            }
        }
        return codingManager;
    }
    
    /**
     * Gets the template manager
     * @return Template manager instance
     *
     * Получает менеджер шаблонов
     * @return Экземпляр менеджера шаблонов
     *
     * Ruft den Vorlagenmanager ab
     * @return Vorlagenmanager-Instanz
     */
    public TemplateManager getTemplateManager() { 
        return templateManager != null ? templateManager : 
            (templateManager = dependencyContainer.resolve(TemplateManager.class));
    }
    
    /**
     * Gets the scoreboard manager
     * @return Scoreboard manager instance
     *
     * Получает менеджер скорборда
     * @return Экземпляр менеджера скорборда
     *
     * Ruft den Scoreboard-Manager ab
     * @return Scoreboard-Manager-Instanz
     */
    public ScoreboardManager getScoreboardManager() { 
        return scoreboardManager != null ? scoreboardManager : 
            (scoreboardManager = dependencyContainer.resolve(ScoreboardManager.class));
    }
    
    /**
     * Gets the game scoreboard manager
     * @return Game scoreboard manager instance
     *
     * Получает менеджер игрового скорборда
     * @return Экземпляр менеджера игрового скорборда
     *
     * Ruft den Spiel-Scoreboard-Manager ab
     * @return Spiel-Scoreboard-Manager-Instanz
     */
    public GameScoreboardManager getGameScoreboardManager() {
        return gameScoreboardManager;
    }
    
    /**
     * Gets the trusted player manager
     * @return Trusted player manager instance
     *
     * Получает менеджер доверенных игроков
     * @return Экземпляр менеджера доверенных игроков
     *
     * Ruft den vertrauenswürdigen Spieler-Manager ab
     * @return Vertrauenswürdiger Spieler-Manager-Instanz
     */
    public ITrustedPlayerManager getTrustedPlayerManager() { 
        return trustedPlayerManagerInterface != null ? trustedPlayerManagerInterface : 
            (trustedPlayerManagerInterface = dependencyContainer.resolve(ITrustedPlayerManager.class));
    }
    
    /**
     * Gets the GUI manager
     * @return GUI manager instance
     *
     * Получает менеджер GUI
     * @return Экземпляр менеджера GUI
     *
     * Ruft den GUI-Manager ab
     * @return GUI-Manager-Instanz
     */
    public GUIManager getGuiManager() {
        return guiManager != null ? guiManager :
            (guiManager = dependencyContainer.resolve(GUIManager.class));
    }
    
    /**
     * Gets the variable manager
     * @return Variable manager instance
     *
     * Получает менеджер переменных
     * @return Экземпляр менеджера переменных
     *
     * Ruft den Variablenmanager ab
     * @return Variablenmanager-Instanz
     */
    public VariableManager getVariableManager() { 
        return variableManager;
    }
    
    /**
     * Gets the block placement handler
     * @return Block placement handler instance
     *
     * Получает обработчик размещения блоков
     * @return Экземпляр обработчика размещения блоков
     *
     * Ruft den Blockplatzierungshandler ab
     * @return Blockplatzierungshandler-Instanz
     */
    public BlockPlacementHandler getBlockPlacementHandler() {
        return blockPlacementHandler != null ? blockPlacementHandler :
            (blockPlacementHandler = dependencyContainer.resolve(BlockPlacementHandler.class));
    }
    
    /**
     * Gets the VisualDebugger instance
     * @return VisualDebugger instance
     *
     * Получает экземпляр VisualDebugger
     * @return Экземпляр VisualDebugger
     *
     * Ruft die VisualDebugger-Instanz ab
     * @return VisualDebugger-Instanz
     */
    public VisualDebugger getScriptDebugger() {
        return visualDebugger != null ? visualDebugger :
            (visualDebugger = dependencyContainer.resolve(VisualDebugger.class));
    }
    
    /**
     * Gets the script performance monitor
     * @return Script performance monitor instance
     *
     * Получает монитор производительности скриптов
     * @return Экземпляр монитора производительности скриптов
     *
     * Ruft den Skriptleistungsmonitor ab
     * @return Skriptleistungsmonitor-Instanz
     */
    public ScriptPerformanceMonitor getScriptPerformanceMonitor() {
        return scriptPerformanceMonitor != null ? scriptPerformanceMonitor :
            (scriptPerformanceMonitor = dependencyContainer.resolve(ScriptPerformanceMonitor.class));
    }
    
    /**
     * Gets the event data extractor registry
     * @return Event data extractor registry instance
     *
     * Получает реестр экстракторов данных событий
     * @return Экземпляр реестра экстракторов данных событий
     *
     * Ruft die Ereignisdaten-Extraktor-Registry ab
     * @return Ereignisdaten-Extraktor-Registry-Instanz
     */
    public EventDataExtractorRegistry getEventDataExtractorRegistry() {
        if (eventDataExtractorRegistry == null) {
            eventDataExtractorRegistry = new EventDataExtractorRegistry();
            registerService(EventDataExtractorRegistry.class, eventDataExtractorRegistry);
        }
        return eventDataExtractorRegistry;
    }
    
    /**
     * Gets the block container manager
     * @return Block container manager instance
     *
     * Получает менеджер контейнеров блоков
     * @return Экземпляр менеджера контейнеров блоков
     *
     * Ruft den Blockcontainer-Manager ab
     * @return Blockcontainer-Manager-Instanz
     */
    public BlockContainerManager getBlockContainerManager() {
        return containerManager != null ? containerManager :
            (containerManager = dependencyContainer.resolve(BlockContainerManager.class));
    }
    
    /**
     * Gets the block config manager
     * @return Block config manager instance
     *
     * Получает менеджер конфигурации блоков
     * @return Экземпляр менеджера конфигурации блоков
     *
     * Ruft den Blockkonfigurationsmanager ab
     * @return Blockkonfigurationsmanager-Instanz
     */
    public BlockConfigManager getBlockConfigManager() { 
        return blockConfigManager != null ? blockConfigManager :
            (blockConfigManager = dependencyContainer.resolve(BlockConfigManager.class));
    }
    
    /**
     * Gets the auto connection manager
     * @return Auto connection manager instance
     *
     * Получает менеджер автоматических соединений
     * @return Экземпляр менеджера автоматических соединений
     *
     * Ruft den Auto-Verbindungsmanager ab
     * @return Auto-Verbindungsmanager-Instanz
     */
    public AutoConnectionManager getAutoConnectionManager() {
        return autoConnectionManager != null ? autoConnectionManager :
            (autoConnectionManager = dependencyContainer.resolve(AutoConnectionManager.class));
    }
    
    /**
     * Gets the dev inventory manager
     * @return Dev inventory manager instance
     *
     * Получает менеджер инвентаря разработки
     * @return Экземпляр менеджера инвентаря разработки
     *
     * Ruft den Dev-Inventarmanager ab
     * @return Dev-Inventarmanager-Instanz
     */
    public DevInventoryManager getDevInventoryManager() {
        return devInventoryManager != null ? devInventoryManager :
            (devInventoryManager = dependencyContainer.resolve(DevInventoryManager.class));
    }
    
    /**
     * Gets the config manager
     * @return Config manager instance
     *
     * Получает менеджер конфигурации
     * @return Экземпляр менеджера конфигурации
     *
     * Ruft den Konfigurationsmanager ab
     * @return Konfigurationsmanager-Instanz
     */
    public com.megacreative.utils.ConfigManager getConfigManager() { 
        if (configManager == null) {
            configManager = dependencyContainer.resolve(com.megacreative.utils.ConfigManager.class);
            registerService(com.megacreative.utils.ConfigManager.class, configManager);
        }
        return configManager;
    }
    
    /**
     * Gets the block config service
     * @return Block config service instance
     *
     * Получает сервис конфигурации блоков
     * @return Экземпляр сервиса конфигурации блоков
     *
     * Ruft den Blockkonfigurationsservice ab
     * @return Blockkonfigurationsservice-Instanz
     */
    public BlockConfigService getBlockConfigService() {
        return blockConfigService;
    }
    
    /**
     * Gets the action factory
     * @return Action factory instance
     *
     * Получает фабрику действий
     * @return Экземпляр фабрики действий
     *
     * Ruft die Aktionsfabrik ab
     * @return Aktionsfabrik-Instanz
     */
    public ActionFactory getActionFactory() {
        return actionFactory;
    }
    
    /**
     * Gets the condition factory
     * @return Condition factory instance
     *
     * Получает фабрику условий
     * @return Экземпляр фабрики условий
     *
     * Ruft die Bedingungsfabrik ab
     * @return Bedingungsfabrik-Instanz
     */
    public ConditionFactory getConditionFactory() {
        return conditionFactory;
    }
    
    /**
     * Gets the player events listener
     * @return Player events listener instance
     *
     * Получает слушатель событий игроков
     * @return Экземпляр слушателя событий игроков
     *
     * Ruft den Spielerereignis-Listener ab
     * @return Spielerereignis-Listener-Instanz
     */
    public PlayerEventsListener getPlayerEventsListener() {
        if (playerEventsListener == null) {
            playerEventsListener = new PlayerEventsListener((MegaCreative) plugin);
            registerService(PlayerEventsListener.class, playerEventsListener);
        }
        return playerEventsListener;
    }
    
    /**
     * Gets the custom event manager
     * @return Custom event manager instance
     *
     * Получает менеджер пользовательских событий
     * @return Экземпляр менеджера пользовательских событий
     *
     * Ruft den benutzerdefinierten Ereignismanager ab
     * @return Benutzerdefinierter Ereignismanager-Instanz
     */
    public CustomEventManager getCustomEventManager() {
        if (customEventManager == null) {
            customEventManager = new CustomEventManager((MegaCreative) plugin);
            registerService(CustomEventManager.class, customEventManager);
        }
        return customEventManager;
    }
    
    /**
     * Gets the dev world protection listener
     * @return Dev world protection listener instance
     *
     * Получает слушатель защиты мира разработки
     * @return Экземпляр слушателя защиты мира разработки
     *
     * Ruft den Dev-Welt-Schutz-Listener ab
     * @return Dev-Welt-Schutz-Listener-Instanz
     */
    public DevWorldProtectionListener getDevWorldProtectionListener() {
        if (devWorldProtectionListener == null) {
            // Get required dependencies
            // Получить необходимые зависимости
            // Erforderliche Abhängigkeiten abrufen
            ITrustedPlayerManager trustedPlayerManager = getTrustedPlayerManager();
            BlockConfigService blockConfigService = getBlockConfigService();
            
            this.devWorldProtectionListener = new DevWorldProtectionListener((MegaCreative) plugin, 
                (com.megacreative.managers.TrustedPlayerManager) trustedPlayerManager, 
                blockConfigService);
            registerService(DevWorldProtectionListener.class, devWorldProtectionListener);
        }
        return devWorldProtectionListener;
    }
    
    /**
     * Gets the code block clipboard
     * @return Code block clipboard instance
     *
     * Получает буфер обмена кодовых блоков
     * @return Экземпляр буфера обмена кодовых блоков
     *
     * Ruft die Codeblock-Zwischenablage ab
     * @return Codeblock-Zwischenablage-Instanz
     */
    public CodeBlockClipboard getCodeBlockClipboard() {
        if (codeBlockClipboard == null) {
            this.codeBlockClipboard = new CodeBlockClipboard();
            registerService(CodeBlockClipboard.class, codeBlockClipboard);
        }
        return codeBlockClipboard;
    }
    
    /**
     * Gets the function manager
     * @return Function manager instance
     *
     * Получает менеджер функций
     * @return Экземпляр менеджера функций
     *
     * Ruft den Funktionsmanager ab
     * @return Funktionsmanager-Instanz
     */
    public com.megacreative.services.FunctionManager getFunctionManager() {
        if (functionManager == null) {
            this.functionManager = new com.megacreative.services.FunctionManager((MegaCreative) plugin);
            registerService(com.megacreative.services.FunctionManager.class, functionManager);
        }
        return functionManager;
    }
    
    /**
     * 🎆 Reference system: Get Advanced Function Manager
     *
     * 🎆 Reference system: Получить продвинутый менеджер функций
     *
     * 🎆 Reference system: Erweiterten Funktionsmanager abrufen
     */
    public AdvancedFunctionManager getAdvancedFunctionManager() {
        return advancedFunctionManager;
    }
    
    /**
     * 🎆 Reference system: Get Interactive GUI Manager
     *
     * 🎆 Reference system: Получить интерактивный менеджер GUI
     *
     * 🎆 Reference system: Interaktiven GUI-Manager abrufen
     */
    public InteractiveGUIManager getInteractiveGUIManager() {
        if (interactiveGUIManager == null) {
            this.interactiveGUIManager = new InteractiveGUIManager((MegaCreative) plugin);
            registerService(InteractiveGUIManager.class, interactiveGUIManager);
        }
        return interactiveGUIManager;
    }
    
    /**
     * 🎆 Reference system: Get Reference System Style GUI
     *
     * 🎆 Reference system: Получить GUI в стиле Reference System
     *
     * 🎆 Reference system: Reference System Style GUI abrufen
     */
    public ReferenceSystemStyleGUI getReferenceSystemStyleGUI() {
        if (referenceSystemStyleGUI == null) {
            this.referenceSystemStyleGUI = new ReferenceSystemStyleGUI((MegaCreative) plugin);
            registerService(ReferenceSystemStyleGUI.class, referenceSystemStyleGUI);
        }
        return referenceSystemStyleGUI;
    }
    
    /**
     * 🎆 Reference system: Get Enhanced Action Parameter GUI
     *
     * 🎆 Reference system: Получить улучшенный GUI параметров действий
     *
     * 🎆 Reference system: Verbesserte Aktionsparameter-GUI abrufen
     */
    public EnhancedActionParameterGUI getEnhancedActionParameterGUI() {
        if (enhancedActionParameterGUI == null) {
            this.enhancedActionParameterGUI = new EnhancedActionParameterGUI((MegaCreative) plugin);
            registerService(EnhancedActionParameterGUI.class, enhancedActionParameterGUI);
        }
        return enhancedActionParameterGUI;
    }
    
    /**
     * Get CodeCompiler service
     *
     * Получить сервис CodeCompiler
     *
     * CodeCompiler-Service abrufen
     */
    public com.megacreative.services.CodeCompiler getCodeCompiler() {
        if (codeCompiler == null) {
            this.codeCompiler = new com.megacreative.services.CodeCompiler((MegaCreative) plugin);
            registerService(com.megacreative.services.CodeCompiler.class, codeCompiler);
        }
        return codeCompiler;
    }
    
    /**
     * Get EnemyPlayerRestrictionManager service
     *
     * Получить сервис EnemyPlayerRestrictionManager
     *
     * EnemyPlayerRestrictionManager-Service abrufen
     */
    public EnemyPlayerRestrictionManager getEnemyPlayerRestrictionManager() {
        if (enemyPlayerRestrictionManager == null) {
            this.enemyPlayerRestrictionManager = new EnemyPlayerRestrictionManager((MegaCreative) plugin);
            registerService(EnemyPlayerRestrictionManager.class, enemyPlayerRestrictionManager);
        }
        return enemyPlayerRestrictionManager;
    }
    
    /**
     * Get the script test runner
     * @return Script test runner instance
     *
     * Получает испытательный запускатель скриптов
     * @return Экземпляр испытательного запускателя скриптов
     *
     * Ruft den Skript-Test-Runner ab
     * @return Skript-Test-Runner-Instanz
     */
    public ScriptTestRunner getScriptTestRunner() {
        return scriptTestRunner;
    }
    
    /**
     * Initialize core services
     *
     * Инициализировать основные сервисы
     *
     * Kernservices initialisieren
     */
    private void initializeCoreServices() {
        // Initialize core services like ConfigManager
        // Инициализировать основные сервисы, такие как ConfigManager
        // Kernservices wie ConfigManager initialisieren
        if (configManager == null) {
            configManager = new com.megacreative.utils.ConfigManager((MegaCreative) plugin);
            configManager.loadConfig(); // Load the configuration immediately after creation
            // Загрузить конфигурацию сразу после создания
            // Die Konfiguration sofort nach der Erstellung laden
            registerService(com.megacreative.utils.ConfigManager.class, configManager);
        }
    }
    
    /**
     * Initialize managers
     *
     * Инициализировать менеджеры
     *
     * Manager initialisieren
     */
    private void initializeManagers() {
        // Initialize interface-based managers
        // Инициализировать менеджеры на основе интерфейсов
        // Schnittstellenbasierte Manager initialisieren
        if (playerManager == null) {
            PlayerManagerImpl playerManagerImpl = new com.megacreative.managers.PlayerManagerImpl((MegaCreative) plugin);
            this.playerManager = playerManagerImpl;
            registerService(IPlayerManager.class, playerManagerImpl);
            dependencyContainer.registerType(com.megacreative.interfaces.IPlayerManager.class, com.megacreative.managers.PlayerManagerImpl.class);
        }
        
        if (worldManager == null) {
            // Use the constructor that accepts ConfigManager and set codingManager later
            // Использовать конструктор, который принимает ConfigManager, и установить codingManager позже
            // Den Konstruktor verwenden, der ConfigManager akzeptiert, und codingManager später setzen
            WorldManagerImpl worldManagerImpl = new com.megacreative.managers.WorldManagerImpl(getConfigManager());
            this.worldManager = worldManagerImpl;
            // Set the coding manager after it's available
            // Установить менеджер кодирования после того, как он станет доступен
            // Den Coding-Manager setzen, nachdem er verfügbar ist
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
            // Сохранить ссылку на реализацию для приведения типов
            // Referenz auf Implementierung für Typumwandlung behalten
            this.trustedPlayerManagerInterface = trustedPlayerManager;
        }
    }
    
    /**
     * Initialize implementation managers
     *
     * Инициализировать менеджеры реализации
     *
     * Implementierungsmanager initialisieren
     */
    private void initializeImplementationManagers() {
        // Initialize implementation managers
        // Инициализировать менеджеры реализации
        // Implementierungsmanager initialisieren
        if (gameScoreboardManager == null) {
            this.gameScoreboardManager = new GameScoreboardManager((MegaCreative) plugin);
            registerService(GameScoreboardManager.class, gameScoreboardManager);
        }
    }
    
    /**
     * Initialize coding services
     *
     * Инициализировать сервисы кодирования
     *
     * Kodierservices initialisieren
     */
    private void initializeCodingServices() {
        // Initialize coding system services
        // Инициализировать сервисы системы кодирования
        // Kodiersystem-Services initialisieren
        if (blockPlacementHandler == null) {
            this.blockPlacementHandler = new BlockPlacementHandler((MegaCreative) plugin);
            registerService(BlockPlacementHandler.class, blockPlacementHandler);
        }
    }
    
    /**
     * Initialize new architecture services
     *
     * Инициализировать сервисы новой архитектуры
     *
     * Neue Architekturservices initialisieren
     */
    private void initializeNewArchitectureServices() {
        // Initialize BlockConfigService first as it's a core dependency
        // Инициализировать BlockConfigService первым, так как это основная зависимость
        // BlockConfigService zuerst initialisieren, da es eine Kerndependenz ist
        if (blockConfigService == null) {
            this.blockConfigService = new BlockConfigService((MegaCreative) plugin);
            registerService(BlockConfigService.class, blockConfigService);
        }
        
        // Load block configurations
        // Загрузить конфигурации блоков
        // Blockkonfigurationen laden
        blockConfigService.reload();
        
        // Initialize GUI Manager with required dependencies
        // Инициализировать менеджер GUI с необходимыми зависимостями
        // GUI-Manager mit erforderlichen Abhängigkeiten initialisieren
        if (guiManager == null) {
            this.guiManager = new GUIManager(getPlayerManager(), getVariableManager());
            registerService(GUIManager.class, guiManager);
        }
        
        // Initialize PlayerEventsListener
        // Инициализировать PlayerEventsListener
        // PlayerEventsListener initialisieren
        if (playerEventsListener == null) {
            this.playerEventsListener = new PlayerEventsListener((MegaCreative) plugin);
            registerService(PlayerEventsListener.class, playerEventsListener);
        }
        
        // Initialize CustomEventManager
        // Инициализировать CustomEventManager
        // CustomEventManager initialisieren
        if (customEventManager == null) {
            this.customEventManager = new CustomEventManager((MegaCreative) plugin);
            registerService(CustomEventManager.class, customEventManager);
        }
        
        // Initialize DevWorldProtectionListener
        // Инициализировать DevWorldProtectionListener
        // DevWorldProtectionListener initialisieren
        if (devWorldProtectionListener == null) {
            this.devWorldProtectionListener = new DevWorldProtectionListener(
                (MegaCreative) plugin,
                (com.megacreative.managers.TrustedPlayerManager) getTrustedPlayerManager(),
                getBlockConfigService()
            );
            registerService(DevWorldProtectionListener.class, devWorldProtectionListener);
        }
        
        // Initialize CodeBlockClipboard
        // Инициализировать CodeBlockClipboard
        // CodeBlockClipboard initialisieren
        if (codeBlockClipboard == null) {
            this.codeBlockClipboard = new CodeBlockClipboard();
            registerService(CodeBlockClipboard.class, codeBlockClipboard);
        }
        
        // Initialize EventDataExtractorRegistry
        // Инициализировать EventDataExtractorRegistry
        // EventDataExtractorRegistry initialisieren
        if (eventDataExtractorRegistry == null) {
            this.eventDataExtractorRegistry = new EventDataExtractorRegistry();
            registerService(EventDataExtractorRegistry.class, eventDataExtractorRegistry);
        }
        
        // 🎆 FRAMELAND: Initialize comprehensive event manager
        // 🎆 FRAMELAND: Инициализировать комплексный менеджер событий
        // 🎆 FRAMELAND: Umfassenden Ereignismanager initialisieren
        if (referenceSystemEventManager == null) {
            this.referenceSystemEventManager = new ReferenceSystemEventManager((MegaCreative) plugin);
            registerService(ReferenceSystemEventManager.class, referenceSystemEventManager);
        }
        
        // Initialize Enemy Player Restriction Manager
        // Инициализировать менеджер ограничения враждебных игроков
        // Feindlichen Spielerbeschränkungsmanager initialisieren
        if (enemyPlayerRestrictionManager == null) {
            this.enemyPlayerRestrictionManager = new EnemyPlayerRestrictionManager((MegaCreative) plugin);
            registerService(EnemyPlayerRestrictionManager.class, enemyPlayerRestrictionManager);
        }
        
        log.info("BlockConfigService initialized with " + blockConfigService.getAllBlockConfigs().size() + " block configurations");
        // BlockConfigService инициализирован с конфигурациями блоков
        // BlockConfigService initialisiert mit Blockkonfigurationen
        log.info(" YYS Reference System Event Manager initialized with comprehensive event coverage");
        // YYS Менеджер событий Reference System инициализирован с комплексным покрытием событий
        // YYS Reference System Ereignismanager mit umfassender Ereignisabdeckung initialisiert
        log.info(" YYS Reference System Advanced Execution Engine integrated with DefaultScriptEngine");
        // YYS Продвинутый движок выполнения Reference System интегрирован с DefaultScriptEngine
        // YYS Reference System Erweiterte Ausführungs-Engine in DefaultScriptEngine integriert
        log.info(" YYS Reference System Interactive GUI System initialized with 6 element types");
        // YYS Интерактивная система GUI Reference System инициализирована с 6 типами элементов
        // YYS Reference System Interaktives GUI-System mit 6 Elementtypen initialisiert
    }
    
    /**
     * Register services in DI container
     *
     * Зарегистрировать сервисы в контейнере DI
     *
     * Services im DI-Container registrieren
     */
    private void registerServicesInDI() {
        // Register all services in the dependency container for auto-injection
        // Зарегистрировать все сервисы в контейнере зависимостей для автоматического внедрения
        // Alle Services im Abhängigkeitscontainer für Auto-Injection registrieren
        for (var entry : services.entrySet()) {
            if (!dependencyContainer.isRegistered(entry.getKey())) {
                @SuppressWarnings("unchecked")
                Class<Object> keyClass = (Class<Object>) entry.getKey();
                dependencyContainer.registerSingleton(keyClass, entry.getValue());
            }
        }
    }
    
    /**
     * Post-initialize services
     *
     * Пост-инициализировать сервисы
     *
     * Services post-initialisieren
     */
    private void postInitialize() {
        // Services that need initialization after all dependencies are available
        // Сервисы, которые нуждаются в инициализации после того, как все зависимости доступны
        // Services, die nach Verfügbarkeit aller Abhängigkeiten initialisiert werden müssen
        if (worldManager instanceof com.megacreative.managers.WorldManagerImpl) {
            ((com.megacreative.managers.WorldManagerImpl) worldManager).initialize();
        }
    }
}