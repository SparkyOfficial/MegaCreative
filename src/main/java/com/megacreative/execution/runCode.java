package com.megacreative.execution;

import com.megacreative.MegaCreative;
import com.megacreative.configs.WorldCode;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 🎆 Reference System-Style Code Execution Engine
 * 
 * Executes compiled code strings from WorldCode configuration, similar to FrameLand's runCode system.
 * This is the bridge between compiled visual code and actual game execution.
 */
public class runCode implements Listener {
    
    private final MegaCreative plugin;
    private final ScriptEngine scriptEngine;
    
    public runCode(MegaCreative plugin) {
        this.plugin = plugin;
        this.scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        plugin.getLogger().info("🎆 runCode execution engine initialized");
    }
    
    // === Event Handlers ===
    
    @EventHandler
    public void joinEvent(PlayerJoinEvent event) {
        executeWorldCode(event.getPlayer(), "joinEvent", event);
    }
    
    @EventHandler
    public void quitEvent(PlayerQuitEvent event) {
        executeWorldCode(event.getPlayer(), "quitEvent", event);
    }
    
    @EventHandler
    public void breakEvent(BlockBreakEvent event) {
        executeWorldCode(event.getPlayer(), "breakEvent", event);
    }
    
    @EventHandler
    public void placeEvent(BlockPlaceEvent event) {
        executeWorldCode(event.getPlayer(), "placeEvent", event);
    }
    
    @EventHandler
    public void moveEvent(PlayerMoveEvent event) {
        // Оптимизация: не проверять на каждое микродвижение
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockY() == event.getTo().getBlockY() && 
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        executeWorldCode(event.getPlayer(), "moveEvent", event);
    }
    
    @EventHandler
    public void LMBEvent(PlayerInteractEvent event) {
        if ((event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) && 
            event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
            executeWorldCode(event.getPlayer(), "LMBEvent", event);
        }
    }
    
