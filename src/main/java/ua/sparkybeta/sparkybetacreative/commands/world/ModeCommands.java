package ua.sparkybeta.sparkybetacreative.commands.world;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.coding.CodingKit;
import ua.sparkybeta.sparkybetacreative.commands.SubCommand;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.settings.WorldMode;

import java.util.List;

public class ModeCommands implements SubCommand {

    private final WorldMode mode;

    public ModeCommands(WorldMode mode) {
        this.mode = mode;
    }

    @Override
    public @NotNull String getName() {
        return mode.name().toLowerCase();
    }

    @Override
    public @Nullable String getPermission() {
        return "sparky.world." + getName();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendError(sender, "This command can only be run by a player.");
            return true;
        }

        SparkyWorld world = SparkyBetaCreative.getInstance().getWorldManager().getWorld(player);
        if (world == null) {
            MessageUtils.sendError(player, "You are not in a SparkyWorld.");
            return true;
        }

        if (!world.getOwner().equals(player.getUniqueId())) {
            MessageUtils.sendError(player, "You are not the owner of this world.");
            return true;
        }

        world.setMode(mode);
        MessageUtils.sendSuccess(player, "World mode set to " + mode.name());
        
        switch (mode) {
            case PLAY:
            case BUILD:
                player.setGameMode(mode == WorldMode.BUILD ? GameMode.CREATIVE : GameMode.ADVENTURE);
                if (player.getWorld().getName().endsWith("_dev")) {
                    SparkyBetaCreative.getInstance().getWorldManager().teleportToWorld(player, world);
                }
                break;
            case DEV:
                player.setGameMode(GameMode.CREATIVE);
                SparkyBetaCreative.getInstance().getWorldManager().teleportToDevWorld(player, world)
                    .thenAccept(success -> {
                        if (success) {
                            CodingKit.give(player);
                        }
                    });
                break;
        }
        
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
} 