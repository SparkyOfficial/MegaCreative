package com.megacreative.interfaces;

import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс для управления доверенными игроками
 */
public interface ITrustedPlayerManager {
    
    /**
     * Инициализация менеджера доверенных игроков
     */
    void initialize();
    
    /**
     * Добавляет доверенного игрока к миру
     * @param world Мир
     * @param trustedPlayer Доверенный игрок
     * @param owner Владелец мира
     */
    void addTrustedPlayer(CreativeWorld world, Player trustedPlayer, Player owner);
    
    /**
     * Удаляет доверенного игрока из мира
     * @param world Мир
     * @param trustedPlayer Доверенный игрок
     * @param owner Владелец мира
     */
    void removeTrustedPlayer(CreativeWorld world, Player trustedPlayer, Player owner);
    
    /**
     * Проверяет, является ли игрок доверенным для мира
     * @param world Мир
     * @param player Игрок
     * @return true если игрок доверенный
     */
    boolean isTrustedPlayer(CreativeWorld world, Player player);
    
    /**
     * Получает список доверенных игроков мира
     * @param world Мир
     * @return Список доверенных игроков
     */
    List<UUID> getTrustedPlayers(CreativeWorld world);
    
    /**
     * Получает список миров, где игрок является доверенным
     * @param player Игрок
     * @return Список миров
     */
    List<CreativeWorld> getTrustedWorlds(Player player);
    
    /**
     * Очищает всех доверенных игроков из мира
     * @param world Мир
     * @param owner Владелец мира
     */
    void clearTrustedPlayers(CreativeWorld world, Player owner);
    
    /**
     * Получает количество доверенных игроков в мире
     * @param world Мир
     * @return Количество доверенных игроков
     */
    int getTrustedPlayerCount(CreativeWorld world);
    
    /**
     * Сохраняет данные доверенных игроков
     */
    void saveTrustedPlayers();
    
    /**
     * Загружает данные доверенных игроков
     */
    void loadTrustedPlayers();
    
    /**
     * Удаляет игрока из всех доверенных списков
     * @param playerId UUID игрока
     */
    void removePlayerFromAllTrustedLists(UUID playerId);
    
    /**
     * Проверяет, может ли игрок программировать в мире разработки
     * @param player Игрок
     * @return true если игрок может программировать
     */
    boolean canCodeInDevWorld(Player player);
}
