package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.gui.interactive.InteractiveGUI;
import com.megacreative.gui.interactive.InteractiveGUIManager;
import com.megacreative.gui.interactive.ReferenceSystemStyleGUI;
import com.megacreative.coding.CodeBlock;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Команда интерактивного GUI в стиле Reference System
 * Демонстрирует и тестирует систему интерактивного GUI с различными примерами
 * Использование: /interactive <demo|block|world|test> [аргументы...]
 *
 * Reference System-Style Interactive GUI Command
 * Demonstrates and tests the interactive GUI system with various examples
 * Usage: /interactive <demo|block|world|test> [args...]
 *
 * Reference System-Stil interaktiver GUI-Befehl
 * Demonstriert und testet das interaktive GUI-System mit verschiedenen Beispielen
 * Verwendung: /interactive <demo|block|world|test> [Argumente...]
 */
public class InteractiveCommand implements CommandExecutor, TabCompleter {
    
    private final MegaCreative plugin;
    private final InteractiveGUIManager guiManager;
    private final ReferenceSystemStyleGUI frameGUI;
    
    /**
     * Инициализирует команду интерактивного GUI
     * @param plugin основной экземпляр плагина
     *
     * Initializes the interactive GUI command
     * @param plugin main plugin instance
     *
     * Initialisiert den interaktiven GUI-Befehl
     * @param plugin Haupt-Plugin-Instanz
     */
    public InteractiveCommand(MegaCreative plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getServiceRegistry().getInteractiveGUIManager();
        this.frameGUI = plugin.getServiceRegistry().getReferenceSystemStyleGUI();
    }

    /**
     * Обрабатывает выполнение команды интерактивного GUI
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles interactive GUI command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des interaktiven GUI-Befehls
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, 
                            @NotNull Command command, 
                            @NotNull String label, 
                            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "demo":
                openDemoGUI(player);
                break;
            case "block":
                openBlockEditor(player, args);
                break;
            case "world":
                openWorldSettings(player);
                break;
            case "test":
                openTestGUI(player, args);
                break;
            case "help":
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    /**
     * Отображает справочную информацию по команде
     * @param player игрок, которому отправляется справка
     *
     * Shows command help
     * @param player player to send help to
     *
     * Zeigt Befehlshilfe an
     * @param player Spieler, dem die Hilfe gesendet wird
     */
    private void showHelp(Player player) {
        player.sendMessage("§6.EVT Reference System Interactive GUI System");
        player.sendMessage("§e/interactive demo §7- Open interactive element demo");
        player.sendMessage("§e/interactive block <action> §7- Open block parameter editor");
        player.sendMessage("§e/interactive world §7- Open world settings GUI");
        player.sendMessage("§e/interactive test <element> §7- Test specific element type");
        player.sendMessage("§e/interactive help §7- Show this help");
        player.sendMessage("");
        player.sendMessage("§7Available test elements: material, toggle, slider, color, item");
    }
    
    /**
     * Открывает демонстрационный GUI интерактивных элементов
     * @param player игрок, для которого открывается GUI
     *
     * Opens the interactive element demonstration GUI
     * @param player player for whom GUI is opened
     *
     * Öffnet die interaktive Element-Demonstrations-GUI
     * @param player Spieler, für den die GUI geöffnet wird
     */
    private void openDemoGUI(Player player) {
        InteractiveGUI gui = guiManager.createInteractiveGUI(player, 
            ".EVT Reference System Interactive Demo", 54);
        
        InteractiveGUIManager.MaterialSelectorElement materialSelector = createMaterialSelector();
        
        materialSelector.addChangeListener(value -> 
            player.sendMessage("§a.EVT Material changed to: §e" + value.getValue()));
        
        gui.setElement(10, materialSelector);
        
        InteractiveGUIManager.ModeToggleElement modeToggle = 
            new InteractiveGUIManager.ModeToggleElement("demo_mode", 
                java.util.Map.of("modes", java.util.List.of("ON", "OFF", "AUTO")));
        
        modeToggle.addChangeListener(value -> 
            player.sendMessage("§a.EVT Mode changed to: §e" + value.getValue()));
        
        gui.setElement(12, modeToggle);
        
        InteractiveGUIManager.NumberSliderElement numberSlider = 
            new InteractiveGUIManager.NumberSliderElement("demo_number", 
                java.util.Map.of("min", 0.0, "max", 100.0, "step", 5.0, "value", 50.0));
        
        numberSlider.addChangeListener(value -> 
            player.sendMessage("§a.EVT Number changed to: §e" + value.getValue()));
        
        gui.setElement(14, numberSlider);
        
        InteractiveGUIManager.ColorPickerElement colorPicker = 
            new InteractiveGUIManager.ColorPickerElement("demo_color", java.util.Map.of());
        
        colorPicker.addChangeListener(value -> 
            player.sendMessage("§a.EVT Color changed to: §e" + value.getValue()));
        
        gui.setElement(16, colorPicker);
        
        InteractiveGUIManager.ItemStackEditorElement itemEditor = 
            new InteractiveGUIManager.ItemStackEditorElement("demo_item", java.util.Map.of());
        
        itemEditor.addChangeListener(value -> 
            player.sendMessage("§a.EVT Item changed to: §e" + value.getValue()));
        
        gui.setElement(28, itemEditor);
        
        gui.open();
        player.sendMessage("§a.EVT Opened Reference System Interactive Demo!");
    }
    
