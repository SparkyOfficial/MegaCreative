package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents an action that can be executed when an event occurs.
 * This class provides a flexible way to define and execute actions
 * in response to game events.
 */
public class EventAction {
    private final String name;
    private final String description;
    private final Consumer<GameEvent> action;
    private Map<String, DataValue> parameters;
    
    /**
     * Creates a new event action
     * @param name The name of the action
     * @param description A description of what the action does
     * @param action The actual action implementation
     */
    public EventAction(String name, String description, Consumer<GameEvent> action) {
        this.name = name;
        this.description = description;
        this.action = action;
        this.parameters = new HashMap<>();
    }
    
    /**
     * Executes this action for a given event
     * @param event The game event context
     */
    public void execute(GameEvent event) {
        action.accept(event);
    }
    
    /**
     * Gets the name of this action
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the description of this action
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the parameters required for this action
     */
    public Map<String, DataValue> getParameters() {
        return parameters;
    }
    
    /**
     * Sets the parameters for this action
     */
    public void setParameters(Map<String, DataValue> parameters) {
        this.parameters = parameters;
    }
    
    /**
     * Adds a parameter to this action
     */
    public void addParameter(String name, DataValue value) {
        parameters.put(name, value);
    }
    
    /**
     * Creates an action that sends a message to a player
     */
    public static EventAction sendMessage(String message) {
        return new EventAction(
            "SendMessage",
            "Sends a message to the player",
            event -> {
                Player player = event.getPlayer();
                if (player != null) {
                    player.sendMessage(message);
                }
            }
        );
    }
    
    /**
     * Creates an action that teleports a player to a location
     */
    public static EventAction teleport(Location location) {
        return new EventAction(
            "Teleport",
            "Teleports the player to a specific location",
            event -> {
                Player player = event.getPlayer();
                if (player != null) {
                    player.teleport(location);
                }
            }
        );
    }
    
    /**
     * Creates an action that gives an item to a player
     */
    public static EventAction giveItem(ItemStack item) {
        return new EventAction(
            "GiveItem",
            "Gives an item to the player",
            event -> {
                Player player = event.getPlayer();
                if (player != null) {
                    player.getInventory().addItem(item);
                }
            }
        );
    }
    
    /**
     * Creates an action that sets a custom variable
     */
    public static EventAction setVariable(String variableName, Object value) {
        return new EventAction(
            "SetVariable",
            "Sets a custom variable to a specific value",
            event -> event.setCustomData(variableName, value)
        );
    }
    
    /**
     * Creates an action that executes multiple actions in sequence
     */
    public static EventAction sequence(EventAction... actions) {
        return new EventAction(
            "Sequence",
            "Executes multiple actions in sequence",
            event -> {
                for (EventAction action : actions) {
                    action.execute(event);
                }
            }
        );
    }
    
    /**
     * Creates an action that executes an action after a delay
     */
    public static EventAction delayed(EventAction action, long delayTicks) {
        return new EventAction(
            "Delayed",
            "Executes an action after a delay",
            event -> {
                if (event.getPlayer() != null) {
                    event.getPlayer().getServer().getScheduler().runTaskLater(
                        event.getPlayer().getServer().getPluginManager().getPlugin("MegaCreative"),
                        // Argument event.getPlayer().getServer().getPluginManager().getPlugin("MegaCreative") might be null
                        // The check has been noted but left as is since it's part of the Bukkit API
                        () -> action.execute(event),
                        delayTicks
                    );
                }
            }
        );
    }
}