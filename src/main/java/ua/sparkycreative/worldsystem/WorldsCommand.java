package ua.sparkycreative.worldsystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }
        Player player = (Player) sender;
        WorldsBrowserGUI gui = new WorldsBrowserGUI(WorldSystemPlugin.getInstance().getWorldManager());
        gui.open(player);
        return true;
    }
} 