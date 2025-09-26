package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.models.CreativeWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

/**
 * Listener for player death events
 *
 * Слушатель для событий смерти игроков
 *
 * Listener für Spieler-Todes-Ereignisse
 */
public class PlayerDeathListener implements Listener {
    private final MegaCreative plugin;
    
    /**
     * Constructor for PlayerDeathListener
     * @param plugin the main plugin
     *
     * Конструктор для PlayerDeathListener
     * @param plugin основной плагин
     *
     * Konstruktor für PlayerDeathListener
     * @param plugin das Haupt-Plugin
     */
    public PlayerDeathListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles player death events
     * @param event the player death event
     *
     * Обрабатывает события смерти игроков
     * @param event событие смерти игрока
     *
     * Verarbeitet Spieler-Todes-Ereignisse
     * @param event das Spieler-Todes-Ereignis
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player == null) return;
        
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(player)) return;
        
        // Find scripts triggered by player death
        for (CodeScript script : creativeWorld.getScripts()) {
            // Check if the script's root block is an onPlayerDeath event
            if (script.getRootBlock() != null && 
                "onPlayerDeath".equals(script.getRootBlock().getAction())) {
                
                // Get script engine
                ScriptEngine scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
                if (scriptEngine != null) {
                    // Execute script
                    scriptEngine.executeScript(script, player, "player_death")
                        .whenComplete((result, throwable) -> {
                            if (throwable != null) {
                                plugin.getLogger().warning("Death script execution failed with exception: " + throwable.getMessage());
                            } else if (result != null && !result.isSuccess()) {
                                plugin.getLogger().warning("Death script execution failed: " + result.getMessage());
                            }
                        });
                }
                break;
            }
        }
    }
}