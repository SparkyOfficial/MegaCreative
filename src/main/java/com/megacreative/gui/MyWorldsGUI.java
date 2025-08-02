package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.listeners.GuiListener;

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
import java.util.List;

public class MyWorldsGUI implements Listener {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    
    public MyWorldsGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§8§lМои миры");
        
        // Регистрируем GUI в централизованной системе
        GuiListener.registerOpenGui(player, this);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заполнение стеклом
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glass);
        }
        
        // Кнопка создания мира
        if (plugin.getWorldManager().getPlayerWorldCount(player) < 5) {
            ItemStack createButton = new ItemStack(Material.EMERALD);
            ItemMeta createMeta = createButton.getItemMeta();
            createMeta.setDisplayName("§a§lСоздать новый мир");
            createMeta.setLore(Arrays.asList(
                "§7Создайте новый мир для строительства",
                "§7и программирования",
                "§e▶ Нажмите для создания"
            ));
            createButton.setItemMeta(createMeta);
            inventory.setItem(49, createButton);
        }
        
        // Отображение миров игрока
        List<CreativeWorld> playerWorlds = plugin.getWorldManager().getPlayerWorlds(player);
        int slot = 10;
        
        for (CreativeWorld world : playerWorlds) {
            if (slot > 43) break;
            
            ItemStack worldItem = new ItemStack(world.getWorldType().getIcon());
            ItemMeta worldMeta = worldItem.getItemMeta();
            worldMeta.setDisplayName("§f§l" + world.getName());
            worldMeta.setLore(Arrays.asList(
                "§7ID: §f" + world.getId(),
                "§7Тип: §f" + world.getWorldType().getDisplayName(),
                "§7Режим: §f" + world.getMode().getDisplayName(),
                "§7Онлайн: §f" + world.getOnlineCount(),
                "",
                "§a▶ ЛКМ - Войти в мир",
                "§c▶ ПКМ - Настройки мира"
            ));
            worldItem.setItemMeta(worldMeta);
            inventory.setItem(slot, worldItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2;
        }
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
        
        // Создание нового мира
        if (clicked.getType() == Material.EMERALD && displayName.contains("Создать")) {
            player.closeInventory();
            // Удаляем регистрацию GUI
            GuiListener.unregisterOpenGui(player);
            new WorldCreationGUI(plugin, player).open();
            return;
        }
        
        // Клик по миру
        List<CreativeWorld> playerWorlds = plugin.getWorldManager().getPlayerWorlds(player);
        int slot = event.getSlot();
        int worldIndex = getWorldIndexFromSlot(slot);
        
        if (worldIndex >= 0 && worldIndex < playerWorlds.size()) {
            CreativeWorld world = playerWorlds.get(worldIndex);
            
            if (event.isLeftClick()) {
                // Вход в мир
                player.closeInventory();
                // Удаляем регистрацию GUI
                GuiListener.unregisterOpenGui(player);
                player.performCommand("join " + world.getId());
            } else if (event.isRightClick()) {
                // Настройки мира
                player.closeInventory();
                // Удаляем регистрацию GUI
                GuiListener.unregisterOpenGui(player);
                new WorldSettingsGUI(plugin, player, world).open();
            }
        }
    }
    
    private int getWorldIndexFromSlot(int slot) {
        if (slot < 10 || slot > 43) return -1;
        
        int row = slot / 9;
        int col = slot % 9;
        
        if (col == 0 || col == 8) return -1;
        
        return (row - 1) * 7 + (col - 1);
    }
}
