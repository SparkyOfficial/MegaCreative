package ua.sparkybeta.sparkybetacreative;

import org.bukkit.plugin.java.JavaPlugin;
import ua.sparkybeta.sparkybetacreative.commands.CommandManager;
import ua.sparkybeta.sparkybetacreative.commands.coding.BlockCommand;
import ua.sparkybeta.sparkybetacreative.commands.coding.ValueCommand;
import ua.sparkybeta.sparkybetacreative.commands.mode.BuildCommand;
import ua.sparkybeta.sparkybetacreative.commands.mode.DevCommand;
import ua.sparkybeta.sparkybetacreative.commands.mode.PlayCommand;
import ua.sparkybeta.sparkybetacreative.commands.utility.HubCommand;
import ua.sparkybeta.sparkybetacreative.commands.utility.WorldsCommand;
import ua.sparkybeta.sparkybetacreative.commands.world.CodeSaveCommand;
import ua.sparkybeta.sparkybetacreative.commands.world.CreateCommand;
import ua.sparkybeta.sparkybetacreative.commands.world.DeleteCommand;
import ua.sparkybeta.sparkybetacreative.coding.executable.CodeExecutor;
import ua.sparkybeta.sparkybetacreative.listeners.ChatInputListener;
import ua.sparkybeta.sparkybetacreative.listeners.CodeBlockInteractListener;
import ua.sparkybeta.sparkybetacreative.listeners.MenuItemsListener;
import ua.sparkybeta.sparkybetacreative.listeners.WorldProtectionListener;
import ua.sparkybeta.sparkybetacreative.listeners.SignBreakListener;
import ua.sparkybeta.sparkybetacreative.util.ChatInputManager;
import ua.sparkybeta.sparkybetacreative.worlds.FileWorldManager;
import ua.sparkybeta.sparkybetacreative.worlds.WorldManager;

public final class SparkyBetaCreative extends JavaPlugin {

    private static SparkyBetaCreative instance;
    private WorldManager worldManager;
    private ChatInputManager chatInputManager;

    @Override
    public void onEnable() {
        instance = this;
        this.worldManager = new FileWorldManager();
        this.worldManager.onEnable();
        this.chatInputManager = new ChatInputManager();

        registerCommands();
        registerListeners();

        getLogger().info("SparkyBetaCreative has been enabled!");
    }

    @Override
    public void onDisable() {
        if (this.worldManager != null) {
            this.worldManager.onDisable();
        }
        getLogger().info("SparkyBetaCreative has been disabled!");
    }

    private void registerCommands() {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CreateCommand());
        commandManager.register(new DeleteCommand());
        commandManager.register(new CodeSaveCommand());
        getCommand("world").setExecutor(commandManager);
        getCommand("world").setTabCompleter(commandManager);

        getCommand("value").setExecutor(new ValueCommand());
        getCommand("play").setExecutor(new PlayCommand());
        getCommand("dev").setExecutor(new DevCommand());
        getCommand("build").setExecutor(new BuildCommand());
        getCommand("hub").setExecutor(new HubCommand());
        getCommand("worlds").setExecutor(new WorldsCommand());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MenuItemsListener(), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this.chatInputManager), this);
        getServer().getPluginManager().registerEvents(new WorldProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new CodeExecutor(), this);
        getServer().getPluginManager().registerEvents(new CodeBlockInteractListener(), this);
        getServer().getPluginManager().registerEvents(new SignBreakListener(), this);
    }

    public static SparkyBetaCreative getInstance() {
        return instance;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public ChatInputManager getChatInputManager() {
        return chatInputManager;
    }
} 