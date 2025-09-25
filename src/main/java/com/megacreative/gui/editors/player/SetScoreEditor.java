package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SetScoreEditor extends AbstractParameterEditor {
    
    public SetScoreEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Set Score Editor");
    }
    
    @Override
    public void populateItems() {
        // Implementation for populating set score items
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        // Implementation for handling inventory clicks
        event.setCancelled(true);
    }
}