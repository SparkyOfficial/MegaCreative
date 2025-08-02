package com.megacreative.coding.values;

import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.PlaceholderResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Представляет локацию, которая может быть вычислена в контексте выполнения.
 */
public class LocationValue implements Value<Location> {
    private final String xExpression;
    private final String yExpression;
    private final String zExpression;
    private final String worldExpression;

    public LocationValue(String xExpression, String yExpression, String zExpression, String worldExpression) {
        this.xExpression = xExpression;
        this.yExpression = yExpression;
        this.zExpression = zExpression;
        this.worldExpression = worldExpression;
    }

    public LocationValue(String xExpression, String yExpression, String zExpression) {
        this(xExpression, yExpression, zExpression, null);
    }

    @Override
    public Location get(ExecutionContext context) {
        // Обрабатываем плейсхолдеры в координатах
        String resolvedX = PlaceholderResolver.resolve(xExpression, context);
        String resolvedY = PlaceholderResolver.resolve(yExpression, context);
        String resolvedZ = PlaceholderResolver.resolve(zExpression, context);
        
        try {
            double x = Double.parseDouble(resolvedX);
            double y = Double.parseDouble(resolvedY);
            double z = Double.parseDouble(resolvedZ);
            
            // Определяем мир
            String worldName;
            if (worldExpression != null) {
                worldName = PlaceholderResolver.resolve(worldExpression, context);
            } else {
                // Если мир не указан, используем мир игрока
                Player player = context.getPlayer();
                worldName = player != null ? player.getWorld().getName() : "world";
            }
            
            return new Location(context.getPlugin().getServer().getWorld(worldName), x, y, z);
        } catch (NumberFormatException e) {
            // Если не удалось парсить координаты, возвращаем локацию игрока
            Player player = context.getPlayer();
            if (player != null) {
                return player.getLocation();
            }
            return null;
        }
    }

    /**
     * Создает LocationValue для локации игрока.
     * @return LocationValue, представляющий локацию игрока
     */
    public static LocationValue playerLocation() {
        return new LocationValue("%player_x%", "%player_y%", "%player_z%");
    }

    /**
     * Создает LocationValue для локации блока.
     * @return LocationValue, представляющий локацию блока
     */
    public static LocationValue blockLocation() {
        return new LocationValue("%block_x%", "%block_y%", "%block_z%");
    }

    /**
     * Возвращает исходные выражения координат.
     * @return Массив с выражениями [x, y, z, world]
     */
    public String[] getRawExpressions() {
        return new String[]{xExpression, yExpression, zExpression, worldExpression};
    }
} 