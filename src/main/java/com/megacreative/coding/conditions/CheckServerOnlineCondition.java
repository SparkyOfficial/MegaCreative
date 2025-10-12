package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;

@BlockMeta(id = "checkServerOnline", displayName = "§aCheck Server Online", type = BlockType.CONDITION)
public class CheckServerOnlineCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue checkTypeValue = block.getParameter("check_type");
            
            String checkType = "online"; 
            if (checkTypeValue != null && !checkTypeValue.isEmpty()) {
                
                ParameterResolver resolver = new ParameterResolver(context);
                DataValue resolvedCheckType = resolver.resolve(context, checkTypeValue);
                checkType = resolvedCheckType.asString();
            }

            int playerCount = Bukkit.getOnlinePlayers().size();
            
            switch (checkType.toLowerCase()) {
                case "online":
                    return playerCount > 0;
                case "empty":
                    return playerCount == 0;
                case "full":
                    return Bukkit.getMaxPlayers() <= playerCount;
                default:
                    
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