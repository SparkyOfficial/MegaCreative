package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.models.CreativeWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.entity.Player;

/**
 * Listener for player command events
 *
 * Слушатель для событий команд игроков
 *
 * Listener für Spieler-Befehls-Ereignisse
 */
public class CommandListener implements Listener {
    private final MegaCreative plugin;
    
    /**
     * Constructor for CommandListener
     * @param plugin the main plugin
     *
     * Конструктор для CommandListener
     * @param plugin основной плагин
     *
     * Konstruktor für CommandListener
     * @param plugin das Haupt-Plugin
     */
    public CommandListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles player command events
     * @param event the player command preprocess event
     *
     * Обрабатывает события команд игроков
     * @param event событие предварительной обработки команды игрока
     *
     * Verarbeitet Spieler-Befehls-Ereignisse
     * @param event das Spieler-Befehl-Vorverarbeitungs-Ereignis
     */
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        
        String message = event.getMessage();
        if (message == null || !message.startsWith("/")) return;
        
        String command = message.substring(1); 
        
        
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null) return;
        
        
        if (!creativeWorld.canCode(player)) return;
        
        
        for (CodeScript script : creativeWorld.getScripts()) {
            
            if (script.getRootBlock() != null && 
                "onCommand".equals(script.getRootBlock().getAction())) {
                
                
                Object commandParam = script.getRootBlock().getParameter("command");
                String triggerCommand = commandParam != null ? commandParam.toString() : null;
                
                
                if (triggerCommand != null && command.startsWith(triggerCommand)) {
                    
                    ScriptEngine scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
                    if (scriptEngine != null) {
                        
                        scriptEngine.executeScript(script, player, "command")
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    plugin.getLogger().warning("Command script execution failed with exception: " + throwable.getMessage());
                                } else if (result != null && !result.isSuccess()) {
                                    plugin.getLogger().warning("Command script execution failed: " + result.getMessage());
                                }
                            });
                    }
                    break;
                }
            }
        }
    }
}