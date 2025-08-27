package com.megacreative;

import com.megacreative.commands.*;
import com.megacreative.listeners.*;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.CodingManagerImpl;
<<<<<<< HEAD
import com.megacreative.coding.BlockConnectionVisualizer;
=======

>>>>>>> ba7215a (Я вернулся)
import com.megacreative.coding.ScriptDebugger;
import com.megacreative.coding.CodingItems;
import com.megacreative.coding.BlockConfiguration;
import com.megacreative.managers.*;
import com.megacreative.interfaces.*;
import com.megacreative.models.CreativeWorld;
import com.megacreative.utils.ConfigManager;
import com.megacreative.core.DependencyContainer;
import com.megacreative.config.ConfigurationValidator;
import com.megacreative.exceptions.ConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
<<<<<<< HEAD
import java.util.logging.Logger;
=======

>>>>>>> ba7215a (Я вернулся)
import com.megacreative.commands.CreateScriptCommand;
import java.util.ArrayList;
import java.util.List;
import com.megacreative.managers.BlockConfigManager;

public class MegaCreative extends JavaPlugin {
    
    private static MegaCreative instance;
    private DependencyContainer dependencyContainer;
    private ConfigManager configManager;
    private IWorldManager worldManager;
    private IPlayerManager playerManager;
    private ICodingManager codingManager;
    private BlockPlacementHandler blockPlacementHandler;
<<<<<<< HEAD
    private BlockConnectionVisualizer blockConnectionVisualizer;
=======

>>>>>>> ba7215a (Я вернулся)
    private ScriptDebugger scriptDebugger;
    private DataManager dataManager;
    private TemplateManager templateManager;
    private ScoreboardManager scoreboardManager;
    private TrustedPlayerManager trustedPlayerManager;
<<<<<<< HEAD
    private Logger logger;
=======

>>>>>>> ba7215a (Я вернулся)
    
    // --- НОВЫЙ МЕНЕДЖЕР ДЛЯ ВИРТУАЛЬНЫХ ИНВЕНТАРЕЙ ---
    private BlockConfigManager blockConfigManager;
    
    // --- НОВЫЙ МЕНЕДЖЕР ДЛЯ КОНФИГУРАЦИИ БЛОКОВ ---
    private BlockConfiguration blockConfiguration;

    // Maps для хранения состояния
    private Map<UUID, CreativeWorld> commentInputs = new HashMap<>();
    private Map<UUID, String> deleteConfirmations = new HashMap<>();
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Инициализация Dependency Container
        this.dependencyContainer = new DependencyContainer();
        
        // Инициализация конфигурации
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
<<<<<<< HEAD
        // Инициализация системы логирования
        com.megacreative.utils.LogUtils.initialize(this);
        
=======
>>>>>>> ba7215a (Я вернулся)
        // Валидация конфигурации
        try {
            ConfigurationValidator validator = new ConfigurationValidator(this);
            validator.validateMainConfig();
            validator.validateCodingBlocksConfig();
<<<<<<< HEAD
            com.megacreative.utils.LogUtils.info("Конфигурация успешно валидирована");
        } catch (ConfigurationException e) {
            com.megacreative.utils.LogUtils.error("Ошибка валидации конфигурации: " + e.getMessage());
=======
        } catch (ConfigurationException e) {
>>>>>>> ba7215a (Я вернулся)
            // Создаем резервную копию и пытаемся восстановить
            ConfigurationValidator validator = new ConfigurationValidator(this);
            validator.createBackup();
            try {
                validator.restoreFromBackup();
<<<<<<< HEAD
                com.megacreative.utils.LogUtils.info("Конфигурация восстановлена из резервной копии");
            } catch (ConfigurationException restoreEx) {
                com.megacreative.utils.LogUtils.error("Не удалось восстановить конфигурацию: " + restoreEx.getMessage());
=======
            } catch (ConfigurationException restoreEx) {
                // Игнорируем ошибки восстановления
>>>>>>> ba7215a (Я вернулся)
            }
        }
        
        // Инициализируем менеджеры через DI контейнер
        this.worldManager = new WorldManagerImpl(this);
        this.playerManager = new PlayerManagerImpl(this);
        this.codingManager = new CodingManagerImpl(this);
        
