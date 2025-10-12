package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Action to spawn particle effects with configurable parameters.
 * Includes safety limits to prevent server lag.
 */
@BlockMeta(id = "spawnParticleEffect", displayName = "§aSpawn Particle Effect", type = BlockType.ACTION)
public class SpawnParticleEffectAction implements BlockAction {
    
    
    private static final int MAX_PARTICLES = 1000;
    
    private static final double MAX_SPREAD = 10.0;
    
    private static final double MAX_SPEED = 5.0;
    
    private static final double MAX_OFFSET = 10.0;

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            
            DataValue particleValue = block.getParameter("particle");
            DataValue countValue = block.getParameter("count", DataValue.of(10));
            DataValue offsetXValue = block.getParameter("offsetX", DataValue.of(0.5));
            DataValue offsetYValue = block.getParameter("offsetY", DataValue.of(0.5));
            DataValue offsetZValue = block.getParameter("offsetZ", DataValue.of(0.5));
            DataValue speedValue = block.getParameter("speed", DataValue.of(0.1));
            DataValue patternValue = block.getParameter("pattern", DataValue.of("basic"));
            
            if (particleValue == null || particleValue.isEmpty()) {
                return ExecutionResult.error("Particle type parameter is missing.");
            }
            
            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedParticle = resolver.resolve(context, particleValue);
            DataValue resolvedCount = resolver.resolve(context, countValue);
            DataValue resolvedOffsetX = resolver.resolve(context, offsetXValue);
            DataValue resolvedOffsetY = resolver.resolve(context, offsetYValue);
            DataValue resolvedOffsetZ = resolver.resolve(context, offsetZValue);
            DataValue resolvedSpeed = resolver.resolve(context, speedValue);
            DataValue resolvedPattern = resolver.resolve(context, patternValue);
            
            if (resolvedParticle == null || resolvedParticle.isEmpty()) {
                return ExecutionResult.error("Could not resolve particle type.");
            }
            
