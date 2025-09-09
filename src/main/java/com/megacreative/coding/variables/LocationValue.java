package com.megacreative.coding.variables;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Specialized DataValue for storing and working with Location objects.
 * This provides type safety and convenience methods for location operations.
 */
public class LocationValue extends DataValue {
    
    private final Location location;
    
    public LocationValue(Location location) {
        super(location);
        this.location = location;
    }
    
    public LocationValue(double x, double y, double z, World world) {
        this(new Location(world, x, y, z));
    }
    
    public LocationValue(String worldName, double x, double y, double z) {
        this(new Location(null, x, y, z));
        // Note: World will need to be set separately if not available at construction time
    }
    
    @Override
    public Location asLocation() {
        return location;
    }
    
    @Override
    public boolean isLocation() {
        return true;
    }
    
    @Override
    public String asString() {
        if (location == null) {
            return "null";
        }
        return String.format("Location{world=%s, x=%.2f, y=%.2f, z=%.2f, yaw=%.2f, pitch=%.2f}", 
            location.getWorld() != null ? location.getWorld().getName() : "null",
            location.getX(), location.getY(), location.getZ(), 
            location.getYaw(), location.getPitch());
    }
    
    @Override
    public double asNumber() {
        // For location, we might return a hash code or some other numeric representation
        return location != null ? location.hashCode() : 0;
    }
    
    @Override
    public boolean asBoolean() {
        return location != null;
    }
    
    // Convenience methods for location operations
    public double getX() {
        return location != null ? location.getX() : 0;
    }
    
    public double getY() {
        return location != null ? location.getY() : 0;
    }
    
    public double getZ() {
        return location != null ? location.getZ() : 0;
    }
    
    public World getWorld() {
        return location != null ? location.getWorld() : null;
    }
    
    public LocationValue add(double x, double y, double z) {
        if (location == null) {
            return this;
        }
        Location newLocation = location.clone();
        newLocation.add(x, y, z);
        return new LocationValue(newLocation);
    }
    
    public LocationValue subtract(double x, double y, double z) {
        if (location == null) {
            return this;
        }
        Location newLocation = location.clone();
        newLocation.subtract(x, y, z);
        return new LocationValue(newLocation);
    }
    
    public double distance(LocationValue other) {
        if (location == null || other == null || other.location == null) {
            return Double.MAX_VALUE;
        }
        return location.distance(other.location);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        
        LocationValue that = (LocationValue) obj;
        return location != null ? location.equals(that.location) : that.location == null;
    }
    
    @Override
    public int hashCode() {
        return location != null ? location.hashCode() : 0;
    }
}