package com.megacreative.commands;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.CodeCompiler;

/**
 * Команда для переключения мира в режим игры
 *
 * Command to switch world to play mode
 *
 * Befehl zum Wechseln der Welt in den Spielmodus
 */
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command is only available to players!");
            return true;
        }
        
        // Simplified PlayCommand - just switch to play world
        // All logic is now in PlayerWorldChangeListener
        
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        if (creativeWorld == null) {
            player.sendMessage("§cYou are not in a MegaCreative world!");
            return true;
        }
        
        // Switch to play world using WorldManager
        plugin.getServiceRegistry().getWorldManager().switchToPlayWorld(player, creativeWorld.getId());
        
        return true;
    }
    
    /**
     * Finds and executes the onJoin script for the player
     * @param player The player to execute the script for
     * @param creativeWorld The creative world to search for scripts
     */
    private void executeOnJoinScript(Player player, CreativeWorld creativeWorld) {
        // Find the onJoin script using the new architecture
        if (creativeWorld.getScripts() != null) {
            for (CodeScript script : creativeWorld.getScripts()) {
                // Check if the script's root block is an onJoin event
                if (script.getRootBlock() != null && 
                    "onJoin".equals(script.getRootBlock().getAction())) {
                    
                    // Execute script using ScriptEngine
                    if (plugin.getServiceRegistry().getScriptEngine() != null) {
                        plugin.getServiceRegistry().getScriptEngine().executeScript(script, player, "play_mode_start")
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    player.sendMessage("§cError executing onJoin script: " + throwable.getMessage());
                                    plugin.getLogger().warning("onJoin script execution failed with exception: " + throwable.getMessage());
                                } else if (result != null && !result.isSuccess()) {
                                    player.sendMessage("§conJoin script execution failed: " + result.getMessage());
                                    plugin.getLogger().warning("onJoin script execution failed: " + result.getMessage());
                                } else {
                                    plugin.getLogger().info("Successfully executed onJoin script for player: " + player.getName());
                                }
                            });
                    }
                    break;
                }
            }
        }
    }
}