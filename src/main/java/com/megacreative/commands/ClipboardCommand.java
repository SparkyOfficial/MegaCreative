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
 * Command interface for the code block clipboard tool
 */
public class ClipboardCommand implements CommandExecutor, TabCompleter {
    
    private final MegaCreative plugin;
    private final CodeBlockClipboard clipboard;
    
    // Selection state for region copying
    private final Map<UUID, Location> firstCorners = new HashMap<>();
    private final Map<UUID, Location> secondCorners = new HashMap<>();
    
    public ClipboardCommand(MegaCreative plugin, CodeBlockClipboard clipboard) {
        this.plugin = plugin;
        this.clipboard = clipboard;
    }
    
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
    
    private void handleCopy(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /clipboard copy <block|chain|region>");
            return;
        }
        
        String copyType = args[1].toLowerCase();
        
        switch (copyType) {
            case "block" -> {
                Location targetLoc = player.getTargetBlock(null, 10).getLocation();
                player.sendMessage("§a✓ Копирование блока...");
            }
            case "chain" -> {
                Location targetLoc = player.getTargetBlock(null, 10).getLocation();
                player.sendMessage("§a✓ Копирование цепочки...");
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
    
    private void handlePreview(Player player) {
        Location targetLoc = player.getLocation().getBlock().getLocation();
        clipboard.showPreview(player, targetLoc);
    }
    
    private void handleClear(Player player) {
        clipboard.clear(player);
    }
    
    private void handleInfo(Player player) {
        String info = clipboard.getClipboardInfo(player);
        player.sendMessage("§6=== Буфер обмена ===");
        player.sendMessage(info);
    }
    
    private void handleSave(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /clipboard save <имя>");
            return;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        clipboard.saveToShared(player, name);
    }
    
    private void handleLoad(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /clipboard load <имя>");
            return;
        }
        
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        clipboard.loadFromShared(player, name);
    }
    
    private void handleList(Player player) {
        clipboard.listShared(player);
    }
    
    private void handlePos1(Player player) {
        Location loc = player.getLocation().getBlock().getLocation();
        firstCorners.put(player.getUniqueId(), loc);
        player.sendMessage(String.format("§a✓ Первая точка установлена: %d, %d, %d", 
            loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }
    
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