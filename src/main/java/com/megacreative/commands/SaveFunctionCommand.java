package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class SaveFunctionCommand implements CommandExecutor {

    private final MegaCreative plugin;

    public SaveFunctionCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только для игроков!");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cИспользование: /savefunction <имя_функции>");
            return true;
        }

        String functionName = args[0];

        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null || !player.getWorld().getName().endsWith("_dev")) {
            player.sendMessage("§cВы должны находиться в мире разработки для сохранения функции!");
            return true;
        }

        if (!plugin.getTrustedPlayerManager().canCodeInDevWorld(player)) {
            player.sendMessage("§cУ вас нет прав на создание функций в этом мире!");
            return true;
        }

        Map<Location, CodeBlock> blockCodeBlocks = plugin.getBlockPlacementHandler().getBlockCodeBlocks();
        CodeBlock rootBlock = findRootBlock(blockCodeBlocks);

        if (rootBlock == null) {
            player.sendMessage("§cНе найден корневой блок (Блок сохранения функции) для начала функции!");
            return true;
        }

        CodeScript function = new CodeScript(functionName, true, rootBlock, CodeScript.ScriptType.FUNCTION);
        function.setAuthor(player.getName());

        creativeWorld.getScripts().add(function);
        plugin.getWorldManager().saveWorld(creativeWorld);
        player.sendMessage("§a✓ Функция '" + functionName + "' успешно сохранена!");

        return true;
    }

    private CodeBlock findRootBlock(Map<Location, CodeBlock> blocks) {
        for (CodeBlock block : blocks.values()) {
            if (block.getMaterial() == Material.BOOKSHELF && "saveFunction".equals(block.getAction())) {
                return block.getNextBlock(); // Корнем функции является блок, идущий ПОСЛЕ блока сохранения
            }
        }
        return null;
    }
} 