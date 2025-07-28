package ua.sparkybeta.sparkybetacreative.commands.world;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.commands.SubCommand;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;

import java.util.List;
import java.util.stream.Collectors;

public class DeleteCommand implements SubCommand {
    @Override
    public @NotNull String getName() {
        return "delete";
    }

    @Override
    public @Nullable String getPermission() {
        return "sparky.world.delete";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendError(sender, "This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            MessageUtils.sendError(player, "Usage: /world delete <world_id>");
            return true;
        }

        String worldId = args[0];
        SparkyWorld world = SparkyBetaCreative.getInstance().getWorldManager().getPlayerWorlds(player.getUniqueId()).stream()
                .filter(w -> w.getCustomId().equalsIgnoreCase(worldId))
                .findFirst()
                .orElse(null);

        if (world == null) {
            MessageUtils.sendError(player, "You do not own a world with that ID.");
            return true;
        }

        MessageUtils.sendInfo(player, "Deleting world " + world.getDisplayName() + "...");
        SparkyBetaCreative.getInstance().getWorldManager().deleteWorld(world)
                .thenAccept(success -> {
                    if (success) {
                        MessageUtils.sendSuccess(player, "World deleted successfully.");
                    } else {
                        MessageUtils.sendError(player, "Failed to delete world.");
                    }
                });
        
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            return SparkyBetaCreative.getInstance().getWorldManager().getPlayerWorlds(player.getUniqueId()).stream()
                    .map(SparkyWorld::getCustomId)
                    .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
} 