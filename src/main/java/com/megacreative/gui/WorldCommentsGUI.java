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
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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
        
        // Register this class as an event listener if not already registered
        if (!isRegistered) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            isRegistered = true;
        }
        
        createInventory();
    }
    
    private void createInventory() {
        List<WorldComment> comments = world.getComments();
        int totalPages = Math.max(1, (int) Math.ceil((double) comments.size() / commentsPerPage));
        int currentPage = Math.min(page, totalPages - 1);
        int startIndex = currentPage * commentsPerPage;
        int endIndex = Math.min(startIndex + commentsPerPage, comments.size());
        
        // Create inventory with 4 rows (36 slots) - 3 for comments, 1 for navigation
        Inventory inventory = Bukkit.createInventory(null, 36, "§6§lКомментарии: " + world.getName());
        
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
        
        // Add page info
        ItemStack pageInfo = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = pageInfo.getItemMeta();
        pageMeta.setDisplayName("§6Страница §e" + (currentPage + 1) + " §6из §e" + totalPages);
        pageInfo.setItemMeta(pageMeta);
        inventory.setItem(31, pageInfo);
        
        // Add comment button
        ItemStack addComment = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta addMeta = addComment.getItemMeta();
        addMeta.setDisplayName("§a§lДобавить комментарий");
        addMeta.setLore(Arrays.asList(
            "§7Нажмите, чтобы добавить новый",
            "§7комментарий к этому миру"
        ));
        addComment.setItemMeta(addMeta);
        inventory.setItem(31, addComment);
        
        // Back button
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c§lНазад");
        backMeta.setLore(Collections.singletonList("§7Вернуться к настройкам мира"));
        backButton.setItemMeta(backMeta);
        inventory.setItem(30, backButton);
    }
    
    /**
     * Opens the GUI for the player
     */
    public void open() {
        if (inventory != null) {
            player.openInventory(inventory);
        }
    }
    
    /**
     * Handles inventory close event to clean up the listener
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer().getUniqueId().equals(player.getUniqueId()) && 
            event.getView().getTopInventory().equals(this.inventory)) {
            // Unregister this listener when the inventory is closed
            HandlerList.unregisterAll(this);
            isRegistered = false;
        }
    }
    
    // Handle inventory clicks for comments GUI
    public static boolean handleClick(InventoryClickEvent event, MegaCreative plugin) {
        if (!(event.getWhoClicked() instanceof Player)) return false;
        
        String viewTitle = event.getView().getTitle();
        if (!viewTitle.startsWith("§6§lКомментарии: ")) return false;
        
        event.setCancelled(true);
        Player clicker = (Player) event.getWhoClicked();
        
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return true;
        
        String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
        if (displayName == null) return true;
        
        // Get the world name from the inventory title
        String worldName = viewTitle.replace("§6§lКомментарии: ", "");
        // Get the world by its display name
        CreativeWorld world = plugin.getWorldManager().getWorldByName(worldName);
        
        if (world == null) {
            clicker.sendMessage("§cОшибка: мир не найден");
            return true;
        }
        
        // Handle back button
        if (displayName.equals("§c§lНазад")) {
            new WorldActionsGUI(plugin, clicker, world);
            return true;
        }
        
        // Handle add comment button
        if (displayName.equals("§a§lДобавить комментарий")) {
            clicker.closeInventory();
            clicker.sendMessage("§a§lНапишите ваш комментарий в чат (или 'отмена' для отмены):");
            plugin.getCommentInputs().put(clicker.getUniqueId(), world);
            return true;
        }
        
        // Handle pagination
        if (displayName.startsWith("§e§l")) {
            if (displayName.contains("Предыдущая")) {
                // Get current page from the item's lore
                int currentPage = 0;
                if (event.getCurrentItem().getItemMeta().hasLore()) {
                    for (String line : event.getCurrentItem().getItemMeta().getLore()) {
                        if (line.startsWith("§7Страница: ")) {
                            try {
                                currentPage = Integer.parseInt(line.replace("§7Страница: ", "").trim()) - 1;
                            } catch (NumberFormatException e) {
                                // Use default page 0
                            }
                            break;
                        }
                    }
                }
                if (currentPage > 0) {
                    new WorldCommentsGUI(plugin, clicker, world, currentPage);
                }
                return true;
            } else if (displayName.contains("Следующая")) {
                // Get next page from the item's lore
                int nextPage = 1;
                if (event.getCurrentItem().getItemMeta().hasLore()) {
                    for (String line : event.getCurrentItem().getItemMeta().getLore()) {
                        if (line.startsWith("§7Страница: ")) {
                            try {
                                nextPage = Integer.parseInt(line.replace("§7Страница: ", "").trim());
                            } catch (NumberFormatException e) {
                                // Use default next page 1
                            }
                            break;
                        }
                    }
                }
                new WorldCommentsGUI(plugin, clicker, world, nextPage - 1);
                return true;
            }
        }
        
        return true;
    }
    
    // Handle chat input for adding comments
    public static boolean handleChat(Player player, String message, MegaCreative plugin) {
        if (!plugin.getCommentInputs().containsKey(player.getUniqueId())) {
            return false;
        }
        
        // Get the world the player is commenting on
        CreativeWorld world = plugin.getCommentInputs().get(player.getUniqueId());
        
        // Check for cancel command
        if (message.equalsIgnoreCase("отмена")) {
            player.sendMessage("§cДобавление комментария отменено.");
            plugin.getCommentInputs().remove(player.getUniqueId());
            return true;
        }
        
        // Validate message length
        if (message.length() > 100) {
            player.sendMessage("§cКомментарий слишком длинный! Максимум 100 символов.");
            return true;
        }
        
        // Create and add the comment
        WorldComment comment = new WorldComment(
            player.getUniqueId(),
            player.getName(),
            message,
            System.currentTimeMillis()
        );
        
        world.addComment(comment);
        
        // Save the world to persist the comment
        plugin.getWorldManager().saveWorld(world);
        
        // Remove from inputs and show the updated comments GUI
        plugin.getCommentInputs().remove(player.getUniqueId());
        
        // Open the comments GUI on the first page
        new WorldCommentsGUI(plugin, player, world, 0);
        
        return true;
    }
}
