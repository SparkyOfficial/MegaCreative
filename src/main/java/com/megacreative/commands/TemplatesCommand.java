package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.gui.TemplateBrowserGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TemplatesCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public TemplatesCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        // Открываем браузер шаблонов
        new TemplateBrowserGUI(plugin, player).open();
        
        return true;
    }
} 