            String particleType = resolvedParticle.asString().toUpperCase();
            String pattern = resolvedPattern.asString().toLowerCase();
            
            
            Particle particle;
            try {
                particle = Particle.valueOf(particleType);
            } catch (IllegalArgumentException e) {
                
                switch (particleType) {
                    case "HAPPY":
                    case "VILLAGER":
                        particle = Particle.VILLAGER_HAPPY;
                        break;
                    case "HEARTS":
                    case "LOVE":
                        particle = Particle.HEART;
                        break;
                    case "MAGIC":
                    case "ENCHANT":
                        particle = Particle.ENCHANTMENT_TABLE;
                        break;
                    default:
                        return ExecutionResult.error("Invalid particle type: " + particleType);
                }
            }
            
            
            int count;
            try {
                count = resolvedCount.asNumber().intValue();
                if (count < 0) {
                    return ExecutionResult.error("Particle count cannot be negative.");
                }
                if (count > MAX_PARTICLES) {
                    context.getPlugin().getLogger().warning("Particle count " + count + " exceeds maximum of " + MAX_PARTICLES + ". Clamping to maximum.");
                    count = MAX_PARTICLES;
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid particle count: " + resolvedCount.asString());
            }
            
            
            double offsetX, offsetY, offsetZ;
            try {
                offsetX = Math.abs(resolvedOffsetX.asNumber().doubleValue());
                offsetY = Math.abs(resolvedOffsetY.asNumber().doubleValue());
                offsetZ = Math.abs(resolvedOffsetZ.asNumber().doubleValue());
                
                if (offsetX > MAX_OFFSET) {
                    context.getPlugin().getLogger().warning("Particle offsetX " + offsetX + " exceeds maximum of " + MAX_OFFSET + ". Clamping to maximum.");
                    offsetX = MAX_OFFSET;
                }
                if (offsetY > MAX_OFFSET) {
                    context.getPlugin().getLogger().warning("Particle offsetY " + offsetY + " exceeds maximum of " + MAX_OFFSET + ". Clamping to maximum.");
                    offsetY = MAX_OFFSET;
                }
                if (offsetZ > MAX_OFFSET) {
                    context.getPlugin().getLogger().warning("Particle offsetZ " + offsetZ + " exceeds maximum of " + MAX_OFFSET + ". Clamping to maximum.");
                    offsetZ = MAX_OFFSET;
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid offset value: " + e.getMessage());
            }
            
            
            double speed;
            try {
                speed = Math.abs(resolvedSpeed.asNumber().doubleValue());
                if (speed > MAX_SPEED) {
                    context.getPlugin().getLogger().warning("Particle speed " + speed + " exceeds maximum of " + MAX_SPEED + ". Clamping to maximum.");
                    speed = MAX_SPEED;
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid speed value: " + resolvedSpeed.asString());
            }
            
            
            Location location = context.getBlockLocation() != null ? 
                context.getBlockLocation() : player.getLocation();
            
            
            if ("basic".equals(pattern) || count <= 10) {
                
                location.getWorld().spawnParticle(
                    particle,
                    location,
                    count,
                    offsetX,
                    offsetY,
                    offsetZ,
                    speed
                );
            } else {
                
                spawnAdvancedPattern(player, location, particle, count, offsetX, offsetY, offsetZ, speed, pattern);
            }
            
            return ExecutionResult.success("Particle effect '" + particleType + "' created with " + count + " particles.");

        } catch (Exception e) {
            return ExecutionResult.error("Error creating particle effect: " + e.getMessage());
        }
    }
    
    /**
     * Spawns particles with advanced patterns
     */
    private void spawnAdvancedPattern(Player player, Location center, Particle particle, 
                                   int count, double offsetX, double offsetY, double offsetZ, 
                                   double speed, String pattern) {
        switch (pattern) {
            case "circle":
            case "circular":
                spawnCircularPattern(player, center, particle, count, offsetX, speed);
                break;
            case "sphere":
                spawnSpherePattern(player, center, particle, count, offsetX, speed);
                break;
            case "burst":
            case "explosion":
                spawnBurstPattern(player, center, particle, count, offsetX, speed);
                break;
            default:
                
                if (count <= 20) {
                    spawnCircularPattern(player, center, particle, count, offsetX, speed);
                } else if (count <= 50) {
                    spawnSpherePattern(player, center, particle, count, offsetX, speed);
                } else {
                    spawnBurstPattern(player, center, particle, count, offsetX, speed);
                }
                break;
        }
    }
    
    /**
     * Creates a circular particle pattern
     */
    private void spawnCircularPattern(Player player, Location center, Particle particle, 
                                    int count, double radius, double speed) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            double y = center.getY() + (Math.random() - 0.5) * radius * 0.5;
            
            Location particleLocation = new Location(center.getWorld(), x, y, z);
            
            double velX = Math.cos(angle) * speed;
            double velZ = Math.sin(angle) * speed;
            double velY = (Math.random() - 0.5) * speed;
            
            player.spawnParticle(particle, particleLocation, 1, velX, velY, velZ, 0);
        }
    }
    
    /**
     * Creates a 3D sphere particle pattern
     */
    private void spawnSpherePattern(Player player, Location center, Particle particle, 
                                  int count, double radius, double speed) {
        for (int i = 0; i < count; i++) {
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);
            
            double x = center.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double y = center.getY() + radius * Math.cos(phi);
            double z = center.getZ() + radius * Math.sin(phi) * Math.sin(theta);
            
            Location particleLocation = new Location(center.getWorld(), x, y, z);
            
            double velX = (x - center.getX()) / radius * speed;
            double velY = (y - center.getY()) / radius * speed;
            double velZ = (z - center.getZ()) / radius * speed;
            
            player.spawnParticle(particle, particleLocation, 1, velX, velY, velZ, 0);
        }
    }
    
    /**
     * Creates an explosive burst pattern
     */
    private void spawnBurstPattern(Player player, Location center, Particle particle, 
                                 int count, double spread, double speed) {
        int ringsCount = Math.min(5, count / 10);
        int particlesPerRing = count / ringsCount;
        
        for (int ring = 0; ring < ringsCount; ring++) {
            double ringHeight = center.getY() + (ring - ringsCount / 2.0) * spread * 0.3;
            double ringRadius = spread * (0.5 + ring * 0.2);
            
            for (int p = 0; p < particlesPerRing; p++) {
                double angle = (2 * Math.PI * p) / particlesPerRing;
                double x = center.getX() + Math.cos(angle) * ringRadius;
                double z = center.getZ() + Math.sin(angle) * ringRadius;
                
                Location particleLocation = new Location(center.getWorld(), x, ringHeight, z);
                
                double velX = Math.cos(angle) * speed * (1 + ring * 0.3);
                double velZ = Math.sin(angle) * speed * (1 + ring * 0.3);
                double velY = (Math.random() - 0.3) * speed;
                
                player.spawnParticle(particle, particleLocation, 1, velX, velY, velZ, 0);
            }
        }
    }
}