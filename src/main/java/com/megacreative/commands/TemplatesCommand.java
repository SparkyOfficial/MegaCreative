package com.megacreative.commands;

import com.megacreative.MegaCreative;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to open the template browser
 * Allows players to browse and use predefined script templates
 */
public class TemplatesCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public TemplatesCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // TODO: Implement template browser GUI
        player.sendMessage("§eTemplate browser is not yet implemented. Coming soon!");
        
        return true;
    }
}