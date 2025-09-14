package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.tools.CodeBlockClipboard;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Команда для работы с буфером обмена кодовых блоков
 * Поддерживает копирование, вставку и управление кодовыми блоками
 * Реализует функциональность выбора регионов и работы с общими буферами
 *
 * Command interface for the code block clipboard tool
 * Supports copying, pasting and managing code blocks
 * Implements region selection and shared buffer functionality
 *
 * Befehlsschnittstelle für das Codeblock-Zwischenablage-Tool
 * Unterstützt das Kopieren, Einfügen und Verwalten von Codeblöcken
 * Implementiert die Regionsauswahl und gemeinsame Pufferfunktionalität
 */
public class ClipboardCommand implements CommandExecutor, TabCompleter {
    
    private final MegaCreative plugin;
    private final CodeBlockClipboard clipboard;
    
    // Состояние выделения для копирования регионов
    // Selection state for region copying
    // Auswahlstatus für das Kopieren von Regionen
    private final Map<UUID, Location> firstCorners = new HashMap<>();
    private final Map<UUID, Location> secondCorners = new HashMap<>();
    
    /**
     * Инициализирует команду буфера обмена с необходимыми зависимостями
     * @param plugin основной экземпляр плагина
     * @param clipboard менеджер буфера обмена кодовых блоков
     *
     * Initializes the clipboard command with required dependencies
     * @param plugin main plugin instance
     * @param clipboard code block clipboard manager
     *
     * Initialisiert den Zwischenablagebefehl mit den erforderlichen Abhängigkeiten
     * @param plugin Haupt-Plugin-Instanz
     * @param clipboard Codeblock-Zwischenablagemanager
     */
    public ClipboardCommand(MegaCreative plugin, CodeBlockClipboard clipboard) {
        this.plugin = plugin;
        this.clipboard = clipboard;
    }
    
    /**
     * Обрабатывает выполнение команды буфера обмена
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles clipboard command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Zwischenablagebefehls
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
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "copy" -> handleCopy(player, args);
            case "paste" -> handlePaste(player, args);
            case "preview" -> handlePreview(player);
            case "clear" -> handleClear(player);
            case "info" -> handleInfo(player);
            case "save" -> handleSave(player, args);
            case "load" -> handleLoad(player, args);
            case "list" -> handleList(player);
            case "pos1", "p1" -> handlePos1(player);
            case "pos2", "p2" -> handlePos2(player);
            case "select" -> handleSelect(player, args);
            default -> showHelp(player);
        }
        
        return true;
    }
    
    /**
     * Обрабатывает команду копирования
     * @param player игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles the copy command
     * @param player player executing the command
     * @param args command arguments
     *
     * Verarbeitet den Kopierbefehl
     * @param player Spieler, der den Befehl ausführt
     * @param args Befehlsargumente
     */
    private void handleCopy(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /clipboard copy <block|chain|region>");
            return;
        }
        
        String copyType = args[1].toLowerCase();
        
