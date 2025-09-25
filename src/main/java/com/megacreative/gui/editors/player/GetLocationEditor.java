package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GetLocationEditor extends AbstractParameterEditor {
    
    public GetLocationEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Get Location Editor");
    }
    
    @Override
    public void populateItems() {
        // Implementation for populating get location items
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        // Implementation for handling inventory clicks
        event.setCancelled(true);
    }
}