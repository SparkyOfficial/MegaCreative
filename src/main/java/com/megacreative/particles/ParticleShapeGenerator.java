package com.megacreative.particles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates various particle shapes using vector math
 */
public class ParticleShapeGenerator {
    
    /**
     * Creates a circle of particles
     * @param center The center location
     * @param radius The radius of the circle
     * @param particleCount Number of particles in the circle
     * @param normal The normal vector of the circle plane
     * @return List of particle locations
     */
    public static List<Vector3D> createCircle(Vector3D center, double radius, int particleCount, Vector3D normal) {
        List<Vector3D> particles = new ArrayList<>();
        
        
        normal = normal.normalize();
        
        
        Vector3D u, v;
        if (Math.abs(normal.z) < 0.9) {
            u = new Vector3D(0, 0, 1).cross(normal).normalize();
        } else {
            u = new Vector3D(1, 0, 0).cross(normal).normalize();
        }
        v = normal.cross(u).normalize();
        
        
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            Vector3D point = u.multiply(Math.cos(angle) * radius)
                              .add(v.multiply(Math.sin(angle) * radius));
            particles.add(center.add(point));
        }
        
        return particles;
    }
    
    /**
     * Creates a sphere of particles
     * @param center The center location
     * @param radius The radius of the sphere
     * @param particleCount Number of particles in the sphere
     * @return List of particle locations
     */
    public static List<Vector3D> createSphere(Vector3D center, double radius, int particleCount) {
        List<Vector3D> particles = new ArrayList<>();
        
        
        double goldenRatio = (1 + Math.sqrt(5)) / 2;
        double angleIncrement = Math.PI * 2 * goldenRatio;
        
        for (int i = 0; i < particleCount; i++) {
            double y = 1 - (i / (double)(particleCount - 1)) * 2;  
            double radiusAtY = Math.sqrt(1 - y * y);  
            
            double theta = angleIncrement * i;  
            
            double x = Math.cos(theta) * radiusAtY;
            double z = Math.sin(theta) * radiusAtY;
            
            particles.add(new Vector3D(
                center.x + x * radius,
                center.y + y * radius,
                center.z + z * radius
            ));
        }
        
        return particles;
    }
    
    /**
     * Creates a helix/spiral of particles
     * @param center The center location
     * @param radius The radius of the helix
     * @param height The height of the helix
     * @param turns Number of turns in the helix
     * @param particleCount Number of particles in the helix
     * @return List of particle locations
     */
    public static List<Vector3D> createHelix(Vector3D center, double radius, double height, 
                                           double turns, int particleCount) {
        List<Vector3D> particles = new ArrayList<>();
        
        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / (particleCount - 1);  
            double angle = 2 * Math.PI * turns * t;
            
            double x = center.x + Math.cos(angle) * radius;
            double y = center.y + t * height;
            double z = center.z + Math.sin(angle) * radius;
            
            particles.add(new Vector3D(x, y, z));
        }
        
        return particles;
    }
    
    /**
     * Creates a wave pattern of particles
     * @param center The center location
     * @param length The length of the wave
     * @param amplitude The amplitude of the wave
     * @param frequency The frequency of the wave
     * @param particleCount Number of particles in the wave
     * @param axis The axis along which the wave extends (0=x, 1=y, 2=z)
     * @return List of particle locations
     */
    public static List<Vector3D> createWave(Vector3D center, double length, double amplitude, 
                                          double frequency, int particleCount, int axis) {
        List<Vector3D> particles = new ArrayList<>();
        
        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / (particleCount - 1);  
            double pos = -length/2 + t * length;
            double wave = amplitude * Math.sin(2 * Math.PI * frequency * t);
            
            Vector3D point;
            switch (axis) {
                case 0: 
                    point = new Vector3D(center.x + pos, center.y + wave, center.z);
                    break;
                case 1: 
                    point = new Vector3D(center.x, center.y + pos, center.z + wave);
                    break;
                case 2: 
                    point = new Vector3D(center.x + wave, center.y, center.z + pos);
                    break;
                default:
                    point = new Vector3D(center.x + pos, center.y + wave, center.z);
            }
            
            particles.add(point);
        }
        
        return particles;
    }
    
    /**
     * Creates a torus (donut) shape of particles
     * @param center The center location
     * @param majorRadius The major radius (distance from center to tube center)
     * @param minorRadius The minor radius (radius of the tube)
     * @param majorParticleCount Number of particles around the major circle
     * @param minorParticleCount Number of particles around the minor circle
     * @return List of particle locations
     */
    public static List<Vector3D> createTorus(Vector3D center, double majorRadius, double minorRadius,
                                           int majorParticleCount, int minorParticleCount) {
        List<Vector3D> particles = new ArrayList<>();
        
        for (int i = 0; i < majorParticleCount; i++) {
            double majorAngle = 2 * Math.PI * i / majorParticleCount;
            
            
            double tubeCenterX = center.x + Math.cos(majorAngle) * majorRadius;
            double tubeCenterZ = center.z + Math.sin(majorAngle) * majorRadius;
            Vector3D tubeCenter = new Vector3D(tubeCenterX, center.y, tubeCenterZ);
            
            
            for (int j = 0; j < minorParticleCount; j++) {
                double minorAngle = 2 * Math.PI * j / minorParticleCount;
                
                
                double relX = Math.cos(majorAngle) * Math.cos(minorAngle) * minorRadius;
                double relY = Math.sin(minorAngle) * minorRadius;
                double relZ = Math.sin(majorAngle) * Math.cos(minorAngle) * minorRadius;
                
                particles.add(new Vector3D(
                    tubeCenter.x + relX,
                    tubeCenter.y + relY,
                    tubeCenter.z + relZ
                ));
            }
        }
        
        return particles;
    }
    
    /**
     * Spawns particles at the given locations
     * @param player The player to spawn particles for
     * @param particle The particle type to spawn
     * @param locations The locations to spawn particles at
     */
    public static void spawnParticles(Player player, Particle particle, List<Vector3D> locations) {
        for (Vector3D location : locations) {
            player.spawnParticle(
                particle,
                location.x, location.y, location.z,
                1, 0, 0, 0, 0
            );
        }
    }
    
    /**
     * Spawns particles at the given locations with an offset from a Bukkit Location
     * @param player The player to spawn particles for
     * @param particle The particle type to spawn
     * @param locations The locations to spawn particles at (relative to offset)
     * @param offset The base location to offset from
     */
    public static void spawnParticles(Player player, Particle particle, List<Vector3D> locations, Location offset) {
        for (Vector3D location : locations) {
            player.spawnParticle(
                particle,
                offset.getX() + location.x, 
                offset.getY() + location.y, 
                offset.getZ() + location.z,
                1, 0, 0, 0, 0
            );
        }
    }
}