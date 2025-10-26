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
 * Action to perform an economy transaction (requires Vault)
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "economyTransaction", displayName = "§bEconomy Transaction", type = BlockType.ACTION)
public class EconomyTransactionAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue operationValue = block.getParameter("operation");
            DataValue amountValue = block.getParameter("amount");
            DataValue targetValue = block.getParameter("target");
            
            if (operationValue == null || amountValue == null) {
                return ExecutionResult.error("Missing required parameters: operation, amount");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedOperation = resolver.resolve(context, operationValue);
            DataValue resolvedAmount = resolver.resolve(context, amountValue);
            
            String operation = resolvedOperation.asString();
            double amount = resolvedAmount.asNumber().doubleValue();
            
            // Get target player (default to current player)
            Player targetPlayer = player;
            if (targetValue != null) {
                DataValue resolvedTarget = resolver.resolve(context, targetValue);
                String targetPlayerName = resolvedTarget.asString();
                targetPlayer = context.getPlugin().getServer().getPlayer(targetPlayerName);
                
                if (targetPlayer == null) {
                    return ExecutionResult.error("Player not found: " + targetPlayerName);
                }
            }
            
            // Perform economy transaction (placeholder implementation)
            // In a real implementation, this would integrate with Vault or another economy plugin
            switch (operation.toLowerCase()) {
                case "give":
                    // Add money to player's balance
                    return ExecutionResult.success("Gave " + amount + " to " + targetPlayer.getName());
                case "take":
                    // Remove money from player's balance
                    return ExecutionResult.success("Took " + amount + " from " + targetPlayer.getName());
                case "check":
                    // Check player's balance
                    return ExecutionResult.success("Balance of " + targetPlayer.getName() + " is [balance]");
                default:
                    return ExecutionResult.error("Invalid operation: " + operation);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to perform economy transaction: " + e.getMessage());
        }
    }
}