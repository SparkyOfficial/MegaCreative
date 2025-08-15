package com.megacreative.coding.gui;

import com.megacreative.MegaCreative;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Відповідає за візуалізацію з'єднань між блоками програмування
 */
public class ConnectionVisualizationManager {

    private final MegaCreative plugin;

    /**
     * Конструктор
     * @param plugin Посилання на основний плагін
     */
    public ConnectionVisualizationManager(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Візуалізує з'єднання між двома блоками
     * @param player Гравець, для якого показувати частинки
     * @param start Початкова локація
     * @param end Кінцева локація
     */
    public void visualizeConnection(Player player, Location start, Location end) {
        if (!start.getWorld().equals(end.getWorld())) {
            return; // Різні світи
        }

        World world = start.getWorld();

        // Розрахунок центрів блоків
        Location startCenter = start.clone().add(0.5, 0.5, 0.5);
        Location endCenter = end.clone().add(0.5, 0.5, 0.5);

        // Вектор напрямку
        Vector direction = endCenter.toVector().subtract(startCenter.toVector());
        double length = direction.length();
        direction.normalize();

        // Створюємо частинки вздовж лінії
        for (double i = 0; i < length; i += 0.25) {
            Vector point = startCenter.toVector().add(direction.clone().multiply(i));
            Location particleLocation = point.toLocation(world);

            // Створюємо частинку тільки для цього гравця
            player.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.YELLOW, 1));
        }
    }

    /**
     * Візуалізує активне з'єднання між двома блоками під час виконання
     * @param player Гравець, для якого показувати частинки
     * @param start Початкова локація
     * @param end Кінцева локація
     */
    public void visualizeActiveConnection(Player player, Location start, Location end) {
        if (!start.getWorld().equals(end.getWorld())) {
            return; // Різні світи
        }

        World world = start.getWorld();

        // Розрахунок центрів блоків
        Location startCenter = start.clone().add(0.5, 0.5, 0.5);
        Location endCenter = end.clone().add(0.5, 0.5, 0.5);

        // Вектор напрямку
        Vector direction = endCenter.toVector().subtract(startCenter.toVector());
        double length = direction.length();
        direction.normalize();

        // Створюємо частинки вздовж лінії
        for (double i = 0; i < length; i += 0.25) {
            Vector point = startCenter.toVector().add(direction.clone().multiply(i));
            Location particleLocation = point.toLocation(world);

            // Створюємо частинку тільки для цього гравця
            player.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.GREEN, 1));
        }
    }

    /**
     * Візуалізує виконання умовного блоку
     * @param player Гравець, для якого показувати частинки
     * @param location Локація блоку
     * @param success Чи виконана умова
     */
    public void visualizeCondition(Player player, Location location, boolean success) {
        Location center = location.clone().add(0.5, 0.5, 0.5);

        // Колір залежно від результату
        Color color = success ? Color.GREEN : Color.RED;

        // Створюємо частинки навколо блоку
        for (double y = 0; y <= 1; y += 0.25) {
            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                double x = Math.cos(angle) * 0.7;
                double z = Math.sin(angle) * 0.7;

                Location particleLocation = center.clone().add(x, y, z);
                player.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1));
            }
        }
    }

    /**
     * Візуалізує виконання дії блоку
     * @param player Гравець, для якого показувати частинки
     * @param location Локація блоку
     */
    public void visualizeAction(Player player, Location location) {
        Location center = location.clone().add(0.5, 0.5, 0.5);

        // Створюємо частинки над блоком
        player.spawnParticle(Particle.VILLAGER_HAPPY, center.clone().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
    }

    /**
     * Візуалізує помилку виконання блоку
     * @param player Гравець, для якого показувати частинки
     * @param location Локація блоку
     */
    public void visualizeError(Player player, Location location) {
        Location center = location.clone().add(0.5, 0.5, 0.5);

        // Створюємо частинки над блоком
        player.spawnParticle(Particle.FLAME, center, 10, 0.3, 0.3, 0.3, 0.05);
    }
}
