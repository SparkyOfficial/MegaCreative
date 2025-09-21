package com.megacreative.coding.actions.particle;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Represents a particle effect that can be played at a location
 */
public class ParticleEffect {
    private final Particle particle;
    private final int count;
    private final double offset;
    private final double spread;
    private final double speed;
    private final String pattern;
    
    public ParticleEffect(Particle particle, int count, double offset, double spread, double speed, String pattern) {
        this.particle = particle;
        this.count = count;
        this.offset = offset;
        this.spread = spread;
        this.speed = speed;
        this.pattern = pattern;
    }
    
    /**
     * Plays the particle effect at the given location
     */
    public void play(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        
        // Simple particle spawning - you can expand this with more complex patterns
        location.getWorld().spawnParticle(particle, location, count, 
            spread, spread, spread, speed);
    }
    
    // Getters
    public Particle getParticle() { return particle; }
    public int getCount() { return count; }
    public double getOffset() { return offset; }
    public double getSpread() { return spread; }
    public double getSpeed() { return speed; }
    public String getPattern() { return pattern; }
}