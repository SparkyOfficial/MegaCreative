package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эту команду может использовать только игрок.");
            return true;
        }

        CreativeWorld world = findCreativeWorld(player.getWorld());
        if (world == null) {
            player.sendMessage("§cВы не находитесь в мире MegaCreative!");
            return true;
        }

        // 1. Создаем блок действия: отправить сообщение
        CodeBlock sendMessageBlock = new CodeBlock(BlockType.ACTION_SEND_MESSAGE);
        sendMessageBlock.setParameter("message", "§aДобро пожаловать, %player%! Ваш первый скрипт работает!");

        // 2. Создаем корневой блок события: вход игрока
        CodeBlock eventBlock = new CodeBlock(BlockType.EVENT_PLAYER_JOIN);
        eventBlock.addChild(sendMessageBlock); // Отправка сообщения - дочерний блок

        // 3. Создаем сам скрипт
        CodeScript script = new CodeScript("Тестовый скрипт приветствия", true, eventBlock);

        // 4. Добавляем скрипт в мир
        world.getScripts().add(script);
        plugin.getWorldManager().saveWorld(world); // Сохраняем изменения

        // 5. Перезагружаем скрипты для этого мира в CodingManager
        plugin.getCodingManager().loadScriptsForWorld(world);

        player.sendMessage("§eТестовый скрипт успешно создан! Перезайдите в мир, чтобы проверить.");

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
