package com.megacreative.gui.editors.player;

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

public class EffectEditor extends AbstractParameterEditor {
    
    public EffectEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Effect Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Effect name slot
        ItemStack effectItem = new ItemStack(Material.POTION);
        ItemMeta effectMeta = effectItem.getItemMeta();
        effectMeta.setDisplayName("§eНазвание эффекта");
        DataValue effectName = codeBlock.getParameter("effect", DataValue.of("SPEED"));
        effectMeta.setLore(java.util.Arrays.asList(
            "§7Введите название эффекта",
            "§aТекущее значение: §f" + (effectName != null ? effectName.asString() : "SPEED")
        ));
        effectItem.setItemMeta(effectMeta);
        inventory.setItem(0, effectItem);
        
        // Duration slot
        ItemStack durationItem = new ItemStack(Material.CLOCK);
        ItemMeta durationMeta = durationItem.getItemMeta();
        durationMeta.setDisplayName("§eДлительность");
        DataValue duration = codeBlock.getParameter("duration", DataValue.of("200"));
        durationMeta.setLore(java.util.Arrays.asList(
            "§7Введите длительность эффекта (в тиках)",
            "§aТекущее значение: §f" + (duration != null ? duration.asString() : "200")
        ));
        durationItem.setItemMeta(durationMeta);
        inventory.setItem(1, durationItem);
        
        // Amplifier slot
        ItemStack amplifierItem = new ItemStack(Material.REDSTONE);
        ItemMeta amplifierMeta = amplifierItem.getItemMeta();
        amplifierMeta.setDisplayName("§eУсиление");
        DataValue amplifier = codeBlock.getParameter("amplifier", DataValue.of("0"));
        amplifierMeta.setLore(java.util.Arrays.asList(
            "§7Введите уровень усиления эффекта",
            "§aТекущее значение: §f" + (amplifier != null ? amplifier.asString() : "0")
        ));
        amplifierItem.setItemMeta(amplifierMeta);
        inventory.setItem(2, amplifierItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7действие применения эффекта игроку",
            "",
            "§eКак использовать:",
            "§71. Укажите название эффекта",
            "§72. Установите длительность",
            "§73. Укажите уровень усиления"
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
            case 0: // Effect name slot
                // Open anvil GUI for effect name input
                DataValue currentEffect = codeBlock.getParameter("effect", DataValue.of("SPEED"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите название эффекта", 
                    newValue -> {
                        codeBlock.setParameter("effect", DataValue.of(newValue));
                        player.sendMessage("§aНазвание эффекта установлено на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Duration slot
                // Open anvil GUI for duration input
                DataValue currentDuration = codeBlock.getParameter("duration", DataValue.of("200"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите длительность", 
                    newValue -> {
                        codeBlock.setParameter("duration", DataValue.of(newValue));
                        player.sendMessage("§aДлительность установлена на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 2: // Amplifier slot
                // Open anvil GUI for amplifier input
                DataValue currentAmplifier = codeBlock.getParameter("amplifier", DataValue.of("0"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите уровень усиления", 
                    newValue -> {
                        codeBlock.setParameter("amplifier", DataValue.of(newValue));
                        player.sendMessage("§aУровень усиления установлен на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки действия применения эффекта игроку.");
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