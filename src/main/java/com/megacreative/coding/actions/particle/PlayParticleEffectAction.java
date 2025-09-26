package com.megacreative.coding.actions.particle;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Unified particle action that supports both basic and advanced particle effects
 * Maintains backward compatibility while providing advanced patterns and control
 */
@BlockMeta(id = "playParticleEffect", displayName = "§aPlay Particle Effect", type = BlockType.ACTION)
public class PlayParticleEffectAction implements BlockAction {
    private static final int MAX_PARTICLES = 100;
    private static final double MAX_SPREAD = 10.0;
    private static final double MAX_SPEED = 5.0;
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            executeParticleEffect(context);
            return ExecutionResult.success("Particle effect played");
        } catch (Exception e) {
            handleParticleError(context, e);
            return ExecutionResult.error("Failed to play particle effect: " + e.getMessage());
        }
    }
    
    private void executeParticleEffect(ExecutionContext context) {
        Player player = validateContext(context);
        if (player == null) return;
        
        ParticleParameters params = resolveParticleParameters(context);
        if (params == null) return;
        
        ParticleEffect effect = createParticleEffect(params);
        if (effect == null) return;
        
        effect.play(player.getLocation());
    }
    
    private void handleParticleError(ExecutionContext context, Exception e) {
        if (context == null) return;
        
        Player player = context.getPlayer();
        if (player != null) {
            player.sendMessage("§cОшибка при воспроизведении частиц: " + e.getMessage());
        }
        
        if (context.getPlugin() != null) {
            context.getPlugin().getLogger().log(java.util.logging.Level.SEVERE, 
                "Ошибка при воспроизведении частиц", e);
        }
    }
    
    private Player validateContext(ExecutionContext context) {
        if (context == null || context.getPlayer() == null || 
            context.getCurrentBlock() == null || 
            context.getPlugin() == null ||
            context.getPlugin().getServiceRegistry().getVariableManager() == null) {
            return null;
        }
        return context.getPlayer();
    }
    
    /**
     * Container class for particle effect parameters
     */
    private static class ParticleParameters {
        final Particle particle;
        final int count;
        final double offset;
        final double spread;
        final double speed;
        final String pattern;
        
        ParticleParameters(Particle particle, int count, double offset, 
                          double spread, double speed, String pattern) {
            this.particle = particle;
            this.count = count;
            this.offset = offset;
            this.spread = spread;
            this.speed = speed;
            this.pattern = pattern;
        }
    }
    
    private ParticleParameters resolveParticleParameters(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        ParameterResolver resolver = new ParameterResolver(context);
        
        String particleStr = resolveParameter(resolver, context, block, "particle", "VILLAGER_HAPPY");
        if (particleStr == null) {
            player.sendMessage("§cНе указан тип частиц");
            return null;
        }
        
        Particle particle = parseParticle(particleStr);
        if (particle == null) {
            player.sendMessage("§cНеизвестный тип частиц: " + particleStr);
            return null;
        }
        
        double offset = resolveNumberParameter(resolver, context, block, "offset", 0.5);
        int count = (int) resolveNumberParameter(resolver, context, block, "count", 10);
        double spread = resolveNumberParameter(resolver, context, block, "spread", offset);
        double speed = resolveNumberParameter(resolver, context, block, "speed", 0.1);
        String pattern = resolveParameter(resolver, context, block, "pattern", "basic");
        
        // Validate and constrain parameters
        count = Math.max(1, Math.min(MAX_PARTICLES, count));
        spread = Math.max(0.1, Math.min(MAX_SPREAD, spread));
        speed = Math.max(0.0, Math.min(MAX_SPEED, speed));
        
        return new ParticleParameters(particle, count, offset, spread, speed, pattern);
    }
    
    /**
     * Spawns particles with advanced patterns
     */
    private ParticleEffect createParticleEffect(ParticleParameters params) {
        // Implementation depends on your ParticleEffect class
        // This is a simplified example
        try {
            return new ParticleEffect(
                params.particle, 
                params.count, 
                params.offset, 
                params.spread, 
                params.speed, 
                params.pattern
            );
        } catch (IllegalArgumentException e) {
            // Handle invalid particle parameters
            throw new IllegalArgumentException("Некорректные параметры частиц: " + e.getMessage(), e);
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
    
    /**
     * Spawns particles with advanced patterns
     */
    private void spawnAdvancedParticleEffect(Player player, Location center, Particle particle, 
                                           int count, double spread, double speed, String pattern) {
        if (player == null || center == null || particle == null) {
            return;
        }
        
        pattern = pattern != null ? pattern.toLowerCase() : "";
        switch (pattern) {
            case "circle":
                // Implement circle pattern
                int points = Math.max(8, Math.min(64, count));
                double radius = spread;
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = center.getX() + radius * Math.cos(angle);
                    double z = center.getZ() + radius * Math.sin(angle);
                    Location loc = new Location(center.getWorld(), x, center.getY(), z);
                    player.spawnParticle(particle, loc, 1, 0, 0, 0, speed);
                }
                break;
                
            case "sphere":
                // Implement sphere pattern
                int pointsSphere = Math.max(8, Math.min(32, (int)Math.sqrt(count)));
                double radiusSphere = spread;
                for (int i = 0; i < pointsSphere; i++) {
                    for (int j = 0; j < pointsSphere; j++) {
                        double theta = Math.PI * i / (pointsSphere - 1);
                        double phi = 2 * Math.PI * j / pointsSphere;
                        double x = center.getX() + radiusSphere * Math.sin(theta) * Math.cos(phi);
                        double y = center.getY() + radiusSphere * Math.cos(theta);
                        double z = center.getZ() + radiusSphere * Math.sin(theta) * Math.sin(phi);
                        Location loc = new Location(center.getWorld(), x, y, z);
                        player.spawnParticle(particle, loc, 1, 0, 0, 0, speed);
                    }
                }
                break;
                
            case "line":
                // Implement line pattern
                int segments = Math.max(1, Math.min(32, count));
                double dx = (Math.random() * 2 - 1) * spread;
                double dy = (Math.random() * 2 - 1) * spread;
                double dz = (Math.random() * 2 - 1) * spread;
                
                for (int i = 0; i < segments; i++) {
                    double t = (double) i / (segments - 1);
                    double x = center.getX() + dx * t;
                    double y = center.getY() + dy * t;
                    double z = center.getZ() + dz * t;
                    Location loc = new Location(center.getWorld(), x, y, z);
                    player.spawnParticle(particle, loc, 1, 0, 0, 0, speed);
                }
                break;
                
            case "basic":
            default:
                // Auto-select pattern based on count if no specific pattern is chosen
                if (pattern.isEmpty() || "basic".equals(pattern)) {
                    // Default basic pattern
                    player.spawnParticle(particle, center, count, spread, spread, spread, speed);
                } else if (count <= 20) {
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
}