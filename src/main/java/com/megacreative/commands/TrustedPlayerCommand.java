package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.TrustedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Команда для управления доверенными игроками
 */
public class TrustedPlayerCommand implements CommandExecutor, TabCompleter {

    private final MegaCreative plugin;

    public TrustedPlayerCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }

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

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§c❌ Использование: /trusted add <игрок> <тип> <добавил>");
            sender.sendMessage("§7Типы: §fBUILDER§7, §fCODER");
            return true;
        }

        String playerName = args[1];
        String typeStr = args[2].toUpperCase();
        String addedBy = args[3];

        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            sender.sendMessage("§c❌ Игрок " + playerName + " не найден!");
            return true;
        }

        TrustedPlayer.TrustedPlayerType type;
        try {
            type = TrustedPlayer.TrustedPlayerType.valueOf("TRUSTED_" + typeStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c❌ Неверный тип! Используйте: §fBUILDER§7 или §fCODER");
            return true;
        }

        if (plugin.getTrustedPlayerManager().isTrustedPlayer(targetPlayer)) {
            sender.sendMessage("§c❌ Игрок " + playerName + " уже является доверенным!");
            return true;
        }

        plugin.getTrustedPlayerManager().addTrustedPlayer(targetPlayer, type, addedBy);
        sender.sendMessage("§a✅ Игрок " + playerName + " добавлен как " + type.getDisplayName());
        targetPlayer.sendMessage("§a✅ Вы получили права " + type.getDisplayName() + "!");
        
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c❌ Использование: /trusted remove <игрок>");
            return true;
        }

        String playerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(playerName);
        
        if (targetPlayer == null) {
            sender.sendMessage("§c❌ Игрок " + playerName + " не найден!");
            return true;
        }

        if (!plugin.getTrustedPlayerManager().isTrustedPlayer(targetPlayer)) {
            sender.sendMessage("§c❌ Игрок " + playerName + " не является доверенным!");
            return true;
        }

        plugin.getTrustedPlayerManager().removeTrustedPlayer(targetPlayer.getUniqueId());
        sender.sendMessage("§a✅ Игрок " + playerName + " удален из доверенных!");
        targetPlayer.sendMessage("§c❌ Ваши права доверенного игрока были отозваны!");
        
        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        List<TrustedPlayer> allTrusted = plugin.getTrustedPlayerManager().getAllTrustedPlayers();
        
        if (allTrusted.isEmpty()) {
            sender.sendMessage("§7Список доверенных игроков пуст.");
            return true;
        }

        sender.sendMessage("§e=== Список доверенных игроков ===");
        
        List<TrustedPlayer> builders = plugin.getTrustedPlayerManager().getTrustedBuilders();
        List<TrustedPlayer> coders = plugin.getTrustedPlayerManager().getTrustedCoders();

        if (!builders.isEmpty()) {
            sender.sendMessage("§6Доверенные строители:");
            for (TrustedPlayer trusted : builders) {
                sender.sendMessage("§7  - §f" + trusted.getPlayerName() + " §7(добавил: §f" + trusted.getAddedBy() + "§7)");
            }
        }

        if (!coders.isEmpty()) {
            sender.sendMessage("§bДоверенные программисты:");
            for (TrustedPlayer trusted : coders) {
                sender.sendMessage("§7  - §f" + trusted.getPlayerName() + " §7(добавил: §f" + trusted.getAddedBy() + "§7)");
            }
        }

        sender.sendMessage("§7Всего: §f" + allTrusted.size() + " игроков");
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c❌ Использование: /trusted info <игрок>");
            return true;
        }

        String playerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(playerName);
        
        if (targetPlayer == null) {
            sender.sendMessage("§c❌ Игрок " + playerName + " не найден!");
            return true;
        }

        TrustedPlayer trustedPlayer = plugin.getTrustedPlayerManager().getTrustedPlayer(targetPlayer.getUniqueId());
        
        if (trustedPlayer == null) {
            sender.sendMessage("§7Игрок " + playerName + " не является доверенным.");
            return true;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String addedDate = sdf.format(new Date(trustedPlayer.getAddedAt()));

        sender.sendMessage("§e=== Информация о доверенном игроке ===");
        sender.sendMessage("§7Имя: §f" + trustedPlayer.getPlayerName());
        sender.sendMessage("§7Тип: §f" + trustedPlayer.getType().getDisplayName());
        sender.sendMessage("§7Добавлен: §f" + addedDate);
        sender.sendMessage("§7Добавил: §f" + trustedPlayer.getAddedBy());
        
        return true;
    }

    private boolean handleGUI(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c❌ Эта команда доступна только игрокам!");
            return true;
        }

        Player player = (Player) sender;
        new com.megacreative.gui.TrustedPlayersGUI(plugin, player).open();
        player.sendMessage("§a✅ Открыто меню управления доверенными игроками!");
        
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e=== Команды управления доверенными игроками ===");
        sender.sendMessage("§7/trusted add <игрок> <тип> <добавил> §f- Добавить доверенного игрока");
        sender.sendMessage("§7/trusted remove <игрок> §f- Удалить доверенного игрока");
        sender.sendMessage("§7/trusted list §f- Показать список доверенных игроков");
        sender.sendMessage("§7/trusted info <игрок> §f- Информация о доверенном игроке");
        sender.sendMessage("§7/trusted gui §f- Открыть GUI управления");
        sender.sendMessage("§7Типы: §fBUILDER§7 (строитель), §fCODER§7 (программист)");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("megacreative.trusted")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list", "info", "gui").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("info")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            return Arrays.asList("BUILDER", "CODER").stream()
                    .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
} 