package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
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
 * Implements FrameLand-style permission checking and access control
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
            // Check access permissions for current world
            if (!world.canAccess(player, world.getDualMode())) {
                // Kick player to hub if they don't have access
                player.sendMessage("§c🚫 У вас нет доступа к этому миру!");
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.performCommand("hub");
                });
                return;
            }
            
            // Send welcome message with permission info
            sendWorldWelcomeMessage(player, world);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());
        
        if (world != null) {
            // Check access permissions for new world
            if (!world.canAccess(player, world.getDualMode())) {\n                player.sendMessage(\"§c🚫 У вас нет доступа к этому миру!\");\n                plugin.getServer().getScheduler().runTask(plugin, () -> {\n                    player.performCommand(\"hub\");\n                });\n                return;\n            }\n            \n            // Apply world-specific settings\n            applyWorldSettings(player, world);\n            \n            // Send mode-specific welcome message\n            sendWorldWelcomeMessage(player, world);\n        }\n    }\n    \n    @EventHandler(priority = EventPriority.HIGH)\n    public void onBlockPlace(BlockPlaceEvent event) {\n        if (!checkWorldPermission(event.getPlayer(), \"build\", event)) {\n            event.getPlayer().sendMessage(\"§c🚫 У вас нет прав на строительство в этом мире!\");\n        }\n    }\n    \n    @EventHandler(priority = EventPriority.HIGH)\n    public void onBlockBreak(BlockBreakEvent event) {\n        if (!checkWorldPermission(event.getPlayer(), \"build\", event)) {\n            event.getPlayer().sendMessage(\"§c🚫 У вас нет прав на разрушение блоков в этом мире!\");\n        }\n    }\n    \n    @EventHandler(priority = EventPriority.HIGH)\n    public void onPlayerInteract(PlayerInteractEvent event) {\n        if (event.getClickedBlock() != null && \n            !checkWorldPermission(event.getPlayer(), \"interact\", event)) {\n            event.getPlayer().sendMessage(\"§c🚫 У вас нет прав на взаимодействие в этом мире!\");\n        }\n    }\n    \n    @EventHandler(priority = EventPriority.HIGH)\n    public void onInventoryOpen(InventoryOpenEvent event) {\n        if (event.getPlayer() instanceof Player player) {\n            CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());\n            \n            if (world != null && !world.canPerform(player, \"interact\")) {\n                event.setCancelled(true);\n                player.sendMessage(\"§c🚫 У вас нет прав на использование инвентарей в этом мире!\");\n            }\n        }\n    }\n    \n    @EventHandler(priority = EventPriority.HIGH)\n    public void onPlayerDropItem(PlayerDropItemEvent event) {\n        CreativeWorld world = worldManager.findCreativeWorldByBukkit(event.getPlayer().getWorld());\n        \n        if (world != null) {\n            WorldPermissions permissions = world.getPermissions();\n            if (!permissions.isAllowItemDrops() && !world.isOwner(event.getPlayer())) {\n                event.setCancelled(true);\n                event.getPlayer().sendMessage(\"§c🚫 Выбрасывание предметов запрещено в этом мире!\");\n            }\n        }\n    }\n    \n    @EventHandler(priority = EventPriority.HIGH)\n    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {\n        if (event.getDamager() instanceof Player damager && event.getEntity() instanceof Player victim) {\n            CreativeWorld world = worldManager.findCreativeWorldByBukkit(damager.getWorld());\n            \n            if (world != null) {\n                WorldPermissions permissions = world.getPermissions();\n                if (!permissions.isAllowPvP()) {\n                    event.setCancelled(true);\n                    damager.sendMessage(\"§c🚫 PvP запрещён в этом мире!\");\n                }\n            }\n        }\n    }\n    \n    @EventHandler(priority = EventPriority.HIGH)\n    public void onEntityExplode(EntityExplodeEvent event) {\n        CreativeWorld world = worldManager.findCreativeWorldByBukkit(event.getLocation().getWorld());\n        \n        if (world != null) {\n            WorldPermissions permissions = world.getPermissions();\n            if (!permissions.isAllowExplosions()) {\n                event.setCancelled(true);\n            }\n        }\n    }\n    \n    @EventHandler(priority = EventPriority.NORMAL)\n    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {\n        Player player = event.getPlayer();\n        CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());\n        \n        if (world != null && world.getDualMode() == CreativeWorld.WorldDualMode.PLAY) {\n            WorldPermissions permissions = world.getPermissions();\n            if (!permissions.isAllowFlightInPlay() && !world.canPerform(player, \"admin\")) {\n                event.setCancelled(true);\n                player.sendMessage(\"§c🚫 Полёт запрещён в игровом режиме этого мира!\");\n                player.setFlying(false);\n                player.setAllowFlight(false);\n            }\n        }\n    }\n    \n    /**\n     * Checks if player has permission to perform action in their current world\n     */\n    private boolean checkWorldPermission(Player player, String action, Cancellable event) {\n        CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());\n        \n        if (world == null) {\n            return true; // Not a managed world\n        }\n        \n        boolean hasPermission = world.canPerform(player, action);\n        if (!hasPermission) {\n            event.setCancelled(true);\n        }\n        \n        return hasPermission;\n    }\n    \n    /**\n     * Applies world-specific settings to player\n     */\n    private void applyWorldSettings(Player player, CreativeWorld world) {\n        WorldPermissions permissions = world.getPermissions();\n        \n        // Apply flight settings for play mode\n        if (world.getDualMode() == CreativeWorld.WorldDualMode.PLAY) {\n            if (!permissions.isAllowFlightInPlay() && !world.canPerform(player, \"admin\")) {\n                player.setFlying(false);\n                player.setAllowFlight(false);\n            }\n        } else {\n            // Allow flight in dev mode\n            player.setAllowFlight(true);\n        }\n    }\n    \n    /**\n     * Sends welcome message with world info and permissions\n     */\n    private void sendWorldWelcomeMessage(Player player, CreativeWorld world) {\n        String modeEmoji = world.getDualMode() == CreativeWorld.WorldDualMode.DEV ? \"🔧\" : \"🎮\";\n        String modeColor = world.getDualMode() == CreativeWorld.WorldDualMode.DEV ? \"§e\" : \"§a\";\n        \n        player.sendMessage(\"\");\n        player.sendMessage(modeColor + modeEmoji + \" Добро пожаловать в \" + world.getName());\n        player.sendMessage(\"§7Режим: \" + world.getDualMode().getDisplayName());\n        \n        // Show permission level\n        WorldPermissions permissions = world.getPermissions();\n        if (world.isOwner(player)) {\n            player.sendMessage(\"§c⚡ Вы владелец этого мира\");\n        } else {\n            WorldPermissions.PermissionLevel level = permissions.getPlayerPermission(player.getUniqueId());\n            player.sendMessage(\"§7Ваш уровень: \" + level.getDisplayName());\n        }\n        \n        // Show access mode\n        WorldPermissions.AccessMode accessMode = world.getDualMode() == CreativeWorld.WorldDualMode.DEV ? \n            permissions.getDevWorldAccess() : permissions.getPlayWorldAccess();\n        player.sendMessage(\"§7Доступ: \" + accessMode.getDisplayName());\n        \n        player.sendMessage(\"\");\n    }\n}\n