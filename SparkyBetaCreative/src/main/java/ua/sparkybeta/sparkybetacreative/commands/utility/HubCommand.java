package ua.sparkybeta.sparkybetacreative.commands.utility;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;

public class HubCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendError(sender, "This command can only be run by a player.");
            return true;
        }

        // Teleports to the main world's spawn (world at index 0)
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        MessageUtils.sendSuccess(player, "Teleported to the hub.");
        return true;
    }
} 