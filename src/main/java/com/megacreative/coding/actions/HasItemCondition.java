package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HasItemCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        Object rawItemName = block.getParameter("item");
        String itemName = ParameterResolver.resolve(context, rawItemName);
        
        if (itemName != null) {
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                return player.getInventory().contains(material);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }
} 