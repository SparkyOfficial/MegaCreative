package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.Location;

/**
 * Action for playing a visual effect at a location.
 * This action retrieves effect parameters from the block and plays the effect.
 */
public class EffectAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the effect parameter from the block
            DataValue effectValue = block.getParameter("effect");
            if (effectValue == null) {
                return ExecutionResult.error("Effect parameter is missing");
            }

            // Resolve any placeholders in the effect name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedEffect = resolver.resolve(context, effectValue);
            
            // Parse effect parameters
            String effectName = resolvedEffect.asString();
            if (effectName == null || effectName.isEmpty()) {
                return ExecutionResult.error("Effect name is empty or null");
            }

            // Get the location where the effect should be played
            Location location = player.getLocation();
            
            // Get optional data parameter
            int data = 0;
            DataValue dataValue = block.getParameter("data");
            if (dataValue != null) {
                DataValue resolvedData = resolver.resolve(context, dataValue);
                try {
                    data = resolvedData.asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Use default data if parsing fails
                }
            }

            // Play the effect
            try {
                Effect effect = Effect.valueOf(effectName.toUpperCase());
                player.getWorld().playEffect(location, effect, data);
                return ExecutionResult.success("Effect played successfully");
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid effect name: " + effectName);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to play effect: " + e.getMessage());
        }
    }
}