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

public class SpawnEntityEditor extends AbstractParameterEditor {
    
    public SpawnEntityEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Spawn Entity Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Get the current entity parameter value from the CodeBlock
        DataValue entityValue = codeBlock.getParameter("entity");
        String currentEntity = (entityValue != null) ? entityValue.asString() : "Не задано";

        // Create an item to represent the entity parameter
        ItemStack entityItem = new ItemStack(Material.ZOMBIE_SPAWN_EGG);
        ItemMeta meta = entityItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aСущество");
            meta.setLore(Arrays.asList(
                "§7Текущее: §f" + currentEntity, 
                "§eНажмите, чтобы изменить."
            ));
            entityItem.setItemMeta(meta);
        }

        // Place the item in the inventory
        inventory.setItem(0, entityItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Entity slot
                openAnvilInputGUI();
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aSpawn Entity parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI() {
        // Get the current entity value
        DataValue entityValue = codeBlock.getParameter("entity");
        String currentEntity = (entityValue != null) ? entityValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newEntity) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("entity", newEntity);
            player.sendMessage("§aEntity saved!");
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
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Enter entity type", callback, cancelCallback);
    }
}