package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.placeholders.ReferenceSystemPlaceholderResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для демонстрации работы с плейсхолдерами в стиле Reference System
 * Поддерживает тестирование различных форматов плейсхолдеров
 * Управление переменными и контекстом выполнения
 *
 * Command for demonstrating placeholder functionality in Reference System style
 * Supports testing various placeholder formats
 * Variable and execution context management
 *
 * Befehl zur Demonstration der Platzhalterfunktionalität im Reference System-Stil
 * Unterstützt das Testen verschiedener Platzhalterformate
 * Variablen- und Ausführungskontextverwaltung
 */
public class PlaceholdersCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    
    /**
     * Инициализирует команду плейсхолдеров с необходимыми зависимостями
     * @param plugin основной экземпляр плагина
     * @param worldManager менеджер мира для управления мирами
     *
     * Initializes the placeholder command with required dependencies
     * @param plugin main plugin instance
     * @param worldManager world manager for world management
     *
     * Initialisiert den Platzhalterbefehl mit den erforderlichen Abhängigkeiten
     * @param plugin Haupt-Plugin-Instanz
     * @param worldManager Weltmanager für die Weltverwaltung
     */
    public PlaceholdersCommand(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }
    
    /**
     * Обрабатывает выполнение команды плейсхолдеров
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles placeholder command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Platzhalterbefehls
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
        
        switch (args[0].toLowerCase()) {
            case "demo":
                showDemo(player);
                break;
            case "test":
                if (args.length > 1) {
                    testPlaceholder(player, String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)));
                } else {
                    player.sendMessage("§cИспользование: /placeholders test <текст с плейсхолдерами>");
                }
                break;
            case "help":
                showHelp(player);
                break;
            case "examples":
                showExamples(player);
                break;
            default:
                
                testPlaceholder(player, String.join(" ", args));
                break;
        }
        
        return true;
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
        player.sendMessage("§8§m                    §r §6§lPlaceholder System §8§m                    ");
        player.sendMessage("§e🎆 Reference System-Style Placeholder System");
        player.sendMessage("");
        player.sendMessage("§7§lКоманды:");
        player.sendMessage("§e/placeholders demo §8- §fПоказать демонстрацию");
        player.sendMessage("§e/placeholders test <текст> §8- §fПротестировать плейсхолдеры");
        player.sendMessage("§e/placeholders examples §8- §fПоказать примеры");
        player.sendMessage("§e/placeholders help §8- §fПоказать эту справку");
        player.sendMessage("");
        player.sendMessage("§7§lОсновные форматы:");
        player.sendMessage("§a• Reference System: §fprefix[content]~");
        player.sendMessage("§a• Modern: §f${variable}");
        player.sendMessage("§a• Classic: §f%variable%");
        player.sendMessage("§8§m                                                        ");
    }
    
    /**
     * Показывает демонстрацию работы плейсхолдеров
     * @param player игрок, для которого показывается демонстрация
     *
     * Shows placeholder functionality demonstration
     * @param player player for whom demonstration is shown
     *
     * Zeigt eine Demonstration der Platzhalterfunktionalität
     * @param player Spieler, für den die Demonstration angezeigt wird
     */
    private void showDemo(Player player) {
        
        plugin.getServiceRegistry().getVariableManager().setPlayerVariable(
            player.getUniqueId(), "demo_score", DataValue.of("1500"));
        plugin.getServiceRegistry().getVariableManager().setPlayerVariable(
            player.getUniqueId(), "demo_level", DataValue.of("25"));
        plugin.getServiceRegistry().getVariableManager().setPlayerVariable(
            player.getUniqueId(), "demo_money", DataValue.of("12345.67"));
            
        
        CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());
        ExecutionContext context = new ExecutionContext(plugin, player, world, null, null, null);
        
        player.sendMessage("§8§m                    §r §6§lPlaceholder Demo §8§m                    ");
        
        
        String[] demoTexts = {
            "color[gold]~=== player[name]~'s Profile ===",
            "color[green]~❤ Health: player[health]~/player[max_health]~",
            "color[blue]~📍 Location: location[formatted]~ in world[name]~",
            "color[yellow]~🎆 Score: apple[demo_score]~ points",
            "color[cyan]~💰 Money: format[apple[demo_money]~|currency]~",
            "color[purple]~⬆ Level: apple[demo_level]~ (player[level]~ exp)",
            "color[red]~🎲 Random: random[1-100]~",
            "color[green]~🕒 Time: time[HH:mm:ss]~",
            "color[aqua]~🧮 Math: math[apple[demo_score]~/10]~ per level",
            "color[reset]~Mixed: apple[demo_score]~, ${player_name}, %world%"
        };
        
        for (String text : demoTexts) {
            String resolved = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
            player.sendMessage(resolved);
        }
        
        player.sendMessage("§8§m                                                        ");
        player.sendMessage("§7Переменные demo_score, demo_level, demo_money были установлены для демо");
    }
    
    /**
     * Тестирует работу плейсхолдеров с заданным текстом
     * @param player игрок, для которого выполняется тест
     * @param text текст с плейсхолдерами для тестирования
     *
     * Tests placeholder functionality with given text
     * @param player player for whom test is performed
     * @param text text with placeholders to test
     *
     * Testet die Platzhalterfunktionalität mit dem angegebenen Text
     * @param player Spieler, für den der Test durchgeführt wird
     * @param text Text mit Platzhaltern zum Testen
     */
    private void testPlaceholder(Player player, String text) {
        
        CreativeWorld world = worldManager.findCreativeWorldByBukkit(player.getWorld());
        ExecutionContext context = new ExecutionContext(plugin, player, world, null, null, null);
        
        player.sendMessage("§8§m                    §r §6§lPlaceholder Test §8§m                    ");
        player.sendMessage("§7Исходный текст:");
        player.sendMessage("§f" + text);
        player.sendMessage("");
        player.sendMessage("§7Результат:");
        
        String resolved = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        player.sendMessage(resolved);
        
        player.sendMessage("§8§m                                                        ");
    }
    
    /**
     * Показывает примеры использования плейсхолдеров
     * @param player игрок, которому показываются примеры
     *
     * Shows placeholder usage examples
     * @param player player to show examples to
     *
     * Zeigt Beispiele für die Verwendung von Platzhaltern an
     * @param player Spieler, dem die Beispiele angezeigt werden
     */
    private void showExamples(Player player) {
        player.sendMessage("§8§m                    §r §6§lPlaceholder Examples §8§m                    ");
        
        String[] examples = {
            "§7Переменные:",
            "§f  apple[score]~ - §7Значение переменной",
            "§f  apple[missing|Нет данных]~ - §7С значением по умолчанию",
            "",
            "§7Игрок:",
            "§f  player[name]~ - §7Имя игрока",
            "§f  player[health]~/player[max_health]~ - §7Здоровье",
            "§f  player[level]~ - §7Уровень",
            "",
            "§7Математика:",
            "§f  math[5+3]~ - §7Простые вычисления",
            "§f  math[apple[score]~*2]~ - §7С переменными",
            "",
            "§7Форматирование:",
            "§f  format[1234.567|2]~ - §771234.57",
            "§f  format[apple[money]~|currency]~ - §7$1234.57",
            "",
            "§7Цвета:",
            "§f  color[red]~Красный color[green]~Зелёный color[reset]~Обычный",
            "",
            "§7Время:",
            "§f  time[HH:mm]~ - §715:30",
            "§f  time[date]~ - §772023-12-25",
            "",
            "§7Случайные числа:",
            "§f  random[1-100]~ - §7От 1 до 100",
            "§f  random[10]~ - §7От 0 до 10"
        };
        
        for (String example : examples) {
            player.sendMessage(example);
        }
        
        player.sendMessage("§8§m                                                        ");
        player.sendMessage("§7Используйте §e/placeholders test <текст> §7чтобы протестировать!");
    }
}