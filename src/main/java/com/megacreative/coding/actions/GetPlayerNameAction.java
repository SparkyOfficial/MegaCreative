package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action to get a player's name and store it in a variable
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "getPlayerName", displayName = "§bGet Player Name", type = BlockType.ACTION)
public class GetPlayerNameAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameter
            DataValue variableValue = block.getParameter("variable");
            
            if (variableValue == null) {
                return ExecutionResult.error("Missing required parameter: variable");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedVariable = resolver.resolve(context, variableValue);
            
            String variableName = resolvedVariable.asString();
            
            // Get player name
            String playerName = player.getName();
            
            // Store in variable
            context.setVariable(variableName, playerName);
            
            return ExecutionResult.success("Stored player name '" + playerName + "' in variable '" + variableName + "'");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get player name: " + e.getMessage());
        }
    }
}