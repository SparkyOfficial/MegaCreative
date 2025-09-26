package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@BlockMeta(id = "hasItem", displayName = "§aHas Item", type = BlockType.CONDITION)
public class HasItemCondition implements BlockCondition {
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        
        if (player == null || block == null) return false;
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawItemName = block.getParameter("item");
        if (rawItemName == null) return false;
        
        String itemName = resolver.resolve(context, rawItemName).asString();
        
        if (itemName != null && !itemName.isEmpty()) {
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