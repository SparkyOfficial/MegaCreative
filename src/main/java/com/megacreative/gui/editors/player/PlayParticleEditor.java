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

public class PlayParticleEditor extends AbstractParameterEditor {
    
    public PlayParticleEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Play Particle Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Get the current particle parameter value from the CodeBlock
        DataValue particleValue = codeBlock.getParameter("particle");
        String currentParticle = (particleValue != null) ? particleValue.asString() : "Не задано";
        
        // Get the current count parameter value from the CodeBlock
        DataValue countValue = codeBlock.getParameter("count");
        String currentCount = (countValue != null) ? countValue.asString() : "Не задано";
        
        // Get the current offset parameter value from the CodeBlock
        DataValue offsetValue = codeBlock.getParameter("offset");
        String currentOffset = (offsetValue != null) ? offsetValue.asString() : "Не задано";

        // Create items to represent the parameters
        ItemStack particleItem = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta particleMeta = particleItem.getItemMeta();
        if (particleMeta != null) {
            particleMeta.setDisplayName("§aЧастица");
            particleMeta.setLore(Arrays.asList(
                "§7Текущая: §f" + currentParticle, 
                "§eНажмите, чтобы изменить."
            ));
            particleItem.setItemMeta(particleMeta);
        }
        
        ItemStack countItem = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta countMeta = countItem.getItemMeta();
        if (countMeta != null) {
            countMeta.setDisplayName("§aКоличество");
            countMeta.setLore(Arrays.asList(
                "§7Текущее: §f" + currentCount, 
                "§eНажмите, чтобы изменить."
            ));
            countItem.setItemMeta(countMeta);
        }
        
        ItemStack offsetItem = new ItemStack(Material.COMPASS);
        ItemMeta offsetMeta = offsetItem.getItemMeta();
        if (offsetMeta != null) {
            offsetMeta.setDisplayName("§aРазброс");
            offsetMeta.setLore(Arrays.asList(
                "§7Текущий: §f" + currentOffset, 
                "§eНажмите, чтобы изменить."
            ));
            offsetItem.setItemMeta(offsetMeta);
        }

        // Place the items in the inventory
        inventory.setItem(0, particleItem);
        inventory.setItem(1, countItem);
        inventory.setItem(2, offsetItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Particle slot
                openAnvilInputGUI("particle", "Enter particle type");
                break;
                
            case 1: // Count slot
                openAnvilInputGUI("count", "Enter particle count");
                break;
                
            case 2: // Offset slot
                openAnvilInputGUI("offset", "Enter particle offset");
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aPlay Particle parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI(String parameter, String title) {
        // Get the current parameter value
        DataValue paramValue = codeBlock.getParameter(parameter);
        String currentParam = (paramValue != null) ? paramValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newParam) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter(parameter, newParam);
            player.sendMessage("§a" + parameter + " saved!");
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
        new com.megacreative.gui.AnvilInputGUI(plugin, player, title, callback, cancelCallback);
    }
}