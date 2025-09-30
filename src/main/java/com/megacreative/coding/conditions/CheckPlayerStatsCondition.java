package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

@BlockMeta(id = "checkPlayerStats", displayName = "§aCheck Player Stats", type = BlockType.CONDITION)
public class CheckPlayerStatsCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        try {
            // Get parameters from the new parameter system
            DataValue statTypeValue = block.getParameter("stat_type");
            DataValue valueValue = block.getParameter("value");
            DataValue operatorValue = block.getParameter("operator");
            
            if (statTypeValue == null || statTypeValue.isEmpty()) {
                context.getPlugin().getLogger().warning("CheckPlayerStatsCondition: 'stat_type' parameter is missing.");
                return false;
            }
            
            if (valueValue == null || valueValue.isEmpty()) {
                context.getPlugin().getLogger().warning("CheckPlayerStatsCondition: 'value' parameter is missing.");
                return false;
            }
            
            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedStatType = resolver.resolve(context, statTypeValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            DataValue resolvedOperator = operatorValue != null ? resolver.resolve(context, operatorValue) : null;
            
            String statType = resolvedStatType.asString().toUpperCase();
            double checkValue = resolvedValue.asNumber().doubleValue();
            String operator = resolvedOperator != null ? resolvedOperator.asString() : "==";
            
            // Get the player's statistic value
            double playerStatValue = 0;
            try {
                Statistic statistic = Statistic.valueOf(statType);
                playerStatValue = player.getStatistic(statistic);
            } catch (IllegalArgumentException e) {
                context.getPlugin().getLogger().warning("CheckPlayerStatsCondition: Invalid statistic type '" + statType + "'.");
                return false;
            }
            
            // Compare values based on operator
            switch (operator) {
                case ">=":
                    return playerStatValue >= checkValue;
                case "<=":
                    return playerStatValue <= checkValue;
                case ">":
                    return playerStatValue > checkValue;
                case "<":
                    return playerStatValue < checkValue;
                case "==":
                case "=":
                    return playerStatValue == checkValue;
                case "!=":
                    return playerStatValue != checkValue;
                default:
                    context.getPlugin().getLogger().warning("CheckPlayerStatsCondition: Invalid operator '" + operator + "'.");
                    return false;
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error evaluating CheckPlayerStatsCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold stats parameters
     */
    private static class CheckPlayerStatsParams {
        String statType = "";
        String valueStr = "";
        double value = 0.0;
        String operator = "==";
    }
}