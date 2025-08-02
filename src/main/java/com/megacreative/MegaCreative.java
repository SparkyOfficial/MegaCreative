package com.megacreative;

import com.megacreative.commands.*;
import com.megacreative.listeners.*;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.CodingManager;
import com.megacreative.coding.BlockConnectionVisualizer;
import com.megacreative.coding.ScriptDebugger;
import com.megacreative.coding.CodingItems;
import com.megacreative.managers.*;
import com.megacreative.models.CreativeWorld;
import com.megacreative.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import com.megacreative.commands.CreateScriptCommand;

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
    private Logger logger;
    
    // Система комментариев
    private Map<UUID, CreativeWorld> commentInputs = new HashMap<>();
    // Система подтверждения удаления мира
    private Map<UUID, String> deleteConfirmations = new HashMap<>();
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Инициализация конфигурации
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Инициализируем менеджеры
        this.worldManager = new WorldManager(this);
        this.playerManager = new PlayerManager(this);
        this.codingManager = new CodingManager(this);
        this.blockPlacementHandler = new BlockPlacementHandler(this);
        this.blockConnectionVisualizer = new BlockConnectionVisualizer(this);
        this.scriptDebugger = new ScriptDebugger(this);
        this.dataManager = new DataManager(this);
        this.templateManager = new TemplateManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        
        // Регистрируем команды и события
        registerCommands();
        registerEvents();
        
        // ЗАПУСК ПРОВЕРКИ ИНВЕНТАРЕЙ
        startInventoryChecker();
        
        getLogger().info("MegaCreative включен!");
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
        
        logger.info("MegaCreative отключен!");
    }
    
    private void registerCommands() {
        getCommand("megacreative").setExecutor(new MainCommand(this));
        getCommand("myworlds").setExecutor(new MyWorldsCommand(this));
        getCommand("worldbrowser").setExecutor(new WorldBrowserCommand(this));
        getCommand("join").setExecutor(new JoinCommand(this));
        getCommand("play").setExecutor(new PlayCommand(this));
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
                        if (!hasAllCodingItems(player)) {
                            player.getInventory().clear();
                            CodingItems.giveCodingItems(player);
                            player.sendMessage("§e§l!§r §eВаш инвентарь был обновлен, чтобы содержать все инструменты для кодинга.");
                        }
                    }
                }
            }
        }.runTaskTimer(this, 100L, 100L); // Проверка каждые 5 секунд (100 тиков)
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
}
