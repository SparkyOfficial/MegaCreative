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

public class WorldTimeEditor extends AbstractParameterEditor {
    
    public WorldTimeEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "World Time Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Time comparison type slot (DAY/NIGHT)
        ItemStack timeTypeItem = new ItemStack(Material.CLOCK);
        ItemMeta timeTypeMeta = timeTypeItem.getItemMeta();
        timeTypeMeta.setDisplayName("§eТип времени");
        timeTypeMeta.setLore(java.util.Arrays.asList(
            "§7Выберите тип времени для проверки",
            "§aТекущее значение: §f" + codeBlock.getParameter("timeType", DataValue.of("DAY")).asString()
        ));
        timeTypeItem.setItemMeta(timeTypeMeta);
        inventory.setItem(0, timeTypeItem);
        
        // Comparison operator slot
        ItemStack operatorItem = new ItemStack(Material.COMPARATOR);
        ItemMeta operatorMeta = operatorItem.getItemMeta();
        operatorMeta.setDisplayName("§eОператор сравнения");
        operatorMeta.setLore(java.util.Arrays.asList(
            "§7Выберите оператор сравнения",
            "§aТекущее значение: §f" + codeBlock.getParameter("operator", DataValue.of("==")).asString()
        ));
        operatorItem.setItemMeta(operatorMeta);
        inventory.setItem(1, operatorItem);
        
        // Time value slot
        ItemStack timeValueItem = new ItemStack(Material.SUNFLOWER);
        ItemMeta timeValueMeta = timeValueItem.getItemMeta();
        timeValueMeta.setDisplayName("§eЗначение времени");
        timeValueMeta.setLore(java.util.Arrays.asList(
            "§7Введите значение времени (0-24000)",
            "§aТекущее значение: §f" + codeBlock.getParameter("timeValue", DataValue.of("6000")).asString()
        ));
        timeValueItem.setItemMeta(timeValueMeta);
        inventory.setItem(2, timeValueItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7условие проверки времени в мире",
            "",
            "§eКак использовать:",
            "§71. Установите тип времени",
            "§72. Выберите оператор сравнения",
            "§73. Укажите значение времени"
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
            case 0: // Time type slot
                // Toggle between DAY and NIGHT
                String currentTimeType = codeBlock.getParameter("timeType", DataValue.of("DAY")).asString();
                String newTimeType = "DAY".equals(currentTimeType) ? "NIGHT" : "DAY";
                codeBlock.setParameter("timeType", DataValue.of(newTimeType));
                player.sendMessage("§aТип времени установлен на: §f" + newTimeType);
                populateItems(); // Refresh the inventory
                break;
                
            case 1: // Operator slot
                // Cycle through comparison operators
                String currentOperator = codeBlock.getParameter("operator", DataValue.of("==")).asString();
                String newOperator = getNextOperator(currentOperator);
                codeBlock.setParameter("operator", DataValue.of(newOperator));
                player.sendMessage("§aОператор сравнения установлен на: §f" + newOperator);
                populateItems(); // Refresh the inventory
                break;
                
            case 2: // Time value slot
                // Open anvil GUI for time value input
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите значение времени", 
                    newValue -> {
                        try {
                            int timeValue = Integer.parseInt(newValue);
                            if (timeValue >= 0 && timeValue <= 24000) {
                                codeBlock.setParameter("timeValue", DataValue.of(timeValue));
                                player.sendMessage("§aЗначение времени установлено на: §f" + timeValue);
                            } else {
                                player.sendMessage("§cЗначение должно быть между 0 и 24000!");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage("§cВведите корректное числовое значение!");
                        }
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // onCancel
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки условия проверки времени в мире.");
                break;
        }
    }
    
    /**
     * Gets the next operator in the cycle
     */
    private String getNextOperator(String currentOperator) {
        switch (currentOperator) {
            case "==": return "!=";
            case "!=": return "<";
            case "<": return ">";
            case ">": return "<=";
            case "<=": return ">=";
            case ">=": return "==";
            default: return "==";
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