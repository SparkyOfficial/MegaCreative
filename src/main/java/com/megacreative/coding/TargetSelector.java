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
    
    
    private static final String SELECTOR_NEAREST = "@p";
    private static final String SELECTOR_RANDOM = "@r";
    private static final String SELECTOR_ALL = "@a";
    private static final String SELECTOR_SELF = "@s";
    
    
    private static final String ARG_RADIUS = "r";
    private static final String ARG_GAMEMODE = "m";
    private static final String ARG_NAME = "name";
    private static final String ARG_TEAM = "team";
    private static final String ARG_X = "x";
    private static final String ARG_Y = "y";
    private static final String ARG_Z = "z";
    
    
    private static final String COORD_X = "x";
    private static final String COORD_Y = "y";
    private static final String COORD_Z = "z";
    
    
    private static final Random RANDOM = new Random();
    
    public enum TargetType {
        SELF,        
        NEAREST,     
        RANDOM,      
        ALL,         
        ENTITY       
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
                
                targets.add(context.getPlayer());
                break;
                
            default:
                
                targets.add(context.getPlayer());
                break;
        }
        
        
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
        
        String[] args = selectorArgument.split(",");
        List<Player> filteredTargets = new ArrayList<>(targets);
        
        
        Map<String, String> argumentMap = new HashMap<>();
        for (String arg : args) {
            String[] parts = arg.split("=");
            if (parts.length == 2) {
                argumentMap.put(parts[0].trim(), parts[1].trim());
            }
        }
        
        
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
            case ARG_RADIUS: 
                applyRadiusFilter(targets, context, value, allArguments);
                break;
                
            case ARG_GAMEMODE: 
                applyGameModeFilter(targets, value);
                break;
                
            case ARG_NAME: 
                applyNameFilter(targets, value);
                break;
                
            case ARG_TEAM: 
                
                break;
                
            case ARG_X: 
            case ARG_Y: 
            case ARG_Z: 
                
                break;
                
            default:
                
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
            // Log the error but continue with default behavior
            e.printStackTrace();
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
        
        
        if (arguments.containsKey(ARG_X)) {
            try {
                x = Double.parseDouble(arguments.get(ARG_X));
            } catch (NumberFormatException e) {
                // Log the error but continue with default coordinate
                e.printStackTrace();
            }
        }
        
        if (arguments.containsKey(ARG_Y)) {
            try {
                y = Double.parseDouble(arguments.get(ARG_Y));
            } catch (NumberFormatException e) {
                // Log the error but continue with default coordinate
                e.printStackTrace();
            }
        }
        
        if (arguments.containsKey(ARG_Z)) {
            try {
                z = Double.parseDouble(arguments.get(ARG_Z));
            } catch (NumberFormatException e) {
                // Log the error but continue with default coordinate
                e.printStackTrace();
            }
        }
        
        
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
                    
                    break;
            }
        } catch (NumberFormatException e) {
            // Log the error but continue with default game mode behavior
            e.printStackTrace();
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
                continue; 
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
        players.remove(player); 
        
        if (players.isEmpty()) {
            return null;
        }
        
        
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
                    
                    return parseAdvancedSelector(selector);
            }
        }
        
        
        return new TargetSelector(TargetType.SELF, selector);
    }
    
    /**
     * Parses advanced selectors with arguments
     * @param selector The selector string
     * @return A TargetSelector instance
     */
    private static TargetSelector parseAdvancedSelector(String selector) {
        
        int bracketStart = selector.indexOf('[');
        int bracketEnd = selector.indexOf(']');
        
        if (bracketStart == -1 || bracketEnd == -1 || bracketEnd <= bracketStart) {
            
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
        
        
        String selectorType = selector.substring(0, bracketStart);
        
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
    
    
    public TargetType getTargetType() {
        return targetType;
    }
    
    public String getSelectorArgument() {
        return selectorArgument;
    }
}