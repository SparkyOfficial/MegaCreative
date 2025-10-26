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
 * Action to set the time in a world
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "setTime", displayName = "§bSet Time", type = BlockType.ACTION)
public class SetTimeAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameter
            DataValue timeValue = block.getParameter("time");
            
            if (timeValue == null) {
                return ExecutionResult.error("Missing required parameter: time");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTime = resolver.resolve(context, timeValue);
            
            long time = resolvedTime.asNumber().longValue();
            
            // Set time in player's world
            player.getWorld().setTime(time);
            
            return ExecutionResult.success("Set time to " + time);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set time: " + e.getMessage());
        }
    }
}