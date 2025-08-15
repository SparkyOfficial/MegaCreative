package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.gui.WorldBrowserGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldBrowserCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public WorldBrowserCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        new WorldBrowserGUI(plugin, player).open();
        return true;
    }
}
