package com.megacreative.coding.variables;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.*;

/**
 * Specialized DataValue for storing and working with Location objects.
 * This provides type safety and convenience methods for location operations.
 */
public class LocationValue implements DataValue, Cloneable {
    
    private Location location;
    
    public LocationValue(Location location) {
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
    public ValueType getType() {
        return ValueType.LOCATION;
    }
    
    @Override
    public Object getValue() {
        return location;
    }
    
    @Override
    public void setValue(Object value) throws IllegalArgumentException {
        if (value instanceof Location) {
            this.location = (Location) value;
        } else {
            throw new IllegalArgumentException("Value must be a Location instance");
        }
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
    public Number asNumber() throws NumberFormatException {
        // For location, we might return a hash code or some other numeric representation
        return location != null ? location.hashCode() : 0;
    }
    
    @Override
    public boolean asBoolean() {
        return location != null;
    }
    
    @Override
    public boolean isEmpty() {
        return location == null;
    }
    
    @Override
    public boolean isValid() {
        return location != null && location.getWorld() != null;
    }
    
    @Override
    public String getDescription() {
        return "Location: " + asString();
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
    public DataValue clone() {
        try {
            // Call super.clone() first to create the new instance
            LocationValue cloned = (LocationValue) super.clone();
            // Clone the Location if it exists
            if (location != null) {
                cloned.location = location.clone();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new AssertionError("Clone not supported", e);
        }
    }
    
    @Override
    public DataValue copy() {
        return clone();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        if (location != null) {
            Map<String, Object> locData = new HashMap<>();
            locData.put("world", location.getWorld() != null ? location.getWorld().getName() : null);
            locData.put("x", location.getX());
            locData.put("y", location.getY());
            locData.put("z", location.getZ());
            locData.put("yaw", location.getYaw());
            locData.put("pitch", location.getPitch());
            map.put("value", locData);
        } else {
            map.put("value", null);
        }
        return map;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LocationValue that = (LocationValue) obj;
        return location != null ? location.equals(that.location) : that.location == null;
    }
    
    @Override
    public int hashCode() {
        return location != null ? location.hashCode() : 0;
    }
}