package com.megacreative.core;

import com.megacreative.MegaCreative;
import com.megacreative.commands.*;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.managers.DevInventoryManager;
import com.megacreative.coding.BlockPlacementHandler;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import java.util.logging.Logger;

/**
 * Registry for plugin commands that uses dependency injection
 * Registers commands with their required dependencies
 *
 * Реестр команд плагина, использующий внедрение зависимостей
 * Регистрирует команды с их необходимыми зависимостями
 *
 * Befehlsregister für Plugin-Befehle, das Dependency Injection verwendet
 * Registriert Befehle mit ihren erforderlichen Abhängigkeiten
 */
public class CommandRegistry {
    
    private static final Logger log = Logger.getLogger(CommandRegistry.class.getName());
    
    private final MegaCreative plugin;
    private final ServiceRegistry serviceRegistry;
    
    public CommandRegistry(MegaCreative plugin, ServiceRegistry serviceRegistry) {
        this.plugin = plugin;
        this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * Register all plugin commands with their dependencies
     */
    public void registerCommands() {
        try {
            // Register build command with its dependencies
            registerBuildCommand();
            
            // Register other commands here as we refactor them
            registerSimpleCommands();
            
            log.info("Commands registered successfully");
        } catch (Exception e) {
            log.severe("Failed to register commands: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Register the build command with its dependencies
     */
    private void registerBuildCommand() {
        if (plugin == null || serviceRegistry == null) return;
    
        try {
            if (plugin.getCommand("build") != null) {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                PlayerModeManager playerModeManager = serviceRegistry.getPlayerModeManager();
                DevInventoryManager devInventoryManager = serviceRegistry.getDevInventoryManager();
                BlockPlacementHandler blockPlacementHandler = serviceRegistry.getBlockPlacementHandler();
                
                BuildCommand buildCommand = new BuildCommand(
                    worldManager,
                    playerModeManager,
                    devInventoryManager,
                    blockPlacementHandler
                );
                
                plugin.getCommand("build").setExecutor(buildCommand);
            }
        } catch (Exception e) {
            log.severe("Failed to register build command: " + e.getMessage());
        }
    }

    /**
     * Register simple commands that don't require many dependencies
     */
    private void registerSimpleCommands() {
        if (plugin == null) return;
        
        try {
            // Register commands with proper dependency injection
            if (plugin.getCommand("megacreative") != null) {
                plugin.getCommand("megacreative").setExecutor(new MainCommand(plugin));
            }
            
            if (plugin.getCommand("myworlds") != null) {
                plugin.getCommand("myworlds").setExecutor(new MyWorldsCommand(plugin));
            }
            
            if (plugin.getCommand("worldbrowser") != null) {
                plugin.getCommand("worldbrowser").setExecutor(new WorldBrowserCommand(plugin));
            }
            
            if (plugin.getCommand("trusted") != null) {
                plugin.getCommand("trusted").setExecutor(new TrustedPlayerCommand(plugin));
            }
            
            if (plugin.getCommand("dev") != null) {
                plugin.getCommand("dev").setExecutor(new DevCommand(plugin));
            }
            
            if (plugin.getCommand("worldsettings") != null) {
                plugin.getCommand("worldsettings").setExecutor(new WorldSettingsCommand(plugin));
            }
            
            if (plugin.getCommand("addfloor") != null) {
                plugin.getCommand("addfloor").setExecutor(new AddFloorCommand(plugin));
            }
            
            if (plugin.getCommand("workspace") != null) {
                plugin.getCommand("workspace").setExecutor(new WorkspaceCommand(plugin));
            }
            
            if (plugin.getCommand("delete") != null) {
                plugin.getCommand("delete").setExecutor(new DeleteCommand(plugin));
            }
            
            if (plugin.getCommand("confirmdelete") != null) {
                plugin.getCommand("confirmdelete").setExecutor(new ConfirmDeleteCommand(plugin));
            }
            
            // Register interactive GUI command
            if (plugin.getCommand("interactive") != null) {
                InteractiveCommand interactiveCommand = new InteractiveCommand(plugin);
                plugin.getCommand("interactive").setExecutor(interactiveCommand);
                plugin.getCommand("interactive").setTabCompleter(interactiveCommand);
            }
            
            // Enemy player management command
            if (plugin.getCommand("enemy") != null) {
                EnemyPlayerCommand enemyPlayerCommand = new EnemyPlayerCommand(plugin);
                plugin.getCommand("enemy").setExecutor(enemyPlayerCommand);
                plugin.getCommand("enemy").setTabCompleter(enemyPlayerCommand);
            }
            
            // Performance monitoring command
            if (plugin.getCommand("performance") != null) {
                PerformanceCommand performanceCommand = new PerformanceCommand(plugin);
                plugin.getCommand("performance").setExecutor(performanceCommand);
                plugin.getCommand("performance").setTabCompleter(performanceCommand);
            }
            
            // Global chat command
            if (plugin.getCommand("cc") != null) {
                plugin.getCommand("cc").setExecutor(new GlobalChatCommand(plugin));
            }
            
            // Register create world command
            if (plugin.getCommand("create") != null && serviceRegistry != null) {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                plugin.getCommand("create").setExecutor(new CreateWorldCommand(plugin, worldManager));
            }
        } catch (Exception e) {
            log.severe("Failed to register simple commands: " + e.getMessage());
        }
    }
}
