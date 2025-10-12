package com.megacreative.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Main menu implementation for MegaCreative
 * This is a concrete implementation of AbstractMenu
 */
public class MainMenu extends AbstractMenu {
    
    private final Player player;
    
    public MainMenu(Player player) {
        super(6, "§e§lMegaCreative §7- §fMain Menu");
        this.player = player;
    }
    
    @Override
    public void fillItems(Player player) {
        
        fillBorder();
        
        
        setItem(10, createMenuItem(Material.BOOK, "§e§lMy Worlds", "§7Manage your creative worlds"));
        setItem(11, createMenuItem(Material.COMMAND_BLOCK, "§e§lScripts", "§7Create and manage scripts"));
        setItem(12, createMenuItem(Material.REDSTONE, "§e§lBlocks", "§7Configure code blocks"));
        setItem(13, createMenuItem(Material.PLAYER_HEAD, "§e§lPlayer Settings", "§7Manage your preferences"));
        setItem(14, createMenuItem(Material.BARRIER, "§e§lWorld Templates", "§7Browse world templates"));
        setItem(15, createMenuItem(Material.EMERALD, "§e§lMarketplace", "§7Browse community creations"));
        setItem(16, createMenuItem(Material.NETHER_STAR, "§e§lPlugins", "§7Manage plugins and extensions"));
        
        
        setItem(28, createMenuItem(Material.CLOCK, "§6§lRecent Worlds", "§7Quick access to recent worlds"));
        setItem(29, createMenuItem(Material.MAP, "§6§lFavorites", "§7Your favorite scripts and worlds"));
        setItem(30, createMenuItem(Material.PAPER, "§6§lTutorials", "§7Learn how to use MegaCreative"));
        setItem(31, createMenuItem(Material.COMPASS, "§6§lDocumentation", "§7Browse the complete documentation"));
        setItem(32, createMenuItem(Material.BELL, "§6§lNotifications", "§7View recent notifications"));
        setItem(33, createMenuItem(Material.CHEST, "§6§lResources", "§7Manage resources and assets"));
        setItem(34, createMenuItem(Material.ENCHANTING_TABLE, "§6§lAdvanced Tools", "§7Access advanced features"));
        
        
        setItem(40, createMenuItem(Material.END_CRYSTAL, "§d§lMagic Center", "§7Access reference system features"));
        
        
        setItem(45, createPlayerInfoItem());
        
        
        setItem(53, createSystemInfoItem());
    }
    
    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        
        
        switch (slot) {
            case 10:
                
                player.sendMessage("§eOpening My Worlds menu...");
                player.closeInventory();
                
                break;
            case 11:
                
                player.sendMessage("§eOpening Scripts menu...");
                player.closeInventory();
                
                break;
            case 12:
                
                player.sendMessage("§eOpening Blocks menu...");
                player.closeInventory();
                
                break;
            case 13:
                
                player.sendMessage("§eOpening Player Settings menu...");
                player.closeInventory();
                
                break;
            case 14:
                
                player.sendMessage("§eOpening World Templates menu...");
                player.closeInventory();
                
                break;
            case 15:
                
                player.sendMessage("§eOpening Marketplace...");
                player.closeInventory();
                
                break;
            case 16:
                
                player.sendMessage("§eOpening Plugins menu...");
                player.closeInventory();
                
                break;
            case 40:
                
                player.sendMessage("§d✨ Accessing reference system magic...");
                player.closeInventory();
                
                break;
            default:
                
                break;
        }
    }
    
    @Override
    public void onOpen(@NotNull InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        player.sendMessage("§aWelcome to MegaCreative! §7(Reference System Style)");
        
        
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
            player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 1);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    }
    
    /**
     * Create a menu item with display name and lore
     */
    private ItemStack createMenuItem(Material material, String displayName, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        
        List<String> loreList = new ArrayList<>();
        loreList.add(lore);
        loreList.add("");
        loreList.add("§e⚡ Click to open");
        meta.setLore(loreList);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create player info item
     */
    private ItemStack createPlayerInfoItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lPlayer: §f" + player.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Rank: §6Creator");
        lore.add("§7Worlds: §a" + player.getWorld().getName());
        lore.add("");
        lore.add("§e✨ Reference System User");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Create system info item
     */
    private ItemStack createSystemInfoItem() {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§lSystem Info");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Version: §f1.0.0");
        lore.add("§7Status: §aOnline");
        lore.add("§7Mode: §dReference System");
        lore.add("");
        lore.add("§e✨ Enhanced with Magic");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
}