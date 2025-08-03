package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Команда для экспорта и импорта скриптов.
 * Позволяет сохранять и загружать скрипты в формате JSON.
 */
public class ScriptExportCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final Gson gson;
    
    public ScriptExportCommand(MegaCreative plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "export":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /scriptexport export <имя_скрипта>");
                    return true;
                }
                exportScript(player, args[1]);
                break;
                
            case "import":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /scriptexport import <имя_файла>");
                    return true;
                }
                importScript(player, args[1]);
                break;
                
            case "list":
                listExportedScripts(player);
                break;
                
            case "help":
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private void exportScript(Player player, String scriptName) {
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world == null) {
            player.sendMessage("§cВы должны находиться в мире разработки!");
            return;
        }
        
        // Ищем скрипт
        CodeScript script = null;
        for (CodeScript s : world.getScripts()) {
            if (s.getName().equalsIgnoreCase(scriptName)) {
                script = s;
                break;
            }
        }
        
        if (script == null) {
            player.sendMessage("§cСкрипт '" + scriptName + "' не найден!");
            return;
        }
        
        try {
            // Создаем папку для экспорта, если её нет
            File exportDir = new File(plugin.getDataFolder(), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            // Создаем файл
            String fileName = scriptName.replaceAll("[^a-zA-Z0-9_-]", "_") + ".json";
            File exportFile = new File(exportDir, fileName);
            
            // Экспортируем скрипт
            String json = gson.toJson(script);
            try (FileWriter writer = new FileWriter(exportFile)) {
                writer.write(json);
            }
            
            player.sendMessage("§a✓ Скрипт '" + scriptName + "' экспортирован в файл: §f" + fileName);
            player.sendMessage("§7Путь: §f" + exportFile.getAbsolutePath());
            
        } catch (IOException e) {
            player.sendMessage("§c✗ Ошибка при экспорте: " + e.getMessage());
            plugin.getLogger().warning("Ошибка при экспорте скрипта: " + e.getMessage());
        }
    }
    
    private void importScript(Player player, String fileName) {
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world == null) {
            player.sendMessage("§cВы должны находиться в мире разработки!");
            return;
        }
        
        try {
            // Проверяем расширение файла
            if (!fileName.endsWith(".json")) {
                fileName += ".json";
            }
            
            // Ищем файл
            File exportDir = new File(plugin.getDataFolder(), "exports");
            File importFile = new File(exportDir, fileName);
            
            if (!importFile.exists()) {
                player.sendMessage("§cФайл '" + fileName + "' не найден!");
                return;
            }
            
            // Читаем файл
            String json = new String(Files.readAllBytes(Paths.get(importFile.getAbsolutePath())));
            
            // Импортируем скрипт
            CodeScript importedScript = gson.fromJson(json, CodeScript.class);
            
            // Проверяем, не существует ли уже скрипт с таким именем
            String originalName = importedScript.getName();
            String finalName = findUniqueName(world, originalName);
            
            importedScript.setName(finalName);
            world.getScripts().add(importedScript);
            
            // Сохраняем мир
            plugin.getWorldManager().saveWorld(world);
            
            player.sendMessage("§a✓ Скрипт импортирован как: §f" + finalName);
            if (!originalName.equals(finalName)) {
                player.sendMessage("§e⚠ Имя изменено (уже существовал скрипт с таким именем)");
            }
            
        } catch (IOException e) {
            player.sendMessage("§c✗ Ошибка при импорте: " + e.getMessage());
            plugin.getLogger().warning("Ошибка при импорте скрипта: " + e.getMessage());
        } catch (Exception e) {
            player.sendMessage("§c✗ Ошибка при парсинге файла: " + e.getMessage());
            plugin.getLogger().warning("Ошибка при парсинге скрипта: " + e.getMessage());
        }
    }
    
    private void listExportedScripts(Player player) {
        File exportDir = new File(plugin.getDataFolder(), "exports");
        if (!exportDir.exists()) {
            player.sendMessage("§eПапка экспорта пуста");
            return;
        }
        
        File[] files = exportDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            player.sendMessage("§eНет экспортированных скриптов");
            return;
        }
        
        player.sendMessage("§e=== Экспортированные скрипты ===");
        for (File file : files) {
            String fileName = file.getName();
            String scriptName = fileName.substring(0, fileName.length() - 5); // Убираем .json
            player.sendMessage("§f- " + scriptName);
        }
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§e=== Экспорт/Импорт скриптов ===");
        player.sendMessage("§7Команды:");
        player.sendMessage("§f/scriptexport export <имя> §7- Экспортировать скрипт");
        player.sendMessage("§f/scriptexport import <файл> §7- Импортировать скрипт");
        player.sendMessage("§f/scriptexport list §7- Список экспортированных скриптов");
        player.sendMessage("§f/scriptexport help §7- Показать эту справку");
    }
    
    /**
     * Находит уникальное имя для скрипта, добавляя суффикс если необходимо.
     */
    private String findUniqueName(CreativeWorld world, String baseName) {
        String name = baseName;
        int counter = 1;
        
        boolean nameExists = true;
        while (nameExists) {
            nameExists = false;
            for (CodeScript script : world.getScripts()) {
                if (script.getName().equals(name)) {
                    nameExists = true;
                    break;
                }
            }
            if (nameExists) {
                name = baseName + "_" + counter;
                counter++;
            }
        }
        
        return name;
    }
} 