package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.particles.AnimatedParticleSystem;
import com.megacreative.particles.ParticleShapeGenerator;
import com.megacreative.particles.Vector3D;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Action to spawn advanced particle effects with vector math for shapes and animations
 */
@BlockMeta(
    id = "spawn_advanced_particle_effect",
    displayName = "Spawn Advanced Particle Effect",
    type = BlockType.ACTION
)
public class SpawnAdvancedParticleEffectAction implements BlockAction {
    
    private final MegaCreative plugin;
    
    public SpawnAdvancedParticleEffectAction(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
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
            DataValue shapeValue = block.getParameter("shape", DataValue.of("circle"));
            DataValue radiusValue = block.getParameter("radius", DataValue.of(1.0));
            DataValue particleCountValue = block.getParameter("particleCount", DataValue.of(20));
            DataValue isAnimatedValue = block.getParameter("isAnimated", DataValue.of(false));
            DataValue durationValue = block.getParameter("duration", DataValue.of(100)); 
            
            if (particleValue == null || particleValue.isEmpty()) {
                return ExecutionResult.error("Particle type parameter is missing.");
            }
            
            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedParticle = resolver.resolve(context, particleValue);
            DataValue resolvedShape = resolver.resolve(context, shapeValue);
            DataValue resolvedRadius = resolver.resolve(context, radiusValue);
            DataValue resolvedParticleCount = resolver.resolve(context, particleCountValue);
            DataValue resolvedIsAnimated = resolver.resolve(context, isAnimatedValue);
            DataValue resolvedDuration = resolver.resolve(context, durationValue);
            
            if (resolvedParticle == null || resolvedParticle.isEmpty()) {
                return ExecutionResult.error("Could not resolve particle type.");
            }
            
            String particleType = resolvedParticle.asString().toUpperCase();
            String shape = resolvedShape.asString().toLowerCase();
            double radius = Math.abs(resolvedRadius.asNumber().doubleValue());
            int particleCount = Math.max(1, resolvedParticleCount.asNumber().intValue());
            boolean isAnimated = resolvedIsAnimated.asBoolean();
            int duration = Math.max(1, resolvedDuration.asNumber().intValue());
            
            
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
            
            
            Location location = context.getBlockLocation() != null ? 
                context.getBlockLocation() : player.getLocation();
            
            Vector3D center = new Vector3D(0, 0, 0); 
            
            if (isAnimated) {
                
                AnimatedParticleSystem system = new AnimatedParticleSystem(plugin, player, location, particle);
                
                
                switch (shape) {
                    case "circle":
                        system.addAnimation(new AnimatedParticleSystem.RotatingCircleAnimation(
                            center, radius, particleCount, new Vector3D(0, 1, 0), 0.2
                        ));
                        break;
                    case "sphere":
                        system.addAnimation(new AnimatedParticleSystem.PulsingSphereAnimation(
                            center, radius * 0.5, radius, particleCount
                        ));
                        break;
                    case "wave":
                        system.addAnimation(new AnimatedParticleSystem.MovingWaveAnimation(
                            center, radius * 2, radius * 0.5, 2, particleCount, 0
                        ));
                        break;
                    default:
                        system.addAnimation(new AnimatedParticleSystem.RotatingCircleAnimation(
                            center, radius, particleCount, new Vector3D(0, 1, 0), 0.2
                        ));
                        break;
                }
                
                
                system.start(duration);
                
                return ExecutionResult.success("Started animated " + shape + " particle effect with " + particleCount + " particles.");
            } else {
                
                List<Vector3D> particles;
                
                switch (shape) {
                    case "circle":
                        particles = ParticleShapeGenerator.createCircle(
                            center, radius, particleCount, new Vector3D(0, 1, 0)
                        );
                        break;
                    case "sphere":
                        particles = ParticleShapeGenerator.createSphere(
                            center, radius, particleCount
                        );
                        break;
                    case "helix":
                        particles = ParticleShapeGenerator.createHelix(
                            center, radius, radius * 2, 3, particleCount
                        );
                        break;
                    case "wave":
                        particles = ParticleShapeGenerator.createWave(
                            center, radius * 2, radius * 0.5, 2, particleCount, 0
                        );
                        break;
                    case "torus":
                        particles = ParticleShapeGenerator.createTorus(
                            center, radius, radius * 0.3, Math.max(8, particleCount/4), 4
                        );
                        break;
                    default:
                        particles = ParticleShapeGenerator.createCircle(
                            center, radius, particleCount, new Vector3D(0, 1, 0)
                        );
                        break;
                }
                
                
                ParticleShapeGenerator.spawnParticles(player, particle, particles, location);
                
                return ExecutionResult.success("Created " + shape + " particle effect with " + particles.size() + " particles.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ExecutionResult.error("Error creating particle effect: " + e.getMessage());
        }
    }
}