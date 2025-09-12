package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public PlayCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        // 🎆 ENHANCED: Check for dual world switching support
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "switch", "world" -> {
                    // Find current world and switch to its play version
                    CreativeWorld currentWorld = findCreativeWorld(player.getWorld());
                    if (currentWorld != null && currentWorld.isPaired()) {
                        plugin.getWorldManager().switchToPlayWorld(player, currentWorld.getId());
                        return true;
                    }
                    // Fall through to normal play mode
                }
            }
        }
        
        // Найти мир игрока по его текущему местоположению
        World currentWorld = player.getWorld();
        CreativeWorld creativeWorld = findCreativeWorld(currentWorld);
        
        if (creativeWorld == null) {
            player.sendMessage("§cВы не находитесь в мире MegaCreative!");
            return true;
        }
        
        if (!creativeWorld.canEdit(player)) {
            player.sendMessage("§cУ вас нет прав на изменение этого мира!");
            return true;
        }
        
        // Переключение в режим игры
        creativeWorld.setMode(WorldMode.PLAY);
        
        // Телепортация в основной мир (если находится в dev мире)
        if (currentWorld.getName().endsWith("_dev")) {
            World mainWorld = Bukkit.getWorld(creativeWorld.getWorldName());
            if (mainWorld != null) {
                player.teleport(mainWorld.getSpawnLocation());
            }
        }
        
        // Установка режима игры
        player.setGameMode(GameMode.ADVENTURE);
        
        player.sendMessage("§aРежим мира изменен на §f§lИГРА§a!");
        player.sendMessage("§7✅ Код активирован, скрипты будут выполняться");
        player.sendMessage("§7Игроки в режиме приключения");
        
        // Сохранение изменений
        plugin.getWorldManager().saveWorld(creativeWorld);
        
        return true;
    }
    
    private CreativeWorld findCreativeWorld(World bukkitWorld) {
        String worldName = bukkitWorld.getName();
        
        // Убираем префикс и суффиксы
        if (worldName.startsWith("megacreative_")) {
            String id = worldName.replace("megacreative_", "").replace("_dev", "");
            return plugin.getWorldManager().getWorld(id);
        }
        
        return null;
    }
}
