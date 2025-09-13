package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 🎆 ENHANCED: Reference system-style world switching command
 * Usage: /switch [code|dev|play|world]
 */
public class SwitchCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    
    public SwitchCommand(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        // Find current creative world
        CreativeWorld currentWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
        if (currentWorld == null) {
            player.sendMessage("§cВы не находитесь в мире MegaCreative!");
            return true;
        }
        
        if (args.length == 0) {
            sendSwitchHelp(player, currentWorld);
            return true;
        }
        
        String mode = args[0].toLowerCase();
        switch (mode) {
            case "code", "dev", "development" -> {
                worldManager.switchToDevWorld(player, currentWorld.getId());
            }
            case "play", "world", "game" -> {
                worldManager.switchToPlayWorld(player, currentWorld.getId());
            }
            default -> {
                player.sendMessage("§c❌ Неизвестный режим: " + mode);
                sendSwitchHelp(player, currentWorld);
            }
        }
        
        return true;
    }
    
    private void sendSwitchHelp(Player player, CreativeWorld world) {
        player.sendMessage("§8§m                    §r §6§lSwitch World Mode §8§m                    ");
        player.sendMessage("§7Текущий мир: §f" + world.getName());
        player.sendMessage("§7Режим: §f" + world.getDualMode().getDisplayName());
        
        if (world.isPaired()) {
            player.sendMessage("");
            player.sendMessage("§e🔧 /switch code §8- §fПереключиться в режим разработки");
            player.sendMessage("§a🎮 /switch play §8- §fПереключиться в игровой режим");
        } else {
            player.sendMessage("");
            player.sendMessage("§7⚠ Этот мир не имеет парной архитектуры");
            player.sendMessage("§7Используйте §f/dev §7для создания мира разработки");
        }
        
        player.sendMessage("§8§m                                                        ");
    }
}