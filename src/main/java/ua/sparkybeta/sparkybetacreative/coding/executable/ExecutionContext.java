package ua.sparkybeta.sparkybetacreative.coding.executable;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ExecutionContext {
    private final Map<String, Object> variables = new HashMap<>();
    private final Event bukkitEvent;

    public ExecutionContext(Event bukkitEvent) {
        this.bukkitEvent = bukkitEvent;
    }

    public Player getPlayer() {
        return (Player) variables.get("player");
    }

    public Location getLocation() {
        return (Location) variables.get("location");
    }

    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
} 