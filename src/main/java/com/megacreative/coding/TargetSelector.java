package com.megacreative.coding;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

/**
 * Target selector system for actions, similar to Minecraft's @a, @p, @r, @s selectors
 */
public class TargetSelector {
    
    // Constants for selector types
    private static final String SELECTOR_NEAREST = "@p";
    private static final String SELECTOR_RANDOM = "@r";
    private static final String SELECTOR_ALL = "@a";
    private static final String SELECTOR_SELF = "@s";
    
    // Constants for selector arguments
    private static final String ARG_RADIUS = "r";
    private static final String ARG_GAMEMODE = "m";
    private static final String ARG_NAME = "name";
    private static final String ARG_TEAM = "team";
    private static final String ARG_X = "x";
    private static final String ARG_Y = "y";
    private static final String ARG_Z = "z";
    
    // Additional constants for selector argument values
    private static final String COORD_X = "x";
    private static final String COORD_Y = "y";
    private static final String COORD_Z = "z";
    
    // Random instance for generating random numbers
    private static final Random RANDOM = new Random();
    
    public enum TargetType {
        SELF,        // @s - the executing player
        NEAREST,     // @p - nearest player
        RANDOM,      // @r - random player
        ALL,         // @a - all players
        ENTITY       // Specific entity type
    }
    
    private final TargetType targetType;
    private final String selectorArgument;
    
    public TargetSelector(TargetType targetType, String selectorArgument) {
        this.targetType = targetType;
        this.selectorArgument = selectorArgument;
    }
    
    /**
     * Resolves the target selector to a list of players
     * @param context The execution context
     * @return List of target players
     */
    public List<Player> resolveTargets(ExecutionContext context) {
        List<Player> targets = new ArrayList<>();
        
        if (context.getPlayer() == null) {
            return targets;
        }
        
        switch (targetType) {
            case SELF:
                targets.add(context.getPlayer());
                break;
                
            case NEAREST:
                Player nearest = findNearestPlayer(context.getPlayer());
                if (nearest != null) {
                    targets.add(nearest);
                }
                break;
                
            case RANDOM:
                Player random = findRandomPlayer(context.getPlayer());
                if (random != null) {
                    targets.add(random);
                }
                break;
                
            case ALL:
                targets.addAll(context.getPlayer().getWorld().getPlayers());
                break;
                
            case ENTITY:
                // Handle entity selection if needed
                targets.add(context.getPlayer());
                break;
                
            default:
                // Default case - add the executing player
                targets.add(context.getPlayer());
                break;
        }
        
        // Apply filters based on selector arguments
        if (selectorArgument != null && !selectorArgument.isEmpty()) {
            targets = applyFilters(targets, context);
        }
        
        return targets;
    }
    
    /**
     * Applies filters to the target list based on selector arguments
     * @param targets The list of potential targets
     * @param context The execution context
     * @return Filtered list of targets
     */
    private List<Player> applyFilters(List<Player> targets, ExecutionContext context) {
        // Parse the selector arguments
        String[] args = selectorArgument.split(",");
        List<Player> filteredTargets = new ArrayList<>(targets);
        
        // Parse arguments into a map for easier access
        Map<String, String> argumentMap = new HashMap<>();
        for (String arg : args) {
            String[] parts = arg.split("=");
            if (parts.length == 2) {
                argumentMap.put(parts[0].trim(), parts[1].trim());
            }
        }
        
        // Process each argument
        for (Map.Entry<String, String> entry : argumentMap.entrySet()) {
            processArgument(entry.getKey(), entry.getValue(), filteredTargets, context, argumentMap);
        }
        
        return filteredTargets;
    }
    
