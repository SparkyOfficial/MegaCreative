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
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendError(sender, "Only players can use this command.");
            return true;
        }

        WorldManager worldManager = SparkyBetaCreative.getInstance().getWorldManager();
        SparkyWorld sparkyWorld = worldManager.getWorld(player);

        if (sparkyWorld == null) {
            MessageUtils.sendError(player, "You are not in a SparkyWorld.");
            return true;
        }

        sparkyWorld.setMode(WorldMode.PLAY);
        worldManager.teleportToWorld(player, sparkyWorld).thenAccept(success -> {
            if (success) {
                // Manually call the PlayerJoinEvent for the new world context
                PlayerJoinEvent fakeJoinEvent = new PlayerJoinEvent(player, Component.text(""));
                Bukkit.getPluginManager().callEvent(fakeJoinEvent);

                player.setGameMode(GameMode.SURVIVAL);
                MessageUtils.sendSuccess(player, "You are now in PLAY mode.");
            } else {
                MessageUtils.sendError(player, "Failed to teleport to the world.");
            }
        });

        return true;
    }
} 