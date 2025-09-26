package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

@BlockMeta(id = "playerHealth", displayName = "§aPlayer Health", type = BlockType.CONDITION)
public class PlayerHealthCondition implements BlockCondition {
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        
        if (player == null || block == null) return false;
        
        VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
        if (variableManager == null) return false;
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawHealth = block.getParameter("health");
        DataValue rawOperator = block.getParameter("operator");
        
        if (rawHealth == null || rawOperator == null) return false;
        
        String healthStr = resolver.resolve(context, rawHealth).asString();
        String operatorStr = resolver.resolve(context, rawOperator).asString();
        
        if (healthStr == null || operatorStr == null) return false;
        
        try {
            double requiredHealth = Double.parseDouble(healthStr);
            double currentHealth = player.getHealth();
            
            switch (operatorStr) {
                case ">":
                    return currentHealth > requiredHealth;
                case ">=":
                    return currentHealth >= requiredHealth;
                case "<":
                    return currentHealth < requiredHealth;
                case "<=":
                    return currentHealth <= requiredHealth;
                case "==":
                case "=":
                    return currentHealth == requiredHealth;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
}