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

public class SetExperienceEditor extends AbstractParameterEditor {
    
    public SetExperienceEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Set Experience Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Get the current experience parameter value from the CodeBlock
        DataValue experienceValue = codeBlock.getParameter("experience");
        String currentExperience = (experienceValue != null) ? experienceValue.asString() : "Не задано";

        // Create an item to represent the experience parameter
        ItemStack experienceItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = experienceItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aОчки опыта");
            meta.setLore(Arrays.asList(
                "§7Текущие: §f" + currentExperience, 
                "§eНажмите, чтобы изменить."
            ));
            experienceItem.setItemMeta(meta);
        }

        // Place the item in the inventory
        inventory.setItem(0, experienceItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Experience slot
                openAnvilInputGUI();
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aSet Experience parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI() {
        // Get the current experience value
        DataValue experienceValue = codeBlock.getParameter("experience");
        String currentExperience = (experienceValue != null) ? experienceValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newExperience) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("experience", newExperience);
            player.sendMessage("§aExperience saved!");
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
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Enter experience amount", callback, cancelCallback);
    }
}