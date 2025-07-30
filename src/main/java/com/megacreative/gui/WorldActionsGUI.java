package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class WorldActionsGUI implements Listener {
    
    private final MegaCreative plugin;
    private final Player player;
    private final CreativeWorld world;
    private final Inventory inventory;
    
    public WorldActionsGUI(MegaCreative plugin, Player player, CreativeWorld world) {
        this.plugin = plugin;
        this.player = player;
        this.world = world;
        this.inventory = Bukkit.createInventory(null, 27, "§8§lДействия: " + world.getName());
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заполнение стеклом
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, glass);
        }
        
        // Лайк
        boolean hasLiked = world.getLikedBy().contains(player.getUniqueId());
        ItemStack likeItem = new ItemStack(hasLiked ? Material.LIME_DYE : Material.GREEN_DYE);
        ItemMeta likeMeta = likeItem.getItemMeta();
        likeMeta.setDisplayName(hasLiked ? "§a§lУбрать лайк" : "§a§lПоставить лайк");
        likeMeta.setLore(Arrays.asList(
            "§7Лайков: §f" + world.getLikes(),
            hasLiked ? "§7Вы уже поставили лайк" : "§7Нажмите, чтобы поставить лайк"
        ));
        likeItem.setItemMeta(likeMeta);
        inventory.setItem(11, likeItem);
        
        // Дизлайк
        boolean hasDisliked = world.getDislikedBy().contains(player.getUniqueId());
        ItemStack dislikeItem = new ItemStack(hasDisliked ? Material.RED_DYE : Material.ORANGE_DYE);
        ItemMeta dislikeMeta = dislikeItem.getItemMeta();
        dislikeMeta.setDisplayName(hasDisliked ? "§c§lУбрать дизлайк" : "§c§lПоставить дизлайк");
        dislikeMeta.setLore(Arrays.asList(
            "§7Дизлайков: §f" + world.getDislikes(),
            hasDisliked ? "§7Вы уже поставили дизлайк" : "§7Нажмите, чтобы поставить дизлайк"
        ));
        dislikeItem.setItemMeta(dislikeMeta);
        inventory.setItem(13, dislikeItem);
        
        // Избранное
        boolean isFavorite = plugin.getPlayerManager().isFavorite(player.getUniqueId(), world.getId());
        ItemStack favoriteItem = new ItemStack(isFavorite ? Material.NETHER_STAR : Material.NETHER_STAR);
        ItemMeta favoriteMeta = favoriteItem.getItemMeta();
        favoriteMeta.setDisplayName(isFavorite ? "§e§lУбрать из избранного" : "§e§lДобавить в избранное");
        favoriteMeta.setLore(Arrays.asList(
            isFavorite ? "§7Мир в вашем избранном" : "§7Добавить мир в избранное"
        ));
        favoriteItem.setItemMeta(favoriteMeta);
        inventory.setItem(15, favoriteItem);
        
        // Комментарии
        int commentsCount = world.getComments().size();
        ItemStack commentsItem = new ItemStack(Material.BOOK);
        ItemMeta commentsMeta = commentsItem.getItemMeta();
        commentsMeta.setDisplayName("§b§lКомментарии");
        commentsMeta.setLore(Arrays.asList(
            "§7Количество: " + commentsCount,
            "§7Нажмите, чтобы просмотреть",
            "§7и добавить комментарии"
        ));
        commentsItem.setItemMeta(commentsMeta);
        inventory.setItem(22, commentsItem);
        
        // Назад
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c§lНазад");
        backButton.setItemMeta(backMeta);
        inventory.setItem(18, backButton);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Назад
        if (clicked.getType() == Material.ARROW && displayName.contains("Назад")) {
            player.closeInventory();
            new WorldBrowserGUI(plugin, player).open();
            return;
        }
        
        // Лайк
        if (displayName.contains("лайк") && !displayName.contains("дизлайк")) {
            if (world.addLike(player.getUniqueId())) {
                player.sendMessage("§aВы поставили лайк миру §f" + world.getName());
            } else {
                world.getLikedBy().remove(player.getUniqueId());
                world.setLikes(world.getLikes() - 1);
                player.sendMessage("§7Вы убрали лайк с мира §f" + world.getName());
            }
            plugin.getWorldManager().saveWorld(world);
            setupInventory();
        }
        
        // Дизлайк
        else if (displayName.contains("дизлайк")) {
            if (world.addDislike(player.getUniqueId())) {
                player.sendMessage("§cВы поставили дизлайк миру §f" + world.getName());
            } else {
                world.getDislikedBy().remove(player.getUniqueId());
                world.setDislikes(world.getDislikes() - 1);
                player.sendMessage("§7Вы убрали дизлайк с мира §f" + world.getName());
            }
            plugin.getWorldManager().saveWorld(world);
            setupInventory();
        }
        
        // Избранное
        else if (displayName.contains("избранное")) {
            boolean isFavorite = plugin.getPlayerManager().isFavorite(player.getUniqueId(), world.getId());
            if (isFavorite) {
                plugin.getPlayerManager().removeFromFavorites(player.getUniqueId(), world.getId());
                world.removeFromFavorites(player.getUniqueId());
                player.sendMessage("§7Мир §f" + world.getName() + " §7убран из избранного");
            } else {
                plugin.getPlayerManager().addToFavorites(player.getUniqueId(), world.getId());
                world.addToFavorites(player.getUniqueId());
                player.sendMessage("§aMир §f" + world.getName() + " §aдобавлен в избранное");
            }
            setupInventory();
        }
        
        // Комментарий
        else if (displayName.equals("§b§lКомментарии")) {
            new WorldCommentsGUI(plugin, player, world, 0);
        }
    }
}
