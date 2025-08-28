package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final MegaCreative plugin;
    // Хранилище задач-обновляторов для каждого игрока, чтобы их можно было остановить
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    public ScoreboardManager(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Устанавливает и начинает обновлять скорборд для игрока.
     * Этот метод нужно вызывать, когда игрок заходит на сервер или меняет мир.
     */
    public void setScoreboard(Player player) {
        // Если для игрока уже есть задача, отменяем её
        if (activeTasks.containsKey(player.getUniqueId())) {
            activeTasks.get(player.getUniqueId()).cancel();
        }

        // Запускаем новую задачу, которая будет обновляться каждую секунду (20 тиков)
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                removeScoreboard(player); // Если игрок оффлайн, удаляем его скорборд
                return;
            }
            updateScoreboard(player); // Обновляем информацию
        }, 0L, 20L);

        activeTasks.put(player.getUniqueId(), task);
    }

    /**
     * Удаляет скорборд и останавливает его обновление для игрока.
     * Вызывать при выходе игрока с сервера.
     */
    public void removeScoreboard(Player player) {
        if (activeTasks.containsKey(player.getUniqueId())) {
            activeTasks.get(player.getUniqueId()).cancel();
            activeTasks.remove(player.getUniqueId());
        }
        // Очищаем скорборд у игрока
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    /**
     * Основная логика обновления данных на скорборде.
     */
    private void updateScoreboard(Player player) {
        // Ищем CreativeWorld, в котором находится игрок
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());

        // Получаем менеджер скорбордов
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        // Создаем "цель" (Objective) - это и есть наш скорборд сбоку
        Objective objective = board.registerNewObjective("MegaCreative", "dummy", "§b§lMegaCreative");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Если игрок находится в мире MegaCreative, показываем информацию о мире
        if (creativeWorld != null) {
            String worldName = creativeWorld.getName();
            // Обрезаем длинное имя мира
            if (worldName.length() > 16) {
                worldName = worldName.substring(0, 15) + "…";
            }
            String description = creativeWorld.getDescription();
            // Обрезаем длинное описание
            if (description == null || description.isEmpty()) {
                description = "Нет описания";
            } else if (description.length() > 24) {
                description = description.substring(0, 23) + "…";
            }
            
            // Начинаем заполнять скорборд снизу вверх (score 1 - самая нижняя строка)
            objective.getScore("§7megacreative.world").setScore(1); // Пример адреса
            objective.getScore("§1").setScore(2); // Пустая строка-разделитель (используем §1, §2 и т.д. чтобы сделать строки уникальными)
            objective.getScore("§fОнлайн: §a" + creativeWorld.getOnlineCount()).setScore(3);
            objective.getScore("§fРежим: §e" + creativeWorld.getMode().getDisplayName()).setScore(4);
            objective.getScore("§2").setScore(5);
            objective.getScore("§fОписание: §7" + description).setScore(6);
            objective.getScore("§fID: §e" + creativeWorld.getId()).setScore(7);
            objective.getScore("§fМир: §a" + worldName).setScore(8);
            objective.getScore("§7§m----------------").setScore(9);

        } else {
            // Если игрок в обычном мире (хаб/лобби), показываем общую информацию
            objective.getScore("§7megacreative.world").setScore(1); // Пример адреса
            objective.getScore("§1").setScore(2);
            objective.getScore("§fОнлайн сервера: §a" + Bukkit.getOnlinePlayers().size()).setScore(3);
            objective.getScore("§fВаш ранг: §eИгрок").setScore(4); // Пример
            objective.getScore("§2").setScore(5);
            objective.getScore("§aВы находитесь в хабе").setScore(6);
            objective.getScore("§7§m----------------").setScore(7);
        }
        
        // Устанавливаем готовый скорборд игроку
        player.setScoreboard(board);
    }
} 