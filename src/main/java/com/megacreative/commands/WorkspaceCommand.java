package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to open the advanced coding workspace
 * Usage: /workspace [open|close|info]
 */
public class WorkspaceCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public WorkspaceCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player is in a creative world
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world == null) {
            player.sendMessage("§cYou must be in a creative world to use the coding workspace!");
            return true;
        }
        
        // Check coding permissions
        if (!world.canCode(player)) {
            player.sendMessage("§cYou don't have permission to code in this world!");
            return true;
        }
        
        String action = args.length > 0 ? args[0].toLowerCase() : "open";
        
        switch (action) {
            case "open":
                openAdvancedWorkspace(player, world);
                break;
                
            case "close":
                player.sendMessage("§aWorkspace closed!");
                break;
                
            case "info":
                showWorkspaceInfo(player, world);
                break;
                
            default:
                player.sendMessage("§cUsage: /workspace [open|close|info]");
                break;
        }
        
        return true;
    }
    
    private void showWorkspaceInfo(Player player, CreativeWorld world) {
        player.sendMessage("§6§l=== MegaCreative Coding Workspace ===");
        player.sendMessage("§aWorld: §f" + world.getName());
        player.sendMessage("§aOwner: §f" + world.getOwnerName());
        player.sendMessage("§aMode: §f" + world.getMode().name());
        player.sendMessage("");
        player.sendMessage("§eFeatures:");
        player.sendMessage("§7• §fDrag & Drop block coding");
        player.sendMessage("§7• §fReal-time visual feedback");
        player.sendMessage("§7• §fAdvanced variable system");
        player.sendMessage("§7• §fCollaborative editing");
        player.sendMessage("§7• §fTemplate library");
        player.sendMessage("§7• §fBuilt-in debugger");
        player.sendMessage("");
        player.sendMessage("§aUse §e/workspace open §ato start coding!");
    }
    
    private void openAdvancedWorkspace(Player player, CreativeWorld world) {
        player.sendMessage("§6§l=== MegaCreative Advanced Workspace ===");
        player.sendMessage("§aOpening advanced coding environment...");
        player.sendMessage("");
        player.sendMessage("§eFeatures Available:");
        player.sendMessage("§7• §fVisual block placement on structured platform");
        player.sendMessage("§7• §fAutomatic block connections");
        player.sendMessage("§7• §fAdvanced variable system");
        player.sendMessage("§7• §fReal-time execution with visual feedback");
        player.sendMessage("§7• §fContainer-based parameter configuration");
        player.sendMessage("");
        player.sendMessage("§aUse §e/dev §ato enter development mode!");
        
        // Teleport to dev world if not already there
        if (!player.getWorld().getName().endsWith("_dev")) {
            player.performCommand("dev");
        }
    }
}