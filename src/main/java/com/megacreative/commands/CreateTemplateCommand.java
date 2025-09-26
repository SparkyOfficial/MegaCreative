package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Command to create templates from existing scripts
 * Allows players to save their current scripts as reusable templates
 */
public class CreateTemplateCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public CreateTemplateCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§cUsage: /createtemplate <template_name> [description]");
            return true;
        }
        
        String templateName = args[0];
        String description = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "No description";
        
        // Create a simple template as an example
        CodeBlock rootBlock = new CodeBlock(Material.DIAMOND_BLOCK.name(), "onJoin");
        rootBlock.setParameter("message", com.megacreative.coding.values.DataValue.of("Welcome to the server!"));
        
        CodeBlock sendMessageBlock = new CodeBlock(Material.COBBLESTONE.name(), "sendMessage");
        sendMessageBlock.setParameter("message", com.megacreative.coding.values.DataValue.of("Hello, %player%!"));
        rootBlock.setNextBlock(sendMessageBlock);
        
        CodeScript template = new CodeScript(templateName, true, rootBlock);
        template.setDescription(description);
        template.setAuthor(player.getName());
        template.setId(UUID.randomUUID());
        
        // Save the template
        plugin.getServiceRegistry().getTemplateManager().saveTemplate(template);
        
        player.sendMessage("§aTemplate '" + templateName + "' created successfully!");
        player.sendMessage("§7Description: " + description);
        
        return true;
    }
}