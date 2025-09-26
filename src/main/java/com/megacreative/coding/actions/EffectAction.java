package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Function;

/**
 * 🎆 ENHANCED: Action for applying potion effects to players
 * Supports both container-based configuration and parameter-based configuration
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
            // Get effect parameters from configuration
            EffectParams params = getEffectParamsFromContainer(block, context);
            
            if (params.effectStr == null || params.effectStr.isEmpty()) {
                return ExecutionResult.error("Effect is not configured");
            }

            // Resolve any placeholders in the effect name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue effectValue = DataValue.of(params.effectStr);
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

            // Validate and constrain parameters
            int duration = Math.max(1, Math.min(72000, params.duration)); // 1 tick to 1 hour
            int amplifier = Math.max(0, Math.min(255, params.amplifier)); // Level 1-256

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
     * 🎆 ENHANCED: Gets effect parameters from container configuration with fallbacks
     */
    private EffectParams getEffectParamsFromContainer(CodeBlock block, ExecutionContext context) {
        EffectParams params = new EffectParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get effect from the effect_type slot
                Integer effectSlot = slotResolver.apply("effect_type");
                if (effectSlot != null) {
                    ItemStack effectItem = block.getConfigItem(effectSlot);
                    if (effectItem != null && effectItem.hasItemMeta()) {
                        params.effectStr = getEffectFromItem(effectItem);
                    }
                }
                
                // Get duration from the duration slot
                Integer durationSlot = slotResolver.apply("duration");
                if (durationSlot != null) {
                    ItemStack durationItem = block.getConfigItem(durationSlot);
                    if (durationItem != null && durationItem.hasItemMeta()) {
                        params.duration = getIntFromItem(durationItem, 30) * 20; // Convert seconds to ticks
                    }
                }
                
                // Get amplifier from the amplifier slot
                Integer amplifierSlot = slotResolver.apply("amplifier");
                if (amplifierSlot != null) {
                    ItemStack amplifierItem = block.getConfigItem(amplifierSlot);
                    if (amplifierItem != null && amplifierItem.hasItemMeta()) {
                        params.amplifier = getIntFromItem(amplifierItem, 1) - 1; // Convert level to amplifier (0-based)
                    }
                }
            }
            
            // 🎆 ENHANCED: Fallback to parameter-based configuration
            DataValue effectParam = block.getParameter("effect");
            DataValue durationParam = block.getParameter("duration");
            DataValue amplifierParam = block.getParameter("amplifier");
            
            if (params.effectStr == null && effectParam != null && !effectParam.isEmpty()) {
                params.effectStr = effectParam.asString();
            }
            
            if (durationParam != null && !durationParam.isEmpty()) {
                try {
                    params.duration = Integer.parseInt(durationParam.asString()) * 20; // Convert to ticks
                } catch (NumberFormatException e) {
                    // Use default duration
                }
            }
            
            if (amplifierParam != null && !amplifierParam.isEmpty()) {
                try {
                    params.amplifier = Integer.parseInt(amplifierParam.asString()) - 1; // Convert level to amplifier
                } catch (NumberFormatException e) {
                    // Use default amplifier
                }
            }
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting effect parameters from container: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts effect name from an item
     */
    private String getEffectFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the effect name
                return org.bukkit.ChatColor.stripColor(displayName).trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts integer value from an item
     */
    private int getIntFromItem(ItemStack item, int defaultValue) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                try {
                    String cleanName = org.bukkit.ChatColor.stripColor(displayName).trim();
                    return Integer.parseInt(cleanName);
                } catch (NumberFormatException e) {
                    // Use default value if parsing fails
                }
            }
        }
        return defaultValue;
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
    
    /**
     * Helper class to hold effect parameters
     */
    private static class EffectParams {
        String effectStr = "";
        int duration = 600; // 30 seconds in ticks
        int amplifier = 0; // Level 1 (amplifier 0)
    }
}