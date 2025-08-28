package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Менеджер инвентаря разработчика
 * Отслеживает и восстанавливает инструменты разработчика при входе в dev-мир
 */
public class DevInventoryManager implements Listener {
    
    private final MegaCreative plugin;
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Set<UUID> playersInDevWorld = new HashSet<>();
    
    public DevInventoryManager(MegaCreative plugin) {
        this.plugin = plugin;
        startInventoryChecker();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        if (isDevWorld(player.getWorld().getName())) {
            handleDevWorldEntry(player);
        } else if (isDevWorld(event.getFrom().getName())) {
            handleDevWorldExit(player);
        }
    }
    
    private void handleDevWorldEntry(Player player) {
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null || !creativeWorld.canCode(player)) {
            player.sendMessage("§cУ вас нет прав на кодирование в этом мире!");
            return;
        }
        
        savedInventories.put(player.getUniqueId(), player.getInventory().getContents().clone());
        playersInDevWorld.add(player.getUniqueId());
        giveDevTools(player);
        player.sendMessage("§aВы вошли в режим разработки! Инструменты восстанавливаются автоматически.");
    }
    
    private void handleDevWorldExit(Player player) {
        UUID playerId = player.getUniqueId();
        playersInDevWorld.remove(playerId);
        
        ItemStack[] savedInventory = savedInventories.remove(playerId);
        if (savedInventory != null) {
            player.getInventory().setContents(savedInventory);
            player.sendMessage("§aВаш инвентарь восстановлен!");
        }
    }
    
    private void giveDevTools(Player player) {
        player.getInventory().clear();
        
        // Основные блоки кода
        player.getInventory().setItem(0, createDevItem(Material.DIAMOND_BLOCK, CodingItems.EVENT_BLOCK_NAME));
        player.getInventory().setItem(1, createDevItem(Material.COBBLESTONE, CodingItems.ACTION_BLOCK_NAME));
        player.getInventory().setItem(2, createDevItem(Material.OAK_PLANKS, CodingItems.CONDITION_BLOCK_NAME));
        player.getInventory().setItem(3, createDevItem(Material.IRON_BLOCK, CodingItems.VARIABLE_BLOCK_NAME));
        player.getInventory().setItem(4, createDevItem(Material.OBSIDIAN, CodingItems.IF_VAR_BLOCK_NAME));
        player.getInventory().setItem(5, createDevItem(Material.REDSTONE_BLOCK, CodingItems.IF_GAME_BLOCK_NAME));
        player.getInventory().setItem(6, createDevItem(Material.EMERALD_BLOCK, "§aПовторить N раз"));
        player.getInventory().setItem(7, createDevItem(Material.END_STONE, CodingItems.ELSE_BLOCK_NAME));
        player.getInventory().setItem(8, createDevItem(Material.STICK, CodingItems.COPIER_TOOL_NAME));
        
        // Дополнительные инструменты
        player.getInventory().setItem(9, createDevItem(Material.PAPER, CodingItems.DATA_CREATOR_NAME));
        
        player.updateInventory();
    }
    
    private void startInventoryChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID playerId : new HashSet<>(playersInDevWorld)) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        checkAndRestoreTools(player);
                    } else {
                        playersInDevWorld.remove(playerId);
                        savedInventories.remove(playerId);
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 100L);
    }
    
    private void checkAndRestoreTools(Player player) {
        if (!playersInDevWorld.contains(player.getUniqueId())) return;
        
        boolean hasDevTools = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String name = item.getItemMeta().getDisplayName();
                if (name.contains(CodingItems.EVENT_BLOCK_NAME) || name.contains(CodingItems.COPIER_TOOL_NAME)) {
                    hasDevTools = true;
                    break;
                }
            }
        }
        
        if (!hasDevTools) {
            giveDevTools(player);
            player.sendMessage("§aИнструменты разработчика восстановлены!");
        }
    }
    
    private static ItemStack createDevItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private boolean isDevWorld(String worldName) {
        return worldName.endsWith("_dev");
    }
    
    public void forceRestoreTools(Player player) {
        if (playersInDevWorld.contains(player.getUniqueId())) {
            giveDevTools(player);
            player.sendMessage("§aИнструменты принудительно восстановлены!");
        }
    }
}