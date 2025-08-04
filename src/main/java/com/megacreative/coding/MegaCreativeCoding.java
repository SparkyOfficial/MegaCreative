package com.megacreative.coding;

import com.megacreative.coding.blocks.BlockRegistry;
import com.megacreative.coding.commands.BlockCommand;
import com.megacreative.coding.compatibility.ActionRegistry;
import com.megacreative.coding.core.*;
import com.megacreative.coding.gui.BlockInventoryManager;
import com.megacreative.coding.handlers.BlockPlacementHandler;
import com.megacreative.coding.actions.SendMessageAction;
import com.megacreative.coding.actions.GiveItemAction;
import com.megacreative.coding.actions.TeleportAction;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Основной класс плагина MegaCreative Coding.
 * Предоставляет визуальное программирование в Minecraft с помощью блоков.
 */
public class MegaCreativeCoding extends JavaPlugin {
    private static MegaCreativeCoding instance;
    
    // Менеджеры и системы
    private BlockRegistry blockRegistry;
    private BlockSystem blockSystem;
    private ScriptEngine scriptEngine;
    private ScriptManager scriptManager;
    private ScriptValidator scriptValidator;
    private ScriptScheduler scriptScheduler;
    private VariableManager variableManager;
    private EventSystem eventSystem;
    private BlockInventoryManager inventoryManager;
    private BlockPlacementHandler placementHandler;
    private MessageManager messageManager;
    private ActionRegistry actionRegistry;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Инициализация менеджера сообщений
        this.messageManager = new MessageManager(this);
        
        // Инициализация компонентов
        initializeComponents();
        
        // Регистрация команд
        registerCommands();
        
        // Регистрация слушателей
        registerListeners();
        
        // Отправляем приветственное сообщение в консоль
        for (String line : messageManager.getMessages("tutorial.welcome")) {
            getLogger().info(ChatColor.stripColor(line));
        }
        
        getLogger().info("MegaCreative Coding успешно включен!");
    }
    
    @Override
    public void onDisable() {
        // Остановка всех выполняющихся скриптов
        if (scriptManager != null) {
            scriptManager.stopAllScripts();
        }
        
        // Очистка ресурсов
        if (scriptScheduler != null) {
            scriptScheduler.cancelAllTasks();
        }
        
        getLogger().info("MegaCreative Coding успешно выключен!");
    }
    
    /**
     * Инициализирует все компоненты плагина.
     */
    private void initializeComponents() {
        // Инициализация реестра блоков
        this.blockRegistry = new BlockRegistry();
        
        // Инициализация системы блоков
        this.blockSystem = new BlockSystem(this, blockRegistry);
        
        // Инициализация менеджера переменных
        this.variableManager = new VariableManager(this);
        
        // Инициализация системы событий
        this.eventSystem = new EventSystem(this, blockRegistry, variableManager);
        
        // Инициализация движка скриптов
        this.scriptEngine = new ScriptEngine(this, blockRegistry, variableManager);
        
        // Инициализация менеджера скриптов
        this.scriptManager = new ScriptManager(this, scriptEngine, variableManager, eventSystem);
        
        // Инициализация валидатора скриптов
        this.scriptValidator = new ScriptValidator(blockRegistry);
        
        // Инициализация планировщика скриптов
        this.scriptScheduler = new ScriptScheduler(this, scriptManager);
        
        // Инициализация менеджера инвентарей
        this.inventoryManager = new BlockInventoryManager(this);
        
        // Инициализация обработчика размещения блоков
        this.placementHandler = new BlockPlacementHandler(
            blockSystem, 
            blockRegistry, 
            scriptManager, 
            scriptValidator
        );
        
        // Регистрация стандартных блоков
        registerDefaultBlocks();
    }
    
    /**
     * Регистрирует стандартные блоки.
     */
    private void registerDefaultBlocks() {
        // TODO: Регистрация стандартных блоков
        getLogger().info("Зарегистрированы стандартные блоки");
    }
    
    /**
     * Регистрирует команды плагина.
     */
    private void registerCommands() {
        // Регистрация команды /block
        getCommand("block").setExecutor(new BlockCommand(inventoryManager));
    }
    
    /**
     * Регистрирует слушатели событий.
     */
    private void registerListeners() {
        // Регистрация обработчика размещения блоков
        getServer().getPluginManager().registerEvents(placementHandler, this);
    }
    
    /**
     * Возвращает экземпляр плагина.
     */
    /**
     * Получает экземпляр плагина.
     * @return Экземпляр плагина
     */
    public static MegaCreativeCoding getInstance() {
        return instance;
    }
    
    /**
     * Получает реестр действий.
     * @return Реестр действий
     */
    public ActionRegistry getActionRegistry() {
        return actionRegistry;
    }
    
    /**
     * Регистрирует все доступные действия.
     */
    private void registerActions() {
        // Регистрируем действия
        actionRegistry.registerAction(new SendMessageAction());
        actionRegistry.registerAction(new GiveItemAction());
        actionRegistry.registerAction(new TeleportAction());
        // Добавьте здесь другие действия по мере необходимости
        
        getLogger().info("Зарегистрировано " + actionRegistry.getActionCount() + " действий");
    }
    
    // Геттеры для доступа к компонентам
    
    public BlockRegistry getBlockRegistry() {
        return blockRegistry;
    }
    
    public BlockSystem getBlockSystem() {
        return blockSystem;
    }
    
    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }
    
    public ScriptManager getScriptManager() {
        return scriptManager;
    }
    
    public ScriptValidator getScriptValidator() {
        return scriptValidator;
    }
    
    public ScriptScheduler getScriptScheduler() {
        return scriptScheduler;
    }
    
    public VariableManager getVariableManager() {
        return variableManager;
    }
    
    public EventSystem getEventSystem() {
        return eventSystem;
    }
    
    public BlockInventoryManager getInventoryManager() {
        return inventoryManager;
    }
    
    public BlockPlacementHandler getPlacementHandler() {
        return placementHandler;
    }
    
    /**
     * Возвращает менеджер сообщений.
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }
}
