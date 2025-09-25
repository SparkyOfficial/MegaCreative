package com.megacreative.gui.editors.conditions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class IsRidingEditor extends AbstractParameterEditor {
    
    public IsRidingEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Is Riding Editor");
    }
    
    @Override
    public void populateItems() {
        // Implementation for populating is riding items
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        // Implementation for handling inventory clicks
        event.setCancelled(true);
    }
}