    /**
     * Process a single selector argument
     * @param key The argument key (e.g., "r" or "name")
     * @param value The argument value (e.g., "5" or "Steve")
     * @param targets The list of targets to filter
     * @param context The execution context
     * @param allArguments All parsed arguments for coordinate-based calculations
     */
    private void processArgument(String key, String value, List<Player> targets, ExecutionContext context, Map<String, String> allArguments) {
        switch (key) {
            case ARG_RADIUS: // Radius
                applyRadiusFilter(targets, context, value, allArguments);
                break;
                
            case ARG_GAMEMODE: // Game mode
                applyGameModeFilter(targets, value);
                break;
                
            case ARG_NAME: // Player name
                applyNameFilter(targets, value);
                break;
                
            case ARG_TEAM: // Team name
                // This would require scoreboard integration
                break;
                
            case ARG_X: // X coordinate
            case ARG_Y: // Y coordinate
            case ARG_Z: // Z coordinate
                // These are handled with 'r' parameter and stored in allArguments
                break;
                
            default:
                // Default case - no action needed for unknown arguments
                break;
        }
    }
    
    /**
     * Apply radius filter to targets using either player location or specified coordinates
     */
    private void applyRadiusFilter(List<Player> targets, ExecutionContext context, String radiusValue, Map<String, String> allArguments) {
        try {
            double radius = Double.parseDouble(radiusValue);
            Location referenceLocation = getReferenceLocation(context, allArguments);
            
            targets.removeIf(player -> 
                player.getLocation().distance(referenceLocation) > radius);
        } catch (NumberFormatException e) {
            // Invalid radius, ignore
        }
    }
    
    /**
     * Get reference location for distance calculations.
     * Uses specified coordinates if provided, otherwise uses player location.
     */
    private Location getReferenceLocation(ExecutionContext context, Map<String, String> arguments) {
        Location playerLocation = context.getPlayer().getLocation();
        double x = playerLocation.getX();
        double y = playerLocation.getY();
        double z = playerLocation.getZ();
        
        // Override coordinates if specified in arguments
        if (arguments.containsKey(ARG_X)) {
            try {
                x = Double.parseDouble(arguments.get(ARG_X));
            } catch (NumberFormatException e) {
                // Keep player's X coordinate
            }
        }
        
        if (arguments.containsKey(ARG_Y)) {
            try {
                y = Double.parseDouble(arguments.get(ARG_Y));
            } catch (NumberFormatException e) {
                // Keep player's Y coordinate
            }
        }
        
        if (arguments.containsKey(ARG_Z)) {
            try {
                z = Double.parseDouble(arguments.get(ARG_Z));
            } catch (NumberFormatException e) {
                // Keep player's Z coordinate
            }
        }
        
        // Create new location with the determined coordinates
        return new Location(playerLocation.getWorld(), x, y, z);
    }
    
    /**
     * Apply game mode filter to targets
     */
    private void applyGameModeFilter(List<Player> targets, String gameModeValue) {
        try {
            int gameMode = Integer.parseInt(gameModeValue);
            switch (gameMode) {
                case 0:
                    targets.removeIf(player -> player.getGameMode() != org.bukkit.GameMode.SURVIVAL);
                    break;
                case 1:
                    targets.removeIf(player -> player.getGameMode() != org.bukkit.GameMode.CREATIVE);
                    break;
                case 2:
                    targets.removeIf(player -> player.getGameMode() != org.bukkit.GameMode.ADVENTURE);
                    break;
                case 3:
                    targets.removeIf(player -> player.getGameMode() != org.bukkit.GameMode.SPECTATOR);
                    break;
                default:
                    // Invalid game mode, ignore
                    break;
            }
        } catch (NumberFormatException e) {
            // Invalid game mode, ignore
        }
    }
    
    /**
     * Apply name filter to targets
     */
    private void applyNameFilter(List<Player> targets, String nameValue) {
        targets.removeIf(player -> 
            !player.getName().equals(nameValue));
    }
    
    /**
     * Finds the nearest player to the given player
     * @param player The reference player
     * @return The nearest player, or null if none found
     */
    private Player findNearestPlayer(Player player) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        
        Location playerLocation = player.getLocation();
        
