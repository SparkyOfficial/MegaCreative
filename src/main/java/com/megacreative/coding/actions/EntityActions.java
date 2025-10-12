package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Entity-related actions for manipulating entities in the world
 * Includes spawning, modifying, and interacting with entities
 */
@BlockMeta(id = "entityActions", displayName = "§cEntity Actions", type = BlockType.ACTION)
public class EntityActions implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue actionValue = block.getParameter("action");
            if (actionValue == null) {
                return ExecutionResult.error("Action parameter is required");
            }
            
            ParameterResolver resolver = new ParameterResolver(context);
            String action = resolver.resolve(context, actionValue).asString();
            
            switch (action.toLowerCase()) {
                case "spawn":
                    return spawnEntity(block, context, resolver);
                    
                case "kill":
                    return killEntity(block, context, resolver);
                    
                case "teleport":
                    return teleportEntity(block, context, resolver);
                    
                case "damage":
                    return damageEntity(block, context, resolver);
                    
                case "heal":
                    return healEntity(block, context, resolver);
                    
                case "addpotioneffect":
                    return addPotionEffect(block, context, resolver);
                    
                case "removepotioneffect":
                    return removePotionEffect(block, context, resolver);
                    
                case "setvelocity":
                    return setEntityVelocity(block, context, resolver);
                    
                default:
                    return ExecutionResult.error("Unknown entity action: " + action);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error in entity action: " + e.getMessage());
        }
    }
    
    /**
     * Spawns an entity at a specified location
     */
    private ExecutionResult spawnEntity(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            
            DataValue entityTypeValue = block.getParameter("entityType");
            if (entityTypeValue == null) {
                return ExecutionResult.error("Entity type parameter is required");
            }
            
            String entityTypeStr = resolver.resolve(context, entityTypeValue).asString();
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(entityTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid entity type: " + entityTypeStr);
            }
            
            
            Location location = getTargetLocation(block, context, resolver);
            if (location == null || location.getWorld() == null) {
                return ExecutionResult.error("Invalid location for entity spawn");
            }
            
            
            Entity entity = location.getWorld().spawnEntity(location, entityType);
            if (entity != null) {
                
                DataValue nameValue = block.getParameter("name");
                if (nameValue != null) {
                    String name = resolver.resolve(context, nameValue).asString();
                    if (name != null && !name.isEmpty()) {
                        entity.setCustomName(name);
                        entity.setCustomNameVisible(true);
                    }
                }
                
                return ExecutionResult.success("Spawned " + entityTypeStr + " at location");
            } else {
                return ExecutionResult.error("Failed to spawn entity");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error spawning entity: " + e.getMessage());
        }
    }
    
    /**
     * Kills/removes an entity
     */
    private ExecutionResult killEntity(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            Entity targetEntity = getTargetEntity(block, context, resolver);
            if (targetEntity == null) {
                return ExecutionResult.error("Target entity not found");
            }
            
            targetEntity.remove();
            return ExecutionResult.success("Removed entity " + targetEntity.getType());
        } catch (Exception e) {
            return ExecutionResult.error("Error removing entity: " + e.getMessage());
        }
    }
    
    /**
     * Teleports an entity to a specified location
     */
    private ExecutionResult teleportEntity(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            Entity targetEntity = getTargetEntity(block, context, resolver);
            if (targetEntity == null) {
                return ExecutionResult.error("Target entity not found");
            }
            
            Location location = getTargetLocation(block, context, resolver);
            if (location == null || location.getWorld() == null) {
                return ExecutionResult.error("Invalid target location");
            }
            
            boolean success = targetEntity.teleport(location);
            if (success) {
                return ExecutionResult.success("Teleported entity to location");
            } else {
                return ExecutionResult.error("Failed to teleport entity");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error teleporting entity: " + e.getMessage());
        }
    }
    
    /**
     * Damages an entity
     */
    private ExecutionResult damageEntity(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            Entity targetEntity = getTargetEntity(block, context, resolver);
            if (targetEntity == null) {
                return ExecutionResult.error("Target entity not found");
            }
            
            if (!(targetEntity instanceof Damageable)) {
                return ExecutionResult.error("Entity cannot be damaged");
            }
            
            DataValue damageValue = block.getParameter("damage");
            if (damageValue == null) {
                return ExecutionResult.error("Damage parameter is required");
            }
            
            double damage = resolver.resolve(context, damageValue).asNumber().doubleValue();
            ((Damageable) targetEntity).damage(damage);
            
            return ExecutionResult.success("Damaged entity with " + damage + " damage");
        } catch (Exception e) {
            return ExecutionResult.error("Error damaging entity: " + e.getMessage());
        }
    }
    
    /**
     * Heals an entity
     */
    private ExecutionResult healEntity(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            Entity targetEntity = getTargetEntity(block, context, resolver);
            if (targetEntity == null) {
                return ExecutionResult.error("Target entity not found");
            }
            
            if (!(targetEntity instanceof Damageable)) {
                return ExecutionResult.error("Entity cannot be healed");
            }
            
            DataValue healValue = block.getParameter("healAmount");
            double healAmount = healValue != null ? 
                resolver.resolve(context, healValue).asNumber().doubleValue() : 10.0;
            
            Damageable damageable = (Damageable) targetEntity;
            double newHealth = Math.min(damageable.getHealth() + healAmount, damageable.getMaxHealth());
            damageable.setHealth(newHealth);
            
            return ExecutionResult.success("Healed entity by " + healAmount + " health");
        } catch (Exception e) {
            return ExecutionResult.error("Error healing entity: " + e.getMessage());
        }
    }
    
    /**
     * Adds a potion effect to an entity
     */
    private ExecutionResult addPotionEffect(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            Entity targetEntity = getTargetEntity(block, context, resolver);
            if (targetEntity == null) {
                return ExecutionResult.error("Target entity not found");
            }
            
            if (!(targetEntity instanceof LivingEntity)) {
                return ExecutionResult.error("Entity cannot receive potion effects");
            }
            
            
            DataValue effectTypeValue = block.getParameter("effectType");
            if (effectTypeValue == null) {
                return ExecutionResult.error("Effect type parameter is required");
            }
            
            String effectTypeStr = resolver.resolve(context, effectTypeValue).asString();
            PotionEffectType effectType = PotionEffectType.getByName(effectTypeStr.toUpperCase());
            if (effectType == null) {
                return ExecutionResult.error("Invalid potion effect type: " + effectTypeStr);
            }
            
            
            DataValue durationValue = block.getParameter("duration");
            DataValue amplifierValue = block.getParameter("amplifier");
            
            int duration = durationValue != null ? 
                resolver.resolve(context, durationValue).asNumber().intValue() : 600; 
            int amplifier = amplifierValue != null ? 
                resolver.resolve(context, amplifierValue).asNumber().intValue() : 0; 
            
            PotionEffect effect = new PotionEffect(effectType, duration, amplifier);
            ((LivingEntity) targetEntity).addPotionEffect(effect);
            
            return ExecutionResult.success("Added potion effect " + effectTypeStr + " to entity");
        } catch (Exception e) {
            return ExecutionResult.error("Error adding potion effect: " + e.getMessage());
        }
    }
    
    /**
     * Removes a potion effect from an entity
     */
    private ExecutionResult removePotionEffect(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            Entity targetEntity = getTargetEntity(block, context, resolver);
            if (targetEntity == null) {
                return ExecutionResult.error("Target entity not found");
            }
            
            if (!(targetEntity instanceof LivingEntity)) {
                return ExecutionResult.error("Entity cannot have potion effects removed");
            }
            
            
            DataValue effectTypeValue = block.getParameter("effectType");
            if (effectTypeValue == null) {
                return ExecutionResult.error("Effect type parameter is required");
            }
            
            String effectTypeStr = resolver.resolve(context, effectTypeValue).asString();
            PotionEffectType effectType = PotionEffectType.getByName(effectTypeStr.toUpperCase());
            if (effectType == null) {
                return ExecutionResult.error("Invalid potion effect type: " + effectTypeStr);
            }
            
            ((LivingEntity) targetEntity).removePotionEffect(effectType);
            
            return ExecutionResult.success("Removed potion effect " + effectTypeStr + " from entity");
        } catch (Exception e) {
            return ExecutionResult.error("Error removing potion effect: " + e.getMessage());
        }
    }
    
    /**
     * Sets the velocity of an entity
     */
    private ExecutionResult setEntityVelocity(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            Entity targetEntity = getTargetEntity(block, context, resolver);
            if (targetEntity == null) {
                return ExecutionResult.error("Target entity not found");
            }
            
            
            DataValue xValue = block.getParameter("velocityX");
            DataValue yValue = block.getParameter("velocityY");
            DataValue zValue = block.getParameter("velocityZ");
            
            double x = xValue != null ? resolver.resolve(context, xValue).asNumber().doubleValue() : 0.0;
            double y = yValue != null ? resolver.resolve(context, yValue).asNumber().doubleValue() : 0.0;
            double z = zValue != null ? resolver.resolve(context, zValue).asNumber().doubleValue() : 0.0;
            
            Vector velocity = new Vector(x, y, z);
            targetEntity.setVelocity(velocity);
            
            return ExecutionResult.success("Set entity velocity to " + x + ", " + y + ", " + z);
        } catch (Exception e) {
            return ExecutionResult.error("Error setting entity velocity: " + e.getMessage());
        }
    }
    
    /**
     * Gets the target entity based on parameters
     */
    private Entity getTargetEntity(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            
            DataValue entityIdValue = block.getParameter("entityId");
            if (entityIdValue != null) {
                String entityIdStr = resolver.resolve(context, entityIdValue).asString();
                try {
                    UUID entityId = UUID.fromString(entityIdStr);
                    for (World world : context.getPlugin().getServer().getWorlds()) {
                        for (Entity entity : world.getEntities()) {
                            if (entity.getUniqueId().equals(entityId)) {
                                return entity;
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    
                }
            }
            
            
            DataValue entityTypeValue = block.getParameter("entityType");
            if (entityTypeValue != null && context.getPlayer() != null) {
                String entityTypeStr = resolver.resolve(context, entityTypeValue).asString();
                try {
                    EntityType entityType = EntityType.valueOf(entityTypeStr.toUpperCase());
                    Location playerLoc = context.getPlayer().getLocation();
                    java.util.Collection<Entity> nearbyEntities = playerLoc.getWorld().getNearbyEntities(playerLoc, 10, 10, 10);
                    
                    for (Entity entity : nearbyEntities) {
                        if (entity.getType() == entityType) {
                            return entity;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    
                }
            }
            
            
            return context.getPlayer();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Gets the target location based on parameters
     */
    private Location getTargetLocation(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            
            DataValue xValue = block.getParameter("x");
            DataValue yValue = block.getParameter("y");
            DataValue zValue = block.getParameter("z");
            
            if (xValue != null && yValue != null && zValue != null) {
                double x = resolver.resolve(context, xValue).asNumber().doubleValue();
                double y = resolver.resolve(context, yValue).asNumber().doubleValue();
                double z = resolver.resolve(context, zValue).asNumber().doubleValue();
                
                
                World world = null;
                DataValue worldValue = block.getParameter("world");
                if (worldValue != null) {
                    String worldName = resolver.resolve(context, worldValue).asString();
                    world = context.getPlugin().getServer().getWorld(worldName);
                }
                
                
                if (world == null && context.getPlayer() != null) {
                    world = context.getPlayer().getWorld();
                }
                
                if (world != null) {
                    return new Location(world, x, y, z);
                }
            }
            
            
            if (context.getPlayer() != null) {
                return context.getPlayer().getLocation();
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}