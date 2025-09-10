package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class CheckPlayerStatsCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        try {
            // Get parameters from the block
            DataValue statTypeValue = block.getParameter("stat_type");
            DataValue valueValue = block.getParameter("value");
            DataValue operatorValue = block.getParameter("operator", DataValue.of("=="));
            
            if (statTypeValue == null || statTypeValue.isEmpty()) {
                context.getPlugin().getLogger().warning("CheckPlayerStatsCondition: 'stat_type' parameter is missing.");
                return false;
            }
            
            if (valueValue == null || valueValue.isEmpty()) {
                context.getPlugin().getLogger().warning("CheckPlayerStatsCondition: 'value' parameter is missing.");
                return false;
            }
            
            String statType = statTypeValue.asString().toUpperCase();
            double checkValue = valueValue.asNumber().doubleValue();
            String operator = operatorValue.asString();
            
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
}