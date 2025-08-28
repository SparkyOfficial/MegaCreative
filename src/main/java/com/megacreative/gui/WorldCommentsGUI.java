package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldComment;
import com.megacreative.managers.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class WorldCommentsGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final CreativeWorld world;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final int page;
    private static final int COMMENTS_PER_PAGE = 21;
    
    public WorldCommentsGUI(MegaCreative plugin, Player player, CreativeWorld world, int page) {
        this.plugin = plugin;
        this.player = player;
        this.world = world;
        this.page = page;
        this.guiManager = plugin.getGuiManager();
        this.inventory = Bukkit.createInventory(null, 54, "§6§lКомментарии: " + world.getName());
        
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Заполнение стеклом
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glass);
        }
        
        List<WorldComment> comments = world.getComments();
        int startIndex = page * COMMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + COMMENTS_PER_PAGE, comments.size());
        
        // Отображение комментариев
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            if (slot > 43) break;
            
            WorldComment comment = comments.get(i);
            ItemStack commentItem = new ItemStack(Material.PAPER);
            ItemMeta commentMeta = commentItem.getItemMeta();
            commentMeta.setDisplayName("§f§l" + comment.getAuthorName());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String date = sdf.format(new Date(comment.getTimestamp()));
            
                         commentMeta.setLore(Arrays.asList(
                 "§7" + comment.getText(),
                 "",
                 "§7Дата: §f" + date,
                 "§7Автор: §f" + comment.getAuthorName()
             ));
            commentItem.setItemMeta(commentMeta);
            inventory.setItem(slot, commentItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2;
        }
        
        // Кнопка добавления комментария
        ItemStack addButton = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addButton.getItemMeta();
        addMeta.setDisplayName("§a§lДобавить комментарий");
        addMeta.setLore(Arrays.asList(
            "§7Написать новый комментарий",
            "§7к этому миру",
            "§e▶ Нажмите для добавления"
        ));
        addButton.setItemMeta(addMeta);
        inventory.setItem(49, addButton);
        
        // Навигация
        if (page > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("§a§lПредыдущая страница");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(45, prevButton);
        }
        
        if (endIndex < comments.size()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName("§a§lСледующая страница");
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(53, nextButton);
        }
        
        // Кнопка назад
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c§lНазад");
        backButton.setItemMeta(backMeta);
        inventory.setItem(46, backButton);
    }
    
    public void open() {
        // Register with GUIManager and open inventory
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
    }
    
    @Override
    public String getGUITitle() {
        return "World Comments GUI for " + world.getName();
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Кнопка назад
        if (displayName.contains("Назад")) {
            player.closeInventory();
            // GUIManager will handle automatic cleanup
            new WorldActionsGUI(plugin, player, world).open();
            return;
        }
        
        // Добавление комментария
        if (displayName.contains("Добавить комментарий")) {
            player.closeInventory();
            // GUIManager will handle automatic cleanup
            player.sendMessage("§aНапишите ваш комментарий в чат или §eотмена§a для отмены:");
            plugin.getCommentInputs().put(player.getUniqueId(), world.getId());
            return;
        }
        
        // Навигация
        if (displayName.contains("Предыдущая страница")) {
            player.closeInventory();
            // GUIManager will handle automatic cleanup
            new WorldCommentsGUI(plugin, player, world, page - 1).open();
            return;
        }
        
        if (displayName.contains("Следующая страница")) {
            player.closeInventory();
            // GUIManager will handle automatic cleanup
            new WorldCommentsGUI(plugin, player, world, page + 1).open();
            return;
        }
    }
    
    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
}
