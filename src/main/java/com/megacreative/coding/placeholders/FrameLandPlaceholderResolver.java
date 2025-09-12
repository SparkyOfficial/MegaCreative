package com.megacreative.coding.placeholders;

import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Bukkit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 🎆 ENHANCED: FrameLand-style placeholder system with apple[variable]~ syntax
 * Supports multiple placeholder formats:
 * - FrameLand style: apple[variable]~ 
 * - Modern style: ${variable}
 * - Classic style: %variable%
 * - Advanced features: prefix[variable|default]~, math[variable+5]~
 */
public class FrameLandPlaceholderResolver {
    
    // Placeholder patterns
    private static final Pattern FRAMELAND_PATTERN = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\[([^\\]]+)\\]~");
    private static final Pattern MODERN_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final Pattern CLASSIC_PATTERN = Pattern.compile("%([^%]+)%");
    
    // Built-in prefix handlers
    private static final Map<String, PlaceholderHandler> BUILTIN_HANDLERS = new HashMap<>();
    
    static {
        // Core FrameLand-style handlers
        BUILTIN_HANDLERS.put("apple", new VariablePlaceholderHandler()); // apple[variable]~
        BUILTIN_HANDLERS.put("var", new VariablePlaceholderHandler());   // var[variable]~
        BUILTIN_HANDLERS.put("player", new PlayerPlaceholderHandler()); // player[name]~
        BUILTIN_HANDLERS.put("world", new WorldPlaceholderHandler());   // world[name]~
        BUILTIN_HANDLERS.put("math", new MathPlaceholderHandler());     // math[variable+5]~
        BUILTIN_HANDLERS.put("time", new TimePlaceholderHandler());     // time[format]~
        BUILTIN_HANDLERS.put("random", new RandomPlaceholderHandler()); // random[1-100]~
        BUILTIN_HANDLERS.put("location", new LocationPlaceholderHandler()); // location[x]~
        BUILTIN_HANDLERS.put("server", new ServerPlaceholderHandler()); // server[online]~
        BUILTIN_HANDLERS.put("color", new ColorPlaceholderHandler());   // color[red]~
        BUILTIN_HANDLERS.put("format", new FormatPlaceholderHandler()); // format[number|2]~
    }
    
    /**
     * Resolves all placeholders in text using FrameLand-style syntax
     */
    public static String resolvePlaceholders(String text, ExecutionContext context) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String result = text;
        
        // Process FrameLand-style placeholders first (highest priority)
        result = resolveFrameLandPlaceholders(result, context);
        
        // Process modern ${} placeholders
        result = resolveModernPlaceholders(result, context);
        
        // Process classic %% placeholders (lowest priority)
        result = resolveClassicPlaceholders(result, context);
        
        return result;
    }
    
    /**
     * Resolves FrameLand-style placeholders: prefix[content]~
     */
    private static String resolveFrameLandPlaceholders(String text, ExecutionContext context) {
        Matcher matcher = FRAMELAND_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String prefix = matcher.group(1);
            String content = matcher.group(2);
            
            // Handle default values: variable|default
            String defaultValue = "";
            if (content.contains("|")) {
                String[] parts = content.split("\\|", 2);
                content = parts[0];
                defaultValue = parts[1];
            }
            
            // Get handler for prefix
            PlaceholderHandler handler = BUILTIN_HANDLERS.get(prefix.toLowerCase());
            String replacement;
            
            if (handler != null) {
                replacement = handler.resolve(content, context, defaultValue);
            } else {
                // Unknown prefix, treat as variable
                replacement = BUILTIN_HANDLERS.get("apple").resolve(prefix + "." + content, context, defaultValue);
            }
            
            if (replacement == null) {
                replacement = defaultValue.isEmpty() ? matcher.group(0) : defaultValue;
            }
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Resolves modern ${} placeholders
     */
    private static String resolveModernPlaceholders(String text, ExecutionContext context) {
        Matcher matcher = MODERN_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = resolveSimplePlaceholder(placeholder, context);
            
            if (replacement == null) {
                replacement = matcher.group(0); // Keep original if not found
            }
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Resolves classic %% placeholders
     */
    private static String resolveClassicPlaceholders(String text, ExecutionContext context) {
        Matcher matcher = CLASSIC_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = resolveSimplePlaceholder(placeholder, context);
            
            if (replacement == null) {
                replacement = matcher.group(0); // Keep original if not found
            }
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Resolves simple placeholders (backwards compatibility)
     */
    private static String resolveSimplePlaceholder(String placeholder, ExecutionContext context) {
        Player player = context.getPlayer();
        
        // Player-related placeholders
        if (player != null) {
            switch (placeholder.toLowerCase()) {
                case "player":
                case "player_name":
                    return player.getName();
                case "player_display_name":
                    return player.getDisplayName();
                case "player_uuid":
                    return player.getUniqueId().toString();
                case "world":
                case "player_world":
                    return player.getWorld().getName();
                case "x":
                case "player_x":
                    return String.valueOf(player.getLocation().getBlockX());
                case "y":
                case "player_y":
                    return String.valueOf(player.getLocation().getBlockY());
                case "z":
                case "player_z":
                    return String.valueOf(player.getLocation().getBlockZ());
            }
        }
        
        // Variable placeholders
        VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
        if (variableManager != null && player != null) {
            DataValue value = variableManager.getPlayerVariable(player.getUniqueId(), placeholder);
            if (value != null && !value.isEmpty()) {
                return value.asString();
            }
        }
        
        // Built-in placeholders
        switch (placeholder.toLowerCase()) {
            case "timestamp":
                return String.valueOf(System.currentTimeMillis());
            case "random":
                return String.valueOf(Math.random());
            case "server_online":
                return String.valueOf(Bukkit.getOnlinePlayers().size());
            case "server_max":
                return String.valueOf(Bukkit.getMaxPlayers());
        }
        
        return null;
    }
    
    /**
     * Interface for placeholder handlers
     */
    public interface PlaceholderHandler {
        String resolve(String content, ExecutionContext context, String defaultValue);
    }
    
    /**
     * Variable placeholder handler: apple[variable]~, var[variable]~
     */
    public static class VariablePlaceholderHandler implements PlaceholderHandler {
        @Override
        public String resolve(String content, ExecutionContext context, String defaultValue) {
            Player player = context.getPlayer();
            if (player == null) {
                return defaultValue;
            }
            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            if (variableManager == null) {
                return defaultValue;
            }
            
            DataValue value = variableManager.getPlayerVariable(player.getUniqueId(), content);
            return value != null && !value.isEmpty() ? value.asString() : defaultValue;
        }
    }
    
    /**
     * Player placeholder handler: player[name]~, player[uuid]~, player[world]~
     */
    public static class PlayerPlaceholderHandler implements PlaceholderHandler {
        @Override
        public String resolve(String content, ExecutionContext context, String defaultValue) {
            Player player = context.getPlayer();
            if (player == null) {
                return defaultValue;
            }
            
            switch (content.toLowerCase()) {
                case "name":
                    return player.getName();
                case "display_name":
                case "displayname":
                    return player.getDisplayName();
                case "uuid":
                    return player.getUniqueId().toString();
                case "world":
                    return player.getWorld().getName();
                case "health":
                    return String.valueOf((int) player.getHealth());
                case "max_health":
                case "maxhealth":
                    return String.valueOf((int) player.getMaxHealth());
                case "food":
                case "hunger":
                    return String.valueOf(player.getFoodLevel());
                case "level":
                    return String.valueOf(player.getLevel());
                case "exp":
                case "experience":
                    return String.valueOf(player.getExp());
                case "gamemode":
                    return player.getGameMode().name().toLowerCase();
                default:
                    return defaultValue;
            }
        }
    }
    
    /**
     * World placeholder handler: world[name]~, world[time]~
     */
    public static class WorldPlaceholderHandler implements PlaceholderHandler {
        @Override
        public String resolve(String content, ExecutionContext context, String defaultValue) {
            Player player = context.getPlayer();
            if (player == null) {
                return defaultValue;
            }
            
            switch (content.toLowerCase()) {
                case "name":
                    return player.getWorld().getName();
                case "time":
                    return String.valueOf(player.getWorld().getTime());
                case "weather":
                    return player.getWorld().hasStorm() ? "storm" : "clear";
                case "difficulty":
                    return player.getWorld().getDifficulty().name().toLowerCase();
                case "seed":
                    return String.valueOf(player.getWorld().getSeed());
                default:
                    return defaultValue;
            }
        }
    }
    
    /**
     * Math placeholder handler: math[variable+5]~, math[10*2]~
     */
    public static class MathPlaceholderHandler implements PlaceholderHandler {
        @Override
        public String resolve(String content, ExecutionContext context, String defaultValue) {
            try {
                // Replace variables in math expression
                String expression = FrameLandPlaceholderResolver.resolvePlaceholders(content, context);
                
                // Simple math evaluation (basic operations only for security)
                return evaluateSimpleMath(expression);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        
        private String evaluateSimpleMath(String expression) {
            // Basic math evaluation - only allow numbers and basic operators
            expression = expression.replaceAll("[^0-9+\\-*/().\\s]", "");
            
            try {
                // This is a simple implementation - for production, use a proper math parser
                // For now, handle basic operations
                if (expression.contains("+")) {
                    String[] parts = expression.split("\\+", 2);
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return String.valueOf(left + right);
                } else if (expression.contains("-")) {
                    String[] parts = expression.split("-", 2);
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return String.valueOf(left - right);
                } else if (expression.contains("*")) {
                    String[] parts = expression.split("\\*", 2);
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return String.valueOf(left * right);
                } else if (expression.contains("/")) {
                    String[] parts = expression.split("/", 2);
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return String.valueOf(left / right);
                } else {
                    return String.valueOf(Double.parseDouble(expression.trim()));
                }
            } catch (Exception e) {
                return "0";
            }
        }
    }
    
    /**
     * Time placeholder handler: time[HH:mm]~, time[yyyy-MM-dd]~
     */
    public static class TimePlaceholderHandler implements PlaceholderHandler {
        @Override
        public String resolve(String content, ExecutionContext context, String defaultValue) {
            try {
                LocalDateTime now = LocalDateTime.now();
                
                if (content.isEmpty()) {
                    content = "HH:mm:ss";
                }
                
                // Handle predefined formats
                switch (content.toLowerCase()) {
                    case "short":
                        content = "HH:mm";
                        break;
                    case "medium":
                        content = "HH:mm:ss";
                        break;
                    case "long":
                        content = "yyyy-MM-dd HH:mm:ss";
                        break;
                    case "date":
                        content = "yyyy-MM-dd";
                        break;
                    case "time":
                        content = "HH:mm:ss";
                        break;
                }
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(content);
                return now.format(formatter);
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }
    
    /**
     * Random placeholder handler: random[1-100]~, random[5]~
     */
    public static class RandomPlaceholderHandler implements PlaceholderHandler {
        @Override
        public String resolve(String content, ExecutionContext context, String defaultValue) {
            try {
                if (content.contains("-")) {
                    String[] parts = content.split("-", 2);
                    int min = Integer.parseInt(parts[0].trim());
                    int max = Integer.parseInt(parts[1].trim());
                    return String.valueOf(min + (int) (Math.random() * (max - min + 1)));
                } else if (!content.isEmpty()) {
                    int max = Integer.parseInt(content.trim());
                    return String.valueOf((int) (Math.random() * max));
                } else {
                    return String.valueOf(Math.random());
                }
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }
    
    /**
     * Location placeholder handler: location[x]~, location[y]~, location[z]~
     */
    public static class LocationPlaceholderHandler implements PlaceholderHandler {
        @Override
        public String resolve(String content, ExecutionContext context, String defaultValue) {
            Player player = context.getPlayer();
            if (player == null) {
                return defaultValue;
            }
            
            Location location = player.getLocation();
            switch (content.toLowerCase()) {
                case "x":
                    return String.valueOf(location.getBlockX());
                case "y":
                    return String.valueOf(location.getBlockY());
                case "z":
                    return String.valueOf(location.getBlockZ());
                case "yaw":
                    return String.valueOf(location.getYaw());
                case "pitch":
                    return String.valueOf(location.getPitch());
                case "world":
                    return location.getWorld().getName();
                case "formatted":
                    return String.format("%d, %d, %d", location.getBlockX(), location.getBlockY(), location.getBlockZ());
                default:
                    return defaultValue;
            }
        }
    }
    
    /**
     * Server placeholder handler: server[online]~, server[max]~
     */
    public static class ServerPlaceholderHandler implements PlaceholderHandler {
        @Override
        public String resolve(String content, ExecutionContext context, String defaultValue) {
            switch (content.toLowerCase()) {
                case "online":
                    return String.valueOf(Bukkit.getOnlinePlayers().size());
                case "max":
                    return String.valueOf(Bukkit.getMaxPlayers());
                case "version":
                    return Bukkit.getVersion();
                case "name":
                    return Bukkit.getName();
                case "motd":
                    return Bukkit.getMotd();
                default:
                    return defaultValue;
            }
        }
    }
    
    /**
     * Color placeholder handler: color[red]~, color[#FF0000]~
     */
    public static class ColorPlaceholderHandler implements PlaceholderHandler {
        @Override
        public String resolve(String content, ExecutionContext context, String defaultValue) {
            switch (content.toLowerCase()) {
                case "red":
                    return "§c";
                case "green":
                    return "§a";
                case "blue":
                    return "§9";
                case "yellow":
                    return "§e";
                case "purple":
                    return "§d";
                case "cyan":
                    return "§b";
                case "white":
                    return "§f";
                case "black":
                    return "§0";
                case "gray":
                case "grey":
                    return "§7";
                case "dark_red":
                    return "§4";
                case "dark_green":
                    return "§2";
                case "dark_blue":
                    return "§1";
                case "gold":
                    return "§6";
                case "dark_purple":
                    return "§5";
                case "dark_cyan":
                    return "§3";
                case "dark_gray":
                case "dark_grey":
                    return "§8";
                case "bold":
                    return "§l";
                case "italic":
                    return "§o";
                case "underline":
                    return "§n";
                case "strikethrough":
                    return "§m";
                case "reset":
                    return "§r";
                default:
                    return defaultValue;
            }
        }
    }
    
    /**
     * Format placeholder handler: format[number|2]~, format[currency]~
     */
    public static class FormatPlaceholderHandler implements PlaceholderHandler {
        @Override
        public String resolve(String content, ExecutionContext context, String defaultValue) {
            try {
                String[] parts = content.split("\\|", 2);
                String value = parts[0];
                String format = parts.length > 1 ? parts[1] : "0";
                
                // Resolve any placeholders in the value
                value = FrameLandPlaceholderResolver.resolvePlaceholders(value, context);
                
                double number = Double.parseDouble(value);
                
                switch (format.toLowerCase()) {
                    case "currency":
                        return String.format("$%.2f", number);
                    case "percent":
                        return String.format("%.1f%%", number * 100);
                    default:
                        int decimals = Integer.parseInt(format);
                        return String.format("%." + decimals + "f", number);
                }
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }
}