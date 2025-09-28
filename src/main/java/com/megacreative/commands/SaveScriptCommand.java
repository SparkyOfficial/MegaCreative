package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Команда для сохранения скриптов из блоков кода
 * Позволяет сохранять созданные скрипты как обычные скрипты или публичные шаблоны
 * Управление сохранением и экспортом скриптов
 *
 * Command for saving scripts from code blocks
 * Allows saving created scripts as regular scripts or public templates
 * Script saving and export management
 *
 * Befehl zum Speichern von Skripten aus Codeblöcken
 * Ermöglicht das Speichern erstellter Skripte als reguläre Skripte oder öffentliche Vorlagen
 * Skript-Speicherungs- und Exportverwaltung
 */
public class SaveScriptCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует команду сохранения скриптов
     * @param plugin основной экземпляр плагина
     *
     * Initializes the save script command
     * @param plugin main plugin instance
     *
     * Initialisiert den Skript-Speicher-Befehl
     * @param plugin Haupt-Plugin-Instanz
     */
    public SaveScriptCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды сохранения скриптов
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles save script command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Skript-Speicher-Befehls
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§cИспользование: /savescript <имя_скрипта> [template]");
            player.sendMessage("§7Добавьте 'template' для сохранения как публичный шаблон");
            return true;
        }
        
        String scriptName = args[0];
        boolean isTemplate = args.length > 1 && args[1].equalsIgnoreCase("template");
        
        // Проверяем, что игрок находится в мире разработки
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null) {
            player.sendMessage("§cВы должны находиться в мире разработки!");
            return true;
        }
        
        // Получаем все блоки кода из мира
        Map<Location, CodeBlock> blockCodeBlocks = plugin.getServiceRegistry().getBlockPlacementHandler().getBlockCodeBlocks();
        if (blockCodeBlocks.isEmpty()) {
            player.sendMessage("§cВ мире нет блоков кода для сохранения!");
            return true;
        }
        
        // Создаем скрипт
        // Находим корневой блок (событие)
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
        
        if (isTemplate) {
            // Template functionality removed as part of unused code cleanup
            player.sendMessage("§cTemplate functionality has been removed.");
            player.sendMessage("§7Saving as regular script instead.");
            
            // Сохраняем как обычный скрипт для мира
            creativeWorld.getScripts().add(script);
            plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
            player.sendMessage("§a✓ Script '" + scriptName + "' successfully saved!");
        } else {
            // Сохраняем как обычный скрипт для мира
            creativeWorld.getScripts().add(script);
            plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
            player.sendMessage("§a✓ Script '" + scriptName + "' successfully saved!");
        }
        
        player.sendMessage("§7Блоков в скрипте: " + blockCodeBlocks.size());
        
        return true;
    }
}