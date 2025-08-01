package com.megacreative;

import com.megacreative.commands.*;
import com.megacreative.listeners.*;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.CodingManager;
import com.megacreative.coding.BlockConnectionVisualizer;
import com.megacreative.coding.ScriptDebugger;
import com.megacreative.managers.*;
import com.megacreative.models.CreativeWorld;
import com.megacreative.utils.ConfigManager;
import org.bukkit.Bukkit;
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
        
        // Инициализация менеджеров
        worldManager = new WorldManager(this);
        playerManager = new PlayerManager(this);
        codingManager = new CodingManager(this);
        blockPlacementHandler = new BlockPlacementHandler(this);
        blockConnectionVisualizer = new BlockConnectionVisualizer(this);
        scriptDebugger = new ScriptDebugger(this);
        
        // Регистрируем команды и события
        registerCommands();
        registerEvents();
        
        getLogger().info("MegaCreative включен!");
    }
    
    @Override
    public void onDisable() {
        if (worldManager != null) {
            worldManager.saveAllWorlds();
        }
        getLogger().info("MegaCreative отключен!");
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
        getCommand("worldsettings").setExecutor(new WorldSettingsCommand(this));
        getCommand("testscript").setExecutor(new TestScriptCommand(this));
        getCommand("debug").setExecutor(new DebugCommand(this));
        getCommand("visualize").setExecutor(new VisualizeCommand(this));
        getCommand("createscript").setExecutor(new CreateScriptCommand(this));
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
    
    public Map<UUID, CreativeWorld> getCommentInputs() {
        return commentInputs;
    }
    
    public Map<UUID, String> getDeleteConfirmations() {
        return deleteConfirmations;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
