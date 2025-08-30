package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.BooleanValue;
import org.bukkit.entity.Player;

/**
 * Enhanced IsOp condition with DataValue support
 * Provides type-safe boolean evaluation and parameter resolution
 */
public class IsOpCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        // Create ParameterResolver with ExecutionContext
        ParameterResolver resolver = new ParameterResolver(context);
        
        // Get the 'required' parameter (optional, defaults to true)
        DataValue requiredValue = block.getParameter("required");
        boolean expectedOpStatus = true; // Default: expect player to be OP
        
        if (requiredValue != null && !requiredValue.isEmpty()) {
            // Resolve any variables or placeholders
            DataValue resolved = resolver.resolve(context, requiredValue);
            
            // Use DataValue's type-safe boolean conversion
            expectedOpStatus = resolved.asBoolean();
        }
        
        // Check if player's OP status matches expected status
        boolean playerIsOp = player.isOp();
        
        // Debug message for admins
        if (player.isOp() && context.getPlugin().getLogger().isLoggable(java.util.logging.Level.FINE)) {
            context.getPlugin().getLogger().fine(
                "IsOp condition: Player " + player.getName() + 
                " is OP: " + playerIsOp + 
                ", Expected: " + expectedOpStatus + 
                ", Result: " + (playerIsOp == expectedOpStatus)
            );
        }
        
        return playerIsOp == expectedOpStatus;
    }
} 