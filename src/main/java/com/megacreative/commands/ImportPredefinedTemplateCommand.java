package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.templates.SimpleShopTemplate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to import predefined templates into the user's template library
 */
public class ImportPredefinedTemplateCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public ImportPredefinedTemplateCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§cUsage: /importpredefined <template_name>");
            player.sendMessage("§7Available templates: Simple Shop, Kit Starter");
            return true;
        }
        
        String templateName = String.join(" ", args);
        
        CodeScript template = null;
        
        // Match template by name
        if (templateName.equalsIgnoreCase("Simple Shop") || templateName.equalsIgnoreCase("simpleshop")) {
            template = SimpleShopTemplate.createShopTemplate();
        } else if (templateName.equalsIgnoreCase("Kit Starter") || templateName.equalsIgnoreCase("kitstarter")) {
            template = SimpleShopTemplate.createKitStarterTemplate();
        }
        
        if (template == null) {
            player.sendMessage("§cTemplate not found: " + templateName);
            player.sendMessage("§7Available templates: Simple Shop, Kit Starter");
            return true;
        }
        
        // Save the template to user's library
        plugin.getTemplateManager().saveTemplate(template);
        
        player.sendMessage("§aSuccessfully imported template: " + template.getName());
        player.sendMessage("§7You can now use this template in your scripts!");
        
        return true;
    }
}