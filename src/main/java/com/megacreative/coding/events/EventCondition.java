package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Represents a condition that must be met for an event to trigger.
 * This class provides a flexible way to define and check conditions
 * based on event data, player state, and custom variables.
 */
public class EventCondition {
    private final String name;
    private final String description;
    private final Predicate<GameEvent> condition;
    private Map<String, DataValue> requiredVariables;
    
    /**
     * Creates a new event condition
     * @param name The name of the condition
     * @param description A description of what the condition checks
     * @param condition The actual condition check implementation
     */
    public EventCondition(String name, String description, Predicate<GameEvent> condition) {
        this.name = name;
        this.description = description;
        this.condition = condition;
        this.requiredVariables = new HashMap<>();
    }
    
    /**
     * Checks if the condition is met for a given event
     * @param event The game event to check
     * @return true if the condition is met, false otherwise
     */
    public boolean check(GameEvent event) {
        return condition.test(event);
    }
    
    /**
     * Gets the name of this condition
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the description of this condition
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the map of required variables for this condition
     */
    public Map<String, DataValue> getRequiredVariables() {
        return requiredVariables;
    }
    
    /**
     * Sets the required variables for this condition
     */
    public void setRequiredVariables(Map<String, DataValue> variables) {
        this.requiredVariables = variables;
    }
    
    /**
     * Adds a required variable to this condition
     */
    public void addRequiredVariable(String name, DataValue value) {
        requiredVariables.put(name, value);
    }
    
    /**
     * Creates a condition that checks if a player has a specific permission
     */
    public static EventCondition hasPermission(String permission) {
        return new EventCondition(
            "HasPermission",
            "Checks if the player has a specific permission",
            event -> {
                Player player = event.getPlayer();
                return player != null && player.hasPermission(permission);
            }
        );
    }
    
    /**
     * Creates a condition that checks if a player has a specific game mode
     */
    public static EventCondition hasGameMode(org.bukkit.GameMode gameMode) {
        return new EventCondition(
            "HasGameMode",
            "Checks if the player is in a specific game mode",
            event -> {
                Player player = event.getPlayer();
                return player != null && player.getGameMode() == gameMode;
            }
        );
    }
    
    /**
     * Creates a condition that checks if a custom variable has a specific value
     */
    public static EventCondition checkVariable(String variableName, Object expectedValue) {
        return new EventCondition(
            "CheckVariable",
            "Checks if a custom variable matches an expected value",
            event -> {
                Object value = event.getCustomData(variableName);
                return expectedValue.equals(value);
            }
        );
    }
    
    /**
     * Creates a condition that combines multiple conditions with AND logic
     */
    public static EventCondition and(EventCondition... conditions) {
        return new EventCondition(
            "AND",
            "Combines multiple conditions with AND logic",
            event -> {
                for (EventCondition condition : conditions) {
                    if (!condition.check(event)) {
                        return false;
                    }
                }
                return true;
            }
        );
    }
    
    /**
     * Creates a condition that combines multiple conditions with OR logic
     */
    public static EventCondition or(EventCondition... conditions) {
        return new EventCondition(
            "OR",
            "Combines multiple conditions with OR logic",
            event -> {
                for (EventCondition condition : conditions) {
                    if (condition.check(event)) {
                        return true;
                    }
                }
                return false;
            }
        );
    }
    
    /**
     * Creates a condition that negates another condition
     */
    public static EventCondition not(EventCondition condition) {
        return new EventCondition(
            "NOT",
            "Negates the result of another condition",
            event -> !condition.check(event)
        );
    }
}