package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command to list all available templates
 * Shows players what templates they can use
 */
public class ListTemplatesCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public ListTemplatesCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        List<CodeScript> templates = plugin.getServiceRegistry().getTemplateManager().getTemplates();
        
        if (templates.isEmpty()) {
            player.sendMessage("§eNo templates available.");
            player.sendMessage("§7Use /createtemplate to create your first template!");
            return true;
        }
        
        player.sendMessage("§6=== Available Templates ===");
        player.sendMessage("§7Use §f/templates §7to browse templates or §f/importtemplate <name> §7to import one.");
        player.sendMessage("");
        
        for (int i = 0; i < templates.size(); i++) {
            CodeScript template = templates.get(i);
            player.sendMessage("§f" + (i + 1) + ". §a" + template.getName());
            if (template.getDescription() != null && !template.getDescription().isEmpty()) {
                player.sendMessage("   §7" + template.getDescription());
            }
            if (template.getAuthor() != null && !template.getAuthor().isEmpty()) {
                player.sendMessage("   §8Author: " + template.getAuthor());
            }
            player.sendMessage("");
        }
        
        player.sendMessage("§7Total templates: " + templates.size());
        
        return true;
    }
}