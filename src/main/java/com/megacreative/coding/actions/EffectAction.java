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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Action to apply a potion effect to a player
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "effect", displayName = "§bApply Effect", type = BlockType.ACTION)
public class EffectAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue effectValue = block.getParameter("effect_type");
            DataValue durationValue = block.getParameter("duration");
            DataValue amplifierValue = block.getParameter("amplifier");
            
            if (effectValue == null) {
                return ExecutionResult.error("Missing required parameter: effect_type");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedEffect = resolver.resolve(context, effectValue);
            
            String effectStr = resolvedEffect.asString();
            PotionEffectType effectType;
            
            try {
                effectType = PotionEffectType.getByName(effectStr.toUpperCase());
                if (effectType == null) {
                    return ExecutionResult.error("Invalid effect type: " + effectStr);
                }
            } catch (Exception e) {
                return ExecutionResult.error("Invalid effect type: " + effectStr);
            }
            
            // Get duration (default to 30 seconds)
            int duration = 30 * 20; // 30 seconds in ticks
            if (durationValue != null) {
                DataValue resolvedDuration = resolver.resolve(context, durationValue);
                duration = Math.max(1, resolvedDuration.asNumber().intValue()) * 20; // Convert seconds to ticks
            }
            
            // Get amplifier (default to 1)
            int amplifier = 0; // Amplifier is 0-based (0 = level 1)
            if (amplifierValue != null) {
                DataValue resolvedAmplifier = resolver.resolve(context, amplifierValue);
                amplifier = Math.max(0, Math.min(255, resolvedAmplifier.asNumber().intValue() - 1));
            }
            
            // Apply effect
            PotionEffect effect = new PotionEffect(effectType, duration, amplifier);
            player.addPotionEffect(effect);
            
            return ExecutionResult.success("Applied effect " + effectType.getName());
        } catch (Exception e) {
            return ExecutionResult.error("Failed to apply effect: " + e.getMessage());
        }
    }
}