        switch (copyType) {
            case "block" -> {
                Location targetLoc = player.getTargetBlock(null, 10).getLocation();
                clipboard.copyBlock(player, targetLoc);
            }
            case "chain" -> {
                Location targetLoc = player.getTargetBlock(null, 10).getLocation();
                clipboard.copyChain(player, targetLoc);
            }
            case "region" -> {
                Location pos1 = firstCorners.get(player.getUniqueId());
                Location pos2 = secondCorners.get(player.getUniqueId());
                
                if (pos1 == null || pos2 == null) {
                    player.sendMessage("§c✖ Сначала выберите регион с помощью /clipboard pos1 и /clipboard pos2");
                    return;
                }
                
                clipboard.copyRegion(player, pos1, pos2);
            }
            default -> player.sendMessage("§cНеизвестный тип копирования: " + copyType);
        }
    }
    
    /**
     * Обрабатывает команду вставки
     * @param player игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles the paste command
     * @param player player executing the command
     * @param args command arguments
     *
     * Verarbeitet den Einfügebefehl
     * @param player Spieler, der den Befehl ausführt
     * @param args Befehlsargumente
     */
    private void handlePaste(Player player, String[] args) {
        Location targetLoc;
        
        if (args.length >= 4) {
            try {
                int x = Integer.parseInt(args[1]);
                int y = Integer.parseInt(args[2]);
                int z = Integer.parseInt(args[3]);
                targetLoc = new Location(player.getWorld(), x, y, z);
            } catch (NumberFormatException e) {
                player.sendMessage("§cНеверные координаты!");
                return;
            }
        } else {
            targetLoc = player.getLocation().getBlock().getLocation();
        }
        
        clipboard.paste(player, targetLoc);
    }
    
    /**
     * Обрабатывает команду предварительного просмотра
     * @param player игрок, выполняющий команду
     *
     * Handles the preview command
     * @param player player executing the command
     *
     * Verarbeitet den Vorschau-Befehl
     * @param player Spieler, der den Befehl ausführt
     */
    private void handlePreview(Player player) {
        Location targetLoc = player.getLocation().getBlock().getLocation();
        clipboard.showPreview(player, targetLoc);
    }
    
    /**
     * Обрабатывает команду очистки буфера
     * @param player игрок, выполняющий команду
     *
     * Handles the clear command
     * @param player player executing the command
     *
     * Verarbeitet den Löschbefehl
     * @param player Spieler, der den Befehl ausführt
     */
    private void handleClear(Player player) {
        clipboard.clear(player);
    }
    
    /**
     * Обрабатывает команду информации о буфере
     * @param player игрок, выполняющий команду
     *
     * Handles the info command
     * @param player player executing the command
     *
     * Verarbeitet den Info-Befehl
     * @param player Spieler, der den Befehl ausführt
     */
    private void handleInfo(Player player) {
        String info = clipboard.getClipboardInfo(player);
        player.sendMessage("§6=== Буфер обмена ===");
        player.sendMessage(info);
    }
    
    /**
     * Обрабатывает команду сохранения в общий буфер
     * @param player игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles the save command
     * @param player player executing the command
     * @param args command arguments
     *
     * Verarbeitet den Speicherbefehl
     * @param player Spieler, der den Befehl ausführt
     * @param args Befehlsargumente
     */
    private void handleSave(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /clipboard save <имя>");
            return;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        clipboard.saveToShared(player, name);
    }
    
    /**
     * Обрабатывает команду загрузки из общего буфера
     * @param player игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles the load command
     * @param player player executing the command
     * @param args command arguments
     *
     * Verarbeitet den Ladebefehl
     * @param player Spieler, der den Befehl ausführt
     * @param args Befehlsargumente
     */
    private void handleLoad(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /clipboard load <имя>");
            return;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        clipboard.loadFromShared(player, name);
    }
    
    /**
     * Обрабатывает команду списка общих буферов
     * @param player игрок, выполняющий команду
     *
     * Handles the list command
     * @param player player executing the command
     *
     * Verarbeitet den Listenbefehl
     * @param player Spieler, der den Befehl ausführt
     */
    private void handleList(Player player) {
        clipboard.listShared(player);
    }
    
    /**
     * Обрабатывает установку первой точки выделения
     * @param player игрок, выполняющий команду
     *
     * Handles setting the first selection point
     * @param player player executing the command
     *
     * Verarbeitet das Setzen des ersten Auswahlpunkts
     * @param player Spieler, der den Befehl ausführt
     */
    private void handlePos1(Player player) {
        Location loc = player.getLocation().getBlock().getLocation();
        firstCorners.put(player.getUniqueId(), loc);
        player.sendMessage(String.format("§a✓ Первая точка установлена: %d, %d, %d", 
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }
    
    /**
     * Обрабатывает установку второй точки выделения
     * @param player игрок, выполняющий команду
     *
     * Handles setting the second selection point
     * @param player player executing the command
     *
     * Verarbeitet das Setzen des zweiten Auswahlpunkts
     * @param player Spieler, der den Befehl ausführt
     */
    private void handlePos2(Player player) {
        Location loc = player.getLocation().getBlock().getLocation();
        secondCorners.put(player.getUniqueId(), loc);
        player.sendMessage(String.format("§a✓ Вторая точка установлена: %d, %d, %d", 
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            
        Location pos1 = firstCorners.get(player.getUniqueId());
        if (pos1 != null) {
            int volume = Math.abs(loc.getBlockX() - pos1.getBlockX() + 1) * 
                        Math.abs(loc.getBlockY() - pos1.getBlockY() + 1) * 
                        Math.abs(loc.getBlockZ() - pos1.getBlockZ() + 1);
            player.sendMessage("§7Объем региона: " + volume + " блоков");
        }
    }
    
    /**
     * Обрабатывает команду выделения
     * @param player игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles the select command
     * @param player player executing the command
     * @param args command arguments
     *
     * Verarbeitet den Auswahlbefehl
     * @param player Spieler, der den Befehl ausführt
     * @param args Befehlsargumente
     */
    private void handleSelect(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /clipboard select <all|clear>");
            return;
        }
        
        String selectType = args[1].toLowerCase();
        
        switch (selectType) {
            case "all" -> {
                player.sendMessage("§a✓ Выбрана вся область кодирования");
            }
            case "clear" -> {
                firstCorners.remove(player.getUniqueId());
                secondCorners.remove(player.getUniqueId());
                player.sendMessage("§a✓ Выделение очищено");
            }
            default -> player.sendMessage("§cНеизвестный тип выделения: " + selectType);
        }
    }
    
    /**
     * Отображает справочную информацию по команде
     * @param player игрок, которому отправляется справка
     *
     * Displays help information for the command
     * @param player player to send help to
     *
     * Zeigt Hilfsinformationen für den Befehl an
     * @param player Spieler, dem die Hilfe gesendet wird
     */
    private void showHelp(Player player) {
        player.sendMessage("§6=== Буфер обмена кодовых блоков ===");
        player.sendMessage("§f/clipboard copy <block|chain|region> - Копировать");
        player.sendMessage("§f/clipboard paste [x y z] - Вставить");
        player.sendMessage("§f/clipboard preview - Показать превью");
        player.sendMessage("§f/clipboard info - Информация о буфере");
        player.sendMessage("§f/clipboard clear - Очистить буфер");
        player.sendMessage("§7Команды региона:");
        player.sendMessage("§f/clipboard pos1 - Установить первую точку");
        player.sendMessage("§f/clipboard pos2 - Установить вторую точку");
        player.sendMessage("§7Общие буферы:");
        player.sendMessage("§f/clipboard save <имя> - Сохранить в общий буфер");
        player.sendMessage("§f/clipboard load <имя> - Загрузить из общего буфера");
        player.sendMessage("§f/clipboard list - Список общих буферов");
    }
    
    /**
     * Обрабатывает автозавершение команды
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param alias псевдоним команды
     * @param args аргументы команды
     * @return список возможных завершений
     *
     * Handles command tab completion
     * @param sender command sender
     * @param command executed command
     * @param alias command alias
     * @param args command arguments
     * @return list of possible completions
     *
     * Verarbeitet die Befehls-Tab-Vervollständigung
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param alias Befehlsalias
     * @param args Befehlsargumente
     * @return Liste möglicher Vervollständigungen
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("copy", "paste", "preview", "clear", "info", "save", "load", "list", "pos1", "pos2", "select");
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "copy" -> {
                    return List.of("block", "chain", "region");
                }
                case "select" -> {
                    return List.of("all", "clear");
                }
            }
        }
        
        return Collections.emptyList();
    }
}