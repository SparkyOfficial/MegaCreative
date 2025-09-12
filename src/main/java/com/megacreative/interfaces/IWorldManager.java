package com.megacreative.interfaces;

import com.megacreative.models.CreativeWorld;
import com.megacreative.models.CreativeWorldType;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Интерфейс для управления мирами
 */
public interface IWorldManager {
    
    /**
     * Инициализация менеджера миров
     */
    void initialize();
    
    /**
     * Создает новый мир для игрока
     * @param player Игрок, создающий мир
     * @param name Имя мира
     * @param worldType Тип мира
     */
    void createWorld(Player player, String name, CreativeWorldType worldType);
    
    /**
     * 🎆 ENHANCED: Creates a dual world pair for FrameLand-style development
     * @param player Игрок, создающий мир
     * @param name Имя мира
     * @param worldType Тип мира
     */
    void createDualWorld(Player player, String name, CreativeWorldType worldType);
    
    /**
     * Удаляет мир
     * @param worldId ID мира
     * @param requester Игрок, запрашивающий удаление
     */
    void deleteWorld(String worldId, Player requester);
    
    /**
     * Получает мир по ID
     * @param id ID мира
     * @return Мир или null, если не найден
     */
    CreativeWorld getWorld(String id);
    
    /**
     * Находит мир по имени
     * @param name Имя мира
     * @return Мир или null, если не найден
     */
    CreativeWorld getWorldByName(String name);
    
    /**
     * Находит CreativeWorld по Bukkit-миру
     * @param bukkitWorld Bukkit-мир
     * @return CreativeWorld или null, если не найден
     */
    CreativeWorld findCreativeWorldByBukkit(World bukkitWorld);
    
    /**
     * Получает все миры игрока
     * @param player Игрок
     * @return Список миров игрока
     */
    List<CreativeWorld> getPlayerWorlds(Player player);
    
    /**
     * Завершает работу менеджера миров
     */
    void shutdown();
    
    /**
     * Получает все публичные миры
     * @return Список публичных миров
     */
    List<CreativeWorld> getAllPublicWorlds();
    
    /**
     * Получает количество миров игрока
     * @param player Игрок
     * @return Количество миров
     */
    int getPlayerWorldCount(Player player);
    
    /**
     * Сохраняет мир асинхронно
     * @param world Мир для сохранения
     * @param player Игрок (для уведомлений)
     */
    void saveWorldAsync(CreativeWorld world, Player player);
    
    /**
     * Сохраняет мир синхронно
     * @param world Мир для сохранения
     */
    void saveWorld(CreativeWorld world);
    
    /**
     * Сохраняет все миры
     */
    void saveAllWorlds();
    
    /**
     * Получает все творческие миры
     * @return Список всех творческих миров
     */
    List<CreativeWorld> getCreativeWorlds();
    
    // 🎆 ENHANCED: FrameLand-style world switching methods
    
    /**
     * Gets the paired world for dual world architecture
     * @param world The world to find the pair for
     * @return The paired world or null if not found
     */
    CreativeWorld getPairedWorld(CreativeWorld world);
    
    /**
     * Switches player to the development world
     * @param player Player to switch
     * @param worldId ID of the world
     */
    void switchToDevWorld(Player player, String worldId);
    
    /**
     * Switches player to the play world
     * @param player Player to switch
     * @param worldId ID of the world
     */
    void switchToPlayWorld(Player player, String worldId);
}
