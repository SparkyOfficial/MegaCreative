package com.megacreative.particles;

import com.megacreative.MegaCreative;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An animated particle system that can create dynamic, time-based particle effects
 */
public class AnimatedParticleSystem {
    private final MegaCreative plugin;
    private final Player player;
    private final Location center;
    private final Particle particle;
    private final List<ParticleAnimation> animations;
    private BukkitTask animationTask;
    private boolean isRunning = false;
    
    public AnimatedParticleSystem(MegaCreative plugin, Player player, Location center, Particle particle) {
        this.plugin = plugin;
        this.player = player;
        this.center = center;
        this.particle = particle;
        this.animations = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Adds a new animation to the system
     * @param animation The animation to add
     * @return This particle system for chaining
     */
    public AnimatedParticleSystem addAnimation(ParticleAnimation animation) {
        animations.add(animation);
        return this;
    }
    
    /**
     * Starts the particle animation
     * @param durationTicks The duration in ticks (20 ticks = 1 second)
     * @return This particle system for chaining
     */
    public AnimatedParticleSystem start(long durationTicks) {
        if (isRunning) {
            stop();
        }
        
        isRunning = true;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationTicks * 50); // 50ms per tick
        
        animationTask = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime >= endTime) {
                    stop();
                    return;
                }
                
                // Calculate progress (0.0 to 1.0)
                double progress = (double) (currentTime - startTime) / (endTime - startTime);
                
                // Update and render all animations
                for (ParticleAnimation animation : animations) {
                    animation.update(progress);
                    List<Vector3D> particles = animation.getParticles(progress);
                    ParticleShapeGenerator.spawnParticles(player, particle, particles, center);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick
        
        return this;
    }
    
    /**
     * Stops the particle animation
     */
    public void stop() {
        if (animationTask != null && !animationTask.isCancelled()) {
            animationTask.cancel();
        }
        isRunning = false;
    }
    
    /**
     * Checks if the animation is currently running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Interface for particle animations
     */
    public interface ParticleAnimation {
        /**
         * Updates the animation state
         * @param progress Progress from 0.0 to 1.0
         */
        void update(double progress);
        
        /**
         * Gets the particles for the current progress
         * @param progress Progress from 0.0 to 1.0
         * @return List of particle positions
         */
        List<Vector3D> getParticles(double progress);
    }
    
    /**
     * A rotating circle animation
     */
    public static class RotatingCircleAnimation implements ParticleAnimation {
        private final Vector3D center;
        private final double radius;
        private final int particleCount;
        private final Vector3D rotationAxis;
        private final double rotationSpeed;
        private double currentRotation;
        private List<Vector3D> currentParticles;
        
        public RotatingCircleAnimation(Vector3D center, double radius, int particleCount, 
                                     Vector3D rotationAxis, double rotationSpeed) {
            this.center = center;
            this.radius = radius;
            this.particleCount = particleCount;
            this.rotationAxis = rotationAxis.normalize();
            this.rotationSpeed = rotationSpeed;
            this.currentRotation = 0;
            updateParticles();
        }
        
        @Override
        public void update(double progress) {
            currentRotation += rotationSpeed;
            updateParticles();
        }
        
        @Override
        public List<Vector3D> getParticles(double progress) {
            return currentParticles;
        }
        
        private void updateParticles() {
            currentParticles = ParticleShapeGenerator.createCircle(
                center, radius, particleCount, new Vector3D(0, 1, 0)
            );
            
            // Apply rotation
            for (int i = 0; i < currentParticles.size(); i++) {
                Vector3D particle = currentParticles.get(i);
                Vector3D relative = particle.subtract(center);
                
                // Rotate around the rotation axis
                double angle = currentRotation;
                relative = relative.rotateX(angle * rotationAxis.x)
                                  .rotateY(angle * rotationAxis.y)
                                  .rotateZ(angle * rotationAxis.z);
                
                currentParticles.set(i, center.add(relative));
            }
        }
    }
    
    /**
     * A pulsing sphere animation
     */
    public static class PulsingSphereAnimation implements ParticleAnimation {
        private final Vector3D center;
        private final double minRadius;
        private final double maxRadius;
        private final int particleCount;
        private List<Vector3D> currentParticles;
        
        public PulsingSphereAnimation(Vector3D center, double minRadius, double maxRadius, 
                                    int particleCount) {
            this.center = center;
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
            this.particleCount = particleCount;
            updateParticles(0);
        }
        
        @Override
        public void update(double progress) {
            updateParticles(progress);
        }
        
        @Override
        public List<Vector3D> getParticles(double progress) {
            return currentParticles;
        }
        
        private void updateParticles(double progress) {
            // Calculate current radius based on sine wave
            double radius = minRadius + (maxRadius - minRadius) * 
                          (0.5 + 0.5 * Math.sin(progress * Math.PI * 2));
            
            currentParticles = ParticleShapeGenerator.createSphere(
                center, radius, particleCount
            );
        }
    }
    
    /**
     * A moving wave animation
     */
    public static class MovingWaveAnimation implements ParticleAnimation {
        private final Vector3D center;
        private final double length;
        private final double amplitude;
        private final double frequency;
        private final int particleCount;
        private final int axis;
        private List<Vector3D> currentParticles;
        
        public MovingWaveAnimation(Vector3D center, double length, double amplitude, 
                                 double frequency, int particleCount, int axis) {
            this.center = center;
            this.length = length;
            this.amplitude = amplitude;
            this.frequency = frequency;
            this.particleCount = particleCount;
            this.axis = axis;
            updateParticles(0);
        }
        
        @Override
        public void update(double progress) {
            updateParticles(progress);
        }
        
        @Override
        public List<Vector3D> getParticles(double progress) {
            return currentParticles;
        }
        
        private void updateParticles(double progress) {
            // Move the wave by adjusting the phase
            double phase = progress * Math.PI * 4;
            
            currentParticles = ParticleShapeGenerator.createWave(
                center, length, amplitude, frequency + phase, particleCount, axis
            );
        }
    }
}