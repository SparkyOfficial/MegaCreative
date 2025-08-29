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
     * @throws IllegalArgumentException если world, trustedPlayer или owner равны null
     */
    void addTrustedPlayer(CreativeWorld world, Player trustedPlayer, Player owner);
    
    /**
     * Удаляет доверенного игрока из мира
     * @param world Мир
     * @param trustedPlayer Доверенный игрок
     * @param owner Владелец мира
     * @throws IllegalArgumentException если world, trustedPlayer или owner равны null
     */
    void removeTrustedPlayer(CreativeWorld world, Player trustedPlayer, Player owner);
    
    /**
     * Проверяет, является ли игрок доверенным для мира
     * @param world Мир
     * @param player Игрок
     * @return true если игрок доверенный
     * @throws IllegalArgumentException если world или player равны null
     */
    boolean isTrustedPlayer(CreativeWorld world, Player player);
    
    /**
     * Получает список доверенных игроков мира
     * @param world Мир
     * @return Список доверенных игроков (не null, может быть пустым)
     * @throws IllegalArgumentException если world равен null
     */
    List<com.megacreative.models.TrustedPlayer> getTrustedPlayers(CreativeWorld world);
    
    /**
     * Получает список всех доверенных игроков
     * @return Список всех доверенных игроков (не null, может быть пустым)
     */
    List<com.megacreative.models.TrustedPlayer> getAllTrustedPlayers();
    
    /**
     * Получает список доверенных строителей
     * @return Список доверенных строителей (не null, может быть пустым)
     */
    List<com.megacreative.models.TrustedPlayer> getTrustedBuilders();
    
    /**
     * Получает список доверенных кодеров
     * @return Список доверенных кодеров (не null, может быть пустым)
     */
    List<com.megacreative.models.TrustedPlayer> getTrustedCoders();
    
    /**
     * Получает доверенного игрока по UUID
     * @param playerId UUID игрока
     * @return Объект TrustedPlayer или null если не найден или playerId равен null
     */
    com.megacreative.models.TrustedPlayer getTrustedPlayer(UUID playerId);
    
    /**
     * Завершает работу менеджера доверенных игроков
     */
    void shutdown();
    
    /**
     * Получает список миров, где игрок является доверенным
     * @param player Игрок
     * @return Список миров (не null, может быть пустым)
     * @throws IllegalArgumentException если player равен null
     */
    List<CreativeWorld> getTrustedWorlds(Player player);
    
    /**
     * Очищает всех доверенных игроков из мира
     * @param world Мир
     * @param owner Владелец мира
     * @throws IllegalArgumentException если world или owner равны null
     */
    void clearTrustedPlayers(CreativeWorld world, Player owner);
    
    /**
     * Получает количество доверенных игроков в мире
     * @param world Мир
     * @return Количество доверенных игроков (0 если world равен null или нет доверенных игроков)
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
