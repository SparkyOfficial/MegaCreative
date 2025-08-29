package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HubCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IPlayerManager playerManager;
    
    public HubCommand(MegaCreative plugin, IPlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        // Первый мир в списке сервера обычно является лобби
        World hubWorld = Bukkit.getWorlds().get(0); 
        
        player.teleport(hubWorld.getSpawnLocation());
        player.sendMessage("§aВы вернулись в хаб!");
        
        // Очищаем инвентарь и выдаем стартовые предметы
        player.getInventory().clear();
        playerManager.giveStarterItems(player);
        
        return true;
    }
} 