        // Регистрируем сервисы в DI контейнере
        dependencyContainer.register(IWorldManager.class, worldManager);
        dependencyContainer.register(IPlayerManager.class, playerManager);
        dependencyContainer.register(ICodingManager.class, codingManager);
        this.blockPlacementHandler = new BlockPlacementHandler(this);
<<<<<<< HEAD
        this.blockConnectionVisualizer = new BlockConnectionVisualizer(this);
=======

>>>>>>> ba7215a (Я вернулся)
        this.scriptDebugger = new ScriptDebugger(this);
        this.dataManager = new DataManager(this);
        this.templateManager = new TemplateManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.trustedPlayerManager = new TrustedPlayerManager(this);
        
        // --- ИНИЦИАЛИЗАЦИЯ НОВОГО МЕНЕДЖЕРА ---
        this.blockConfigManager = new BlockConfigManager(this);
        
        // --- ИНИЦИАЛИЗАЦИЯ КОНФИГУРАЦИИ БЛОКОВ ---
        this.blockConfiguration = new BlockConfiguration(this);
        
        // Инициализация после создания всех менеджеров
        this.worldManager.initialize();
        
        // Регистрируем команды и события
        registerCommands();
        registerEvents();
        
<<<<<<< HEAD
        this.logger = getLogger();
        this.logger.info("=== MEGACREATIVE ЗАГРУЖЕН ===");
        this.logger.info("Версия: 1.0.0");
        this.logger.info("Команды зарегистрированы: " + getServer().getPluginCommand("megacreative") != null ? "✓" : "✗");
        this.logger.info("Менеджеры инициализированы:");
        this.logger.info("  - WorldManager: " + (worldManager != null ? "✓" : "✗"));
        this.logger.info("  - PlayerManager: " + (playerManager != null ? "✓" : "✗"));
        this.logger.info("  - CodingManager: " + (codingManager != null ? "✓" : "✗"));
        this.logger.info("MegaCreative готов к работе!");
=======

>>>>>>> ba7215a (Я вернулся)
    }
    
    @Override
    public void onDisable() {
        // Останавливаем все повторяющиеся задачи
        com.megacreative.coding.actions.RepeatTriggerAction.stopAllRepeatingTasks();
        
        // Сохраняем все данные
        if (dataManager != null) {
            dataManager.saveAllData();
        }
        
        // Сохраняем миры
        if (worldManager != null) {
            worldManager.saveAllWorlds();
        }
        
<<<<<<< HEAD
        if (logger != null) {
            logger.info("=== MEGACREATIVE ОТКЛЮЧЕН ===");
            logger.info("Все данные сохранены.");
        }
=======

>>>>>>> ba7215a (Я вернулся)
    }
    
    private void registerCommands() {
        getCommand("megacreative").setExecutor(new MainCommand(this));
        getCommand("myworlds").setExecutor(new MyWorldsCommand(this));
        getCommand("worldbrowser").setExecutor(new WorldBrowserCommand(this));
        getCommand("join").setExecutor(new JoinCommand(this));
        getCommand("play").setExecutor(new PlayCommand(this));
        getCommand("trusted").setExecutor(new TrustedPlayerCommand(this));
        getCommand("build").setExecutor(new BuildCommand(this));
        getCommand("dev").setExecutor(new DevCommand(this));
        getCommand("hub").setExecutor(new HubCommand(this));
        getCommand("savescript").setExecutor(new SaveScriptCommand(this));
        getCommand("templates").setExecutor(new TemplatesCommand(this));
        getCommand("scripts").setExecutor(new ScriptsCommand(this));
        getCommand("worldsettings").setExecutor(new WorldSettingsCommand(this));
        getCommand("testscript").setExecutor(new TestScriptCommand(this));
        getCommand("debug").setExecutor(new DebugCommand(this));
<<<<<<< HEAD
        getCommand("visualize").setExecutor(new VisualizeCommand(this));
=======

>>>>>>> ba7215a (Я вернулся)
        getCommand("createscript").setExecutor(new CreateScriptCommand(this));
        getCommand("stoprepeat").setExecutor(new StopRepeatCommand(this));
        getCommand("status").setExecutor(new StatusCommand(this));
    }
    
    private void registerEvents() {
        // Регистрируем слушатели событий
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new DataItemListener(), this);
        getServer().getPluginManager().registerEvents(blockPlacementHandler, this);
        getServer().getPluginManager().registerEvents(new WorldInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        
        // Новые слушатели для расширенных событий
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
        
        // РЕГИСТРАЦИЯ НОВОГО СЛУШАТЕЛЯ ДЛЯ ЗАЩИТЫ ПРЕДМЕТОВ
        getServer().getPluginManager().registerEvents(new DevWorldProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(this), this);
    }
    
    public static MegaCreative getInstance() {
        return instance;
    }
    
    public IWorldManager getWorldManager() {
        return worldManager;
    }
    
    public IPlayerManager getPlayerManager() {
        return playerManager;
    }

    public ICodingManager getCodingManager() {
        return codingManager;
    }

    public BlockPlacementHandler getBlockPlacementHandler() {
        return blockPlacementHandler;
    }

