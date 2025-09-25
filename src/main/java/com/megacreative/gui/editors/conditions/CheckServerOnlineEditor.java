package com.megacreative.gui.editors.conditions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CheckServerOnlineEditor extends AbstractParameterEditor {
    
    public CheckServerOnlineEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Check Server Online Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Server name slot
        ItemStack serverItem = new ItemStack(Material.COMPASS);
        ItemMeta serverMeta = serverItem.getItemMeta();
        serverMeta.setDisplayName("§eИмя сервера");
        DataValue serverName = codeBlock.getParameter("server", DataValue.of("main"));
        serverMeta.setLore(java.util.Arrays.asList(
            "§7Введите имя сервера для проверки",
            "§aТекущее значение: §f" + (serverName != null ? serverName.asString() : "main")
        ));
        serverItem.setItemMeta(serverMeta);
        inventory.setItem(0, serverItem);
        
        // Timeout slot
        ItemStack timeoutItem = new ItemStack(Material.CLOCK);
        ItemMeta timeoutMeta = timeoutItem.getItemMeta();
        timeoutMeta.setDisplayName("§eТаймаут (мс)");
        DataValue timeout = codeBlock.getParameter("timeout", DataValue.of(5000));
        timeoutMeta.setLore(java.util.Arrays.asList(
            "§7Введите таймаут проверки в миллисекундах",
            "§aТекущее значение: §f" + (timeout != null ? timeout.asNumber() : "5000")
        ));
        timeoutItem.setItemMeta(timeoutMeta);
        inventory.setItem(1, timeoutItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7проверку доступности сервера",
            "",
            "§eКак использовать:",
            "§71. Укажите имя сервера для проверки",
            "§72. Установите таймаут проверки"
        ));
        helpItem.setItemMeta(helpMeta);
        inventory.setItem(8, helpItem);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();
        
        switch (slot) {
            case 0: // Server name slot
                // Open anvil GUI for server name input
                DataValue currentServer = codeBlock.getParameter("server", DataValue.of("main"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите имя сервера", 
                    newValue -> {
                        codeBlock.setParameter("server", DataValue.of(newValue));
                        player.sendMessage("§aИмя сервера установлено на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Timeout slot
                // Open anvil GUI for timeout input
                DataValue currentTimeout = codeBlock.getParameter("timeout", DataValue.of(5000));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите таймаут (мс)", 
                    newValue -> {
                        try {
                            int timeout = Integer.parseInt(newValue);
                            codeBlock.setParameter("timeout", DataValue.of(timeout));
                            player.sendMessage("§aТаймаут установлен на: §f" + timeout + " мс");
                            populateItems(); // Refresh the inventory
                        } catch (NumberFormatException e) {
                            player.sendMessage("§cНеверный формат числа: " + newValue);
                        }
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки проверки доступности сервера.");
                break;
        }
    }
    
    @Override
    protected void onLoadContainerItems(org.bukkit.inventory.Inventory containerInventory) {
        // Load items from container to editor inventory
        for (int i = 0; i < Math.min(containerInventory.getSize(), inventory.getSize()); i++) {
            ItemStack item = containerInventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(i, item.clone());
            }
        }
    }
    
    @Override
    protected void onSaveContainerItems(org.bukkit.inventory.Inventory containerInventory) {
        // Save items from editor inventory to container
        for (int i = 0; i < Math.min(containerInventory.getSize(), inventory.getSize()); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                containerInventory.setItem(i, item.clone());
            } else {
                containerInventory.clear(i);
            }
        }
    }
}