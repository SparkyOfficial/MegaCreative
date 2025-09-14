package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.models.CreativeWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Listener for block break events
 *
 * Слушатель для событий разрушения блоков
 *
 * Listener für Block-Zerstörungs-Ereignisse
 */
public class BlockBreakListener implements Listener {
    private final MegaCreative plugin;
    
    /**
     * Constructor for BlockBreakListener
     * @param plugin the main plugin
     *
     * Конструктор для BlockBreakListener
     * @param plugin основной плагин
     *
     * Konstruktor für BlockBreakListener
     * @param plugin das Haupt-Plugin
     */
    public BlockBreakListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles block break events
     * @param event the block break event
     *
     * Обрабатывает события разрушения блоков
     * @param event событие разрушения блока
     *
     * Verarbeitet Block-Zerstörungs-Ereignisse
     * @param event das Block-Zerstörungs-Ereignis
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getBlock().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(player)) return;
        
        // Find scripts triggered by block break
        for (CodeScript script : creativeWorld.getScripts()) {
            // Check if the script's root block is an onBlockBreak event
            if (script.getRootBlock() != null && 
                "onBlockBreak".equals(script.getRootBlock().getAction())) {
                
                // Get script engine
                ScriptEngine scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
                if (scriptEngine != null) {
                    // Execute script
                    scriptEngine.executeScript(script, player, "block_break")
                        .whenComplete((result, throwable) -> {
                            if (throwable != null) {
                                plugin.getLogger().warning("Block break script execution failed with exception: " + throwable.getMessage());
                            } else if (result != null && !result.isSuccess()) {
                                plugin.getLogger().warning("Block break script execution failed: " + result.getMessage());
                            }
                        });
                }
                break;
            }
        }
    }
}