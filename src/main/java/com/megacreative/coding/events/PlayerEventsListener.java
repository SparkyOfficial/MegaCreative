package com.megacreative.coding.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.List;

// Этот класс будет слушать реальные события Bukkit
public class PlayerEventsListener implements Listener {
    private final MegaCreative plugin;
    private final ScriptEngine scriptEngine;
    private final BlockConfigService blockConfigService;
    
    // TPS tracking variables
    private long lastTickTime = System.currentTimeMillis();
    private double tps = 20.0;

    public PlayerEventsListener(MegaCreative plugin) {
        this.plugin = plugin;
        // Получаем ScriptEngine из ServiceRegistry
        this.scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Оптимизация: не проверять на каждое микродвижение
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockY() == event.getTo().getBlockY() && 
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // Find the creative world
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Find scripts triggered by player move
        if (creativeWorld.getScripts() != null) {
            for (CodeScript script : creativeWorld.getScripts()) {
                // Check if the script's root block is an onPlayerMove event
                if (script.getRootBlock() != null && 
                    "onPlayerMove".equals(script.getRootBlock().getAction())) {
                    
                    // Execute script
                    if (scriptEngine != null) {
                        scriptEngine.executeScript(script, event.getPlayer(), "player_move")
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    plugin.getLogger().warning("Move script execution failed with exception: " + throwable.getMessage());
                                } else if (result != null && !result.isSuccess()) {
                                    plugin.getLogger().warning("Move script execution failed: " + result.getMessage());
                                }
                            });
                    }
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Only execute in PLAY mode, allow coding in DEV mode but don't execute scripts
        if (creativeWorld.getMode() == com.megacreative.models.WorldMode.DEV) {
            return; // Don't execute scripts in dev mode
        }
        
        // Find scripts triggered by player join
        if (creativeWorld.getScripts() != null) {
            for (CodeScript script : creativeWorld.getScripts()) {
                // Check if the script's root block is an onJoin event
                if (script.isEnabled() && script.getRootBlock() != null && 
                    "onJoin".equals(script.getRootBlock().getAction())) {
                    
                    // Execute script
                    if (scriptEngine != null) {
                        scriptEngine.executeScript(script, event.getPlayer(), "player_join")
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    plugin.getLogger().warning("Join script execution failed with exception: " + throwable.getMessage());
                                } else if (result != null && !result.isSuccess()) {
                                    plugin.getLogger().warning("Join script execution failed: " + result.getMessage());
                                } else {
                                    plugin.getLogger().info("Successfully executed onJoin script for player: " + event.getPlayer().getName());
                                }
                            });
                    }
                    break; // Only execute the first matching script
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Find scripts triggered by player quit
        if (creativeWorld.getScripts() != null) {
            for (CodeScript script : creativeWorld.getScripts()) {
                // Check if the script's root block is an onLeave event
                if (script.getRootBlock() != null && 
                    "onLeave".equals(script.getRootBlock().getAction())) {
                    
                    // Execute script
                    if (scriptEngine != null) {
                        scriptEngine.executeScript(script, event.getPlayer(), "player_leave")
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    plugin.getLogger().warning("Leave script execution failed with exception: " + throwable.getMessage());
                                } else if (result != null && !result.isSuccess()) {
                                    plugin.getLogger().warning("Leave script execution failed: " + result.getMessage());
                                }
                            });
                    }
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Find scripts triggered by player chat
        if (creativeWorld.getScripts() != null) {
            for (CodeScript script : creativeWorld.getScripts()) {
                // Check if the script's root block is an onChat event
                if (script.getRootBlock() != null && 
                    "onChat".equals(script.getRootBlock().getAction())) {
                    
                    // Execute script
                    if (scriptEngine != null) {
                        scriptEngine.executeScript(script, event.getPlayer(), "player_chat")
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    plugin.getLogger().warning("Chat script execution failed with exception: " + throwable.getMessage());
                                } else if (result != null && !result.isSuccess()) {
                                    plugin.getLogger().warning("Chat script execution failed: " + result.getMessage());
                                }
                            });
                    }
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Find scripts triggered by player command
        if (creativeWorld.getScripts() != null) {
            for (CodeScript script : creativeWorld.getScripts()) {
                // Check if the script's root block is an onCommand event
                if (script.getRootBlock() != null && 
                    "onCommand".equals(script.getRootBlock().getAction())) {
                    
                    // Execute script
                    if (scriptEngine != null) {
                        scriptEngine.executeScript(script, event.getPlayer(), "player_command")
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
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getEntity().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getEntity())) return;
        
        // Find scripts triggered by player death
        if (creativeWorld.getScripts() != null) {
            for (CodeScript script : creativeWorld.getScripts()) {
                // Check if the script's root block is an onPlayerDeath event
                if (script.getRootBlock() != null && 
                    "onPlayerDeath".equals(script.getRootBlock().getAction())) {
                    
                    // Execute script
                    if (scriptEngine != null) {
                        scriptEngine.executeScript(script, event.getEntity(), "player_death")
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
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Find scripts triggered by block place
        if (creativeWorld.getScripts() != null) {
            for (CodeScript script : creativeWorld.getScripts()) {
                // Check if the script's root block is an onBlockPlace event
                if (script.getRootBlock() != null && 
                    "onBlockPlace".equals(script.getRootBlock().getAction())) {
                    
                    // Execute script
                    if (scriptEngine != null) {
                        scriptEngine.executeScript(script, event.getPlayer(), "block_place")
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    plugin.getLogger().warning("Block place script execution failed with exception: " + throwable.getMessage());
                                } else if (result != null && !result.isSuccess()) {
                                    plugin.getLogger().warning("Block place script execution failed: " + result.getMessage());
                                }
                            });
                    }
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Find scripts triggered by block break
        if (creativeWorld.getScripts() != null) {
            for (CodeScript script : creativeWorld.getScripts()) {
                // Check if the script's root block is an onBlockBreak event
                if (script.getRootBlock() != null && 
                    "onBlockBreak".equals(script.getRootBlock().getAction())) {
                    
                    // Execute script
                    if (scriptEngine != null) {
                        scriptEngine.executeScript(script, event.getPlayer(), "block_break")
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
    
    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null) return;
        
        // Check if player can code in this world
        if (!creativeWorld.canCode(event.getPlayer())) return;
        
        // Find scripts triggered by player level change
        if (creativeWorld.getScripts() != null) {
            for (CodeScript script : creativeWorld.getScripts()) {
                // Check if the script's root block is an onPlayerLevelUp event
                if (script.getRootBlock() != null && 
                    "onPlayerLevelUp".equals(script.getRootBlock().getAction())) {
                    
                    // Execute script
                    if (scriptEngine != null) {
                        scriptEngine.executeScript(script, event.getPlayer(), "player_level_up")
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    plugin.getLogger().warning("Level up script execution failed with exception: " + throwable.getMessage());
                                } else if (result != null && !result.isSuccess()) {
                                    plugin.getLogger().warning("Level up script execution failed: " + result.getMessage());
                                }
                            });
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Handles the onTick event - called every server tick for all players in creative worlds
     */
    public void onTick() {
        // Update TPS tracking
        updateTPS();
        
        // Get all creative worlds
        List<CreativeWorld> creativeWorlds = plugin.getWorldManager().getCreativeWorlds();
        
        // Iterate through each creative world
        for (CreativeWorld creativeWorld : creativeWorlds) {
            // Find scripts triggered by onTick event
            if (creativeWorld.getScripts() != null) {
                for (CodeScript script : creativeWorld.getScripts()) {
                    // Check if the script's root block is an onTick event
                    if (script.getRootBlock() != null && 
                        "onTick".equals(script.getRootBlock().getAction())) {
                        
                        // Execute script for each player in the world
                        creativeWorld.getPlayers().forEach(player -> {
                            if (scriptEngine != null) {
                                scriptEngine.executeScript(script, player, "tick")
                                    .whenComplete((result, throwable) -> {
                                        if (throwable != null) {
                                            plugin.getLogger().warning("Tick script execution failed with exception: " + throwable.getMessage());
                                        } else if (result != null && !result.isSuccess()) {
                                            plugin.getLogger().warning("Tick script execution failed: " + result.getMessage());
                                        }
                                    });
                            }
                        });
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Handles server TPS tracking
     */
    public void onServerTPS() {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastTickTime;
        lastTickTime = currentTime;
        
        if (timeDiff > 0) {
            tps = Math.min(20.0, 1000.0 / timeDiff);
        }
        
        // Log TPS if it drops significantly
        if (tps < 15.0) {
            plugin.getLogger().warning("Server TPS dropped to: " + String.format("%.2f", tps));
        }
    }
    
    /**
     * Updates TPS tracking
     */
    private void updateTPS() {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastTickTime;
        lastTickTime = currentTime;
        
        if (timeDiff > 0) {
            tps = Math.min(20.0, 1000.0 / timeDiff);
        }
    }
    
    /**
     * Gets current server TPS
     */
    public double getTPS() {
        return tps;
    }
}