package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Временная команда для тестирования системы кодирования.
 */
public class TestScriptCommand implements CommandExecutor {

    private final MegaCreative plugin;

    public TestScriptCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;
        
        // Создаем тестовый скрипт с параметрами
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        // Блок с сообщением (с параметром)
        CodeBlock sendMessageBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        sendMessageBlock.setParameter("message", "§aДобро пожаловать, %player%! Ваш первый скрипт с параметрами работает!");
        
        // Блок с телепортацией (с параметром)
        CodeBlock teleportBlock = new CodeBlock(Material.COBBLESTONE, "teleport");
        teleportBlock.setParameter("coords", "100 70 200");
        
        // Блок с выдачей предмета (с параметрами)
        CodeBlock giveItemBlock = new CodeBlock(Material.COBBLESTONE, "giveItem");
        giveItemBlock.setParameter("item", "DIAMOND");
        giveItemBlock.setParameter("amount", "5");
        
        // Блок с переменной (с параметрами)
        CodeBlock setVarBlock = new CodeBlock(Material.IRON_BLOCK, "setVar");
        setVarBlock.setParameter("var", "welcomeCount");
        setVarBlock.setParameter("value", "1");
        
        // Блок с условием (с параметрами)
        CodeBlock conditionBlock = new CodeBlock(Material.OAK_PLANKS, "isOp");
        
        // Блок с действием внутри условия
        CodeBlock conditionActionBlock = new CodeBlock(Material.COBBLESTONE, "broadcast");
        conditionActionBlock.setParameter("message", "§eОператор %player% зашел на сервер!");
        conditionBlock.addChild(conditionActionBlock);
        
        // Соединяем блоки в цепочку
        eventBlock.setNext(sendMessageBlock);
        sendMessageBlock.setNext(teleportBlock);
        teleportBlock.setNext(giveItemBlock);
        giveItemBlock.setNext(setVarBlock);
        setVarBlock.setNext(conditionBlock);
        
        CodeScript script = new CodeScript("Тестовый скрипт с параметрами", true, eventBlock);
        
        // Сохраняем скрипт в мире игрока
        var world = plugin.getWorldManager().getWorld(player.getWorld().getName());
        if (world != null) {
            world.getScripts().add(script);
            plugin.getWorldManager().saveWorld(world);
            plugin.getCodingManager().loadScriptsForWorld(world);
            player.sendMessage("§a✓ Тестовый скрипт с параметрами создан!");
            player.sendMessage("§7Скрипт содержит:");
            player.sendMessage("§7- Приветственное сообщение");
            player.sendMessage("§7- Телепортацию на координаты 100 70 200");
            player.sendMessage("§7- Выдачу 5 алмазов");
            player.sendMessage("§7- Установку переменной welcomeCount = 1");
            player.sendMessage("§7- Проверку на оператора с broadcast");
        } else {
            player.sendMessage("§cМир не найден!");
        }
        
        return true;
    }

    private CreativeWorld findCreativeWorld(World bukkitWorld) {
        try {
            String worldName = bukkitWorld.getName();
            // Имя мира может быть как основным (megacreative_ID), так и для разработки (megacreative_ID_dev)
            if (worldName.startsWith("megacreative_")) {
                String id = worldName.replace("megacreative_", "").replace("_dev", "");
                return plugin.getWorldManager().getWorld(id);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка поиска мира в TestScriptCommand: " + e.getMessage());
        }
        return null;
    }
}
