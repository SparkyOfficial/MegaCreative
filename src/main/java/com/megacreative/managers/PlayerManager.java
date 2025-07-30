package com.megacreative.managers;

import com.megacreative.MegaCreative;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PlayerManager {
    
    private final MegaCreative plugin;
    private final Map<UUID, Set<String>> playerFavorites;
    
    public PlayerManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.playerFavorites = new HashMap<>();
    }
    
    public void giveStarterItems(Player player) {
        player.getInventory().clear();
        
        // Алмаз (Мои миры)
        ItemStack myWorldsItem = new ItemStack(Material.DIAMOND);
        ItemMeta myWorldsMeta = myWorldsItem.getItemMeta();
        myWorldsMeta.setDisplayName("§b§lМои миры");
        myWorldsMeta.setLore(Arrays.asList(
            "§7Управляйте своими мирами",
            "§7Создавайте новые миры",
            "§e▶ Нажмите, чтобы открыть"
        ));
        myWorldsItem.setItemMeta(myWorldsMeta);
        
        // Компас (Браузер миров)
        ItemStack browserItem = new ItemStack(Material.COMPASS);
        ItemMeta browserMeta = browserItem.getItemMeta();
        browserMeta.setDisplayName("§a§lБраузер миров");
        browserMeta.setLore(Arrays.asList(
            "§7Исследуйте миры других игроков",
            "§7Ставьте лайки и комментарии",
            "§e▶ Нажмите, чтобы открыть"
        ));
        browserItem.setItemMeta(browserMeta);
        
        player.getInventory().setItem(0, myWorldsItem);
        player.getInventory().setItem(1, browserItem);
    }
    
    public void addToFavorites(UUID playerId, String worldId) {
        playerFavorites.computeIfAbsent(playerId, k -> new HashSet<>()).add(worldId);
    }
    
    public void removeFromFavorites(UUID playerId, String worldId) {
        Set<String> favorites = playerFavorites.get(playerId);
        if (favorites != null) {
            favorites.remove(worldId);
        }
    }
    
    public boolean isFavorite(UUID playerId, String worldId) {
        Set<String> favorites = playerFavorites.get(playerId);
        return favorites != null && favorites.contains(worldId);
    }
    
    public Set<String> getFavorites(UUID playerId) {
        return playerFavorites.getOrDefault(playerId, new HashSet<>());
    }
}
