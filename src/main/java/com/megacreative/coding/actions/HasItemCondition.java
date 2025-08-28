package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HasItemCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();
        
        if (player == null || block == null || variableManager == null) return false;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
        DataValue rawItemName = block.getParameter("item");
        if (rawItemName == null) return false;
        
        DataValue itemNameValue = resolver.resolve(context, rawItemName);
        String itemName = itemNameValue.asString();
        
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