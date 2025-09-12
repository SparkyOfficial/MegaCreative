package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public DeleteCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage("§cИспользование: /delete <ID мира>");
            player.sendMessage("§7Чтобы узнать ID мира, используйте /myworlds");
            return true;
        }
        
        String worldId = args[0];
        CreativeWorld world = plugin.getWorldManager().getWorld(worldId);
        
        if (world == null) {
            player.sendMessage("§cМир с ID " + worldId + " не найден.");
            return true;
        }
        
        // Проверка, что игрок - владелец
        if (!world.isOwner(player)) {
            player.sendMessage("§cТолько владелец может удалить этот мир.");
            return true;
        }
        
        // Подтверждение удаления
        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            player.sendMessage("§eВы уверены, что хотите удалить мир '" + world.getName() + "'?");
            player.sendMessage("§eВсе данные будут потеряны безвозвратно!");
            player.sendMessage("§eДля подтверждения введите: §f/delete " + worldId + " confirm");
            return true;
        }
        
        plugin.getWorldManager().deleteWorld(worldId, player);
        // WorldManager сам отправит сообщение об успехе.
        return true;
    }
}