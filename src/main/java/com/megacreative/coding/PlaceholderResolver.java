package com.megacreative.coding;

import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Система разрешения плейсхолдеров в названиях предметов.
 * Позволяет использовать переменные прямо в GUI конфигурации.
 * 
 * Примеры плейсхолдеров:
 * - %player% - имя игрока
 * - %world% - название мира
 * - %x%, %y%, %z% - координаты игрока
 * - %var:money% - значение переменной
 * - %list:items:0% - первый элемент списка
 * - %bool:isOp% - булева переменная
 */
public class PlaceholderResolver {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    
    /**
     * Разрешает плейсхолдеры в строке
     * @param text Исходный текст с плейсхолдерами
     * @param context Контекст выполнения
     * @return Текст с замененными плейсхолдерами
     */
    public static String resolve(String text, ExecutionContext context) {
        if (text == null) return null;
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = resolvePlaceholder(placeholder, context);
            matcher.appendReplacement(result, replacement != null ? replacement : matcher.group(0));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Разрешает конкретный плейсхолдер
     * @param placeholder Плейсхолдер без %%
     * @param context Контекст выполнения
     * @return Замененное значение или null, если не найден
     */
    private static String resolvePlaceholder(String placeholder, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return null;
        
        // Базовые плейсхолдеры игрока
        switch (placeholder.toLowerCase()) {
            case "player":
                return player.getName();
            case "world":
                return player.getWorld().getName();
            case "x":
                return String.valueOf(player.getLocation().getBlockX());
            case "y":
                return String.valueOf(player.getLocation().getBlockY());
            case "z":
                return String.valueOf(player.getLocation().getBlockZ());
            case "health":
                return String.valueOf((int) player.getHealth());
            case "maxhealth":
                return String.valueOf((int) player.getMaxHealth());
            case "food":
                return String.valueOf(player.getFoodLevel());
            case "exp":
                return String.valueOf(player.getExp());
            case "level":
                return String.valueOf(player.getLevel());
            case "gamemode":
                return player.getGameMode().name();
        }
        
        // Плейсхолдеры переменных
        if (placeholder.startsWith("var:")) {
            String varName = placeholder.substring(4);
            Object value = context.getVariable(varName);
            return value != null ? value.toString() : "null";
        }
        
        // Плейсхолдеры списков
        if (placeholder.startsWith("list:")) {
            String[] parts = placeholder.substring(5).split(":");
            if (parts.length >= 2) {
                String listName = parts[0];
                try {
                    int index = Integer.parseInt(parts[1]);
                    var list = context.getList(listName);
                    if (list != null && index >= 0 && index < list.size()) {
                        return list.get(index).toString();
                    }
                } catch (NumberFormatException e) {
                    // Игнорируем некорректный индекс
                }
            }
            return "null";
        }
        
        // Плейсхолдеры булевых переменных
        if (placeholder.startsWith("bool:")) {
            String boolName = placeholder.substring(5);
            Boolean value = context.getBoolean(boolName);
            return value != null ? value.toString() : "false";
        }
        
        // Плейсхолдеры числовых переменных
        if (placeholder.startsWith("num:")) {
            String numName = placeholder.substring(4);
            Number value = context.getNumber(numName);
            return value != null ? value.toString() : "0";
        }
        
        // Плейсхолдеры глобальных переменных
        if (placeholder.startsWith("global:")) {
            String globalName = placeholder.substring(7);
            Object value = context.getPlugin().getCodingManager().getGlobalVariable(globalName);
            return value != null ? value.toString() : "null";
        }
        
        // Плейсхолдеры серверных переменных
        if (placeholder.startsWith("server:")) {
            String serverName = placeholder.substring(7);
            Object value = context.getPlugin().getCodingManager().getServerVariable(serverName);
            return value != null ? value.toString() : "null";
        }
        
        return null;
    }
    
    /**
     * Проверяет, содержит ли текст плейсхолдеры
     * @param text Текст для проверки
     * @return true, если содержит плейсхолдеры
     */
    public static boolean containsPlaceholders(String text) {
        if (text == null) return false;
        return PLACEHOLDER_PATTERN.matcher(text).find();
    }
    
    /**
     * Получает список всех плейсхолдеров в тексте
     * @param text Текст для анализа
     * @return Список плейсхолдеров
     */
    public static java.util.List<String> extractPlaceholders(String text) {
        java.util.List<String> placeholders = new java.util.ArrayList<>();
        if (text == null) return placeholders;
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        
        return placeholders;
    }
} 