package com.megacreative.gui.editors.actions;

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

public class SetVarEditor extends AbstractParameterEditor {
    
    public SetVarEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Set Variable Editor");
        
        // Set up the inventory with default items
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Variable slot
        ItemStack varStack = new ItemStack(Material.IRON_INGOT);
        ItemMeta varMeta = varStack.getItemMeta();
        varMeta.setDisplayName("§eИмя переменной");
        varMeta.setLore(java.util.Arrays.asList(
            "§7Введите имя переменной",
            "§aТекущее значение: §f" + codeBlock.getParameter("var", DataValue.of("myVar")).asString()
        ));
        varStack.setItemMeta(varMeta);
        inventory.setItem(0, varStack);
        
        // Value slot
        ItemStack valueStack = new ItemStack(Material.GOLD_INGOT);
        ItemMeta valueMeta = valueStack.getItemMeta();
        valueMeta.setDisplayName("§eЗначение переменной");
        valueMeta.setLore(java.util.Arrays.asList(
            "§7Введите значение переменной",
            "§aТекущее значение: §f" + codeBlock.getParameter("value", DataValue.of("0")).asString()
        ));
        valueStack.setItemMeta(valueMeta);
        inventory.setItem(1, valueStack);
        
        // Variable type slot
        ItemStack typeStack = new ItemStack(Material.BOOK);
        ItemMeta typeMeta = typeStack.getItemMeta();
        typeMeta.setDisplayName("§eТип переменной");
        typeMeta.setLore(java.util.Arrays.asList(
            "§7Выберите тип переменной",
            "§aТекущий тип: §f" + codeBlock.getParameter("type", DataValue.of("TEXT")).asString()
        ));
        typeStack.setItemMeta(typeMeta);
        inventory.setItem(2, typeStack);
        
        // Help item
        ItemStack helpStack = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpStack.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7действие установки переменной",
            "",
            "§eКак использовать:",
            "§71. Укажите имя переменной",
            "§72. Установите значение",
            "§73. Выберите тип переменной"
        ));
        helpStack.setItemMeta(helpMeta);
        inventory.setItem(7, helpStack);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        ItemMeta doneMeta = doneStack.getItemMeta();
        doneMeta.setDisplayName("§aГотово");
        doneMeta.setLore(java.util.Arrays.asList(
            "§7Нажмите, чтобы сохранить настройки"
        ));
        doneStack.setItemMeta(doneMeta);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void populateItems() {
        // Refresh the inventory with current values
        setupInventory();
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();
        
        switch (slot) {
            case 0: // Variable slot
                DataValue currentVar = codeBlock.getParameter("var", DataValue.of("myVar"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите имя переменной", 
                    newValue -> {
                        codeBlock.setParameter("var", DataValue.of(newValue));
                        player.sendMessage("§aИмя переменной установлено на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Value slot
                DataValue currentValue = codeBlock.getParameter("value", DataValue.of("0"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите значение", 
                    newValue -> {
                        codeBlock.setParameter("value", DataValue.of(newValue));
                        player.sendMessage("§aЗначение переменной установлено на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 2: // Variable type slot
                // Cycle through variable types
                DataValue currentTypeValue = codeBlock.getParameter("type", DataValue.of("TEXT"));
                String currentType = currentTypeValue.asString();
                String newType = getNextVariableType(currentType);
                codeBlock.setParameter("type", DataValue.of(newType));
                player.sendMessage("§aТип переменной установлен на: §f" + newType);
                populateItems(); // Refresh the inventory
                break;
                
            case 7: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки действия установки переменной.");
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aПараметры действия 'Установить переменную' сохранены!");
                break;
        }
    }
    
    /**
     * Gets the next variable type in the cycle
     */
    private String getNextVariableType(String currentType) {
        switch (currentType) {
            case "TEXT": return "NUMBER";
            case "NUMBER": return "BOOLEAN";
            case "BOOLEAN": return "LOCATION";
            case "LOCATION": return "ITEM";
            case "ITEM": return "TEXT";
            default: return "TEXT";
        }
    }
    
    @Override
    public void open() {
        // Use the default implementation from AbstractParameterEditor
        // This will automatically load items from container and populate the inventory
        super.open();
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