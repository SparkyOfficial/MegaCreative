package com.megacreative.gui.editors.conditions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerGameModeEditor extends AbstractParameterEditor {
    
    public PlayerGameModeEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Player Game Mode Editor");
    }
    
    @Override
    public void populateItems() {
        // Implementation for populating player game mode items
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        // Implementation for handling inventory clicks
        event.setCancelled(true);
    }
}