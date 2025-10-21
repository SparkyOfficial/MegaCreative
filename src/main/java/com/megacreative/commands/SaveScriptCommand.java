package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Allows saving created scripts as regular scripts
 * 
 * Дозволяє зберігати створені скрипти як звичайні скрипти
 * 
 * Ermöglicht das Speichern erstellter Skripte als reguläre Skripte
 * 
 * @author Андрій Будильников
 */
public class SaveScriptCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public SaveScriptCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§cИспользование: /savescript <имя_скрипта>");
            return true;
        }
        
        String scriptName = args[0];
        
        
        World bukkitWorld = player.getWorld();
        CreativeWorld playerWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(bukkitWorld);
        if (playerWorld == null) {
            player.sendMessage("§cВы должны находиться в мире разработки!");
            return true;
        }
        
        
        Map<Location, CodeBlock> blockCodeBlocks = plugin.getServiceRegistry().getBlockPlacementHandler().getBlockCodeBlocks();
        if (blockCodeBlocks.isEmpty()) {
            player.sendMessage("§cВ мире нет блоков кода для сохранения!");
            return true;
        }
        
        
        
        CodeBlock rootBlock = null;
        for (CodeBlock block : blockCodeBlocks.values()) {
            if (block.getMaterial() == org.bukkit.Material.DIAMOND_BLOCK) {
                rootBlock = block;
                break;
            }
        }
        
        if (rootBlock == null) {
            player.sendMessage("§cВ мире нет блока-события (алмазный блок)!");
            return true;
        }
        
        CodeScript script = new CodeScript(scriptName, true, rootBlock);
        
        var scriptEngine = plugin.getServiceRegistry().getScriptEngine();
        
        
        try {
            
            player.sendMessage("§aСкрипт успешно сохранен: " + scriptName);
            
            player.sendMessage("§7Блоков в скрипте: " + blockCodeBlocks.size());
        } catch (Exception e) {
            player.sendMessage("§cПроизошла ошибка при сохранении скрипта: " + e.getMessage());
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error saving script: " + scriptName, e);
        }
        return true;
    }
}