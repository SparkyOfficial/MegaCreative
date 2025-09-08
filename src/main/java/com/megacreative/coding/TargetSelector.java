package com.megacreative.coding;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import java.util.List;
import java.util.ArrayList;

/**
 * Target selector system for actions, similar to Minecraft's @a, @p, @r, @s selectors
 */
public class TargetSelector {
    
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
        }
        
        return targets;
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
        
        int randomIndex = (int) (Math.random() * players.size());
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
                case "@p":
                    return new TargetSelector(TargetType.NEAREST, null);
                case "@r":
                    return new TargetSelector(TargetType.RANDOM, null);
                case "@a":
                    return new TargetSelector(TargetType.ALL, null);
                case "@s":
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
        // For now, just return a basic selector
        // In a full implementation, we would parse the arguments
        if (selector.startsWith("@p[")) {
            return new TargetSelector(TargetType.NEAREST, selector);
        } else if (selector.startsWith("@r[")) {
            return new TargetSelector(TargetType.RANDOM, selector);
        } else if (selector.startsWith("@a[")) {
            return new TargetSelector(TargetType.ALL, selector);
        } else if (selector.startsWith("@s[")) {
            return new TargetSelector(TargetType.SELF, selector);
        }
        
        return new TargetSelector(TargetType.SELF, selector);
    }
    
    // Getters
    public TargetType getTargetType() {
        return targetType;
    }
    
    public String getSelectorArgument() {
        return selectorArgument;
    }
}