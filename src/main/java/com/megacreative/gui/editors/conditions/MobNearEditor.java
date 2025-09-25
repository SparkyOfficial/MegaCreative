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

public class MobNearEditor extends AbstractParameterEditor {
    
    public MobNearEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Mob Near Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Mob type slot
        ItemStack mobItem = new ItemStack(Material.ZOMBIE_SPAWN_EGG);
        ItemMeta mobMeta = mobItem.getItemMeta();
        mobMeta.setDisplayName("§eТип существа");
        DataValue mobType = codeBlock.getParameter("mobType", DataValue.of("ZOMBIE"));
        mobMeta.setLore(java.util.Arrays.asList(
            "§7Выберите тип существа для проверки",
            "§aТекущее значение: §f" + (mobType != null ? mobType.asString() : "ZOMBIE")
        ));
        mobItem.setItemMeta(mobMeta);
        inventory.setItem(0, mobItem);
        
        // Radius slot
        ItemStack radiusItem = new ItemStack(Material.COMPASS);
        ItemMeta radiusMeta = radiusItem.getItemMeta();
        radiusMeta.setDisplayName("§eРадиус поиска");
        DataValue radius = codeBlock.getParameter("radius", DataValue.of("5"));
        radiusMeta.setLore(java.util.Arrays.asList(
            "§7Введите радиус поиска существа",
            "§aТекущее значение: §f" + (radius != null ? radius.asString() : "5")
        ));
        radiusItem.setItemMeta(radiusMeta);
        inventory.setItem(1, radiusItem);
        
        // Count slot
        ItemStack countItem = new ItemStack(Material.EGG);
        ItemMeta countMeta = countItem.getItemMeta();
        countMeta.setDisplayName("§eМинимальное количество");
        DataValue count = codeBlock.getParameter("count", DataValue.of("1"));
        countMeta.setLore(java.util.Arrays.asList(
            "§7Введите минимальное количество существ",
            "§aТекущее значение: §f" + (count != null ? count.asString() : "1")
        ));
        countItem.setItemMeta(countMeta);
        inventory.setItem(2, countItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7условие проверки наличия существ поблизости",
            "",
            "§eКак использовать:",
            "§71. Выберите тип существа",
            "§72. Установите радиус поиска",
            "§73. Укажите минимальное количество"
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
            case 0: // Mob type slot
                // Cycle through common mob types
                String currentMobType = codeBlock.getParameter("mobType", DataValue.of("ZOMBIE")).asString();
                String newMobType = getNextMobType(currentMobType);
                codeBlock.setParameter("mobType", DataValue.of(newMobType));
                player.sendMessage("§aТип существа установлен на: §f" + newMobType);
                populateItems(); // Refresh the inventory
                break;
                
            case 1: // Radius slot
                // Open anvil GUI for radius input
                DataValue currentRadius = codeBlock.getParameter("radius", DataValue.of("5"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите радиус поиска", 
                    newValue -> {
                        try {
                            int radius = Integer.parseInt(newValue);
                            if (radius > 0 && radius <= 100) {
                                codeBlock.setParameter("radius", DataValue.of(radius));
                                player.sendMessage("§aРадиус поиска установлен на: §f" + radius);
                                populateItems(); // Refresh the inventory
                            } else {
                                player.sendMessage("§cРадиус должен быть между 1 и 100!");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage("§cВведите корректное числовое значение!");
                        }
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 2: // Count slot
                // Open anvil GUI for count input
                DataValue currentCount = codeBlock.getParameter("count", DataValue.of("1"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите минимальное количество", 
                    newValue -> {
                        try {
                            int count = Integer.parseInt(newValue);
                            if (count >= 0 && count <= 100) {
                                codeBlock.setParameter("count", DataValue.of(count));
                                player.sendMessage("§aМинимальное количество установлено на: §f" + count);
                                populateItems(); // Refresh the inventory
                            } else {
                                player.sendMessage("§cКоличество должно быть между 0 и 100!");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage("§cВведите корректное числовое значение!");
                        }
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки условия проверки наличия существ поблизости.");
                break;
        }
    }
    
    /**
     * Gets the next mob type in the cycle
     */
    private String getNextMobType(String currentMobType) {
        switch (currentMobType) {
            case "ZOMBIE": return "SKELETON";
            case "SKELETON": return "CREEPER";
            case "CREEPER": return "SPIDER";
            case "SPIDER": return "ENDERMAN";
            case "ENDERMAN": return "PIG";
            case "PIG": return "COW";
            case "COW": return "SHEEP";
            case "SHEEP": return "ZOMBIE";
            default: return "ZOMBIE";
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