package com.megacreative.managers;

import com.megacreative.MegaCreative;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер для работы с игровыми скорбордами.
 * Позволяет создавать, обновлять и управлять скорбордами для игроков.
 */
public class GameScoreboardManager {
    // This field needs to remain as a class field since it's used throughout the class
    // Static analysis flags it as convertible to a local variable, but this is a false positive
    private final MegaCreative plugin;
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    
    public GameScoreboardManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Создает новый скорборд для игрока
     * @param player Игрок, для которого создается скорборд
     * @param title Заголовок скорборда
     * @return Scoreboard объект или null если не удалось создать
     */
    public Scoreboard createPlayerScoreboard(Player player, String title) {
        if (player == null) {
            return null;
        }
        
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("game", "dummy", ChatColor.translateAlternateColorCodes('&', title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        playerScoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
        
        return scoreboard;
    }
    
    /**
     * Получает скорборд игрока
     * @param player Игрок
     * @return Scoreboard объект или null если не найден
     */
    public Scoreboard getPlayerScoreboard(Player player) {
        if (player == null) {
            return null;
        }
        return playerScoreboards.get(player.getUniqueId());
    }
    
    /**
     * Устанавливает значение для игрока в скорборде
     * @param player Игрок
     * @param key Ключ (имя строки)
     * @param value Значение
     * @return true если успешно, false если ошибка
     */
    public boolean setPlayerScore(Player player, String key, int value) {
        if (player == null || key == null) {
            return false;
        }
        
        Scoreboard scoreboard = getPlayerScoreboard(player);
        if (scoreboard == null) {
            return false;
        }
        
        Objective objective = scoreboard.getObjective("game");
        if (objective == null) {
            return false;
        }
        
        Score score = objective.getScore(ChatColor.translateAlternateColorCodes('&', key));
        score.setScore(value);
        
        return true;
    }
    
    /**
     * Получает значение для игрока из скорборда
     * @param player Игрок
     * @param key Ключ (имя строки)
     * @return Значение или 0 если не найдено
     */
    public int getPlayerScore(Player player, String key) {
        if (player == null || key == null) {
            return 0;
        }
        
        Scoreboard scoreboard = getPlayerScoreboard(player);
        if (scoreboard == null) {
            return 0;
        }
        
        Objective objective = scoreboard.getObjective("game");
        if (objective == null) {
            return 0;
        }
        
        Score score = objective.getScore(ChatColor.translateAlternateColorCodes('&', key));
        return score.getScore();
    }
    
    /**
     * Увеличивает значение для игрока в скорборде
     * @param player Игрок
     * @param key Ключ (имя строки)
     * @param increment Значение для увеличения
     * @return Новое значение
     */
    public int incrementPlayerScore(Player player, String key, int increment) {
        int currentValue = getPlayerScore(player, key);
        int newValue = currentValue + increment;
        setPlayerScore(player, key, newValue);
        return newValue;
    }
    
    /**
     * Уменьшает значение для игрока в скорборде
     * @param player Игрок
     * @param key Ключ (имя строки)
     * @param decrement Значение для уменьшения
     * @return Новое значение
     */
    public int decrementPlayerScore(Player player, String key, int decrement) {
        int currentValue = getPlayerScore(player, key);
        int newValue = currentValue - decrement;
        setPlayerScore(player, key, newValue);
        return newValue;
    }
    
    /**
     * Создает глобальный скорборд для игры
     * @param title Заголовок скорборда
     * @return Objective объект или null если не удалось создать
     */
    public Objective createGameScoreboard(String title) {
        if (title == null) {
            return null;
        }
        
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("game_global");
        
        if (objective != null) {
            objective.unregister();
        }
        
        objective = scoreboard.registerNewObjective("game_global", "dummy", ChatColor.translateAlternateColorCodes('&', title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        return objective;
    }
    
    /**
     * Устанавливает значение в глобальном скорборде
     * @param key Ключ (имя строки)
     * @param value Значение
     * @return true если успешно, false если ошибка
     */
    public boolean setGlobalScore(String key, int value) {
        if (key == null) {
            return false;
        }
        
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("game_global");
        
        if (objective == null) {
            return false;
        }
        
        Score score = objective.getScore(ChatColor.translateAlternateColorCodes('&', key));
        score.setScore(value);
        
        return true;
    }
    
    /**
     * Получает значение из глобального скорборда
     * @param key Ключ (имя строки)
     * @return Значение или 0 если не найдено
     */
    public int getGlobalScore(String key) {
        if (key == null) {
            return 0;
        }
        
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("game_global");
        
        if (objective == null) {
            return 0;
        }
        
        Score score = objective.getScore(ChatColor.translateAlternateColorCodes('&', key));
        return score.getScore();
    }
    
    /**
     * Увеличивает значение в глобальном скорборде
     * @param key Ключ (имя строки)
     * @param increment Значение для увеличения
     * @return Новое значение
     */
    public int incrementGlobalScore(String key, int increment) {
        int currentValue = getGlobalScore(key);
        int newValue = currentValue + increment;
        setGlobalScore(key, newValue);
        return newValue;
    }
    
    /**
     * Уменьшает значение в глобальном скорборде
     * @param key Ключ (имя строки)
     * @param decrement Значение для уменьшения
     * @return Новое значение
     */
    public int decrementGlobalScore(String key, int decrement) {
        int currentValue = getGlobalScore(key);
        int newValue = currentValue - decrement;
        setGlobalScore(key, newValue);
        return newValue;
    }
    
    /**
     * Удаляет скорборд игрока
     * @param player Игрок
     * @return true если успешно, false если ошибка
     */
    public boolean removePlayerScoreboard(Player player) {
        if (player == null) {
            return false;
        }
        
        playerScoreboards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        
        return true;
    }
    
    /**
     * Очищает все скорборды
     */
    public void clearAllScoreboards() {
        playerScoreboards.clear();
        
        
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = mainScoreboard.getObjective("game_global");
        if (objective != null) {
            objective.unregister();
        }
    }
}