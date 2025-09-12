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
 * 🎆 ENHANCED: World statistics command for dual world analytics
 * Shows comprehensive usage data for FrameLand-style worlds
 */
public class WorldStatsCommand implements CommandExecutor, TabCompleter {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    private final IPlayerManager playerManager;
    
    public WorldStatsCommand(MegaCreative plugin, IWorldManager worldManager, IPlayerManager playerManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.playerManager = playerManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        if (args.length == 0) {
            // Show statistics for current world
            CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());
            if (world == null) {
                player.sendMessage("§cВы не находитесь в управляемом мире!");
                return true;
            }
            showWorldStatistics(player, world);
        } else {
            // Show statistics for specified world
            String worldName = args[0];
            CreativeWorld world = worldManager.getWorldByName(worldName);
            if (world == null) {
                player.sendMessage("§cМир не найден: " + worldName);
                return true;
            }
            
            // Check if player has permission to view stats
            if (!world.isOwner(player) && !player.hasPermission("megacreative.world.stats.others")) {
                player.sendMessage("§cУ вас нет доступа к статистике этого мира!");
                return true;
            }
            
            showWorldStatistics(player, world);
        }
        
        return true;
    }
    
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
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> worldNames = new ArrayList<>();
            
            // Add worlds the player owns
            for (CreativeWorld world : worldManager.getPlayerWorlds(player)) {
                worldNames.add(world.getName());
            }
            
            // Filter by current input
            String input = args[0].toLowerCase();
            return worldNames.stream()
                .filter(name -> name.toLowerCase().startsWith(input))
                .sorted()
                .toList();
        }
        
        return new ArrayList<>();
    }
}