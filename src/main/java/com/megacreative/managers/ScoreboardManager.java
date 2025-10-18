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
    
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    public ScoreboardManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Shuts down the ScoreboardManager and cleans up resources
     */
    public void shutdown() {
        
        for (BukkitTask task : activeTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        activeTasks.clear();
        
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
    }

    /**
     * Устанавливает и начинает обновлять скорборд для игрока.
     * Этот метод нужно вызывать, когда игрок заходит на сервер или меняет мир.
     */
    public void setScoreboard(Player player) {
        
        if (activeTasks.containsKey(player.getUniqueId())) {
            activeTasks.get(player.getUniqueId()).cancel();
        }

        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                removeScoreboard(player); 
                return;
            }
            updateScoreboard(player); 
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
        
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    /**
     * Основная логика обновления данных на скорборде.
     */
    private void updateScoreboard(Player player) {
        
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());

        
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        
        Objective objective = board.registerNewObjective("MegaCreative", "dummy", "§b§lMegaCreative");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        
        if (creativeWorld != null) {
            String worldName = creativeWorld.getName();
            
            if (worldName.length() > 16) {
                worldName = worldName.substring(0, 15) + "…";
            }
            String description = creativeWorld.getDescription();
            
            if (description == null || description.isEmpty()) {
                description = "Нет описания";
            } else if (description.length() > 24) {
                description = description.substring(0, 23) + "…";
            }
            
            
            objective.getScore("§7megacreative.world").setScore(1); 
            objective.getScore("§1").setScore(2); 
            objective.getScore("§fОнлайн: §a" + creativeWorld.getOnlineCount()).setScore(3);
            objective.getScore("§fРежим: §e" + creativeWorld.getMode().getDisplayName()).setScore(4);
            objective.getScore("§2").setScore(5);
            objective.getScore("§fОписание: §7" + description).setScore(6);
            objective.getScore("§fID: §e" + creativeWorld.getId()).setScore(7);
            objective.getScore("§fМир: §a" + worldName).setScore(8);
            objective.getScore("§7§m----------------").setScore(9);

        } else {
            
            objective.getScore("§7megacreative.world").setScore(1); 
            objective.getScore("§1").setScore(2);
            objective.getScore("§fОнлайн сервера: §a" + Bukkit.getOnlinePlayers().size()).setScore(3);
            objective.getScore("§fВаш ранг: §eИгрок").setScore(4); 
            objective.getScore("§2").setScore(5);
            objective.getScore("§aВы находитесь в хабе").setScore(6);
            objective.getScore("§7§m----------------").setScore(7);
        }
        
        
        player.setScoreboard(board);
    }
} 