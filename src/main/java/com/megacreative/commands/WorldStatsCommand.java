package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IPlayerManager;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * 🎆 ENHANCED: World analytics command for tracking dual world usage
 * Usage: /worldstats [worldId]
 */
public class WorldStatsCommand implements CommandExecutor {
    
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
        
        String worldId;
        
        if (args.length == 0) {
            // Use current world
            CreativeWorld currentWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
            if (currentWorld == null) {
                player.sendMessage("§cВы не находитесь в мире MegaCreative! Укажите ID мира.");
                return true;
            }
            worldId = currentWorld.getId();
        } else {
            worldId = args[0];
        }
        
        CreativeWorld world = worldManager.getWorld(worldId);
        if (world == null) {
            player.sendMessage("§cМир с ID " + worldId + " не найден!");
            return true;
        }
        
        // Check permissions
        if (!world.isOwner(player) && !player.hasPermission("megacreative.admin")) {
            player.sendMessage("§cУ вас нет прав для просмотра статистики этого мира!");
            return true;
        }
        
        showWorldStatistics(player, world);
        return true;
    }
    
    private void showWorldStatistics(Player player, CreativeWorld world) {
        Map<String, Object> stats = playerManager.getWorldStatistics(world.getId());\n        Map<UUID, String> currentPlayers = playerManager.getPlayersInWorld(world.getId());\n        \n        player.sendMessage(\"§8§m                    §r §6§lWorld Statistics §8§m                    \");\n        player.sendMessage(\"§7Мир: §f\" + world.getName() + \" §7(ID: \" + world.getId() + \")\");\n        player.sendMessage(\"§7Режим: §f\" + world.getDualMode().getDisplayName());\n        \n        if (world.isPaired()) {\n            CreativeWorld pairedWorld = worldManager.getPairedWorld(world);\n            if (pairedWorld != null) {\n                player.sendMessage(\"§7Парный мир: §f\" + pairedWorld.getName());\n            }\n        }\n        \n        player.sendMessage(\"\");\n        player.sendMessage(\"§e📊 Статистика использования:\");\n        \n        int uniqueVisitors = (Integer) stats.getOrDefault(\"uniqueVisitors\", 0);\n        long totalTimeSpent = (Long) stats.getOrDefault(\"totalTimeSpent\", 0L);\n        int totalSessions = (Integer) stats.getOrDefault(\"totalSessions\", 0);\n        long averageSessionTime = (Long) stats.getOrDefault(\"averageSessionTime\", 0L);\n        \n        player.sendMessage(\"§7• Уникальных посетителей: §f\" + uniqueVisitors);\n        player.sendMessage(\"§7• Всего сессий: §f\" + totalSessions);\n        player.sendMessage(\"§7• Общее время: §f\" + formatTime(totalTimeSpent));\n        player.sendMessage(\"§7• Среднее время сессии: §f\" + formatTime(averageSessionTime));\n        \n        @SuppressWarnings(\"unchecked\")\n        Map<String, Integer> modeSessions = (Map<String, Integer>) stats.getOrDefault(\"modeSessions\", Map.of());\n        if (!modeSessions.isEmpty()) {\n            player.sendMessage(\"\");\n            player.sendMessage(\"§e🎮 По режимам:\");\n            for (Map.Entry<String, Integer> entry : modeSessions.entrySet()) {\n                String mode = entry.getKey();\n                int sessions = entry.getValue();\n                String emoji = mode.equals(\"DEV\") ? \"🔧\" : \"🎮\";\n                player.sendMessage(\"§7• \" + emoji + \" \" + mode + \": §f\" + sessions + \" сессий\");\n            }\n        }\n        \n        if (!currentPlayers.isEmpty()) {\n            player.sendMessage(\"\");\n            player.sendMessage(\"§e👥 Сейчас в мире (\" + currentPlayers.size() + \"):\");\n            for (Map.Entry<UUID, String> entry : currentPlayers.entrySet()) {\n                UUID playerId = entry.getKey();\n                String mode = entry.getValue();\n                long sessionTime = playerManager.getPlayerSessionTime(playerId);\n                \n                Player onlinePlayer = plugin.getServer().getPlayer(playerId);\n                String playerName = onlinePlayer != null ? onlinePlayer.getName() : \"Unknown\";\n                String emoji = mode.equals(\"DEV\") ? \"🔧\" : \"🎮\";\n                \n                player.sendMessage(\"§7• \" + emoji + \" §f\" + playerName + \" §7(\" + formatTime(sessionTime) + \")\");\n            }\n        }\n        \n        player.sendMessage(\"§8§m                                                        \");\n    }\n    \n    private String formatTime(long milliseconds) {\n        if (milliseconds < 1000) {\n            return \"< 1с\";\n        }\n        \n        long seconds = milliseconds / 1000;\n        long minutes = seconds / 60;\n        long hours = minutes / 60;\n        \n        if (hours > 0) {\n            return hours + \"ч \" + (minutes % 60) + \"м\";\n        } else if (minutes > 0) {\n            return minutes + \"м \" + (seconds % 60) + \"с\";\n        } else {\n            return seconds + \"с\";\n        }\n    }\n}