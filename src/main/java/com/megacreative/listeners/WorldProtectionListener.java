package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldPermissions;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * 🎆 ENHANCED: Comprehensive world protection system for dual world architecture
 * Implements reference system-style permission checking and access control
 */
public class WorldProtectionListener implements Listener {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    
    public WorldProtectionListener(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());
        
        if (world != null) {
            
            if (!world.canAccess(player, world.getDualMode())) {
                
                player.sendMessage("§c🚫 У вас нет доступа к этому миру!");
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.performCommand("hub");
                });
                return;
            }
            
            
            sendWorldWelcomeMessage(player, world);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());
        
        if (world != null) {
            
            if (!world.canAccess(player, world.getDualMode())) {
                player.sendMessage("§c🚫 У вас нет доступа к этому миру!");
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.performCommand("hub");
                });
                return;
            }
            
            
            applyWorldSettings(player, world);
            
            
            sendWorldWelcomeMessage(player, world);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!checkWorldPermission(event.getPlayer(), "build", event)) {
            event.getPlayer().sendMessage("§c🚫 У вас нет прав на строительство в этом мире!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!checkWorldPermission(event.getPlayer(), "build", event)) {
            event.getPlayer().sendMessage("§c🚫 У вас нет прав на разрушение блоков в этом мире!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        
        if (plugin != null && plugin.getServiceRegistry() != null) {
            PlayerModeManager modeManager = plugin.getServiceRegistry().getPlayerModeManager();
            if (modeManager.isInPlayMode(player)) {
                
                
                return;
            }
        }
        
        if (event.getClickedBlock() != null && 
            !checkWorldPermission(player, "interact", event)) {
            player.sendMessage("§c🚫 У вас нет прав на взаимодействие в этом мире!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());
            
            if (world != null && !world.canPerform(player, "interact")) {
                event.setCancelled(true);
                player.sendMessage("§c🚫 У вас нет прав на использование инвентарей в этом мире!");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        CreativeWorld world = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());
        
        if (world != null) {
            WorldPermissions permissions = world.getPermissions();
            if (!permissions.isAllowItemDrops() && !world.isOwner(event.getPlayer())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§c🚫 Выбрасывание предметов запрещено в этом мире!");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof Player victim) {
            CreativeWorld world = worldManager.findCreativeWorldByBukkit(damager.getWorld());
            
            if (world != null) {
                WorldPermissions permissions = world.getPermissions();
                if (!permissions.isAllowPvP()) {
                    event.setCancelled(true);
                    damager.sendMessage("§c🚫 PvP запрещён в этом мире!");
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        CreativeWorld world = worldManager.findCreativeWorldByBukkit(event.getLocation().getWorld());
        
        if (world != null) {
            WorldPermissions permissions = world.getPermissions();
            if (!permissions.isAllowExplosions()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());
        
        if (world != null && world.getDualMode() == CreativeWorld.WorldDualMode.PLAY) {
            WorldPermissions permissions = world.getPermissions();
            if (!permissions.isAllowFlightInPlay() && !world.canPerform(player, "admin")) {
                event.setCancelled(true);
                player.sendMessage("§c🚫 Полёт запрещён в игровом режиме этого мира!");
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }
    }
    
    /**
     * Checks if player has permission to perform action in their current world
     */
    private boolean checkWorldPermission(Player player, String action, Cancellable event) {
        CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());
        
        if (world == null) {
            return true; 
        }
        
        boolean hasPermission = world.canPerform(player, action);
        if (!hasPermission) {
            event.setCancelled(true);
        }
        
        return hasPermission;
    }
    
    /**
     * Applies world-specific settings to player
     */
    private void applyWorldSettings(Player player, CreativeWorld world) {
        WorldPermissions permissions = world.getPermissions();
        
        
        if (world.getDualMode() == CreativeWorld.WorldDualMode.PLAY) {
            if (!permissions.isAllowFlightInPlay() && !world.canPerform(player, "admin")) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        } else {
            
            player.setAllowFlight(true);
        }
    }
    
    /**
     * Sends welcome message with world info and permissions
     */
    private void sendWorldWelcomeMessage(Player player, CreativeWorld world) {
        String modeEmoji = world.getDualMode() == CreativeWorld.WorldDualMode.DEV ? "🔧" : "🎮";
        String modeColor = world.getDualMode() == CreativeWorld.WorldDualMode.DEV ? "§e" : "§a";
        
        player.sendMessage("");
        player.sendMessage(modeColor + modeEmoji + " Добро пожаловать в " + world.getName());
        player.sendMessage("§7Режим: " + world.getDualMode().getDisplayName());
        
        
        WorldPermissions permissions = world.getPermissions();
        if (world.isOwner(player)) {
            player.sendMessage("§c⚡ Вы владелец этого мира");
        } else {
            WorldPermissions.PermissionLevel level = permissions.getPlayerPermission(player.getUniqueId());
            player.sendMessage("§7Ваш уровень: " + level.getDisplayName());
        }
        
        
        WorldPermissions.AccessMode accessMode = world.getDualMode() == CreativeWorld.WorldDualMode.DEV ? 
            permissions.getDevWorldAccess() : permissions.getPlayWorldAccess();
        player.sendMessage("§7Доступ: " + accessMode.getDisplayName());
        
        player.sendMessage("");
    }
}