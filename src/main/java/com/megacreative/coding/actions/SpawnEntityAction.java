package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for spawning an entity.
 * This action spawns an entity of a specified type from the container configuration.
 */
public class SpawnEntityAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get entity parameters from the container configuration
            SpawnEntityParams params = getEntityParamsFromContainer(block, context);
            
            if (params.entityType == null) {
                return ExecutionResult.error("Entity type is not configured");
            }

            // Spawn the entities
            Location spawnLocation = player.getLocation();
            
            int spawnedCount = 0;
            for (int i = 0; i < params.count; i++) {
                // Add some randomness to the spawn location within the radius
                double offsetX = (Math.random() - 0.5) * 2 * params.radius;
                double offsetZ = (Math.random() - 0.5) * 2 * params.radius;
                Location entityLocation = spawnLocation.clone().add(offsetX, 0, offsetZ);
                
                // Make sure the entity spawns on the ground
                entityLocation.setY(spawnLocation.getWorld().getHighestBlockYAt(entityLocation));
                
                if (spawnLocation.getWorld().spawnEntity(entityLocation, params.entityType) != null) {
                    spawnedCount++;
                }
            }
            
            return ExecutionResult.success("Spawned " + spawnedCount + " " + params.entityType.name() + "(s)");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to spawn entity: " + e.getMessage());
        }
    }
    
    /**
     * Gets entity parameters from the container configuration
     */
    private SpawnEntityParams getEntityParamsFromContainer(CodeBlock block, ExecutionContext context) {
        SpawnEntityParams params = new SpawnEntityParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get entity type from the entity slot
                Integer entitySlot = slotResolver.apply("entity_slot");
                if (entitySlot != null) {
                    ItemStack entityItem = block.getConfigItem(entitySlot);
                    if (entityItem != null && entityItem.hasItemMeta()) {
                        // Extract entity type from item name or lore
                        params.entityType = getEntityTypeFromItem(entityItem);
                    }
                }
                
                // Get count from the count slot
                Integer countSlot = slotResolver.apply("count_slot");
                if (countSlot != null) {
                    ItemStack countItem = block.getConfigItem(countSlot);
                    if (countItem != null && countItem.hasItemMeta()) {
                        // Extract count from item name or lore
                        params.count = getCountFromItem(countItem, 1);
                    }
                }
                
                // Get radius from the radius slot
                Integer radiusSlot = slotResolver.apply("radius_slot");
                if (radiusSlot != null) {
                    ItemStack radiusItem = block.getConfigItem(radiusSlot);
                    if (radiusItem != null && radiusItem.hasItemMeta()) {
                        // Extract radius from item name or lore
                        params.radius = getRadiusFromItem(radiusItem, 3.0);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting entity parameters from container in SpawnEntityAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts entity type from an item
     */
    private EntityType getEntityTypeFromItem(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse entity type from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return EntityType.valueOf(cleanName.toUpperCase());
                }
            }
            
            // Fallback to item type mapping
            switch (item.getType()) {
                case ZOMBIE_HEAD: return EntityType.ZOMBIE;
                case SKELETON_SKULL: return EntityType.SKELETON;
                case CREEPER_HEAD: return EntityType.CREEPER;
                case PLAYER_HEAD: return EntityType.PLAYER;
                case WITHER_SKELETON_SKULL: return EntityType.WITHER_SKELETON;
                default: return EntityType.valueOf(item.getType().name());
            }
        } catch (Exception e) {
            return EntityType.ZOMBIE; // Default fallback
        }
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
                    // Try to parse count from display name (e.g., "количество:5")
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    if (cleanName.contains(":")) {
                        String[] parts = cleanName.split(":");
                        if (parts.length > 1) {
                            return Math.max(1, Integer.parseInt(parts[1].trim()));
                        }
                    }
                }
            }
            
            // Fallback to item amount
            return Math.max(1, item.getAmount());
        } catch (Exception e) {
            return defaultCount;
        }
    }
    
    /**
     * Extracts radius from an item
     */
    private double getRadiusFromItem(ItemStack item, double defaultRadius) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse radius from display name (e.g., "радиус:10")
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    if (cleanName.contains(":")) {
                        String[] parts = cleanName.split(":");
                        if (parts.length > 1) {
                            return Math.max(0, Double.parseDouble(parts[1].trim()));
                        }
                    }
                }
            }
            
            // Fallback to item amount
            return Math.max(0, item.getAmount());
        } catch (Exception e) {
            return defaultRadius;
        }
    }
    
    /**
     * Helper class to hold entity parameters
     */
    private static class SpawnEntityParams {
        EntityType entityType = EntityType.ZOMBIE; // Default
        int count = 1;
        double radius = 3.0;
    }
}