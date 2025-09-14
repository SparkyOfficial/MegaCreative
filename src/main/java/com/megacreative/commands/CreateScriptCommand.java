package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для создания предопределенных скриптов
 * Поддерживает различные типы скриптов для автоматизации игровых процессов
 * Управление событиями и действиями в мире
 *
 * Command for creating predefined scripts
 * Supports various script types for automating gameplay processes
 * Event and action management in the world
 *
 * Befehl zum Erstellen vordefinierter Skripte
 * Unterstützt verschiedene Skripttypen zur Automatisierung von Spielprozessen
 * Ereignis- und Aktionsverwaltung in der Welt
 */
public class CreateScriptCommand implements CommandExecutor {
    private final MegaCreative plugin;

    /**
     * Инициализирует команду создания скриптов
     * @param plugin основной экземпляр плагина
     *
     * Initializes the create script command
     * @param plugin main plugin instance
     *
     * Initialisiert den Skripterstellungsbefehl
     * @param plugin Haupt-Plugin-Instanz
     */
    public CreateScriptCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Обрабатывает выполнение команды создания скриптов
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles create script command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Skripterstellungsbefehls
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§eИспользование: §f/createscript <тип>");
            player.sendMessage("§7Доступные типы:");
            player.sendMessage("§7- welcome §8(приветствие игрока)");
            player.sendMessage("§7- teleport §8(телепортация при входе)");
            player.sendMessage("§7- vip §8(VIP приветствие)");
            player.sendMessage("§7- gamemode §8(смена режима игры)");
            player.sendMessage("§7- weather §8(управление погодой)");
            return true;
        }

        String scriptType = args[0].toLowerCase();
        CodeScript script = null;

        switch (scriptType) {
            case "welcome":
                script = createWelcomeScript();
                break;
            case "teleport":
                script = createTeleportScript();
                break;
            case "vip":
                script = createVipScript();
                break;
            case "gamemode":
                script = createGamemodeScript();
                break;
            case "weather":
                script = createWeatherScript();
                break;
            default:
                player.sendMessage("§cНеизвестный тип скрипта: " + scriptType);
                return true;
        }

        if (script != null) {
            var world = plugin.getWorldManager().getWorld(player.getWorld().getName());
            if (world != null) {
                world.getScripts().add(script);
                plugin.getWorldManager().saveWorld(world);
                plugin.getCodingManager().loadScriptsForWorld(world);
                player.sendMessage("§a✓ Скрипт '" + scriptType + "' создан!");
            } else {
                player.sendMessage("§cМир не найден!");
            }
        }

        return true;
    }

    /**
     * Создает скрипт приветствия игрока
     * @return созданный скрипт приветствия
     *
     * Creates a player welcome script
     * @return created welcome script
     *
     * Erstellt ein Spielerbegrüßungsskript
     * @return erstelltes Begrüßungsskript
     */
    private CodeScript createWelcomeScript() {
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        CodeBlock messageBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        messageBlock.setParameter("message", "§aДобро пожаловать, %player%!");
        
        CodeBlock broadcastBlock = new CodeBlock(Material.COBBLESTONE, "broadcast");
        broadcastBlock.setParameter("message", "§eИгрок %player% присоединился к серверу!");
        
        CodeBlock soundBlock = new CodeBlock(Material.COBBLESTONE, "playSound");
        soundBlock.setParameter("sound", "ENTITY_PLAYER_LEVELUP");
        soundBlock.setParameter("volume", "1.0");
        soundBlock.setParameter("pitch", "1.0");
        
        eventBlock.setNext(messageBlock);
        messageBlock.setNext(broadcastBlock);
        broadcastBlock.setNext(soundBlock);
        
        return new CodeScript("Приветствие игрока", true, eventBlock);
    }

    /**
     * Создает скрипт телепортации при входе
     * @return созданный скрипт телепортации
     *
     * Creates a teleport on join script
     * @return created teleport script
     *
     * Erstellt ein Teleportation-beim-Betreten-Skript
     * @return erstelltes Teleportationsskript
     */
    private CodeScript createTeleportScript() {
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        CodeBlock messageBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        messageBlock.setParameter("message", "§aТелепортируем вас на спавн!");
        
        CodeBlock teleportBlock = new CodeBlock(Material.COBBLESTONE, "teleport");
        teleportBlock.setParameter("coords", "0 70 0");
        
        CodeBlock effectBlock = new CodeBlock(Material.COBBLESTONE, "effect");
        effectBlock.setParameter("effect", "SPEED");
        effectBlock.setParameter("duration", "200");
        effectBlock.setParameter("amplifier", "1");
        
        eventBlock.setNext(messageBlock);
        messageBlock.setNext(teleportBlock);
        teleportBlock.setNext(effectBlock);
        
        return new CodeScript("Телепортация на спавн", true, eventBlock);
    }

    /**
     * Создает VIP скрипт приветствия
     * @return созданный VIP скрипт
     *
     * Creates a VIP welcome script
     * @return created VIP script
     *
     * Erstellt ein VIP-Begrüßungsskript
     * @return erstelltes VIP-Skript
     */
    private CodeScript createVipScript() {
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        CodeBlock conditionBlock = new CodeBlock(Material.OAK_PLANKS, "isOp");
        
        CodeBlock vipMessageBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        vipMessageBlock.setParameter("message", "§6§lVIP §aДобро пожаловать, %player%!");
        conditionBlock.addChild(vipMessageBlock);
        
        CodeBlock vipGiveBlock = new CodeBlock(Material.COBBLESTONE, "giveItem");
        vipGiveBlock.setParameter("item", "DIAMOND");
        vipGiveBlock.setParameter("amount", "10");
        conditionBlock.addChild(vipGiveBlock);
        
        CodeBlock vipBroadcastBlock = new CodeBlock(Material.COBBLESTONE, "broadcast");
        vipBroadcastBlock.setParameter("message", "§6§lVIP §e%player% зашел на сервер!");
        conditionBlock.addChild(vipBroadcastBlock);
        
        eventBlock.setNext(conditionBlock);
        
        return new CodeScript("VIP приветствие", true, eventBlock);
    }

    /**
     * Создает скрипт смены режима игры
     * @return созданный скрипт смены режима игры
     *
     * Creates a gamemode change script
     * @return created gamemode script
     *
     * Erstellt ein Spielmodus-Änderungsskript
     * @return erstelltes Spielmodus-Skript
     */
    private CodeScript createGamemodeScript() {
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        CodeBlock conditionBlock = new CodeBlock(Material.OAK_PLANKS, "isOp");
        
        CodeBlock gamemodeBlock = new CodeBlock(Material.COBBLESTONE, "command");
        gamemodeBlock.setParameter("command", "gamemode creative %player%");
        conditionBlock.addChild(gamemodeBlock);
        
        CodeBlock messageBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        messageBlock.setParameter("message", "§aРежим игры изменен на Creative!");
        conditionBlock.addChild(messageBlock);
        
        eventBlock.setNext(conditionBlock);
        
        return new CodeScript("Смена режима игры", true, eventBlock);
    }

    /**
     * Создает скрипт управления погодой
     * @return созданный скрипт управления погодой
     *
     * Creates a weather control script
     * @return created weather script
     *
     * Erstellt ein Wetterkontrollskript
     * @return erstelltes Wetter-Skript
     */
    private CodeScript createWeatherScript() {
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        CodeBlock weatherBlock = new CodeBlock(Material.NETHERITE_BLOCK, "setWeather");
        weatherBlock.setParameter("weather", "clear");
        
        CodeBlock timeBlock = new CodeBlock(Material.NETHERITE_BLOCK, "setTime");
        timeBlock.setParameter("time", "0");
        
        CodeBlock messageBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        messageBlock.setParameter("message", "§aПогода установлена на ясную!");
        
        eventBlock.setNext(weatherBlock);
        weatherBlock.setNext(timeBlock);
        timeBlock.setNext(messageBlock);
        
        return new CodeScript("Управление погодой", true, eventBlock);
    }
}