package com.megacreative.core;

import com.megacreative.MegaCreative;
import com.megacreative.commands.*;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.interfaces.IPlayerManager;
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
            
            registerBuildCommand();
            
            
            registerSimpleCommands();
            
            log.fine("Commands registered successfully");
        } catch (Exception e) {
            log.severe("Failed to register commands: " + e.getMessage());
            log.log(java.util.logging.Level.SEVERE, "Failed to register commands", e);
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
                BuildCommand buildCommand = createBuildCommand();
                buildCmd.setExecutor(buildCommand);
            }
        } catch (Exception e) {
            log.log(java.util.logging.Level.SEVERE, "Failed to register build command", e);
        }
    }

    /**
     * Creates a new BuildCommand instance with required dependencies
     * @return BuildCommand instance
     */
    private BuildCommand createBuildCommand() {
        IWorldManager worldManager = serviceRegistry.getWorldManager();
        PlayerModeManager playerModeManager = serviceRegistry.getPlayerModeManager();
        DevInventoryManager devInventoryManager = serviceRegistry.getDevInventoryManager();
        BlockPlacementHandler blockPlacementHandler = serviceRegistry.getBlockPlacementHandler();
        
        return new BuildCommand(
            worldManager,
            playerModeManager,
            devInventoryManager,
            blockPlacementHandler
        );
    }
    
    /**
     * Register simple commands that don't require many dependencies
     */
    private void registerSimpleCommands() {
        if (plugin == null) return;
        
        try {
            
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
            
            org.bukkit.command.PluginCommand joinCmd = plugin.getCommand("join");
            if (joinCmd != null && serviceRegistry != null) {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                joinCmd.setExecutor(new JoinCommand(plugin, worldManager));
            }
            
            org.bukkit.command.PluginCommand playCmd = plugin.getCommand("play");
            if (playCmd != null) {
                playCmd.setExecutor(new PlayCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand hubCmd = plugin.getCommand("hub");
            if (hubCmd != null && serviceRegistry != null) {
                IPlayerManager playerManager = serviceRegistry.getPlayerManager();
                hubCmd.setExecutor(new HubCommand(plugin, playerManager));
            }
            
            org.bukkit.command.PluginCommand switchCmd = plugin.getCommand("switch");
            if (switchCmd != null && serviceRegistry != null) {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                switchCmd.setExecutor(new SwitchCommand(plugin, worldManager));
            }
            
            org.bukkit.command.PluginCommand groupCmd = plugin.getCommand("group");
            if (groupCmd != null && serviceRegistry != null) {
                groupCmd.setExecutor(new GroupCommand(serviceRegistry));
                groupCmd.setTabCompleter(new GroupCommand(serviceRegistry));
            }
            
            org.bukkit.command.PluginCommand deleteCmd = plugin.getCommand("delete");
            if (deleteCmd != null) {
                deleteCmd.setExecutor(new DeleteCommand(plugin));
            }
            
            org.bukkit.command.PluginCommand confirmdeleteCmd = plugin.getCommand("confirmdelete");
            if (confirmdeleteCmd != null) {
                confirmdeleteCmd.setExecutor(new ConfirmDeleteCommand(plugin));
            }
            
            
            org.bukkit.command.PluginCommand interactiveCmd = plugin.getCommand("interactive");
            if (interactiveCmd != null) {
                InteractiveCommand interactiveCommand = new InteractiveCommand(plugin);
                interactiveCmd.setExecutor(interactiveCommand);
                interactiveCmd.setTabCompleter(interactiveCommand);
            }
            
            
            org.bukkit.command.PluginCommand enemyCmd = plugin.getCommand("enemy");
            if (enemyCmd != null) {
                EnemyPlayerCommand enemyPlayerCommand = new EnemyPlayerCommand(plugin);
                enemyCmd.setExecutor(enemyPlayerCommand);
                enemyCmd.setTabCompleter(enemyPlayerCommand);
            }
            
            
            org.bukkit.command.PluginCommand performanceCmd = plugin.getCommand("performance");
            if (performanceCmd != null) {
                PerformanceCommand performanceCommand = new PerformanceCommand(plugin);
                performanceCmd.setExecutor(performanceCommand);
                performanceCmd.setTabCompleter(performanceCommand);
            }
            
            
            org.bukkit.command.PluginCommand ccCmd = plugin.getCommand("cc");
            if (ccCmd != null) {
                ccCmd.setExecutor(new GlobalChatCommand(plugin));
            }

            org.bukkit.command.PluginCommand clipboardCmd = plugin.getCommand("clipboard");
            if (clipboardCmd != null && serviceRegistry != null) {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                PlaceholdersCommand placeholders = new PlaceholdersCommand(plugin, worldManager);
                clipboardCmd.setExecutor(placeholders);
            }
            
            
            org.bukkit.command.PluginCommand createCmd = plugin.getCommand("create");
            if (createCmd != null && serviceRegistry != null) {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                createCmd.setExecutor(new CreateWorldCommand(plugin, worldManager));
            }
            
            // Register like command
            org.bukkit.command.PluginCommand likeCmd = plugin.getCommand("like");
            if (likeCmd != null && serviceRegistry != null) {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                likeCmd.setExecutor(new LikeCommand(plugin, worldManager));
            }
            
            // Register dislike command
            org.bukkit.command.PluginCommand dislikeCmd = plugin.getCommand("dislike");
            if (dislikeCmd != null && serviceRegistry != null) {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                dislikeCmd.setExecutor(new DislikeCommand(plugin, worldManager));
            }
            
            // Register comment command
            org.bukkit.command.PluginCommand commentCmd = plugin.getCommand("comment");
            if (commentCmd != null && serviceRegistry != null) {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                commentCmd.setExecutor(new CommentCommand(plugin, worldManager));
            }
            
            // Register world stats command
            org.bukkit.command.PluginCommand worldStatsCmd = plugin.getCommand("worldstats");
            if (worldStatsCmd != null && serviceRegistry != null) {
                IWorldManager worldManager = serviceRegistry.getWorldManager();
                IPlayerManager playerManager = serviceRegistry.getPlayerManager();
                worldStatsCmd.setExecutor(new WorldStatsCommand(plugin, worldManager, playerManager));
                worldStatsCmd.setTabCompleter(new WorldStatsCommand(plugin, worldManager, playerManager));
            }
            
            log.fine("All commands registered successfully");
        } catch (Exception e) {
            log.severe("Failed to register simple commands: " + e.getMessage());
        }
    }
}