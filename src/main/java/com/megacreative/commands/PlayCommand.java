package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Конструктор команды PlayCommand
     * @param plugin основной плагин
     *
     * Constructor for PlayCommand
     * @param plugin main plugin
     *
     * Konstruktor für PlayCommand
     * @param plugin Haupt-Plugin
     */
    public PlayCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды /play
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /play command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /play-Befehls
     * @param sender Befehlsabsender
     * @param command Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command is only available to players!");
            return true;
        }
        
        try {
            
            CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            
            if (creativeWorld == null) {
                player.sendMessage("§cYou are not in a MegaCreative world!");
                return true;
            }
            
            
            plugin.getServiceRegistry().getWorldManager().switchToPlayWorld(player, creativeWorld.getId());
            
            
            executeOnJoinScript(player, creativeWorld);
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error executing /play command for player " + player.getName(), e);
            player.sendMessage("§cAn error occurred while switching to play mode. Please contact an administrator.");
            return true;
        }
    }
    
    /**
     * Finds and executes the onJoin script for the player
     * @param player The player to execute the script for
     * @param creativeWorld The creative world to search for scripts
     */
    private void executeOnJoinScript(Player player, CreativeWorld creativeWorld) {
        try {
            
            if (creativeWorld.getScripts() != null) {
                for (CodeScript script : creativeWorld.getScripts()) {
                    
                    if (script.getRootBlock() != null && 
                        "onJoin".equals(script.getRootBlock().getAction())) {
                        
                        
                        if (plugin.getServiceRegistry().getScriptEngine() != null) {
                            plugin.getServiceRegistry().getScriptEngine().executeScript(script, player, "play_mode_start")
                                .whenComplete((result, throwable) -> {
                                    try {
                                        if (throwable != null) {
                                            player.sendMessage("§cError executing onJoin script: " + throwable.getMessage());
                                            plugin.getLogger().log(Level.WARNING, "onJoin script execution failed with exception for player " + player.getName(), throwable);
                                        } else if (result != null) {
                                            if (!result.isSuccess()) {
                                                player.sendMessage("§conJoin script execution failed: " + result.getMessage());
                                                plugin.getLogger().warning("onJoin script execution failed for player " + player.getName() + ": " + result.getMessage());
                                                
                                                
                                                if (result.getError() != null) {
                                                    plugin.getLogger().log(Level.WARNING, "Detailed error for onJoin script execution", result.getError());
                                                }
                                            } else {
                                                plugin.getLogger().fine("Successfully executed onJoin script for player: " + player.getName());
                                            }
                                        } else {
                                            plugin.getLogger().warning("onJoin script execution returned null result for player: " + player.getName());
                                        }
                                    } catch (Exception e) {
                                        plugin.getLogger().log(Level.SEVERE, "Error handling onJoin script completion for player " + player.getName(), e);
                                        player.sendMessage("§cError handling script result: " + e.getMessage());
                                    }
                                });
                        } else {
                            plugin.getLogger().warning("ScriptEngine is not available for onJoin script execution for player: " + player.getName());
                            player.sendMessage("§cScript engine is not available. onJoin script cannot be executed.");
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error finding or executing onJoin script for player " + player.getName(), e);
            player.sendMessage("§cError processing onJoin script: " + e.getMessage());
        }
    }
}