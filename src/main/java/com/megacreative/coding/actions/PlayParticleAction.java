package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for playing a particle effect.
 * This action plays a particle effect at the player's location from container configuration.
 */
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
            
            if (params.particle == null) {
                return ExecutionResult.error("Particle type is not configured");
            }

            // Resolve any placeholders in the particle type
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedParticleName = resolver.resolveString(context, params.particleName);

            // Play the particle effect
            try {
                Particle particle = Particle.valueOf(resolvedParticleName.toUpperCase());
                Location location = player.getLocation();
                
                player.getWorld().spawnParticle(particle, location, params.count, 0.5, 0.5, 0.5, params.speed);
                
                return ExecutionResult.success("Played particle effect: " + particle.name());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid particle type: " + params.particleName);
            }
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
                // Get particle from the particle slot
                Integer particleSlot = slotResolver.apply("particle_slot");
                if (particleSlot != null) {
                    ItemStack particleItem = block.getConfigItem(particleSlot);
                    if (particleItem != null && particleItem.hasItemMeta()) {
                        // Extract particle from item
                        params.particleName = getParticleNameFromItem(particleItem);
                        if (params.particleName != null) {
                            try {
                                params.particle = Particle.valueOf(params.particleName.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                // Use default particle if parsing fails
                                params.particle = Particle.EXPLOSION_NORMAL;
                            }
                        }
                    }
                }
                
                // Get count from the count slot
                Integer countSlot = slotResolver.apply("count_slot");
                if (countSlot != null) {
                    ItemStack countItem = block.getConfigItem(countSlot);
                    if (countItem != null && countItem.hasItemMeta()) {
                        // Extract count from item
                        params.count = getIntFromItem(countItem, 10);
                    }
                }
                
                // Get speed from the speed slot
                Integer speedSlot = slotResolver.apply("speed_slot");
                if (speedSlot != null) {
                    ItemStack speedItem = block.getConfigItem(speedSlot);
                    if (speedItem != null && speedItem.hasItemMeta()) {
                        // Extract speed from item
                        params.speed = getDoubleFromItem(speedItem, 0.1);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting particle parameters from container in PlayParticleAction: " + e.getMessage());
        }
        
        // Set defaults if not configured
        if (params.particle == null) {
            params.particle = Particle.EXPLOSION_NORMAL;
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
     * Extracts integer from an item
     */
    private int getIntFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse integer from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Math.max(1, Integer.parseInt(cleanName));
                }
            }
            
            // Fallback to item amount
            return Math.max(1, item.getAmount());
        } catch (Exception e) {
            return Math.max(1, defaultValue);
        }
    }
    
    /**
     * Extracts double from an item
     */
    private double getDoubleFromItem(ItemStack item, double defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse double from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Math.max(0, Double.parseDouble(cleanName));
                }
            }
            
            // Fallback to item amount
            return Math.max(0, item.getAmount());
        } catch (Exception e) {
            return Math.max(0, defaultValue);
        }
    }
    
    /**
     * Helper class to hold particle parameters
     */
    private static class PlayParticleParams {
        Particle particle = null;
        String particleName = "";
        int count = 10;
        double speed = 0.1;
    }
}