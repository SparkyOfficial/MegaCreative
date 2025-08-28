package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

public class IsInWorldCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();
        
        if (player == null || block == null || variableManager == null) return false;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
        DataValue rawWorldName = block.getParameter("world");
        if (rawWorldName == null) return false;
        
        DataValue worldNameValue = resolver.resolve(context, rawWorldName);
        String worldName = worldNameValue.asString();
        
        return worldName != null && player.getWorld().getName().equals(worldName);
    }
} 