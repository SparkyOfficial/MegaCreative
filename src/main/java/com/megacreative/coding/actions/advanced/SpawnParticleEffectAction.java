package com.megacreative.coding.actions.advanced;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Advanced particle effect action with sophisticated visual control
 * Supports multiple particle types, custom counts, spread patterns, and speeds
 */
public class SpawnParticleEffectAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);

        try {
            // Resolve particle parameters
            String particleName = resolveParameter(resolver, context, block, "particle", "VILLAGER_HAPPY");
            int count = (int) resolveNumberParameter(resolver, context, block, "count", 10);
            double spread = resolveNumberParameter(resolver, context, block, "spread", 1.0);
            double speed = resolveNumberParameter(resolver, context, block, "speed", 0.1);
            
            // Parse particle type
            Particle particle = parseParticle(particleName);
            if (particle == null) {
                player.sendMessage("§cInvalid particle type: " + particleName);
                return;
            }
            
            // Validate parameters
            count = Math.max(1, Math.min(100, count)); // Limit to prevent lag
            spread = Math.max(0.1, Math.min(10.0, spread));
            speed = Math.max(0.0, Math.min(5.0, speed));
            
            // Get spawn location (player's current location + slight offset)
            Location spawnLocation = player.getLocation().add(0, 1, 0);
            
            // Create particle effect with spread pattern
            spawnParticleEffect(player, spawnLocation, particle, count, spread, speed);
            
            // Debug feedback
            if (context.isDebugMode()) {
                player.sendMessage("§7[DEBUG] Spawned " + count + " " + particleName + 
                                 " particles (spread:" + spread + ", speed:" + speed + ")");
            }
            
        } catch (Exception e) {
            player.sendMessage("§cError spawning particles: " + e.getMessage());
            if (context.isDebugMode()) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Spawns particles with advanced spread and speed control
     */
    private void spawnParticleEffect(Player player, Location center, Particle particle, 
                                   int count, double spread, double speed) {
        // Different patterns based on particle count
        if (count <= 10) {
            // Simple circular pattern for small counts
            spawnCircularPattern(player, center, particle, count, spread, speed);
        } else if (count <= 50) {
            // Sphere pattern for medium counts
            spawnSpherePattern(player, center, particle, count, spread, speed);
        } else {
            // Complex burst pattern for large counts
            spawnBurstPattern(player, center, particle, count, spread, speed);
        }
    }
    
    /**
     * Creates a circular particle pattern
     */
    private void spawnCircularPattern(Player player, Location center, Particle particle, 
                                    int count, double spread, double speed) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double x = center.getX() + Math.cos(angle) * spread;
            double z = center.getZ() + Math.sin(angle) * spread;
            double y = center.getY() + (Math.random() - 0.5) * spread * 0.5;
            
            Location particleLocation = new Location(center.getWorld(), x, y, z);
            
            // Spawn with calculated velocity
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
                                  int count, double spread, double speed) {
        for (int i = 0; i < count; i++) {
            // Generate random point on sphere surface
            double theta = Math.random() * 2 * Math.PI; // Azimuthal angle
            double phi = Math.acos(2 * Math.random() - 1); // Polar angle
            
            double x = center.getX() + spread * Math.sin(phi) * Math.cos(theta);
            double y = center.getY() + spread * Math.cos(phi);
            double z = center.getZ() + spread * Math.sin(phi) * Math.sin(theta);
            
            Location particleLocation = new Location(center.getWorld(), x, y, z);
            
            // Velocity pointing outward from center
            double velX = (x - center.getX()) / spread * speed;
            double velY = (y - center.getY()) / spread * speed;
            double velZ = (z - center.getZ()) / spread * speed;
            
            player.spawnParticle(particle, particleLocation, 1, velX, velY, velZ, 0);
        }
    }
    
    /**
     * Creates an explosive burst pattern
     */
    private void spawnBurstPattern(Player player, Location center, Particle particle, 
                                 int count, double spread, double speed) {
        // Multiple rings at different heights
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
                
                // Explosive outward velocity
                double velX = Math.cos(angle) * speed * (1 + ring * 0.3);
                double velZ = Math.sin(angle) * speed * (1 + ring * 0.3);
                double velY = (Math.random() - 0.3) * speed;
                
                player.spawnParticle(particle, particleLocation, 1, velX, velY, velZ, 0);
            }
        }
    }
    
    /**
     * Resolves a text parameter with fallback default
     */
    private String resolveParameter(ParameterResolver resolver, ExecutionContext context, 
                                  CodeBlock block, String paramName, String defaultValue) {
        DataValue rawValue = block.getParameter(paramName);
        if (rawValue == null) return defaultValue;
        
        return resolver.resolve(context, rawValue).asString();
    }
    
    /**
     * Resolves a numeric parameter with fallback default
     */
    private double resolveNumberParameter(ParameterResolver resolver, ExecutionContext context, 
                                        CodeBlock block, String paramName, double defaultValue) {
        DataValue rawValue = block.getParameter(paramName);
        if (rawValue == null) return defaultValue;
        
        try {
            return resolver.resolve(context, rawValue).asNumber().doubleValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Parses particle name to Bukkit Particle enum
     */
    private Particle parseParticle(String particleName) {
        try {
            // Clean and normalize the name
            String cleanName = particleName.toUpperCase()
                                          .replace("MINECRAFT:", "")
                                          .replace(".", "_")
                                          .replace("-", "_");
            
            return Particle.valueOf(cleanName);
            
        } catch (IllegalArgumentException e) {
            // Try common particle name mappings
            return switch (particleName.toLowerCase()) {
                case "fire", "flame" -> Particle.FLAME;
                case "smoke" -> Particle.SMOKE_NORMAL;
                case "heart", "hearts" -> Particle.HEART;
                case "happy", "villager_happy" -> Particle.VILLAGER_HAPPY;
                case "angry", "villager_angry" -> Particle.VILLAGER_ANGRY;
                case "magic", "enchant" -> Particle.ENCHANTMENT_TABLE;
                case "portal" -> Particle.PORTAL;
                case "explosion" -> Particle.EXPLOSION_NORMAL;
                case "firework", "fireworks" -> Particle.FIREWORKS_SPARK;
                case "redstone" -> Particle.REDSTONE;
                case "water" -> Particle.WATER_SPLASH;
                case "lava" -> Particle.LAVA;
                default -> null;
            };
        }
    }
}