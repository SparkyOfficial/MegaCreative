package ua.sparkybeta.sparkybetacreative.commands.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.coding.CodingBlockParser;
import ua.sparkybeta.sparkybetacreative.commands.SubCommand;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;

import java.util.List;

public class CodeSaveCommand implements SubCommand {

    @Override
    public @NotNull String getName() {
        return "savecode";
    }

    @Override
    public @Nullable String getPermission() {
        return "sparky.world.savecode";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendError(sender, "This command can only be run by a player.");
            return true;
        }

        SparkyWorld world = SparkyBetaCreative.getInstance().getWorldManager().getWorld(player);
        if (world == null || !player.getWorld().getName().endsWith("_dev")) {
            MessageUtils.sendError(player, "You must be in your DEV world to save code.");
            return true;
        }

        if (!world.getOwner().equals(player.getUniqueId())) {
            MessageUtils.sendError(player, "You are not the owner of this world.");
            return true;
        }

        MessageUtils.sendInfo(player, "Parsing and saving your code...");

        // Running async to avoid blocking the main thread
        Bukkit.getScheduler().runTaskAsynchronously(SparkyBetaCreative.getInstance(), () -> {
            CodingBlockParser parser = new CodingBlockParser();
            parser.parse(world);
            Bukkit.getScheduler().runTask(SparkyBetaCreative.getInstance(), () -> {
                MessageUtils.sendSuccess(player, "Code saved successfully!");
            });
        });

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
} 