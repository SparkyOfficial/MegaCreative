package ua.sparkybeta.sparkybetacreative.commands.world;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.commands.SubCommand;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.WorldType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CreateCommand implements SubCommand {
    @Override
    public @NotNull String getName() {
        return "create";
    }

    @Override
    public @Nullable String getPermission() {
        return "sparky.world.create";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendError(sender, "This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            MessageUtils.sendError(player, "Usage: /world create <type>");
            return true;
        }

        try {
            WorldType type = WorldType.valueOf(args[0].toUpperCase());

            if (type == WorldType.VOID) {
                MessageUtils.sendError(player, "The VOID world type is reserved for dev worlds and cannot be created manually.");
                return true;
            }

            MessageUtils.sendInfo(player, "Creating a " + type.name() + " world for you...");
            SparkyBetaCreative.getInstance().getWorldManager().createWorld(player, type)
                .thenAccept(success -> {
                    if (success) {
                        MessageUtils.sendSuccess(player, "World created successfully!");
                    } else {
                        MessageUtils.sendError(player, "Failed to create world. You may have reached your world limit or an error occurred.");
                    }
                });
        } catch (IllegalArgumentException e) {
            String availableTypes = Arrays.stream(WorldType.values())
                    .filter(t -> t != WorldType.VOID)
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            MessageUtils.sendError(player, "Invalid world type. Available types: " + availableTypes);
            return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.stream(WorldType.values())
                    .filter(t -> t != WorldType.VOID)
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
} 