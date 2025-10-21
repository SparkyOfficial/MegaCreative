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
 * 
 * Анимированная система частиц, которая может создавать динамические, основанные на времени эффекты частиц
 * 
 * @author Андрій Будильников
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
     * 
     * Добавляет новую анимацию в систему
     * @param animation Анимация для добавления
     * @return Эта система частиц для цепочки вызовов
     */
    public AnimatedParticleSystem addAnimation(ParticleAnimation animation) {
        animations.add(animation);
        return this;
    }
    
    /**
     * Starts the particle animation
     * @param durationTicks The duration in ticks (20 ticks = 1 second)
     * @return This particle system for chaining
     * 
     * Запускает анимацию частиц
     * @param durationTicks Длительность в тиках (20 тиков = 1 секунда)
     * @return Эта система частиц для цепочки вызовов
     */
    public AnimatedParticleSystem start(long durationTicks) {
        if (isRunning) {
            stop();
        }
        
        isRunning = true;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationTicks * 50); 
        
        animationTask = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime >= endTime) {
                    stop();
                    return;
                }
                
                
                double progress = (double) (currentTime - startTime) / (endTime - startTime);
                
                
                for (ParticleAnimation animation : animations) {
                    animation.update(progress);
                    List<Vector3D> particles = animation.getParticles(progress);
                    ParticleShapeGenerator.spawnParticles(player, particle, particles, center);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); 
        
        return this;
    }
    
    /**
     * Stops the particle animation
     * 
     * Останавливает анимацию частиц
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
     * 
     * Проверяет, запущена ли анимация в данный момент
     * @return true, если запущена, false в противном случае
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Interface for particle animations
     * 
     * Интерфейс для анимаций частиц
     */
    public interface ParticleAnimation {
        /**
         * Updates the animation state
         * @param progress Progress from 0.0 to 1.0
         * 
         * Обновляет состояние анимации
         * @param progress Прогресс от 0.0 до 1.0
         */
        void update(double progress);
        
        /**
         * Gets the particles for the current progress
         * @param progress Progress from 0.0 to 1.0
         * @return List of particle positions
         * 
         * Получает частицы для текущего прогресса
         * @param progress Прогресс от 0.0 до 1.0
         * @return Список позиций частиц
         */
        List<Vector3D> getParticles(double progress);
    }
    
    /**
     * A rotating circle animation
     * 
     * Анимация вращающегося круга
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
            
            
            for (int i = 0; i < currentParticles.size(); i++) {
                Vector3D particle = currentParticles.get(i);
                Vector3D relative = particle.subtract(center);
                
                
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
     * 
     * Анимация пульсирующей сферы
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
            
            double phase = progress * Math.PI * 4;
            
            currentParticles = ParticleShapeGenerator.createWave(
                center, length, amplitude, frequency + phase, particleCount, axis
            );
        }
    }
}