    @EventHandler
    public void RMBEvent(PlayerInteractEvent event) {
        if ((event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) && 
            event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
            executeWorldCode(event.getPlayer(), "RMBEvent", event);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void messageEvent(AsyncPlayerChatEvent event) {
        executeWorldCode(event.getPlayer(), "messageEvent", event);
    }
    
    @EventHandler
    public void mobDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            executeWorldCode(event.getEntity().getKiller(), "mobDeathEvent", event);
        }
    }
    
    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        executeWorldCode(event.getEntity(), "playerDeathEvent", event);
    }
    
    @EventHandler
    public void playerKillPlayer(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player && event.getEntity().getKiller() != null && 
            event.getEntity().getKiller() instanceof Player) {
            executeWorldCode(event.getEntity().getKiller(), "plKillPlEvent", event);
        }
    }
    
    @EventHandler
    public void playerKillMob(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null && 
            event.getEntity() instanceof org.bukkit.entity.Monster) {
            executeWorldCode(event.getEntity().getKiller(), "plKillMobEvent", event);
        }
    }
    
    @EventHandler
    public void playerPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            executeWorldCode((Player) event.getDamager(), "plDmgPlEvent", event);
        }
    }
    
    @EventHandler
    public void playerMobDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof org.bukkit.entity.Monster) {
            executeWorldCode((Player) event.getEntity(), "mobDmgPlEvent", event);
        }
    }
    
    @EventHandler
    public void mobPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof org.bukkit.entity.Monster) {
            executeWorldCode((Player) event.getDamager(), "plDmgMobEvent", event);
        }
    }
    
    @EventHandler
    public void inventoryOpenEvent(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            executeWorldCode((Player) event.getPlayer(), "invOpenEvent", event);
        }
    }
    
    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            executeWorldCode((Player) event.getPlayer(), "invCloseEvent", event);
        }
    }
    
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getCurrentItem() != null) {
            executeWorldCode((Player) event.getWhoClicked(), "invClickEvent", event);
        }
    }
    
    @EventHandler
    public void itemPickup(PlayerPickupItemEvent event) {
        executeWorldCode(event.getPlayer(), "itemPickupEvent", event);
    }
    
    @EventHandler
    public void itemDrop(PlayerDropItemEvent event) {
        executeWorldCode(event.getPlayer(), "itemDropEvent", event);
    }
    
    @EventHandler
    public void teleportEvent(PlayerTeleportEvent event) {
        executeWorldCode(event.getPlayer(), "teleportEvent", event);
    }
    
    @EventHandler
    public void slotChange(PlayerItemHeldEvent event) {
        if (event.getPreviousSlot() != event.getNewSlot()) {
            executeWorldCode(event.getPlayer(), "slotChangeEvent", event);
        }
    }
    
    // === Execution Methods ===
    
    /**
     * Executes compiled code for a world event
     */
    private void executeWorldCode(Player player, String eventType, Event event) {
        if (player == null || player.getWorld() == null) return;
        
        // Only execute in play worlds
        if (!player.getWorld().getName().contains("-world")) {
            return;
        }
        
        String worldId = player.getWorld().getName().replace("-world", "");
        
        // Check if world has compiled code
        if (!WorldCode.hasCode(worldId)) {
            return;
        }
        
        // Get compiled code lines
        List<String> codeLines = WorldCode.getCode(worldId);
        if (codeLines == null || codeLines.isEmpty()) {
            return;
        }
        
        // Process each line of compiled code
        for (String codeLine : codeLines) {
            List<String> functions = Arrays.asList(codeLine.split("&"));
            
            // Check if first function matches event type
            if (!functions.isEmpty() && functions.get(0).equals(eventType)) {
                // 🎆 FIXED: Ensure thread safety for async events
                if (event.isAsynchronous()) {
                    final List<String> finalFunctions = functions;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            CodeBlock firstBlock = parseStringToCodeChain(finalFunctions, player);
                            if (firstBlock != null) {
                                scriptEngine.executeBlockChain(firstBlock, player, eventType);
                            }
                        }
                    }.runTask(plugin);
                } else {
                    CodeBlock firstBlock = parseStringToCodeChain(functions, player);
                    if (firstBlock != null) {
                        scriptEngine.executeBlockChain(firstBlock, player, eventType);
                    }
                }
            }
        }
    }

    /**
     * 🎆 NEW: Parses the old string format into a modern, reliable chain of CodeBlock objects.
     * This is the bridge between the legacy string code and the powerful ScriptEngine.
     *
     * @param functions The list of function strings from the old compiled code.
     * @param player The player context.
     * @return The first CodeBlock in the newly created chain, ready for execution.
     */
    private CodeBlock parseStringToCodeChain(List<String> functions, Player player) {
        if (functions.size() <= 1) {
            return null;
        }

        CodeBlock head = null;
        CodeBlock current = null;

        // Start from 1 to skip the event trigger
        for (int i = 1; i < functions.size(); i++) {
            String funcStr = functions.get(i);
            String actionId = getActionIdFromString(funcStr);
            
            if (actionId == null || actionId.isEmpty()) {
                plugin.getLogger().warning("Could not parse action from string: " + funcStr);
                continue;
            }

            // Create a new CodeBlock for this action
            CodeBlock newBlock = new CodeBlock(Material.COMMAND_BLOCK, actionId); // Material is a placeholder
            
            // Here, you would parse parameters from funcStr and add them to the newBlock
            // For example: newBlock.setParameter("message", new DataValue(parsedMessage));

            if (head == null) {
                head = newBlock;
                current = newBlock;
            } else {
                current.setNextBlock(newBlock);
                current = newBlock;
            }
        }

        return head;
    }

    /**
     * Extracts the action ID (e.g., "playerMessage") from a function string.
     */
    private String getActionIdFromString(String func) {
        int parenthesisIndex = func.indexOf('(');
        if (parenthesisIndex != -1) {
            return func.substring(0, parenthesisIndex);
        }
        return func; // No parameters, the whole string is the ID
    }
}