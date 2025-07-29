package ua.sparkycreative.worldsystem;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ua.sparkycreative.worldsystem.logic.*;

public class WorldSystemPlugin extends JavaPlugin {
    private static WorldSystemPlugin instance;
    private WorldManager worldManager;
    private WorldsBrowserGUI worldsBrowserGUI;

    @Override
    public void onEnable() {
        instance = this;
        this.worldManager = new WorldManager(this);
        this.worldsBrowserGUI = new WorldsBrowserGUI(worldManager);
        new WorldBorderListener();
        new DevWorldListener();
        getCommand("worlds").setExecutor(new WorldsCommand());
        getLogger().info("WorldSystem enabled!");
        // EVENT 'по таймеру'
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (!world.getName().endsWith("_dev")) continue;
                    LogicBlockManager manager = DevWorldListener.getLogicManager();
                    LogicExecutor executor = DevWorldListener.getExecutor();
                    for (LogicBlock block : manager.getBlocksOfType(world, LogicBlockType.EVENT)) {
                        String eventType = (String) block.getParams().getOrDefault("event", "по клику");
                        if ("по таймеру".equals(eventType)) {
                            for (Player player : world.getPlayers()) {
                                executor.triggerEvent(world, block, player);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 40, 40); // каждые 2 секунды
        // TODO: регистрация команд, инициализация системы миров
    }

    @Override
    public void onDisable() {
        getLogger().info("WorldSystem disabled!");
    }

    public static WorldSystemPlugin getInstance() {
        return instance;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }
} 