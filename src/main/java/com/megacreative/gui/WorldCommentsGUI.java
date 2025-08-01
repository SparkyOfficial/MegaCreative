package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldComment;
import org.bukkit.event.HandlerList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

public class WorldCommentsGUI implements Listener {
    
    private final MegaCreative plugin;
    private final Player player;
    private final CreativeWorld world;
    private final int page;
    private final int commentsPerPage = 21; // 3 rows of 7 comments each
    private Inventory inventory;
    private boolean isRegistered = false;

    public WorldCommentsGUI(MegaCreative plugin, Player player, CreativeWorld world, int page) {
        this.plugin = plugin;
        this.player = player;
        this.world = world;
        this.page = Math.max(0, page); // Ensure page is not negative
        
        // Register this class as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
        isRegistered = true;
        
        createInventory();
    }
    
    private void createInventory() {
        List<WorldComment> comments = world.getComments();
        int totalPages = Math.max(1, (int) Math.ceil((double) comments.size() / commentsPerPage));
        int currentPage = Math.min(page, totalPages - 1);
        int startIndex = currentPage * commentsPerPage;
        int endIndex = Math.min(startIndex + commentsPerPage, comments.size());
        
        // Create inventory with 4 rows (36 slots) - 3 for comments, 1 for navigation
        inventory = Bukkit.createInventory(null, 36, "§6§lКомментарии: " + world.getName());
        
        // Add comments to the inventory
        for (int i = startIndex; i < endIndex; i++) {
            WorldComment comment = comments.get(i);
            int slot = i - startIndex;
            
            try {
                ItemStack commentItem = new ItemStack(Material.PAPER);
                ItemMeta meta = commentItem.getItemMeta();
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                String date = sdf.format(new Date(comment.getTimestamp()));
                
                meta.setDisplayName("§e" + comment.getAuthorName());
                meta.setLore(Arrays.asList(
                    "§7" + (comment.getText().length() > 30 ? 
                        comment.getText().substring(0, 27) + "..." : 
                        comment.getText()),
                    "",
                    "§8" + date
                ));
                
                commentItem.setItemMeta(meta);
                inventory.setItem(slot, commentItem);
            } catch (Exception e) {
                plugin.getLogger().warning("Error creating comment item: " + e.getMessage());
            }
        }
        
        // Add navigation buttons
        if (currentPage > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName("§e§lПредыдущая страница");
            prevMeta.setLore(Collections.singletonList("§7Страница: " + currentPage));
            prevPage.setItemMeta(prevMeta);
            inventory.setItem(27, prevPage);
        }
        
        if (endIndex < comments.size()) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName("§e§lСледующая страница");
            nextMeta.setLore(Collections.singletonList("§7Страница: " + (currentPage + 2)));
            nextPage.setItemMeta(nextMeta);
            inventory.setItem(35, nextPage);
        }
        
        // Add "Add Comment" button
        ItemStack addComment = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta addMeta = addComment.getItemMeta();
        addMeta.setDisplayName("§a§lДобавить комментарий");
        addMeta.setLore(Collections.singletonList("§7Нажмите, чтобы написать комментарий"));
        addComment.setItemMeta(addMeta);
        inventory.setItem(31, addComment);
    }
    
    public void open() {
        if (inventory != null) {
            player.openInventory(inventory);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getWhoClicked().equals(player)) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        
        if (clicked.getType() == Material.ARROW) {
            if (event.getSlot() == 27) {
                // Previous page
                new WorldCommentsGUI(plugin, player, world, page - 1).open();
            } else if (event.getSlot() == 35) {
                // Next page
                new WorldCommentsGUI(plugin, player, world, page + 1).open();
            }
        } else if (clicked.getType() == Material.WRITABLE_BOOK) {
            // Add comment
            player.closeInventory();
            player.sendMessage("§aНапишите ваш комментарий в чат:");
            plugin.getCommentInputs().put(player.getUniqueId(), world);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            // Unregister this listener to prevent memory leaks
            HandlerList.unregisterAll(this);
        }
    }
}
