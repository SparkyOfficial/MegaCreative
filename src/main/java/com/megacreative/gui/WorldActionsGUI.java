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

/**
 * World Actions GUI - Fixed to use new GUIManager system
 */
public class WorldActionsGUI implements Listener {
    
    private final MegaCreative plugin;
    private final Player player;
    private final CreativeWorld world;
    private final Inventory inventory;
    
    public WorldActionsGUI(MegaCreative plugin, Player player, CreativeWorld world) {
        this.plugin = plugin;
        this.player = player;
        this.world = world;
        this.inventory = Bukkit.createInventory(null, 27, "§8§lДействия с миром: " + world.getName());
        
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Заполнение стеклом
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, glass);
        }
        
        // Войти в мир
        ItemStack joinButton = new ItemStack(Material.EMERALD);
        ItemMeta joinMeta = joinButton.getItemMeta();
        joinMeta.setDisplayName("§a§lВойти в мир");
        joinMeta.setLore(Arrays.asList(
            "§7Перейти в мир для",
            "§7строительства и кодинга",
            "§e▶ Нажмите для входа"
        ));
        joinButton.setItemMeta(joinMeta);
        inventory.setItem(10, joinButton);
        
        // Настройки мира
        ItemStack settingsButton = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settingsButton.getItemMeta();
        settingsMeta.setDisplayName("§e§lНастройки мира");
        settingsMeta.setLore(Arrays.asList(
            "§7Изменить флаги и",
            "§7параметры мира",
            "§e▶ Нажмите для настроек"
        ));
        settingsButton.setItemMeta(settingsMeta);
        inventory.setItem(11, settingsButton);
        
        // Комментарии
        ItemStack commentsButton = new ItemStack(Material.BOOK);
        ItemMeta commentsMeta = commentsButton.getItemMeta();
        commentsMeta.setDisplayName("§b§lКомментарии");
        commentsMeta.setLore(Arrays.asList(
            "§7Просмотреть и добавить",
            "§7комментарии к миру",
            "§e▶ Нажмите для просмотра"
        ));
        commentsButton.setItemMeta(commentsMeta);
        inventory.setItem(12, commentsButton);
        
        // Скрипты
        ItemStack scriptsButton = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta scriptsMeta = scriptsButton.getItemMeta();
        scriptsMeta.setDisplayName("§6§lСкрипты");
        scriptsMeta.setLore(Arrays.asList(
            "§7Управление скриптами",
            "§7автоматизации",
            "§e▶ Нажмите для управления"
        ));
        scriptsButton.setItemMeta(scriptsMeta);
        inventory.setItem(13, scriptsButton);
        
        // Кнопка назад
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§e§lНазад");
        backButton.setItemMeta(backMeta);
        inventory.setItem(22, backButton);
    }
    
    public void open() {
        // Use the new GUIManager system
        plugin.getGuiManager().registerGUI(player, this, inventory);
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
        if (displayName.contains("Назад")) {
            player.closeInventory();
            // GUIManager will handle cleanup automatically
            new WorldBrowserGUI(plugin, player).open();
            return;
        }
        
        // Войти в мир
        if (displayName.contains("Войти в мир")) {
            player.closeInventory();
            // GUIManager will handle cleanup automatically
            player.performCommand("join " + world.getId());
        }
        
        // Настройки мира
        else if (displayName.contains("Настройки мира")) {
            player.closeInventory();
            // GUIManager will handle cleanup automatically
            new WorldSettingsGUI(plugin, player, world).open();
        }
        
        // Комментарии
        else if (displayName.contains("Комментарии")) {
            player.closeInventory();
            // GUIManager will handle cleanup automatically
            new WorldCommentsGUI(plugin, player, world, 0).open();
        }
        
        // Скрипты
        else if (displayName.contains("Скрипты")) {
            player.closeInventory();
            // GUIManager will handle cleanup automatically
            new ScriptsGUI(plugin, player).open();
        }
    }
}
