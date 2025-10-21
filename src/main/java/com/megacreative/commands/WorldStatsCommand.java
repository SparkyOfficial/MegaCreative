package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IPlayerManager;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Команда статистики мира для аналитики двойных миров
 * Показывает комплексные данные об использовании миров в стиле Reference System
 *
 * World statistics command for dual world analytics
 * Shows comprehensive usage data for reference system-style worlds
 *
 * Weltenstatistikbefehl für die Analyse von Doppelwelten
 * Zeigt umfassende Nutzungsdaten für Reference System-Stil-Welten an
 */
public class WorldStatsCommand implements CommandExecutor, TabCompleter {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    private final IPlayerManager playerManager;
    
    /**
     * Инициализирует команду статистики мира
     * @param plugin основной экземпляр плагина
     * @param worldManager менеджер мира
     * @param playerManager менеджер игрока
     *
     * Initializes the world statistics command
     * @param plugin main plugin instance
     * @param worldManager world manager
     * @param playerManager player manager
     *
     * Initialisiert den Weltenstatistikbefehl
     * @param plugin Haupt-Plugin-Instanz
     * @param worldManager Weltmanager
     * @param playerManager Spielermanager
     */
    public WorldStatsCommand(MegaCreative plugin, IWorldManager worldManager, IPlayerManager playerManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.playerManager = playerManager;
    }
    
    /**
     * Обрабатывает выполнение команды статистики мира
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles world statistics command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Weltenstatistikbefehls
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(@org.jetbrains.annotations.NotNull CommandSender sender, 
                            @org.jetbrains.annotations.NotNull Command command, 
                            @org.jetbrains.annotations.NotNull String label, 
                            @org.jetbrains.annotations.NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        if (args.length == 0) {
            
            CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());
            if (world == null) {
                player.sendMessage("§cВы не находитесь в управляемом мире!");
                return true;
            }
            showWorldStatistics(player, world);
        } else {
            
            String worldName = args[0];
            CreativeWorld world = worldManager.getWorldByName(worldName);
            if (world == null) {
                player.sendMessage("§cМир не найден: " + worldName);
                return true;
            }
            
            
            if (!world.isOwner(player) && !player.hasPermission("megacreative.world.stats.others")) {
                player.sendMessage("§cУ вас нет доступа к статистике этого мира!");
                return true;
            }
            
            showWorldStatistics(player, world);
        }
        
        return true;
    }
    
    /**
     * Отображает статистику мира
     * @param player игрок, которому отправляется статистика
     * @param world творческий мир
     *
     * Shows world statistics
     * @param player player to send statistics to
     * @param world creative world
     *
     * Zeigt Weltenstatistiken an
     * @param player Spieler, dem die Statistiken gesendet werden
     * @param world Creative-Welt
     */
    private void showWorldStatistics(Player player, CreativeWorld world) {
        Map<String, Object> stats = playerManager.getWorldStatistics(world.getId());
        Map<UUID, String> currentPlayers = playerManager.getPlayersInWorld(world.getId());
        
        player.sendMessage("§8§m                    §r §6§lWorld Statistics §8§m                    ");
        player.sendMessage("§7Мир: §f" + world.getName() + " §7(ID: " + world.getId() + ")");
        player.sendMessage("§7Режим: §f" + world.getDualMode().getDisplayName());
        
        if (world.isPaired()) {
            CreativeWorld pairedWorld = worldManager.getPairedWorld(world);
            if (pairedWorld != null) {
                player.sendMessage("§7Парный мир: §f" + pairedWorld.getName());
            }
        }
        
        player.sendMessage("");
        player.sendMessage("§e📊 Статистика использования:");
        
        int uniqueVisitors = (Integer) stats.getOrDefault("uniqueVisitors", 0);
        long totalTimeSpent = (Long) stats.getOrDefault("totalTimeSpent", 0L);
        int totalSessions = (Integer) stats.getOrDefault("totalSessions", 0);
        long averageSessionTime = (Long) stats.getOrDefault("averageSessionTime", 0L);
        
        player.sendMessage("§7• Уникальных посетителей: §f" + uniqueVisitors);
        player.sendMessage("§7• Всего сессий: §f" + totalSessions);
        player.sendMessage("§7• Общее время: §f" + formatTime(totalTimeSpent));
        player.sendMessage("§7• Среднее время сессии: §f" + formatTime(averageSessionTime));
        
        @SuppressWarnings("unchecked")
        Map<String, Integer> modeSessions = (Map<String, Integer>) stats.getOrDefault("modeSessions", Map.of());
        if (!modeSessions.isEmpty()) {
            player.sendMessage("");
            player.sendMessage("§e🎮 По режимам:");
            for (Map.Entry<String, Integer> entry : modeSessions.entrySet()) {
                String mode = entry.getKey();
                int sessions = entry.getValue();
                String emoji = mode.equals("DEV") ? "🔧" : "🎮";
                player.sendMessage("§7• " + emoji + " " + mode + ": §f" + sessions + " сессий");
            }
        }
        
        if (!currentPlayers.isEmpty()) {
            player.sendMessage("");
            player.sendMessage("§e👥 Сейчас в мире (" + currentPlayers.size() + "):");
            for (Map.Entry<UUID, String> entry : currentPlayers.entrySet()) {
                UUID playerId = entry.getKey();
                String mode = entry.getValue();
                long sessionTime = playerManager.getPlayerSessionTime(playerId);
                
                Player onlinePlayer = plugin.getServer().getPlayer(playerId);
                String playerName = onlinePlayer != null ? onlinePlayer.getName() : "Unknown";
                String emoji = mode.equals("DEV") ? "🔧" : "🎮";
                
                player.sendMessage("§7• " + emoji + " §f" + playerName + " §7(" + formatTime(sessionTime) + ")");
            }
        }
        
        player.sendMessage("§8§m                                                        ");
    }
    