        for (Player target : player.getWorld().getPlayers()) {
            if (target.equals(player)) {
                continue; // Skip the reference player
            }
            
            double distance = playerLocation.distance(target.getLocation());
            if (distance < nearestDistance) {
                nearest = target;
                nearestDistance = distance;
            }
        }
        
        return nearest;
    }
    
    /**
     * Finds a random player (excluding the reference player)
     * @param player The reference player
     * @return A random player, or null if none found
     */
    private Player findRandomPlayer(Player player) {
        List<Player> players = new ArrayList<>(player.getWorld().getPlayers());
        players.remove(player); // Remove the reference player
        
        if (players.isEmpty()) {
            return null;
        }
        
        // Using java.util.Random.nextInt() instead of Math.random()
        int randomIndex = RANDOM.nextInt(players.size());
        return players.get(randomIndex);
    }
    
    /**
     * Parses a target selector string (e.g., "@p", "@a", "@r", "@s")
     * @param selector The selector string
     * @return A TargetSelector instance
     */
    public static TargetSelector parse(String selector) {
        if (selector == null || selector.isEmpty()) {
            return new TargetSelector(TargetType.SELF, null);
        }
        
        // Handle Minecraft-style selectors
        if (selector.startsWith("@")) {
            switch (selector) {
                case SELECTOR_NEAREST:
                    return new TargetSelector(TargetType.NEAREST, null);
                case SELECTOR_RANDOM:
                    return new TargetSelector(TargetType.RANDOM, null);
                case SELECTOR_ALL:
                    return new TargetSelector(TargetType.ALL, null);
                case SELECTOR_SELF:
                    return new TargetSelector(TargetType.SELF, null);
                default:
                    // Handle selectors with arguments like @p[x=10,y=20,z=30,r=5]
                    return parseAdvancedSelector(selector);
            }
        }
        
        // Default to self
        return new TargetSelector(TargetType.SELF, selector);
    }
    
    /**
     * Parses advanced selectors with arguments
     * @param selector The selector string
     * @return A TargetSelector instance
     */
    private static TargetSelector parseAdvancedSelector(String selector) {
        // Parse selectors with arguments like @p[x=10,y=20,z=30,r=5]
        int bracketStart = selector.indexOf('[');
        int bracketEnd = selector.indexOf(']');
        
        if (bracketStart == -1 || bracketEnd == -1 || bracketEnd <= bracketStart) {
            // Invalid format, return basic selector
            if (selector.startsWith(SELECTOR_NEAREST)) {
                return new TargetSelector(TargetType.NEAREST, selector);
            } else if (selector.startsWith(SELECTOR_RANDOM)) {
                return new TargetSelector(TargetType.RANDOM, selector);
            } else if (selector.startsWith(SELECTOR_ALL)) {
                return new TargetSelector(TargetType.ALL, selector);
            } else if (selector.startsWith(SELECTOR_SELF)) {
                return new TargetSelector(TargetType.SELF, selector);
            }
            return new TargetSelector(TargetType.SELF, selector);
        }
        
        // Extract the selector type
        String selectorType = selector.substring(0, bracketStart);
        // Extract the arguments part
        String arguments = selector.substring(bracketStart + 1, bracketEnd);
        
        TargetType targetType;
        switch (selectorType) {
            case SELECTOR_NEAREST:
                targetType = TargetType.NEAREST;
                break;
            case SELECTOR_RANDOM:
                targetType = TargetType.RANDOM;
                break;
            case SELECTOR_ALL:
                targetType = TargetType.ALL;
                break;
            case SELECTOR_SELF:
                targetType = TargetType.SELF;
                break;
            default:
                targetType = TargetType.SELF;
                break;
        }
        
        return new TargetSelector(targetType, arguments);
    }
    
    // Getters
    public TargetType getTargetType() {
        return targetType;
    }
    
    public String getSelectorArgument() {
        return selectorArgument;
    }
}