    /**
     * Creates a material selector element for the demo GUI
     * @return MaterialSelectorElement instance
     */
    private InteractiveGUIManager.MaterialSelectorElement createMaterialSelector() {
        return new InteractiveGUIManager.MaterialSelectorElement("demo_material", 
                java.util.Map.of("materials", java.util.List.of(
                    Material.STONE, Material.DIRT, Material.GRASS_BLOCK, 
                    Material.OAK_PLANKS, Material.IRON_BLOCK, Material.GOLD_BLOCK)));
    }
    
    /**
     * Открывает редактор параметров блока
     * @param player игрок, для которого открывается редактор
     * @param args аргументы команды
     *
     * Opens block parameter editor
     * @param player player for whom editor is opened
     * @param args command arguments
     *
     * Öffnet den Blockparameter-Editor
     * @param player Spieler, für den der Editor geöffnet wird
     * @param args Befehlsargumente
     */
    private void openBlockEditor(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /interactive block <action>");
            return;
        }
        
        String actionId = args[1];
        
        Location location = player.getLocation();
        CodeBlock block = new CodeBlock(Material.STONE.name(), actionId);
        
        block.setParameter("message", "Hello World!");
        block.setParameter("amount", 1);
        block.setParameter("enabled", true);
        
        InteractiveGUI gui = frameGUI.createBlockParameterEditor(player, block);
        gui.open();
        
        player.sendMessage("§a.EVT Opened block editor for: §e" + actionId);
    }
    
    /**
     * Открывает GUI настроек мира
     * @param player игрок, для которого открываются настройки
     *
     * Opens world settings GUI
     * @param player player for whom settings are opened
     *
     * Öffnet die Welteinstellungs-GUI
     * @param player Spieler, für den die Einstellungen geöffnet werden
     */
    private void openWorldSettings(Player player) {
        
        CreativeWorld world = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world == null) {
            player.sendMessage("§cYou must be in a creative world to use world settings!");
            return;
        }
        
        InteractiveGUI gui = frameGUI.createWorldSettingsGUI(player, world);
        gui.open();
        
        player.sendMessage("§a.EVT Opened world settings for: §e" + world.getName());
    }
    
    /**
     * Открывает тестовый GUI для определенного типа элемента
     * @param player игрок, для которого открывается тестовый GUI
     * @param args аргументы команды
     *
     * Opens test GUI for specific element type
     * @param player player for whom test GUI is opened
     * @param args command arguments
     *
     * Öffnet die Test-GUI für einen bestimmten Elementtyp
     * @param player Spieler, für den die Test-GUI geöffnet wird
     * @param args Befehlsargumente
     */
    private void openTestGUI(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /interactive test <element_type>");
            player.sendMessage("§7Available: material, toggle, slider, color, item, text");
            return;
        }
        
        String elementType = args[1].toLowerCase();
        
        InteractiveGUI gui = guiManager.createInteractiveGUI(player, 
            ".EVT Test: " + elementType.toUpperCase(), 27);
        
        InteractiveGUIManager.InteractiveElement element;
        
        switch (elementType) {
            case "material":
                element = new InteractiveGUIManager.MaterialSelectorElement("test", 
                    java.util.Map.of("materials", java.util.List.of(
                        Material.STONE, Material.DIRT, Material.OAK_PLANKS, Material.IRON_BLOCK)));
                break;
            case "toggle":
                element = new InteractiveGUIManager.ModeToggleElement("test", 
                    java.util.Map.of("modes", java.util.List.of("ENABLED", "DISABLED", "AUTO")));
                break;
            case "slider":
                element = new InteractiveGUIManager.NumberSliderElement("test", 
                    java.util.Map.of("min", 0.0, "max", 50.0, "step", 2.5));
                break;
            case "color":
                element = new InteractiveGUIManager.ColorPickerElement("test", java.util.Map.of());
                break;
            case "item":
                element = new InteractiveGUIManager.ItemStackEditorElement("test", java.util.Map.of());
                break;
            case "text":
                element = new InteractiveGUIManager.TextInputElement("test", 
                    java.util.Map.of("value", "Sample text"));
                break;
            default:
                player.sendMessage("§cUnknown element type: " + elementType);
                return;
        }
        
        // Element is guaranteed to be non-null when we reach this point due to the early return in default case
        element.addChangeListener(value -> 
            player.sendMessage("§a.EVT " + elementType + " value: §e" + value.getValue()));
        
        gui.setElement(13, element);
        gui.open();
        
        player.sendMessage("§a.EVT Opened test GUI for: §e" + elementType);
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
    public List<String> onTabComplete(@NotNull CommandSender sender, 
                                     @NotNull Command command, 
                                     @NotNull String alias, 
                                     @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            
            return java.util.stream.Stream.of("demo", "block", "world", "test", "help")
                .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                .toList();
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "block":
                    
                    return java.util.stream.Stream.of("sendMessage", "teleport", "giveItem", "playSound", "effect")
                        .filter(action -> action.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
                        
                case "test":
                    
                    return java.util.stream.Stream.of("material", "toggle", "slider", "color", "item", "text")
                        .filter(type -> type.startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }
        
        return completions;
    }
}