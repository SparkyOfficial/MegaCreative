package com.megacreative.coding;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.LocationValue;
import com.megacreative.coding.values.PlayerValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterResolver {
    private final ExecutionContext context;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    public ParameterResolver(ExecutionContext context) {
        this.context = context;
    }

    public DataValue resolve(ExecutionContext context, DataValue value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        String text = value.asString();
        if (text == null || !text.contains("${")) {
            return value;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = resolvePlaceholder(placeholder, context);
            matcher.appendReplacement(result, replacement != null ? replacement : matcher.group(0));
        }
        matcher.appendTail(result);

        return DataValue.of(result.toString());
    }

    private String resolvePlaceholder(String placeholder, ExecutionContext context) {
        // Handle player-related placeholders
        Player player = context.getPlayer();
        if (player != null) {
            switch (placeholder.toLowerCase()) {
                case "player_name":
                    return player.getName();
                case "player_display_name":
                    return player.getDisplayName();
                case "player_uuid":
                    return player.getUniqueId().toString();
                case "player_world":
                    return player.getWorld().getName();
                case "player_x":
                    return String.valueOf(player.getLocation().getX());
                case "player_y":
                    return String.valueOf(player.getLocation().getY());
                case "player_z":
                    return String.valueOf(player.getLocation().getZ());
            }
        }

        // Handle location-related placeholders
        if (context.getBlockLocation() != null) {
            Location location = context.getBlockLocation();
            switch (placeholder.toLowerCase()) {
                case "block_x":
                    return String.valueOf(location.getX());
                case "block_y":
                    return String.valueOf(location.getY());
                case "block_z":
                    return String.valueOf(location.getZ());
                case "block_world":
                    return location.getWorld().getName();
            }
        }

        // Handle variable placeholders
        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager != null) {
            // We need to get the world name from the context
            String worldName = "global"; // Default to global scope
            if (context.getCreativeWorld() != null) {
                worldName = context.getCreativeWorld().getWorldName();
            }
            
            DataValue variableValue = variableManager.getVariable(worldName, IVariableManager.VariableScope.GLOBAL, placeholder);
            if (variableValue != null) {
                return variableValue.asString();
            }
        }

        // Handle built-in placeholders
        switch (placeholder.toLowerCase()) {
            case "timestamp":
                return String.valueOf(System.currentTimeMillis());
            case "random":
                return String.valueOf(Math.random());
        }

        // If we can't resolve it, return null
        return null;
    }
}