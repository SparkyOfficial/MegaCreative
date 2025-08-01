package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.gui.ScriptsGUI;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScriptsCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public ScriptsCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        // Проверяем, что игрок находится в мире MegaCreative
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null) {
            player.sendMessage("§cВы должны находиться в мире MegaCreative!");
            return true;
        }
        
        // Проверяем права на кодирование
        if (!creativeWorld.canCode(player)) {
            player.sendMessage("§cУ вас нет прав на управление скриптами в этом мире!");
            return true;
        }
        
        // Открываем GUI скриптов
        new ScriptsGUI(player, creativeWorld, plugin).open();
        
        return true;
    }
} 