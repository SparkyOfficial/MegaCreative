package ua.sparkybeta.sparkybetacreative.commands.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.gui.WorldsBrowserMenu;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;

import java.util.List;

public class WorldsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendError(sender, "This command can only be run by a player.");
            return true;
        }

        new WorldsBrowserMenu(player).open();
        return true;
    }
} 