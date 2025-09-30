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
            org.bukkit.command.PluginCommand buildCmd = plugin.getCommand("build");
            if (buildCmd != null) {
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
                
                buildCmd.setExecutor(buildCommand);
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
            org.bukkit.command.PluginCommand megacreativeCmd = plugin.getCommand("megacreative");
            if (megacreativeCmd != null) {
                megacreativeCmd.setExecutor(new MainCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand myworldsCmd = plugin.getCommand("myworlds");
            if (myworldsCmd != null) {
                myworldsCmd.setExecutor(new MyWorldsCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand worldbrowserCmd = plugin.getCommand("worldbrowser");
            if (worldbrowserCmd != null) {
                worldbrowserCmd.setExecutor(new WorldBrowserCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand trustedCmd = plugin.getCommand("trusted");
            if (trustedCmd != null) {
                trustedCmd.setExecutor(new TrustedPlayerCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand devCmd = plugin.getCommand("dev");
            if (devCmd != null) {
                devCmd.setExecutor(new DevCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand worldsettingsCmd = plugin.getCommand("worldsettings");
            if (worldsettingsCmd != null) {
                worldsettingsCmd.setExecutor(new WorldSettingsCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand addfloorCmd = plugin.getCommand("addfloor");
            if (addfloorCmd != null) {
                addfloorCmd.setExecutor(new AddFloorCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand workspaceCmd = plugin.getCommand("workspace");
            if (workspaceCmd != null) {
                workspaceCmd.setExecutor(new WorkspaceCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand deleteCmd = plugin.getCommand("delete");
            if (deleteCmd != null) {
                deleteCmd.setExecutor(new DeleteCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand confirmdeleteCmd = plugin.getCommand("confirmdelete");
            if (confirmdeleteCmd != null) {
                confirmdeleteCmd.setExecutor(new ConfirmDeleteCommand(plugin));
            }
            
            // Register interactive GUI command
            org.bukkit.command.PluginCommand interactiveCmd = plugin.getCommand("interactive");
            if (interactiveCmd != null) {
                InteractiveCommand interactiveCommand = new InteractiveCommand(plugin);
                interactiveCmd.setExecutor(interactiveCommand);
                interactiveCmd.setTabCompleter(interactiveCommand);
            }
            
            // Enemy player management command
            org.bukkit.command.PluginCommand enemyCmd = plugin.getCommand("enemy");
            if (enemyCmd != null) {
                EnemyPlayerCommand enemyPlayerCommand = new EnemyPlayerCommand(plugin);
                enemyCmd.setExecutor(enemyPlayerCommand);
                enemyCmd.setTabCompleter(enemyPlayerCommand);
            }
            
            // Performance monitoring command
            org.bukkit.command.PluginCommand performanceCmd = plugin.getCommand("performance");
            if (performanceCmd != null) {
                PerformanceCommand performanceCommand = new PerformanceCommand(plugin);
                performanceCmd.setExecutor(performanceCommand);
                performanceCmd.setTabCompleter(performanceCommand);
            }
            
            // Global chat command
            org.bukkit.command.PluginCommand ccCmd = plugin.getCommand("cc");
            if (ccCmd != null) {
                ccCmd.setExecutor(new GlobalChatCommand(plugin));
            }
            
            // Register create world command
            org.bukkit.command.PluginCommand createCmd = plugin.getCommand("create");
            if (createCmd != null && serviceRegistry != null) {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                createCmd.setExecutor(new CreateWorldCommand(plugin, worldManager));
            }
            
            // Register missing commands
            org.bukkit.command.PluginCommand templatesCmd = plugin.getCommand("templates");
            if (templatesCmd != null) {
                templatesCmd.setExecutor(new TemplatesCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand clipboardCmd = plugin.getCommand("clipboard");
            if (clipboardCmd != null) {
                clipboardCmd.setExecutor(new ClipboardCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand testCmd = plugin.getCommand("test");
            if (testCmd != null) {
                testCmd.setExecutor(new TestCommand(plugin));
            }
        } catch (Exception e) {
            log.severe("Failed to register simple commands: " + e.getMessage());
        }
    }
}