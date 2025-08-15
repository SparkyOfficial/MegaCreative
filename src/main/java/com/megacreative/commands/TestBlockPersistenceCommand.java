package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import com.megacreative.coding.CodeBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Команда для тестирования системы сохранения блоков
 */
public class TestBlockPersistenceCommand implements CommandExecutor {

    private final MegaCreative plugin;

    public TestBlockPersistenceCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                showBlockInfo(player);
                break;
            case "test":
                testBlockPersistence(player);
                break;
            case "clear":
                clearBlocks(player);
                break;
            default:
                showHelp(player);
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage("§e=== Тестирование системы блоков ===");
        player.sendMessage("§7/testblockpersistence info §f- Показать информацию о блоках");
        player.sendMessage("§7/testblockpersistence test §f- Создать тестовый блок");
        player.sendMessage("§7/testblockpersistence clear §f- Очистить все блоки");
    }

    private void showBlockInfo(Player player) {
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        if (world == null) {
            player.sendMessage("§cВы не в мире MegaCreative!");
            return;
        }

        if (world.getMode() != WorldMode.DEV) {
            player.sendMessage("§cВы не в мире разработки!");
            return;
        }

        Map<String, CodeBlock> blocks = world.getDevWorldBlocks();
        
        player.sendMessage("§e=== Информация о блоках ===");
        player.sendMessage("§7Мир: §f" + world.getName());
        player.sendMessage("§7Количество блоков: §f" + blocks.size());
        
        if (blocks.isEmpty()) {
            player.sendMessage("§7Блоков нет");
        } else {
            player.sendMessage("§7Блоки:");
            for (Map.Entry<String, CodeBlock> entry : blocks.entrySet()) {
                String location = entry.getKey();
                CodeBlock block = entry.getValue();
                player.sendMessage("§7  - §f" + location + " §7→ §e" + block.getAction());
            }
        }
    }

    private void testBlockPersistence(Player player) {
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        if (world == null) {
            player.sendMessage("§cВы не в мире MegaCreative!");
            return;
        }

        if (world.getMode() != WorldMode.DEV) {
            player.sendMessage("§cВы не в мире разработки!");
            return;
        }

        // Создаем тестовый блок
        Location testLocation = player.getLocation();
        CodeBlock testBlock = new CodeBlock(Material.STONE, "testAction");
        testBlock.setParameter("testParam", "testValue");
        
        // Сохраняем блок
        String locationKey = String.format("%s,%d,%d,%d", 
            testLocation.getWorld().getName(),
            testLocation.getBlockX(),
            testLocation.getBlockY(),
            testLocation.getBlockZ());
        
        world.addDevWorldBlock(locationKey, testBlock);
        plugin.getWorldManager().saveWorld(world);
        
        player.sendMessage("§a✅ Тестовый блок создан и сохранен!");
        player.sendMessage("§7Локация: §f" + locationKey);
        player.sendMessage("§7Действие: §f" + testBlock.getAction());
        player.sendMessage("§7Параметр: §f" + testBlock.getParameters());
    }

    private void clearBlocks(Player player) {
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        if (world == null) {
            player.sendMessage("§cВы не в мире MegaCreative!");
            return;
        }

        if (world.getMode() != WorldMode.DEV) {
            player.sendMessage("§cВы не в мире разработки!");
            return;
        }

        int blockCount = world.getDevWorldBlocks().size();
        world.getDevWorldBlocks().clear();
        plugin.getWorldManager().saveWorld(world);
        
        player.sendMessage("§a✅ Очищено §f" + blockCount + " §aблоков!");
    }
} 