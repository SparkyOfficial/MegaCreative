package com.megacreative.coding.actions.particle;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for playing a particle effect to a player.
 * This action retrieves particle parameters from the container configuration and plays the particle effect.
 */
@BlockMeta(id = "playParticle", displayName = "§aPlay Particle", type = BlockType.ACTION)
public class PlayParticleAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get particle parameters from the container configuration
            PlayParticleParams params = getParticleParamsFromContainer(block, context);
            
            if (params.particleNameStr == null || params.particleNameStr.isEmpty()) {
                return ExecutionResult.error("Particle is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue particleNameVal = DataValue.of(params.particleNameStr);
            DataValue resolvedParticleName = resolver.resolve(context, particleNameVal);
            
            // Parse parameters
            String particleName = resolvedParticleName.asString();
            int count = params.count;

            // Parse the particle
            Particle particle;
            try {
                particle = Particle.valueOf(particleName.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Use default particle if parsing fails
                particle = Particle.FLAME;
            }

            // Play the particle
            player.getWorld().spawnParticle(particle, player.getLocation(), count);
            return ExecutionResult.success("Particle played successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to play particle: " + e.getMessage());
        }
    }
    
    /**
     * Gets particle parameters from the container configuration
     */
    private PlayParticleParams getParticleParamsFromContainer(CodeBlock block, ExecutionContext context) {
        PlayParticleParams params = new PlayParticleParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get particle name from the particle slot
                Integer particleSlot = slotResolver.apply("particle_slot");
                if (particleSlot != null) {
                    ItemStack particleItem = block.getConfigItem(particleSlot);
                    if (particleItem != null && particleItem.hasItemMeta()) {
                        // Extract particle name from item
                        params.particleNameStr = getParticleNameFromItem(particleItem);
                    }
                }
                
                // Get count from the count slot
                Integer countSlot = slotResolver.apply("count_slot");
                if (countSlot != null) {
                    ItemStack countItem = block.getConfigItem(countSlot);
                    if (countItem != null && countItem.hasItemMeta()) {
                        // Extract count from item
                        params.count = getCountFromItem(countItem, 10);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting particle parameters from container in PlayParticleAction: " + e.getMessage());
        }
        
        // Set defaults if not configured
        if (params.particleNameStr == null || params.particleNameStr.isEmpty()) {
            params.particleNameStr = "FLAME";
        }
        
        return params;
    }
    
    /**
     * Extracts particle name from an item
     */
    private String getParticleNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the particle name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts count from an item
     */
    private int getCountFromItem(ItemStack item, int defaultCount) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse count from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Integer.parseInt(cleanName);
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultCount;
        }
    }
    
    /**
     * Helper class to hold particle parameters
     */
    private static class PlayParticleParams {
        String particleNameStr = "";
        int count = 10;
    }
}