<<<<<<< HEAD
    public BlockConnectionVisualizer getBlockConnectionVisualizer() {
        return blockConnectionVisualizer;
    }
=======

>>>>>>> ba7215a (Я вернулся)
    
    public ScriptDebugger getScriptDebugger() {
        return scriptDebugger;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public TemplateManager getTemplateManager() {
        return templateManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public TrustedPlayerManager getTrustedPlayerManager() {
        return trustedPlayerManager;
    }
    
    public BlockConfigManager getBlockConfigManager() {
        return blockConfigManager;
    }
    
    public BlockConfiguration getBlockConfiguration() {
        return blockConfiguration;
    }
    
    public Map<UUID, CreativeWorld> getCommentInputs() {
        return commentInputs;
    }
    
    public Map<UUID, String> getDeleteConfirmations() {
        return deleteConfirmations;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DependencyContainer getDependencyContainer() {
        return dependencyContainer;
    }
    
    private void startInventoryChecker() {
        // Убираем неэффективную проверку каждые 5 секунд
        // Вместо этого будем проверять при входе в dev-мир через PlayerWorldChangeListener
<<<<<<< HEAD
        getLogger().info("Проверка инвентаря перенесена на событие смены мира");
    }

    // Этот метод-хелпер нужно тоже добавить в MegaCreative.java
    private boolean hasAllCodingItems(Player player) {
        // Упрощенная проверка. Проверяем наличие хотя бы нескольких ключевых предметов.
        // Для 100% точности нужно проверять каждый предмет из giveCodingItems
        boolean hasLinker = false;
        boolean hasInspector = false;
        boolean hasEventBlock = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                String name = item.getItemMeta().getDisplayName();
                if (name.contains("Связующий жезл")) hasLinker = true;
                if (name.contains("Инспектор блоков")) hasInspector = true;
                if (name.contains("Событие игрока")) hasEventBlock = true;
            }
        }
        return hasLinker && hasInspector && hasEventBlock;
    }
    
    // Новый метод для определения недостающих предметов
    private List<String> getMissingCodingItems(Player player) {
        List<String> missingItems = new ArrayList<>();
        
        // Проверяем наличие ключевых предметов
        boolean hasLinker = false;
        boolean hasInspector = false;
        boolean hasEventBlock = false;
        boolean hasActionBlock = false;
        boolean hasConditionBlock = false;
        boolean hasVariableBlock = false;
        boolean hasRepeatBlock = false;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                String name = item.getItemMeta().getDisplayName();
                if (name.contains("Связующий жезл")) hasLinker = true;
                if (name.contains("Инспектор блоков")) hasInspector = true;
                if (name.contains("Событие игрока")) hasEventBlock = true;
                if (name.contains("Действие игрока")) hasActionBlock = true;
                if (name.contains("Условие")) hasConditionBlock = true;
                if (name.contains("Переменная")) hasVariableBlock = true;
                if (name.contains("Повторить")) hasRepeatBlock = true;
            }
        }
        
        if (!hasLinker) missingItems.add("Связующий жезл");
        if (!hasInspector) missingItems.add("Инспектор блоков");
        if (!hasEventBlock) missingItems.add("Блок события");
        if (!hasActionBlock) missingItems.add("Блок действия");
        if (!hasConditionBlock) missingItems.add("Блок условия");
        if (!hasVariableBlock) missingItems.add("Блок переменной");
        if (!hasRepeatBlock) missingItems.add("Блок повтора");
        
        return missingItems;
    }
=======
    }


>>>>>>> ba7215a (Я вернулся)
}
