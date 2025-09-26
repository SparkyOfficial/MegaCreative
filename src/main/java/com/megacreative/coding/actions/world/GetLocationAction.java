package com.megacreative.coding.actions.world;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for getting a saved location.
 * This action retrieves parameters from the container configuration and gets a saved location.
 */
@BlockMeta(id = "getLocation", displayName = "§aGet Location", type = BlockType.ACTION)
public class GetLocationAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get parameters from the container configuration
            GetLocationParams params = getLocationParamsFromContainer(block, context);

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue locationNameVal = DataValue.of(params.locationNameStr);
            DataValue resolvedLocationName = resolver.resolve(context, locationNameVal);

            DataValue targetVariableVal = DataValue.of(params.targetVariableStr);
            DataValue resolvedTargetVariable = resolver.resolve(context, targetVariableVal);

            // Parse parameters
            String locationName = resolvedLocationName.asString();
            String targetVariable = resolvedTargetVariable.asString();

            if (locationName == null || locationName.isEmpty()) {
                return ExecutionResult.error("Invalid location name");
            }
            if (targetVariable == null || targetVariable.isEmpty()) {
                return ExecutionResult.error("Invalid target variable");
            }

            // Get the location using the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            DataValue locationValue = variableManager.getPlayerVariable(player.getUniqueId(), locationName);
            
            if (locationValue == null) {
                return ExecutionResult.error("Location not found: " + locationName);
            }
            
            // Store the location in the target variable
            variableManager.setPlayerVariable(player.getUniqueId(), targetVariable, locationValue);

            return ExecutionResult.success("Location retrieved successfully");

        } catch (Exception e) {
            return ExecutionResult.error("Failed to get location: " + e.getMessage());
        }
    }

    /**
     * Gets location parameters from the container configuration
     */
    private GetLocationParams getLocationParamsFromContainer(CodeBlock block, ExecutionContext context) {
        GetLocationParams params = new GetLocationParams();

        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();

            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());

            if (slotResolver != null) {
                // Get location name from the locationName slot
                Integer locationNameSlot = slotResolver.apply("locationName");
                if (locationNameSlot != null) {
                    ItemStack locationNameItem = block.getConfigItem(locationNameSlot);
                    if (locationNameItem != null && locationNameItem.hasItemMeta()) {
                        params.locationNameStr = getLocationNameFromItem(locationNameItem);
                    }
                }

                // Get target variable from the targetVariable slot
                Integer targetVariableSlot = slotResolver.apply("targetVariable");
                if (targetVariableSlot != null) {
                    ItemStack targetVariableItem = block.getConfigItem(targetVariableSlot);
                    if (targetVariableItem != null && targetVariableItem.hasItemMeta()) {
                        params.targetVariableStr = getTargetVariableFromItem(targetVariableItem);
                    }
                }
            }

        } catch (Exception e) {
            context.getPlugin().getLogger().warning(
                    "Error getting location parameters from container in GetLocationAction: " + e.getMessage()
            );
        }

        return params;
    }

    /**
     * Extracts location name from an item
     */
    private String getLocationNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the location name
                return displayName.replaceAll("§[0-9a-fk-or]", "").trim();
            }
        }
        return "";
    }

    /**
     * Extracts target variable from an item
     */
    private String getTargetVariableFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the target variable
                return displayName.replaceAll("§[0-9a-fk-or]", "").trim();
            }
        }
        return "";
    }

    /**
     * Helper class to hold location parameters
     */
    private static class GetLocationParams {
        String locationNameStr = "";
        String targetVariableStr = "";
    }
}