    /**
     * Форматирует время в читаемый формат
     * @param milliseconds время в миллисекундах
     * @return отформатированная строка времени
     *
     * Formats time into readable format
     * @param milliseconds time in milliseconds
     * @return formatted time string
     *
     * Formatiert die Zeit in ein lesbares Format
     * @param milliseconds Zeit in Millisekunden
     * @return formatierte Zeitzeichenfolge
     */
    private String formatTime(long milliseconds) {
        if (milliseconds < 1000) {
            return "< 1с";
        }
        
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return hours + "ч " + (minutes % 60) + "м";
        } else if (minutes > 0) {
            return minutes + "м " + (seconds % 60) + "с";
        } else {
            return seconds + "с";
        }
    }
    
    /**
     * Обрабатывает автозавершение команды
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param alias псевдоним команды
     * @param args аргументы команды
     * @return список возможных завершений
     *
     * Handles command tab completion
     * @param sender command sender
     * @param command executed command
     * @param alias command alias
     * @param args command arguments
     * @return list of possible completions
     *
     * Verarbeitet die Befehls-Tab-Vervollständigung
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param alias Befehlsalias
     * @param args Befehlsargumente
     * @return Liste möglicher Vervollständigungen
     */
    @Override
    public List<String> onTabComplete(@org.jetbrains.annotations.NotNull CommandSender sender, 
                                     @org.jetbrains.annotations.NotNull Command command, 
                                     @org.jetbrains.annotations.NotNull String alias, 
                                     @org.jetbrains.annotations.NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> worldNames = new ArrayList<>();
            
            
            for (CreativeWorld world : worldManager.getPlayerWorlds(player)) {
                worldNames.add(world.getName());
            }
            
            
            String input = args[0].toLowerCase();
            return worldNames.stream()
                .filter(name -> name.toLowerCase().startsWith(input))
                .sorted()
                .toList();
        }
        
        return new ArrayList<>();
    }
}