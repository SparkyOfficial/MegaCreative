package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;

public class CheckServerOnlineCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue checkTypeValue = block.getParameter("check_type", DataValue.of("online"));
            String checkType = checkTypeValue.asString().toLowerCase();

            int playerCount = Bukkit.getOnlinePlayers().size();
            
            switch (checkType) {
                case "online":
                    return playerCount > 0;
                case "empty":
                    return playerCount == 0;
                case "full":
                    return Bukkit.getMaxPlayers() <= playerCount;
                default:
                    // Check if player count is greater than or equal to a specific number
                    try {
                        int requiredPlayers = Integer.parseInt(checkType);
                        return playerCount >= requiredPlayers;
                    } catch (NumberFormatException e) {
                        context.getPlugin().getLogger().warning("CheckServerOnlineCondition: Invalid check_type '" + checkType + "'.");
                        return false;
                    }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error evaluating CheckServerOnlineCondition: " + e.getMessage());
            return false;
        }
    }
}