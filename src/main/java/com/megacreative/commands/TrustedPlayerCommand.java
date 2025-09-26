package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.managers.TrustedPlayerManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.TrustedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Команда для управления доверенными игроками
 * Позволяет добавлять, удалять и просматривать доверенных игроков в мирах
 * Управление правами доступа и разрешениями для доверенных игроков
 *
 * Command for managing trusted players
 * Allows adding, removing and viewing trusted players in worlds
 * Management of access rights and permissions for trusted players
 *
 * Befehl zur Verwaltung vertrauenswürdiger Spieler
 * Ermöglicht das Hinzufügen, Entfernen und Anzeigen vertrauenswürdiger Spieler in Welten
 * Verwaltung von Zugriffsrechten und Berechtigungen für vertrauenswürdige Spieler
 */
public class TrustedPlayerCommand implements CommandExecutor, TabCompleter {

    private final MegaCreative plugin;

    /**
     * Инициализирует команду управления доверенными игроками
     * @param plugin основной экземпляр плагина
     *
     * Initializes the trusted player management command
     * @param plugin main plugin instance
     *
     * Initialisiert den Befehl zur Verwaltung vertrauenswürdiger Spieler
     * @param plugin Haupt-Plugin-Instanz
     */
    public TrustedPlayerCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Обрабатывает выполнение команды управления доверенными игроками
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles trusted player management command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Befehls zur Verwaltung vertrauenswürdiger Spieler
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("megacreative.trusted")) {
            sender.sendMessage("§c❌ У вас нет прав для использования этой команды!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                return handleAdd(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "list":
                return handleList(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "gui":
                return handleGUI(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }

    /**
     * Обрабатывает добавление доверенного игрока
     * @param sender отправитель команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles adding a trusted player
     * @param sender command sender
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet das Hinzufügen eines vertrauenswürdigen Spielers
     * @param sender Befehlsabsender
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    private boolean handleAdd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c❌ Эту команду можно использовать только в игре!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c❌ Использование: /trusted add <игрок>");
            return true;
        }

        Player player = (Player) sender;
        String targetPlayerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        
        if (targetPlayer == null) {
            sender.sendMessage("§c❌ Игрок " + targetPlayerName + " не найден!");
            return true;
        }
        
        // Get the world where the player is currently located
        CreativeWorld world = plugin.getServiceRegistry().getWorldManager().getWorldByName(player.getWorld().getName());
        if (world == null) {
            sender.sendMessage("§c❌ Вы должны находиться в мире MegaCreative!");
            return true;
        }
        
        // Check if the player is the owner of the world
        if (!world.getOwnerId().equals(player.getUniqueId())) {
            sender.sendMessage("§c❌ Вы не являетесь владельцем этого мира!");
            return true;
        }
        
        // Add the player as trusted
        try {
            plugin.getServiceRegistry().getTrustedPlayerManager().addTrustedPlayer(world, targetPlayer, player);
            sender.sendMessage("§a✅ Игрок " + targetPlayer.getName() + " добавлен в доверенные!");
            targetPlayer.sendMessage("§a✅ Вы были добавлены в доверенные игроки мира " + world.getName() + "!");
        } catch (Exception e) {
            sender.sendMessage("§c❌ Ошибка при добавлении игрока: " + e.getMessage());
        }
        
        return true;
    }

    /**
     * Обрабатывает удаление доверенного игрока
     * @param sender отправитель команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles removing a trusted player
     * @param sender command sender
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet das Entfernen eines vertraуenswürdigen Spielers
     * @param sender Befehlsabsender
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c❌ Эту команду можно использовать только в игре!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c❌ Использование: /trusted remove <игрок>");
            return true;
        }

        Player player = (Player) sender;
        String targetPlayerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        
        if (targetPlayer == null) {
            sender.sendMessage("§c❌ Игрок " + targetPlayerName + " не найден!");
            return true;
        }
        
        // Get the world where the player is currently located
        CreativeWorld world = plugin.getServiceRegistry().getWorldManager().getWorldByName(player.getWorld().getName());
        if (world == null) {
            sender.sendMessage("§c❌ Вы должны находиться в мире MegaCreative!");
            return true;
        }
        
        // Check if the player is the owner of the world
        if (!world.getOwnerId().equals(player.getUniqueId())) {
            sender.sendMessage("§c❌ Вы не являетесь владельцем этого мира!");
            return true;
        }
        
        // Check if the target player is trusted in this world
        if (!plugin.getServiceRegistry().getTrustedPlayerManager().isTrustedPlayer(world, targetPlayer)) {
            sender.sendMessage("§c❌ Игрок " + targetPlayer.getName() + " не является доверенным в этом мире!");
            return true;
        }
        
        // Remove the player from trusted
        try {
            plugin.getServiceRegistry().getTrustedPlayerManager().removeTrustedPlayer(world, targetPlayer, player);
            sender.sendMessage("§a✅ Игрок " + targetPlayer.getName() + " удален из доверенных!");
            targetPlayer.sendMessage("§c❌ Ваши права доверенного игрока в мире " + world.getName() + " были отозваны!");
        } catch (Exception e) {
            sender.sendMessage("§c❌ Ошибка при удалении игрока: " + e.getMessage());
        }
        
        return true;
    }

    /**
     * Обрабатывает отображение списка доверенных игроков
     * @param sender отправитель команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles displaying the list of trusted players
     * @param sender command sender
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Anzeige der Liste vertrauenswürdiger Spieler
     * @param sender Befehlsabsender
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    private boolean handleList(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c❌ Эта команда доступна только игрокам!");
            return true;
        }

        Player player = (Player) sender;
        CreativeWorld world = plugin.getServiceRegistry().getWorldManager().getWorldByName(player.getWorld().getName());
        if (world == null) {
            player.sendMessage("§c❌ Вы должны находиться в мире MegaCreative!");
            return true;
        }

        // Get trusted players for the current world
        List<TrustedPlayer> trustedPlayers = plugin.getServiceRegistry().getTrustedPlayerManager().getTrustedPlayers(world);
        
        if (trustedPlayers.isEmpty()) {
            sender.sendMessage("§7В этом мире нет доверенных игроков.");
            return true;
        }

        // Separate builders and coders
        List<TrustedPlayer> builders = new ArrayList<>();
        List<TrustedPlayer> coders = new ArrayList<>();
        
        for (TrustedPlayer trusted : trustedPlayers) {
            if (trusted.getType() == TrustedPlayer.TrustedPlayerType.TRUSTED_CODER) {
                coders.add(trusted);
            } else {
                builders.add(trusted);
            }
        }

        // Display the lists
        if (!builders.isEmpty()) {
            sender.sendMessage("§aДоверенные строители:");
            for (TrustedPlayer trusted : builders) {
                sender.sendMessage("§7- §f" + trusted.getPlayerName() + " §7(добавил: §f" + trusted.getAddedBy() + "§7)");
            }
        }
        
        if (!coders.isEmpty()) {
            sender.sendMessage("\n§bДоверенные программисты:");
            for (TrustedPlayer trusted : coders) {
                sender.sendMessage("§7- §f" + trusted.getPlayerName() + " §7(добавил: §f" + trusted.getAddedBy() + "§7)");
            }
        }

        sender.sendMessage("\n§7Всего: §f" + trustedPlayers.size() + " игроков");
        return true;
    }

    /**
     * Обрабатывает отображение информации о доверенном игроке
     * @param sender отправитель команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles displaying information about a trusted player
     * @param sender command sender
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Anzeige von Informationen über einen vertrauenswürdigen Spieler
     * @param sender Befehlsabsender
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c❌ Эта команда доступна только игрокам!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c❌ Использование: /trusted info <игрок>");
            return true;
        }

        Player player = (Player) sender;
        CreativeWorld world = plugin.getServiceRegistry().getWorldManager().getWorldByName(player.getWorld().getName());
        if (world == null) {
            sender.sendMessage("§c❌ Вы должны находиться в мире MegaCreative!");
            return true;
        }
        
        String targetPlayerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        
        if (targetPlayer == null) {
            sender.sendMessage("§c❌ Игрок " + targetPlayerName + " не найден!");
            return true;
        }

        // Check if the target player is trusted in this world
        if (!plugin.getServiceRegistry().getTrustedPlayerManager().isTrustedPlayer(world, targetPlayer)) {
            sender.sendMessage("§7Игрок " + targetPlayerName + " не является доверенным в этом мире.");
            return true;
        }
        
        // Get the trusted player info
        List<TrustedPlayer> trustedPlayers = plugin.getServiceRegistry().getTrustedPlayerManager().getTrustedPlayers(world);
        TrustedPlayer trustedPlayer = trustedPlayers.stream()
            .filter(tp -> tp.getPlayerId().equals(targetPlayer.getUniqueId()))
            .findFirst()
            .orElse(null);
            
        if (trustedPlayer == null) {
            sender.sendMessage("§c❌ Ошибка при получении информации об игроке.");
            return true;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String addedDate = sdf.format(new Date(trustedPlayer.getAddedAt()));

        sender.sendMessage("§e=== Информация о доверенном игроке ===");
        sender.sendMessage("§7Имя: §f" + trustedPlayer.getPlayerName());
        sender.sendMessage("§7Тип: §f" + (trustedPlayer.getType() == TrustedPlayer.TrustedPlayerType.TRUSTED_CODER ? "Программист" : "Строитель"));
        sender.sendMessage("§7Добавлен: §f" + addedDate);
        sender.sendMessage("§7Добавил: §f" + trustedPlayer.getAddedBy());
        
        return true;
    }

    /**
     * Обрабатывает открытие GUI управления доверенными игроками
     * @param sender отправитель команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles opening the trusted players management GUI
     * @param sender command sender
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet das Öffnen der GUI zur Verwaltung vertrauenswürdiger Spieler
     * @param sender Befehlsabsender
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    private boolean handleGUI(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c❌ Эта команда доступна только игрокам!");
            return true;
        }

        Player player = (Player) sender;
        
        // Check if player is in a valid world
        CreativeWorld world = plugin.getServiceRegistry().getWorldManager().getWorldByName(player.getWorld().getName());
        if (world == null) {
            player.sendMessage("§c❌ Вы должны находиться в мире MegaCreative!");
            return true;
        }
        
        // Check if player is the owner of the world
        if (!world.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§c❌ Вы не являетесь владельцем этого мира!");
            return true;
        }
        
        // Open the GUI
        try {
            new com.megacreative.gui.TrustedPlayersGUI(plugin, player).open();
            player.sendMessage("§a✅ Открыто меню управления доверенными игроками!");
        } catch (Exception e) {
            player.sendMessage("§c❌ Ошибка при открытии меню: " + e.getMessage());
            plugin.getLogger().severe("Error opening TrustedPlayersGUI: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
        
        return true;
    }

    /**
     * Отправляет справочную информацию по команде
     * @param sender отправитель команды
     *
     * Sends help information for the command
     * @param sender command sender
     *
     * Sendet Hilfsinformationen für den Befehl
     * @param sender Befehlsabsender
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e=== Команды управления доверенными игроками ===");
        sender.sendMessage("§7/trusted add <игрок> §f- Добавить доверенного игрока");
        sender.sendMessage("§7/trusted remove <игрок> §f- Удалить доверенного игрока");
        sender.sendMessage("§7/trusted list §f- Показать список доверенных игроков в текущем мире");
        sender.sendMessage("§7/trusted info <игрок> §f- Информация о доверенном игроке");
        sender.sendMessage("§7/trusted gui §f- Открыть GUI управления доверенными игроками");
        sender.sendMessage("§7Примечание: §fКоманды работают только в мире, где вы находитесь");
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
        if (!sender.hasPermission("megacreative.trusted") || !(sender instanceof Player)) {
            return new ArrayList<>();
        }

        // Get the player and their current world
        Player player = (Player) sender;
        CreativeWorld world = plugin.getServiceRegistry().getWorldManager().getWorldByName(player.getWorld().getName());
        if (world == null) {
            return new ArrayList<>();
        }

        // Only show tab completion for world owners
        if (!world.getOwnerId().equals(player.getUniqueId())) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list", "info", "gui").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("info")) {
                // Only show players that are trusted in the current world
                List<TrustedPlayer> trustedPlayers = plugin.getServiceRegistry().getTrustedPlayerManager().getTrustedPlayers(world);
                return trustedPlayers.stream()
                        .map(trusted -> Bukkit.getOfflinePlayer(trusted.getPlayerId()).getName())
                        .filter(Objects::nonNull)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("add")) {
                // Show all online players except the sender and those already trusted
                List<UUID> trustedPlayerIds = plugin.getServiceRegistry().getTrustedPlayerManager().getTrustedPlayers(world).stream()
                        .map(TrustedPlayer::getPlayerId)
                        .collect(Collectors.toList());
                
                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.getUniqueId().equals(player.getUniqueId())) // Exclude self
                        .filter(p -> !trustedPlayerIds.contains(p.getUniqueId())) // Exclude already trusted
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}