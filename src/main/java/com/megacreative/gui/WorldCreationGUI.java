package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.CreativeWorldType;
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

public class WorldCreationGUI implements Listener {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    
    public WorldCreationGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, "§8§lСоздание мира");
        
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
        
        // Типы миров
        int[] slots = {10, 11, 12, 13, 14, 15};
        CreativeWorldType[] types = CreativeWorldType.values();
        
        for (int i = 0; i < Math.min(slots.length, types.length); i++) {
            CreativeWorldType type = types[i];
            ItemStack typeItem = new ItemStack(type.getIcon());
            ItemMeta typeMeta = typeItem.getItemMeta();
            typeMeta.setDisplayName("§f§l" + type.getDisplayName());
            typeMeta.setLore(Arrays.asList(
                "§7Создать мир типа " + type.getDisplayName(),
                "§7Окружение: §f" + type.getEnvironment().name(),
                "§e▶ Нажмите для создания"
            ));
            typeItem.setItemMeta(typeMeta);
            inventory.setItem(slots[i], typeItem);
        }
        
        // Кнопка назад
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c§lНазад");
        backMeta.setLore(Arrays.asList("§7Вернуться к списку миров"));
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
        
        // Кнопка назад
        if (clicked.getType() == Material.ARROW && displayName.contains("Назад")) {
            player.closeInventory();
            new MyWorldsGUI(plugin, player).open();
            return;
        }
        
        // Выбор типа мира
        for (CreativeWorldType type : CreativeWorldType.values()) {
            if (clicked.getType() == type.getIcon() && displayName.contains(type.getDisplayName())) {
                createWorld(type);
                return;
            }
        }
    }
    
    private void createWorld(CreativeWorldType type) {
        String worldName = "Мир " + player.getName();
        // WorldManager теперь сам обрабатывает лимиты, сообщения и телепортацию асинхронно.
        plugin.getWorldManager().createWorld(player, worldName, type);
    }
}
