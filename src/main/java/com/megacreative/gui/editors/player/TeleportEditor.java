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

/**
 * Parameter editor for the Teleport action
 * Allows players to set the coordinates for teleportation
 */
public class TeleportEditor extends AbstractParameterEditor {

    /**
     * Constructor for TeleportEditor
     * @param plugin The main plugin instance
     * @param player The player using the editor
     * @param codeBlock The code block being edited
     */
    public TeleportEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 27, "Настроить телепортацию");
    }

    /**
     * Populates the inventory with items for configuring the teleport coordinates
     */
    @Override
    public void populateItems() {
        // Get the current coordinates parameter value from the CodeBlock
        DataValue coordsValue = codeBlock.getParameter("coords");
        String currentCoords = (coordsValue != null) ? coordsValue.asString() : "Не задано";

        // Create an item to represent the coordinates parameter
        ItemStack coordsItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = coordsItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aКоординаты телепортации");
            meta.setLore(Arrays.asList(
                "§7Текущие: §f" + currentCoords, 
                "§eНажмите, чтобы изменить."
            ));
            coordsItem.setItemMeta(meta);
        }

        // Place the item in the center of the inventory
        inventory.setItem(13, coordsItem);
        
        // Add info item
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§eИнформация");
            infoMeta.setLore(Arrays.asList(
                "§7Формат координат: x y z",
                "§7Пример: 100 64 200",
                "§7Можно использовать переменные:",
                "§7%player_x% %player_y% %player_z%"
            ));
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(4, infoItem);
    }

    /**
     * Handles clicks in the inventory
     * @param event The inventory click event
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        if (slot == 13) {
            // Open an anvil GUI for text input
            openAnvilInputGUI();
        }
    }
    
    /**
     * Opens an anvil GUI for entering the coordinates
     */
    private void openAnvilInputGUI() {
        // Get the current coordinates value
        DataValue coordsValue = codeBlock.getParameter("coords");
        String currentCoords = (coordsValue != null) ? coordsValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newCoords) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("coords", DataValue.fromObject(newCoords));
            player.sendMessage("§aКоординаты телепортации сохранены!");
            player.closeInventory();
        };
        
        // Create a cancel callback
        Runnable cancelCallback = () -> player.closeInventory();
        
        // Create and open the anvil input GUI
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Введите координаты (x y z)", callback, cancelCallback);
    }
}