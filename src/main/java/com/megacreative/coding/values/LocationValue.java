package com.megacreative.coding.values;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;
import java.util.Map;
import java.util.HashMap;

public class LocationValue implements DataValue {
    private final Location location;

    public LocationValue(Location location) {
        this.location = location;
    }

    public static LocationValue of(Location location) {
        return new LocationValue(location);
    }

    public static LocationValue of(String worldName, double x, double y, double z) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new LocationValue(new Location(world, x, y, z));
    }

    public Location getLocation() {
        return location;
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
        // LocationValue is immutable, so we don't allow setting values
        throw new IllegalArgumentException("LocationValue is immutable");
    }

    @Override
    public String asString() {
        if (location == null) {
            return "";
        }
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
    }

    @Override
    public Number asNumber() {
        return 0; // Location doesn't convert to a single number
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
        if (location == null) {
            return "Empty location";
        }
        return "Location in " + location.getWorld().getName() + " at " + 
               location.getX() + "," + location.getY() + "," + location.getZ();
    }

    @Override
    public DataValue clone() {
        if (location == null) {
            return new LocationValue(null);
        }
        return new LocationValue(location.clone());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "LOCATION");
        map.put("value", location);
        return map;
    }
}