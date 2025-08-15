package com.megacreative.coding;

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
import org.bukkit.Location;

import java.util.List;
import java.util.function.Consumer;
import java.util.Arrays;

public class CodingActionGUI {
    private final Player player;
    private final Material material;
    private final Location blockLocation;
    private final Consumer<String> onSelect;
    private final List<String> actions;
    private final Inventory inventory;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 7; // 7 слотов для действий, 2 для навигации

    public CodingActionGUI(Player player, Material material, Location blockLocation, List<String> actions, Consumer<String> onSelect) {
        this.player = player;
        this.material = material;
        this.blockLocation = blockLocation;
        this.actions = actions;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(null, 9, "§bВыберите действие или условие");
        // Регистрируем GUI в централизованной системе
        GuiListener.registerOpenGui(player, this);
        setupInventory();
    }

    private void setupInventory() {
        inventory.clear();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, actions.size());
        
        // Добавляем действия для текущей страницы
        for (int i = 0; i < endIndex - startIndex; i++) {
            String action = actions.get(startIndex + i);
            Material icon = getActionIconFromConfigOrDefault(action);
            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = getActionDisplayNameFromConfigOrDefault(action);
                meta.setDisplayName("§a" + displayName);
                meta.setLore(Arrays.asList(
                    "§7" + getActionDescriptionFromConfigOrDefault(action),
                    "§e▶ Нажмите для выбора"
                ));
                item.setItemMeta(meta);
            }
            inventory.setItem(i, item);
        }
        
        // Добавляем кнопки навигации
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("§e§lПредыдущая страница");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(7, prevButton);
        }
        
        if (endIndex < actions.size()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName("§e§lСледующая страница");
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(8, nextButton);
        }
    }

    private Material getActionIconFromConfigOrDefault(String action) {
        // позже можно расширить BlockConfiguration, чтобы хранить метаданные иконок/описаний
        // на данный момент возвращаем материал блока по умолчанию
        return material;
    }

    private String getActionDescriptionFromConfigOrDefault(String action) {
        var blockConfig = com.megacreative.MegaCreative.getInstance().getBlockConfiguration().getBlockConfig(material);
        if (blockConfig != null && blockConfig.getDescription() != null && !blockConfig.getDescription().isEmpty()) {
            return blockConfig.getDescription();
        }
        return "Действие: " + action;
    }

    private String getActionDisplayNameFromConfigOrDefault(String action) {
        // пока показываем ключ действия; можно расширить конфиг действиями с локализованным именем
        return action;
    }

    public void open() {
        // DEBUG: Логируем открытие GUI
        com.megacreative.MegaCreative.getInstance().getLogger().info("=== DEBUG: ОТКРЫТИЕ CodingActionGUI ===");
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Игрок: " + player.getName());
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Доступные действия: " + actions);
        
        player.openInventory(inventory);
    }

    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) return;
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Обработка навигации
        if (displayName.contains("Предыдущая страница")) {
            currentPage--;
            setupInventory();
            return;
        }
        if (displayName.contains("Следующая страница")) {
            currentPage++;
            setupInventory();
            return;
        }
        
        // Обработка выбора действия
        String action = displayName.replace("§a", "");
        if (actions.contains(action)) {
            // DEBUG: Логируем выбор действия
            com.megacreative.MegaCreative.getInstance().getLogger().info("=== DEBUG: ВЫБОР ДЕЙСТВИЯ ===");
            com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Игрок: " + player.getName());
            com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Выбрано действие: " + action);
            com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Локация: " + blockLocation);
            
            onSelect.accept(action);
            player.closeInventory();
            // Удаляем регистрацию GUI
            GuiListener.unregisterOpenGui(player);
        }
    }
}