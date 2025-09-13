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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🎆 Reference System-Style Interactive GUI Command
 * 
 * Demonstrates and tests the interactive GUI system with various examples.
 * Usage: /interactive <demo|block|world|test> [args...]
 */
public class InteractiveCommand implements CommandExecutor, TabCompleter {
    
    private final MegaCreative plugin;
    private final InteractiveGUIManager guiManager;
    private final ReferenceSystemStyleGUI frameGUI;
    
    public InteractiveCommand(MegaCreative plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getServiceRegistry().getInteractiveGUIManager();
        this.frameGUI = plugin.getServiceRegistry().getReferenceSystemStyleGUI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
     * Shows command help
     */
    private void showHelp(Player player) {
        player.sendMessage("§6🎆 Reference System Interactive GUI System");
        player.sendMessage("§e/interactive demo §7- Open interactive element demo");
        player.sendMessage("§e/interactive block <action> §7- Open block parameter editor");
        player.sendMessage("§e/interactive world §7- Open world settings GUI");
        player.sendMessage("§e/interactive test <element> §7- Test specific element type");
        player.sendMessage("§e/interactive help §7- Show this help");
        player.sendMessage("");
        player.sendMessage("§7Available test elements: material, toggle, slider, color, item");
    }
    
    /**
     * Opens the interactive element demonstration GUI
     */
    private void openDemoGUI(Player player) {
        InteractiveGUI gui = guiManager.createInteractiveGUI(player, 
            "🎆 Reference System Interactive Demo", 54);
        
        // Material selector demo
        InteractiveGUIManager.MaterialSelectorElement materialSelector = 
            new InteractiveGUIManager.MaterialSelectorElement("demo_material", 
                java.util.Map.of("materials", Arrays.asList(
                    Material.STONE, Material.DIRT, Material.GRASS_BLOCK, 
                    Material.OAK_PLANKS, Material.IRON_BLOCK, Material.GOLD_BLOCK)));
        
        materialSelector.addChangeListener(value -> 
            player.sendMessage("§a🎆 Material changed to: §e" + value.getValue()));
        
        gui.setElement(10, materialSelector);
        
        // Mode toggle demo
        InteractiveGUIManager.ModeToggleElement modeToggle = 
            new InteractiveGUIManager.ModeToggleElement("demo_mode", 
                java.util.Map.of("modes", Arrays.asList("ON", "OFF", "AUTO")));
        
        modeToggle.addChangeListener(value -> 
            player.sendMessage("§a🎆 Mode changed to: §e" + value.getValue()));
        
        gui.setElement(12, modeToggle);
        
        // Number slider demo
        InteractiveGUIManager.NumberSliderElement numberSlider = 
            new InteractiveGUIManager.NumberSliderElement("demo_number", 
                java.util.Map.of("min", 0.0, "max", 100.0, "step", 5.0, "value", 50.0));
        
        numberSlider.addChangeListener(value -> 
            player.sendMessage("§a🎆 Number changed to: §e" + value.getValue()));
        
        gui.setElement(14, numberSlider);
        
        // Color picker demo
        InteractiveGUIManager.ColorPickerElement colorPicker = 
            new InteractiveGUIManager.ColorPickerElement("demo_color", java.util.Map.of());
        
        colorPicker.addChangeListener(value -> 
            player.sendMessage("§a🎆 Color changed to: §e" + value.getValue()));
        
        gui.setElement(16, colorPicker);
        
        // Item editor demo
        InteractiveGUIManager.ItemStackEditorElement itemEditor = 
            new InteractiveGUIManager.ItemStackEditorElement("demo_item", java.util.Map.of());
        
        itemEditor.addChangeListener(value -> 
            player.sendMessage("§a🎆 Item changed to: §e" + value.getValue()));
        
        gui.setElement(28, itemEditor);
        
        gui.open();
        player.sendMessage("§a🎆 Opened Reference System Interactive Demo!");
    }
    
    /**
     * Opens block parameter editor
     */
    private void openBlockEditor(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /interactive block <action>");
            return;
        }
        
        String actionId = args[1];
        
        // Create a dummy code block for demonstration
        Location location = player.getLocation();
        CodeBlock dummyBlock = new CodeBlock(Material.STONE, actionId);
        // Note: CodeBlock doesn't have setLocation method in current implementation
        // Location will be handled by the block placement system
        
        // Add some example parameters
        dummyBlock.setParameter("message", "Hello World!");
        dummyBlock.setParameter("amount", 1);
        dummyBlock.setParameter("enabled", true);
        
        InteractiveGUI gui = frameGUI.createBlockParameterEditor(player, dummyBlock);
        gui.open();
        
        player.sendMessage("§a🎆 Opened block editor for: §e" + actionId);
    }
    
    /**
     * Opens world settings GUI
     */
    private void openWorldSettings(Player player) {
        // Find current creative world
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world == null) {
            player.sendMessage("§cYou must be in a creative world to use world settings!");
            return;
        }
        
        InteractiveGUI gui = frameGUI.createWorldSettingsGUI(player, world);
        gui.open();
        
        player.sendMessage("§a🎆 Opened world settings for: §e" + world.getName());
    }
    
    /**
     * Opens test GUI for specific element type
     */
    private void openTestGUI(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /interactive test <element_type>");
            player.sendMessage("§7Available: material, toggle, slider, color, item, text");
            return;
        }
        
        String elementType = args[1].toLowerCase();
        
        InteractiveGUI gui = guiManager.createInteractiveGUI(player, 
            "🎆 Test: " + elementType.toUpperCase(), 27);
        
        InteractiveGUIManager.InteractiveElement element = null;
        
        switch (elementType) {
            case "material":
                element = new InteractiveGUIManager.MaterialSelectorElement("test", 
                    java.util.Map.of("materials", Arrays.asList(
                        Material.STONE, Material.DIRT, Material.OAK_PLANKS, Material.IRON_BLOCK)));
                break;
            case "toggle":
                element = new InteractiveGUIManager.ModeToggleElement("test", 
                    java.util.Map.of("modes", Arrays.asList("ENABLED", "DISABLED", "AUTO")));
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
        
        if (element != null) {
            element.addChangeListener(value -> 
                player.sendMessage("§a🎆 " + elementType + " value: §e" + value.getValue()));
            
            gui.setElement(13, element);
            gui.open();
            
            player.sendMessage("§a🎆 Opened test GUI for: §e" + elementType);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Main command completions
            List<String> commands = Arrays.asList("demo", "block", "world", "test", "help");
            return commands.stream()
                .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "block":
                    // Action ID completions
                    return Arrays.asList("sendMessage", "teleport", "giveItem", "playSound", "effect")
                        .stream()
                        .filter(action -> action.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                        
                case "test":
                    // Element type completions
                    return Arrays.asList("material", "toggle", "slider", "color", "item", "text")
                        .stream()
                        .filter(type -> type.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}