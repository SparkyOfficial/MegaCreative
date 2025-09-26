package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Unified particle action that supports both basic and advanced particle effects
 * Maintains backward compatibility while providing advanced patterns and control
 */
public class PlayParticleEffectAction implements BlockAction {
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();

        if (player == null || block == null || variableManager == null) {
            return ExecutionResult.error("Player, block, or variable manager is null");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        try {
            // Enhanced parameter resolution with defaults for backward compatibility
            String particleStr = resolveParameter(resolver, context, block, "particle", "VILLAGER_HAPPY");
            int count = (int) resolveNumberParameter(resolver, context, block, "count", 10);
            double offset = resolveNumberParameter(resolver, context, block, "offset", 0.5);
            double spread = resolveNumberParameter(resolver, context, block, "spread", offset); // Use offset as spread for compatibility
            double speed = resolveNumberParameter(resolver, context, block, "speed", 0.1);
            String pattern = resolveParameter(resolver, context, block, "pattern", "basic");

            if (particleStr == null) {
                return ExecutionResult.error("Particle type is null");
            }

            // Enhanced particle parsing
            Particle particle = parseParticle(particleStr);
            if (particle == null) {
                player.sendMessage("§cНеизвестный тип частиц: " + particleStr);
                return ExecutionResult.error("Unknown particle type: " + particleStr);
            }
            
            // Validate parameters with performance limits
            count = Math.max(1, Math.min(100, count));
            spread = Math.max(0.1, Math.min(10.0, spread));
            speed = Math.max(0.0, Math.min(5.0, speed));
            
            // Determine spawn location
            Location location = context.getBlockLocation() != null ? 
                context.getBlockLocation() : player.getLocation();
            
            // Choose pattern based on parameter or count
            if (pattern.equals("basic") || count <= 10) {
                // Basic behavior for backward compatibility
                location.getWorld().spawnParticle(particle, location, count, offset, offset, offset);
            } else {
                // Advanced patterns
                spawnAdvancedParticleEffect(player, location, particle, count, spread, speed, pattern);
            }
            
            player.sendMessage("§a✨ Эффект частиц '" + particleStr + "' воспроизведен!");
            return ExecutionResult.success("Particle effect played");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметрах count/offset");
            return ExecutionResult.error("Number format error in count/offset parameters");
        } catch (Exception e) {
            player.sendMessage("§cОшибка создания частиц: " + e.getMessage());
            return ExecutionResult.error("Failed to create particles: " + e.getMessage());
        }
    }
    
    /**
     * Spawns particles with advanced patterns
     */
    private void spawnAdvancedParticleEffect(Player player, Location center, Particle particle, 
                                           int count, double spread, double speed, String pattern) {
        switch (pattern.toLowerCase()) {
            case "circle":
            case "circular":
                spawnCircularPattern(player, center, particle, count, spread, speed);
                break;
            case "sphere":
                spawnSpherePattern(player, center, particle, count, spread, speed);
                break;
            case "burst":
            case "explosion":
                spawnBurstPattern(player, center, particle, count, spread, speed);
                break;
            default:
                // Auto-select pattern based on count
                if (count <= 20) {
                    spawnCircularPattern(player, center, particle, count, spread, speed);
                } else if (count <= 50) {
                    spawnSpherePattern(player, center, particle, count, spread, speed);
                } else {
                    spawnBurstPattern(player, center, particle, count, spread, speed);
                }
                break;
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
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);
            
            double x = center.getX() + spread * Math.sin(phi) * Math.cos(theta);
            double y = center.getY() + spread * Math.cos(phi);
            double z = center.getZ() + spread * Math.sin(phi) * Math.sin(theta);
            
            Location particleLocation = new Location(center.getWorld(), x, y, z);
            
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
     * Enhanced particle parsing with support for multiple formats
     */
    private Particle parseParticle(String particleName) {
        try {
            // First try basic uppercase conversion for backward compatibility
            try {
                return Particle.valueOf(particleName.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Enhanced parsing for advanced usage
                String cleanName = particleName.toUpperCase()
                                               .replace("MINECRAFT:", "")
                                               .replace(".", "_")
                                               .replace("-", "_");
                
                // Try direct enum lookup
                try {
                    return Particle.valueOf(cleanName);
                } catch (IllegalArgumentException e2) {
                    // Map common aliases
                    switch (cleanName) {
                        case "HAPPY":
                        case "VILLAGER":
                            return Particle.VILLAGER_HAPPY;
                        case "HEARTS":
                        case "LOVE":
                            return Particle.HEART;
                        case "MAGIC":
                        case "ENCHANT":
                            return Particle.ENCHANTMENT_TABLE;
                        default:
                            return null;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
    }
} 