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

public class CommandListener implements Listener {
    private final MegaCreative plugin;
    
    public CommandListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        
        String message = event.getMessage();
        if (message == null || !message.startsWith("/")) return;
        
        String command = message.substring(1); // Remove the leading '/'
        
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(player)) return;
        
        // Find scripts triggered by this command
        for (CodeScript script : creativeWorld.getScripts()) {
            // Check if the script's root block is an onCommand event
            if (script.getRootBlock() != null && 
                "onCommand".equals(script.getRootBlock().getAction())) {
                
                // Get the command parameter from the root block
                Object commandParam = script.getRootBlock().getParameter("command");
                String triggerCommand = commandParam != null ? commandParam.toString() : null;
                
                // Check if this script should be triggered by this command
                if (triggerCommand != null && command.startsWith(triggerCommand)) {
                    // Get script engine
                    ScriptEngine scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
                    if (scriptEngine != null) {
                        // Execute script
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