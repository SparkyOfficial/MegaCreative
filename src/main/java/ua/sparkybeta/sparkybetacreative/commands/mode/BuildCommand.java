package ua.sparkybeta.sparkybetacreative.commands.mode;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.WorldManager;
import ua.sparkybeta.sparkybetacreative.worlds.settings.WorldMode;

public class BuildCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendError(sender, "Only players can use this command.");
            return true;
        }

        WorldManager worldManager = SparkyBetaCreative.getInstance().getWorldManager();
        SparkyWorld sparkyWorld;

        if (args.length > 0) {
            sparkyWorld = worldManager.getWorldByCustomId(args[0]);
            if (sparkyWorld == null) {
                MessageUtils.sendError(player, "A world with that ID was not found.");
                return true;
            }
        } else {
            sparkyWorld = worldManager.getWorld(player);
        }

        if (sparkyWorld == null) {
            MessageUtils.sendError(player, "You are not in a SparkyWorld and did not specify a world ID.");
            return true;
        }

        if (!sparkyWorld.getOwner().equals(player.getUniqueId())) {
            MessageUtils.sendError(player, "You are not the owner of this world.");
            return true;
        }

        sparkyWorld.setMode(WorldMode.BUILD);
        worldManager.teleportToWorld(player, sparkyWorld);
        player.setGameMode(GameMode.CREATIVE);
        MessageUtils.sendSuccess(player, "You are now in BUILD mode.");

        return true;
    }
} 