package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to add new floors to dev worlds
 * Allows players to expand their coding space vertically
 */
public class AddFloorCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public AddFloorCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player is in a dev world
        String worldName = player.getWorld().getName();
        if (!isDevWorld(worldName)) {
            player.sendMessage("§cЭта команда работает только в мирах разработки!");
            return true;
        }
        
        // Parse floor number
        int floorNumber;
        if (args.length == 0) {
            // Default to floor 1 if no argument provided
            floorNumber = 1;
        } else {
            try {
                floorNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cОшибка: Укажите корректный номер этажа (число)");
                player.sendMessage("§7Использование: /addfloor <номер_этажа>");
                return true;
            }
        }
        
        // Validate floor number
        if (floorNumber < 1) {
            player.sendMessage("§cНомер этажа должен быть больше 0!");
            return true;
        }
        
        if (floorNumber > DevWorldGenerator.getMaxFloors()) {
            player.sendMessage("§cМаксимальное количество этажей: " + DevWorldGenerator.getMaxFloors());
            return true;
        }
        
        // Check permissions
        if (!player.hasPermission("megacreative.addfloor") && !player.isOp()) {
            player.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }
        
        // Create the floor
        try {
            DevWorldGenerator.addFloorForPlayer(player.getWorld(), player, floorNumber);
        } catch (Exception e) {
            player.sendMessage("§cОшибка при создании этажа: " + e.getMessage());
            plugin.getLogger().severe("Failed to create floor " + floorNumber + " for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    /**
     * Checks if the world is a development world
     */
    private boolean isDevWorld(String worldName) {
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative");
    }
}