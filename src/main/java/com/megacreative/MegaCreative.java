package com.megacreative;

import com.megacreative.commands.*;
import com.megacreative.listeners.*;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.CodingManager;
import com.megacreative.coding.BlockConnectionVisualizer;
import com.megacreative.coding.ScriptDebugger;
import com.megacreative.coding.CodingItems;
import com.megacreative.coding.BlockConfiguration;
import com.megacreative.managers.*;
import com.megacreative.models.CreativeWorld;
import com.megacreative.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.logging.Logger;
import com.megacreative.commands.CreateScriptCommand;
import java.util.ArrayList;
import java.util.List;
import com.megacreative.managers.BlockConfigManager;

public class MegaCreative extends JavaPlugin {
    
    private static MegaCreative instance;
    private ConfigManager configManager;
    private WorldManager worldManager;
    private PlayerManager playerManager;
    private CodingManager codingManager;
    private BlockPlacementHandler blockPlacementHandler;
    private BlockConnectionVisualizer blockConnectionVisualizer;
    private ScriptDebugger scriptDebugger;
    private DataManager dataManager;
    private TemplateManager templateManager;
    private ScoreboardManager scoreboardManager;
    private TrustedPlayerManager trustedPlayerManager;

    
    // --- НОВЫЙ МЕНЕДЖЕР ДЛЯ ВИРТУАЛЬНЫХ ИНВЕНТАРЕЙ ---
    private BlockConfigManager blockConfigManager;
    
    // --- НОВЫЙ МЕНЕДЖЕР ДЛЯ КОНФИГУРАЦИИ БЛОКОВ ---
    private BlockConfiguration blockConfiguration;

    // Maps для хранения состояния
    private Map<UUID, CreativeWorld> commentInputs = new ConcurrentHashMap<>();
    private Map<UUID, String> deleteConfirmations = new ConcurrentHashMap<>();
    private Map<UUID, Long> lastItemCheckTime = new ConcurrentHashMap<>(); // Для предотвращения спама
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Инициализация конфигурации
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Инициализируем менеджеры в правильном порядке
        this.codingManager = new CodingManager(this);
        this.worldManager = new WorldManager(this);
        this.playerManager = new PlayerManager(this);
        this.blockPlacementHandler = new BlockPlacementHandler(this);
        this.blockConnectionVisualizer = new BlockConnectionVisualizer(this);
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
        
        // ЗАПУСК ПРОВЕРКИ ИНВЕНТАРЕЙ
        startInventoryChecker();
        
        getLogger().info("MegaCreative включен!");
    }
    
    @Override
    public void onDisable() {
        // 1. Останавливаем все повторяющиеся задачи
        com.megacreative.coding.actions.RepeatTriggerAction.stopAllRepeatingTasks();
        
        // 2. Завершаем работу менеджеров (это должно включать сохранение данных)
        if (worldManager != null) {
            worldManager.shutdown(); // Предполагается, что этот метод сохраняет миры и останавливает сервисы
        }
        if (dataManager != null) {
            dataManager.shutdown(); // Предполагается, что этот метод сохраняет данные и останавливает сервисы
        }
        
        // Очищаем остальные ресурсы, если необходимо
        
        getLogger().info("MegaCreative отключен!");
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
        getCommand("visualize").setExecutor(new VisualizeCommand(this));
        getCommand("createscript").setExecutor(new CreateScriptCommand(this));
        getCommand("stoprepeat").setExecutor(new StopRepeatCommand(this));
        getCommand("status").setExecutor(new StatusCommand(this));
        getCommand("testhybrid").setExecutor(new TestHybridCommand(this));
        getCommand("cleanworlds").setExecutor(new CleanWorldsCommand(this));
        getCommand("create").setExecutor(new CreateWorldCommand(this));
        getCommand("savefunction").setExecutor(new SaveFunctionCommand(this));
        getCommand("scriptexport").setExecutor(new ScriptExportCommand(this));
        getCommand("testblockpersistence").setExecutor(new TestBlockPersistenceCommand(this));
    }
    
    private void registerEvents() {
        // Регистрируем слушатели событий
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new DataItemListener(), this);
        getServer().getPluginManager().registerEvents(blockPlacementHandler, this);
        getServer().getPluginManager().registerEvents(new WorldInteractListener(this), this);
        // InventoryClickListener удален - логика перенесена в GuiListener
        
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
    
    public WorldManager getWorldManager() {
        return worldManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public CodingManager getCodingManager() {
        return codingManager;
    }

    public BlockPlacementHandler getBlockPlacementHandler() {
        return blockPlacementHandler;
    }

    public BlockConnectionVisualizer getBlockConnectionVisualizer() {
        return blockConnectionVisualizer;
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
    
    public Map<UUID, CreativeWorld> getCommentInputs() {
        return commentInputs;
    }
    
    public Map<UUID, String> getDeleteConfirmations() {
        return deleteConfirmations;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    private void startInventoryChecker() {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().getName().endsWith("_dev")) {
                        // ИСПРАВЛЕНИЕ: Перевыдаем ВСЕ предметы для кодинга
                        UUID playerId = player.getUniqueId();
                        long currentTime = System.currentTimeMillis();
                        long lastCheck = lastItemCheckTime.getOrDefault(playerId, 0L);
                        
                        // Перевыдаем предметы только раз в 2 минуты
                        if (currentTime - lastCheck > 120000) {
                            // Очищаем инвентарь от старых предметов кодинга
                            clearOldCodingItems(player);
                            // Выдаем все предметы заново
                            CodingItems.giveAllCodingItems(player);
                            player.sendMessage("§a§l!§r §aИнструменты для кодинга обновлены!");
                            lastItemCheckTime.put(playerId, currentTime);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 200L, 1200L); // Проверка каждые 60 секунд
    }
    
    // ИСПРАВЛЕНИЕ: Очищаем старые предметы кодинга
    private void clearOldCodingItems(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String name = item.getItemMeta().getDisplayName();
                if (name.contains("Связующий жезл") || name.contains("Инспектор блоков") || 
                    name.contains("Событие игрока") || name.contains("Действие игрока") || 
                    name.contains("Условие игрока") || name.contains("Присвоить переменную") || 
                    name.contains("Повторить N раз") || name.contains("Блок кода")) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
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
                if (name.contains("Условие игрока")) hasConditionBlock = true;
                if (name.contains("Присвоить переменную")) hasVariableBlock = true; // Исправлено!
                if (name.contains("Повторить N раз")) hasRepeatBlock = true;
            }
        }
        
        // Проверяем только самые важные предметы, чтобы избежать спама
        if (!hasLinker) missingItems.add("Связующий жезл");
        if (!hasInspector) missingItems.add("Инспектор блоков");
        if (!hasEventBlock) missingItems.add("Блок события");
        
        // Добавляем остальные только если их действительно нет
        if (!hasActionBlock && missingItems.size() < 3) missingItems.add("Блок действия");
        if (!hasConditionBlock && missingItems.size() < 3) missingItems.add("Блок условия");
        if (!hasVariableBlock && missingItems.size() < 3) missingItems.add("Блок переменной");
        if (!hasRepeatBlock && missingItems.size() < 3) missingItems.add("Блок повтора");
        
        return missingItems;
    }
}
