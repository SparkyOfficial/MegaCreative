package ua.sparkycreative.worldsystem.logic;

import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class LogicBlock {
    private final LogicBlockType type;
    private final Location location;
    private final Map<String, Object> params = new HashMap<>();
    private final ArrayList<Location> outputs = new ArrayList<>();

    public LogicBlock(LogicBlockType type, Location location) {
        this.type = type;
        this.location = location;
    }

    public LogicBlockType getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public ArrayList<Location> getOutputs() {
        return outputs;
    }
} 