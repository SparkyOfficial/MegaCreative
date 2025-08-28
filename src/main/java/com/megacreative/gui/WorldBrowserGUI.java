package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * World Browser GUI - Fixed to use new GUIManager system
 */
public class WorldBrowserGUI {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    private int page = 0;
    
    public WorldBrowserGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§8§lБраузер миров");
        
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
        
        // Получение публичных миров
        List<CreativeWorld> publicWorlds = plugin.getWorldManager().getAllPublicWorlds();
        int startIndex = page * 28;
        int endIndex = Math.min(startIndex + 28, publicWorlds.size());
        
        // Отображение миров
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            if (slot > 43) break;
            
            CreativeWorld world = publicWorlds.get(i);
            ItemStack worldItem = new ItemStack(world.getWorldType().getIcon());
            ItemMeta worldMeta = worldItem.getItemMeta();
            worldMeta.setDisplayName("§f§l" + world.getName());
            
            boolean isFavorite = plugin.getPlayerManager().isFavorite(player.getUniqueId(), world.getId());
            
            worldMeta.setLore(Arrays.asList(
                "§7ID: §f" + world.getId(),
                "§7Владелец: §f" + world.getOwnerName(),
                "§7Тип: §f" + world.getWorldType().getDisplayName(),
                "§7Онлайн: §f" + world.getOnlineCount(),
                "§7Рейтинг: " + (world.getRating() >= 0 ? "§a+" : "§c") + world.getRating(),
                "§7Избранное: " + (isFavorite ? "§a✓" : "§7✗"),
                "",
                "§a▶ ЛКМ - Войти в мир",
                "§e▶ ПКМ - Действия с миром"
            ));
            worldItem.setItemMeta(worldMeta);
            inventory.setItem(slot, worldItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2;
        }
        
        // Навигация
        if (page > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("§a§lПредыдущая страница");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(45, prevButton);
        }
        
        if (endIndex < publicWorlds.size()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName("§a§lСледующая страница");
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(53, nextButton);
        }
        
        // Информация
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e§lИнформация");
        infoMeta.setLore(Arrays.asList(
            "§7Всего миров: §f" + publicWorlds.size(),
            "§7Страница: §f" + (page + 1) + "/" + ((publicWorlds.size() - 1) / 28 + 1),
            "",
            "§7Здесь отображаются все публичные миры",
            "§7Вы можете войти в любой из них"
        ));
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(49, infoItem);
    }
    
    public void open() {
        // Use the new GUIManager system instead of old GuiListener
        plugin.getGuiManager().registerGUI(player, this, inventory);
        player.openInventory(inventory);
    }
    
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Навигация
        if (displayName.contains("Предыдущая страница")) {
            page--;
            setupInventory();
            return;
        }
        
        if (displayName.contains("Следующая страница")) {
            page++;
            setupInventory();
            return;
        }
        
        // Клик по миру
        List<CreativeWorld> publicWorlds = plugin.getWorldManager().getAllPublicWorlds();
        int slot = event.getSlot();
        int worldIndex = getWorldIndexFromSlot(slot);
        
        if (worldIndex >= 0 && worldIndex < publicWorlds.size()) {
            CreativeWorld world = publicWorlds.get(worldIndex);
            
            if (event.isLeftClick()) {
                // Вход в мир
                player.closeInventory();
                player.performCommand("join " + world.getId());
            } else if (event.isRightClick()) {
                // Действия с миром
                player.closeInventory();
                new WorldActionsGUI(plugin, player, world).open();
            }
        }
    }
    
    private int getWorldIndexFromSlot(int slot) {
        if (slot < 10 || slot > 43) return -1;
        
        int row = slot / 9;
        int col = slot % 9;
        
        if (col == 0 || col == 8) return -1;
        
        return (row - 1) * 7 + (col - 1) + page * 28;
    }
}
