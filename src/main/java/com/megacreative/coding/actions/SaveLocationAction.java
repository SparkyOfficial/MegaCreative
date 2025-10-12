package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

/**
 * Action for saving a player's location.
 * This action retrieves parameters from the new parameter system and saves a player's location.
 */
@BlockMeta(id = "saveLocation", displayName = "§aSave Location", type = BlockType.ACTION)
public class SaveLocationAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue locationNameValue = block.getParameter("locationName");

            if (locationNameValue == null || locationNameValue.isEmpty()) {
                return ExecutionResult.error("Location name parameter is missing");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedLocationName = resolver.resolve(context, locationNameValue);
            
            
            String locName = resolvedLocationName.asString();
            
            if (locName == null || locName.isEmpty()) {
                return ExecutionResult.error("Invalid location name");
            }

            
            com.megacreative.coding.variables.VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            org.bukkit.Location location = player.getLocation();
            
            
            String locationStr = location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getWorld().getName();
            
            
            variableManager.setPlayerVariable(player.getUniqueId(), locName, DataValue.of(locationStr));
            
            return ExecutionResult.success("Location '" + locName + "' saved successfully at " + locationStr);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to save location: " + e.getMessage());
        }
    }
}