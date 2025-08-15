package com.megacreative.coding;

import com.megacreative.MegaCreative;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Менеджер для управління з'єднаннями між блоками програмування
 */
public class BlockConnectionManager implements Listener {

    private final MegaCreative plugin;
    private final CodingManager codingManager;
    private final Map<String, Map<String, List<String>>> connections = new HashMap<>();

    /**
     * Конструктор
     * @param plugin Посилання на основний плагін
     * @param codingManager Менеджер програмування
     */
    public BlockConnectionManager(MegaCreative plugin, CodingManager codingManager) {
        this.plugin = plugin;
        this.codingManager = codingManager;
    }

    /**
     * Додає з'єднання між блоками
     * @param worldId ID світу
     * @param blockFromId ID першого блоку
     * @param blockToId ID другого блоку
     */
    public void addConnection(String worldId, String blockFromId, String blockToId) {
        Map<String, List<String>> worldConnections = connections.computeIfAbsent(worldId, k -> new HashMap<>());
        List<String> blockConnections = worldConnections.computeIfAbsent(blockFromId, k -> new ArrayList<>());
        if (!blockConnections.contains(blockToId)) {
            blockConnections.add(blockToId);
        }
    }

    /**
     * Видаляє з'єднання між блоками
     * @param worldId ID світу
     * @param blockFromId ID першого блоку
     * @param blockToId ID другого блоку
     */
    public void removeConnection(String worldId, String blockFromId, String blockToId) {
        if (connections.containsKey(worldId)) {
            Map<String, List<String>> worldConnections = connections.get(worldId);
            if (worldConnections.containsKey(blockFromId)) {
                List<String> blockConnections = worldConnections.get(blockFromId);
                blockConnections.remove(blockToId);
                if (blockConnections.isEmpty()) {
                    worldConnections.remove(blockFromId);
                }
            }
        }
    }

    /**
     * Отримує всі з'єднання блоку
     * @param worldId ID світу
     * @param blockId ID блоку
     * @return Список ID з'єднаних блоків
     */
    public List<String> getConnections(String worldId, String blockId) {
        if (connections.containsKey(worldId)) {
            Map<String, List<String>> worldConnections = connections.get(worldId);
            if (worldConnections.containsKey(blockId)) {
                return new ArrayList<>(worldConnections.get(blockId));
            }
        }
        return new ArrayList<>();
    }

    /**
     * Очищає всі з'єднання блоку
     * @param worldId ID світу
     * @param blockId ID блоку
     */
    public void clearConnections(String worldId, String blockId) {
        if (connections.containsKey(worldId)) {
            Map<String, List<String>> worldConnections = connections.get(worldId);
            worldConnections.remove(blockId);
        }
    }

    /**
     * Зберігає з'єднання у файл
     */
    public void saveConnections() {
        // Реалізація збереження з'єднань
    }

    /**
     * Завантажує з'єднання з файлу
     */
    public void loadConnections() {
        // Реалізація завантаження з'єднань
    }
}
