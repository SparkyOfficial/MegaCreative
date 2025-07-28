package ua.sparkybeta.sparkybetacreative.commands.mode;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.coding.CodingKit;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.WorldManager;
import ua.sparkybeta.sparkybetacreative.worlds.settings.WorldMode;

public class DevCommand implements CommandExecutor {

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
        
        if (!sparkyWorld.getOwner().equals(player.getUniqueId())) {
            MessageUtils.sendError(player, "You are not the owner of this world.");
            return true;
        }

        sparkyWorld.setMode(WorldMode.DEV);
        worldManager.teleportToDevWorld(player, sparkyWorld);
        player.setGameMode(GameMode.CREATIVE);
        MessageUtils.sendSuccess(player, "You are now in DEV mode.");
        
        sendDevModeGuide(player);
        CodingKit.give(player);

        return true;
    }

    private void sendDevModeGuide(Player player) {
        MessageUtils.sendInfo(player, "§lWelcome to DEV mode!");
        MessageUtils.sendInfo(player, "§7You have been given category blocks.");
        MessageUtils.sendInfo(player, "§7Right-click with a category block to place it.");
        MessageUtils.sendInfo(player, "§7Right-click the placed block again to configure it and link to other blocks.");
        MessageUtils.sendInfo(player, "Use §e/value <type> <value>§f to create items for arguments in the config menu.");
    }
} 