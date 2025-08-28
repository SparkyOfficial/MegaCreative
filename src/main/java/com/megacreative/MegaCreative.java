package com.megacreative;

import com.megacreative.commands.*;
import com.megacreative.listeners.*;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.CodingManagerImpl;
import com.megacreative.coding.AutoConnectionManager;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.containers.BlockContainerManager;
import com.megacreative.coding.executors.ExecutorEngine;
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

    private ScriptDebugger scriptDebugger;
    private DataManager dataManager;
    private TemplateManager templateManager;
    private ScoreboardManager scoreboardManager;
    private TrustedPlayerManager trustedPlayerManager;

    // === НОВЫЕ СИСТЕМЫ РАЗРАБОТКИ ===
    private AutoConnectionManager autoConnectionManager;
    private DevInventoryManager devInventoryManager;
    private VariableManager variableManager;
    private BlockContainerManager containerManager;
    private ExecutorEngine executorEngine;
    
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
        

        // Валидация конфигурации
        try {
            ConfigurationValidator validator = new ConfigurationValidator(this);
            validator.validateMainConfig();
            validator.validateCodingBlocksConfig();
        } catch (ConfigurationException e) {
            // Создаем резервную копию и пытаемся восстановить
            ConfigurationValidator validator = new ConfigurationValidator(this);
            validator.createBackup();
            try {
                validator.restoreFromBackup();
            } catch (ConfigurationException restoreEx) {
                // Игнорируем ошибки восстановления
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

        this.scriptDebugger = new ScriptDebugger(this);
        this.dataManager = new DataManager(this);
        this.templateManager = new TemplateManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.trustedPlayerManager = new TrustedPlayerManager(this);
        
        // --- ИНИЦИАЛИЗАЦИЯ НОВОГО МЕНЕДЖЕРА ---
        this.blockConfigManager = new BlockConfigManager(this);
        
        // --- ИНИЦИАЛИЗАЦИЯ КОНФИГУРАЦИИ БЛОКОВ ---
        this.blockConfiguration = new BlockConfiguration(this);
        
        // === ИНИЦИАЛИЗАЦИЯ НОВЫХ СИСТЕМ РАЗРАБОТКИ ===
        this.autoConnectionManager = new AutoConnectionManager(this);
        this.devInventoryManager = new DevInventoryManager(this);
        this.variableManager = new VariableManager(this);
        this.containerManager = new BlockContainerManager(this);
        this.executorEngine = new ExecutorEngine(this);
        
        // Инициализация после создания всех менеджеров
        this.worldManager.initialize();
        
        // Регистрируем команды и события
        registerCommands();
        registerEvents();
        

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
        getCommand("createscript").setExecutor(new CreateScriptCommand(this));
        getCommand("stoprepeat").setExecutor(new StopRepeatCommand(this));
        getCommand("status").setExecutor(new StatusCommand(this));
        
        // === НОВЫЕ КОМАНДЫ ===
        getCommand("addfloor").setExecutor(new AddFloorCommand(this));
        getCommand("workspace").setExecutor(new WorkspaceCommand(this));
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
        
        // === РЕГИСТРАЦИЯ НОВЫХ СИСТЕМ ===
        getServer().getPluginManager().registerEvents(autoConnectionManager, this);
        getServer().getPluginManager().registerEvents(devInventoryManager, this);
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
    
    // === ГЕТТЕРЫ ДЛЯ НОВЫХ СИСТЕМ ===
    
    public AutoConnectionManager getAutoConnectionManager() {
        return autoConnectionManager;
    }
    
    public DevInventoryManager getDevInventoryManager() {
        return devInventoryManager;
    }
    
    public VariableManager getVariableManager() {
        return variableManager;
    }
    
    public BlockContainerManager getContainerManager() {
        return containerManager;
    }
    
    public ExecutorEngine getExecutorEngine() {
        return executorEngine;
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
    }
}
