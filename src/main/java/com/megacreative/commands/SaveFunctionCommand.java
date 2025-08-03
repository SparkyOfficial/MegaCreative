package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaveFunctionCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public SaveFunctionCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 1) {
            player.sendMessage("§cИспользование: /savefunction <имя_функции>");
            return true;
        }
        
        String functionName = args[0];
        
        // Проверяем, что игрок находится в творческом мире
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null) {
            player.sendMessage("§cЭта команда доступна только в творческих мирах!");
            return true;
        }
        
        // Проверяем права
        if (!plugin.getTrustedPlayerManager().canCodeInDevWorld(player)) {
            player.sendMessage("§c❌ У вас нет прав для создания функций в этом мире!");
            return true;
        }
        
        // Создаем корневой блок для функции (используем DIAMOND_BLOCK как для событий)
        var rootBlock = new com.megacreative.coding.CodeBlock(
            org.bukkit.Material.DIAMOND_BLOCK, 
            "function"
        );
        
        // Создаем функцию
        CodeScript function = new CodeScript(
            functionName, 
            true, 
            rootBlock, 
            CodeScript.ScriptType.FUNCTION
        );
        
        // Добавляем функцию в мир
        creativeWorld.getScripts().add(function);
        
        // Сохраняем мир
        plugin.getWorldManager().saveWorld(creativeWorld);
        
        player.sendMessage("§a✓ Функция '" + functionName + "' создана!");
        player.sendMessage("§7Теперь вы можете использовать блок 'Вызвать функцию' для её вызова.");
        
        return true;
    }
} 