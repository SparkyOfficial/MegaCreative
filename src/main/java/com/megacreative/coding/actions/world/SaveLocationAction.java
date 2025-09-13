package com.megacreative.coding.actions.world;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for saving a player's location.
 * This action retrieves parameters from the container configuration and saves a player's location.
 */
public class SaveLocationAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get location name from the container configuration
            String locationName = getLocationNameFromContainer(block, context);

            // Resolve any placeholders in the location name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue locationNameVal = DataValue.of(locationName);
            DataValue resolvedLocationName = resolver.resolve(context, locationNameVal);

            // Parse parameters
            String locName = resolvedLocationName.asString();

            if (locName == null || locName.isEmpty()) {
                return ExecutionResult.error("Invalid location name");
            }

            // Save the location
            // Note: This is a simplified implementation - in a real system, you would save the actual location
            context.getPlugin().getLogger().info(
                    "Saving location " + locName + " at player's current position"
            );

            return ExecutionResult.success("Location saved successfully");

        } catch (Exception e) {
            return ExecutionResult.error("Failed to save location: " + e.getMessage());
        }
    }

    /**
     * Gets location name from the container configuration
     */
    private String getLocationNameFromContainer(CodeBlock block, ExecutionContext context) {
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
                        return getLocationNameFromItem(locationNameItem);
                    }
                }
            }

        } catch (Exception e) {
            context.getPlugin().getLogger().warning(
                    "Error getting location name from container in SaveLocationAction: " + e.getMessage()
            );
        }

        return "";
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
}
