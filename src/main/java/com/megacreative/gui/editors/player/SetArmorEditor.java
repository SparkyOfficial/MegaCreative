package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.function.Consumer;

public class SetArmorEditor extends AbstractParameterEditor {
    
    public SetArmorEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Set Armor Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Get the current armor parameter value from the CodeBlock
        DataValue armorValue = codeBlock.getParameter("armor");
        String currentArmor = (armorValue != null) ? armorValue.asString() : "Не задано";

        // Create an item to represent the armor parameter
        ItemStack armorItem = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta meta = armorItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aБроня");
            meta.setLore(Arrays.asList(
                "§7Текущая: §f" + currentArmor, 
                "§eНажмите, чтобы изменить."
            ));
            armorItem.setItemMeta(meta);
        }

        // Place the item in the inventory
        inventory.setItem(0, armorItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Armor slot
                openAnvilInputGUI();
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aSet Armor parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI() {
        // Get the current armor value
        DataValue armorValue = codeBlock.getParameter("armor");
        String currentArmor = (armorValue != null) ? armorValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newArmor) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("armor", newArmor);
            player.sendMessage("§aArmor saved!");
            // Reopen the editor to show the updated value
            populateItems();
        };
        
        // Create a cancel callback
        Runnable cancelCallback = () -> {
            // Reopen the editor
            populateItems();
            player.openInventory(inventory);
        };
        
        // Create and open the anvil input GUI
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Enter armor items (helmet:DIAMOND_HELMET,chestplate:DIAMOND_CHESTPLATE,etc.)", callback, cancelCallback);
    }
}