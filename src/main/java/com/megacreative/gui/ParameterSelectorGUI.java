package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.managers.GUIManager;
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
import java.util.function.Consumer;

public class ParameterSelectorGUI implements Listener {
    
    private final MegaCreative plugin;
    private final Player player;
    private final CodeBlock block;
    private final String parameterName;
    private final List<String> options;
    private final Consumer<String> onSelect;
    private final Inventory inventory;
    
    public ParameterSelectorGUI(MegaCreative plugin, Player player, CodeBlock block, String parameterName, List<String> options, Consumer<String> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.parameterName = parameterName;
        this.options = options;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(null, 27, "§8§lВыбор параметра: " + parameterName);
        
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
        
        // Отображение опций
        int slot = 10;
        for (String option : options) {
            if (slot > 16) break;
            
            ItemStack optionItem = new ItemStack(Material.PAPER);
            ItemMeta optionMeta = optionItem.getItemMeta();
            optionMeta.setDisplayName("§f§l" + option);
            optionMeta.setLore(Arrays.asList(
                "§7Параметр: §f" + parameterName,
                "§7Блок: §f" + block.getAction(),
                "",
                "§a▶ Нажмите для выбора"
            ));
            optionItem.setItemMeta(optionMeta);
            inventory.setItem(slot, optionItem);
            
            slot++;
        }
        
        // Кнопка отмены
        ItemStack cancelButton = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName("§c§lОтмена");
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(22, cancelButton);
    }
    
    public void open() {
        // Регистрируем GUI в централизованной системе
        plugin.getGuiManager().registerGUI(player, new GUIManager.ManagedGUIInterface() {
            @Override
            public void onInventoryClick(InventoryClickEvent event) {
                handleInventoryClick(event);
            }
            
            @Override
            public String getGUITitle() {
                return "ParameterSelectorGUI: " + parameterName;
            }
        }, inventory);
        player.openInventory(inventory);
    }
    
    private void handleInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Кнопка отмены
        if (displayName.contains("Отмена")) {
            player.closeInventory();
            return;
        }
        
        // Выбор опции
        if (displayName.startsWith("§f§l")) {
            String selectedOption = displayName.substring(4); // Убираем "§f§l"
            
            player.closeInventory();
            
            // Вызываем callback
            onSelect.accept(selectedOption);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        handleInventoryClick(event);
    }
}