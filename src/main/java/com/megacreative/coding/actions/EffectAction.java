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
 * 🎆 ENHANCED: Action for applying potion effects to players
 * Uses the new parameter system for configuration
 */
@BlockMeta(id = "effect", displayName = "§aApply Effect", type = BlockType.ACTION)
public class EffectAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get effect parameters from the new parameter system
            DataValue effectValue = block.getParameter("effect");
            DataValue durationValue = block.getParameter("duration");
            DataValue amplifierValue = block.getParameter("amplifier");
            
            if (effectValue == null || effectValue.isEmpty()) {
                return ExecutionResult.error("No effect provided");
            }

            // Resolve any placeholders in the effect name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedEffect = resolver.resolve(context, effectValue);
            
            String effectName = resolvedEffect.asString();
            if (effectName == null || effectName.isEmpty()) {
                return ExecutionResult.error("Invalid effect name");
            }

            // Parse effect type
            PotionEffectType effectType = parsePotionEffect(effectName);
            if (effectType == null) {
                return ExecutionResult.error("Unknown effect: " + effectName);
            }

            // Parse duration (default to 30 seconds)
            int duration = 600; // 30 seconds in ticks
            if (durationValue != null && !durationValue.isEmpty()) {
                try {
                    duration = Integer.parseInt(durationValue.asString()) * 20; // Convert seconds to ticks
                } catch (NumberFormatException e) {
                    // Use default duration
                }
            }

            // Parse amplifier (default to level 1)
            int amplifier = 0; // Level 1 (amplifier 0)
            if (amplifierValue != null && !amplifierValue.isEmpty()) {
                try {
                    amplifier = Integer.parseInt(amplifierValue.asString()) - 1; // Convert level to amplifier
                } catch (NumberFormatException e) {
                    // Use default amplifier
                }
            }

            // Validate and constrain parameters
            duration = Math.max(1, Math.min(72000, duration)); // 1 tick to 1 hour
            amplifier = Math.max(0, Math.min(255, amplifier)); // Level 1-256

            // Create and apply the potion effect
            PotionEffect potionEffect = new PotionEffect(effectType, duration, amplifier);
            player.addPotionEffect(potionEffect);
            
            String durationText = (duration / 20) + " seconds";
            String levelText = "Level " + (amplifier + 1);
            
            return ExecutionResult.success("Effect '" + effectType.getName() + "' (" + levelText + ", " + durationText + ") applied");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to apply effect: " + e.getMessage());
        }
    }
    
    /**
     * 🎆 ENHANCED: Parse potion effect with support for common names
     */
    private PotionEffectType parsePotionEffect(String effectName) {
        if (effectName == null || effectName.isEmpty()) {
            return null;
        }
        
        try {
            // Try parsing as direct enum name first
            return PotionEffectType.getByName(effectName.toUpperCase());
        } catch (Exception e) {
            // Try common effect name mappings
            String upperName = effectName.toUpperCase().replace(" ", "_");
            
            switch (upperName) {
                case "SPEED":
                case "FASTER":
                case "FAST":
                    return PotionEffectType.SPEED;
                case "SLOW":
                case "SLOWNESS":
                case "SLOWER":
                    return PotionEffectType.SLOW;
                case "STRENGTH":
                case "STRONG":
                case "DAMAGE":
                    return PotionEffectType.INCREASE_DAMAGE;
                case "WEAKNESS":
                case "WEAK":
                    return PotionEffectType.WEAKNESS;
                case "HEAL":
                case "HEALTH":
                case "HEALING":
                    return PotionEffectType.HEAL;
                case "POISON":
                case "TOXIC":
                    return PotionEffectType.POISON;
                case "REGEN":
                case "REGENERATION":
                    return PotionEffectType.REGENERATION;
                case "JUMP":
                case "JUMP_BOOST":
                    return PotionEffectType.JUMP;
                case "INVIS":
                case "INVISIBLE":
                case "INVISIBILITY":
                    return PotionEffectType.INVISIBILITY;
                case "GLOW":
                case "GLOWING":
                    return PotionEffectType.GLOWING;
                case "NIGHT_VISION":
                case "NIGHTVISION":
                case "NIGHT":
                    return PotionEffectType.NIGHT_VISION;
                case "RESISTANCE":
                case "DAMAGE_RESISTANCE":
                    return PotionEffectType.DAMAGE_RESISTANCE;
                case "FIRE_RESISTANCE":
                case "FIRE_RES":
                    return PotionEffectType.FIRE_RESISTANCE;
                case "WATER_BREATHING":
                case "WATER_BREATH":
                    return PotionEffectType.WATER_BREATHING;
                case "SATURATION":
                case "HUNGER":
                    return PotionEffectType.SATURATION;
                default:
                    // Try with minecraft namespace
                    return PotionEffectType.getByName("minecraft:" + effectName.toLowerCase());
            }
        }
    }
}