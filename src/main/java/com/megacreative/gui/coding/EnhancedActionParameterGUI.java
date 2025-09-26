package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.gui.interactive.InteractiveGUI;
import com.megacreative.gui.interactive.InteractiveGUIManager;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 🎆 Enhanced Reference System-Style Action Parameter GUI
 * 
 * Combines the power of InteractiveGUI with action parameter configuration.
 * Provides dynamic, real-time parameter editing with visual feedback.
 *
 * 🎆 Улучшенный графический интерфейс параметров действий в стиле Reference System
 * 
 * Объединяет мощность InteractiveGUI с настройкой параметров действий.
 * Обеспечивает динамическое редактирование параметров в реальном времени с визуальной обратной связью.
 *
 * 🎆 Erweiterte Reference System-Stil Aktionsparameter-GUI
 * 
 * Kombiniert die Leistung von InteractiveGUI mit der Konfiguration von Aktionsparametern.
 * Bietet dynamische, Echtzeit-Parameterbearbeitung mit visueller Rückmeldung.
 */
public class EnhancedActionParameterGUI {
    
    private final MegaCreative plugin;
    private final InteractiveGUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    /**
     * Initializes enhanced action parameter GUI
     * @param plugin Reference to main plugin
     */
    public EnhancedActionParameterGUI(MegaCreative plugin) {
        this.plugin = plugin;
        this.guiManager = new InteractiveGUIManager(plugin);
        
        // Add null check for service registry
        if (plugin != null && plugin.getServiceRegistry() != null) {
            this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        } else {
            this.blockConfigService = null;
        }
    }
    
    /**
     * Creates an enhanced parameter editor for a code block
     */
    public InteractiveGUI createParameterEditor(Player player, Location blockLocation, String actionId) {
        // Get the code block
        CodeBlock block = getCodeBlock(blockLocation);
        if (block == null) {
            player.sendMessage("§cError: Code block not found at location");
            return null;
        }
        
        // Create interactive GUI with enhanced design
        InteractiveGUI gui = guiManager.createInteractiveGUI(player, 
            "§8🎆 " + actionId + " Parameters", 54);
        
        // Add decorative border with category-specific materials
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);
        
        // Fill border slots
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                gui.getInventory().setItem(i, borderItem);
            }
        }
        
        // Add title display with enhanced visual design
        gui.getInventory().setItem(4, createTitleItem(actionId, block));
        
        // Setup parameter editors based on action type
        setupParameterEditors(gui, actionId, block, player);
        
        // Add control buttons with enhanced design
        setupControlButtons(gui, block, player, blockLocation);
        
        return gui;
    }
    
    /**
     * Sets up parameter editors based on action configuration
     */
    private void setupParameterEditors(InteractiveGUI gui, String actionId, CodeBlock block, Player player) {
        // Get action configuration
        var actionConfigurations = blockConfigService != null ? blockConfigService.getActionConfigurations() : null;
        if (actionConfigurations == null) {
            setupGenericParameterEditors(gui, block);
            return;
        }
        
        var actionConfig = actionConfigurations.getConfigurationSection(actionId);
        if (actionConfig == null) {
            setupGenericParameterEditors(gui, block);
            return;
        }
        
        // Setup specific parameter editors based on configuration
        var parametersConfig = actionConfig.getConfigurationSection("parameters");
        if (parametersConfig != null) {
            setupConfiguredParameterEditors(gui, parametersConfig, block);
        } else {
            setupGenericParameterEditors(gui, block);
        }
    }
    
    /**
     * Sets up parameter editors from configuration
     */
    private void setupConfiguredParameterEditors(InteractiveGUI gui, 
                                               org.bukkit.configuration.ConfigurationSection parametersConfig, 
                                               CodeBlock block) {
        int slot = 10; // Start position
        
        for (String paramName : parametersConfig.getKeys(false)) {
            var paramConfig = parametersConfig.getConfigurationSection(paramName);
            if (paramConfig == null) continue;
            
            String paramType = paramConfig.getString("type", "string");
            String displayName = paramConfig.getString("display_name", paramName);
            String description = paramConfig.getString("description", "Parameter: " + paramName);
            
            // Create appropriate interactive element based on type
            InteractiveGUIManager.InteractiveElement element = createParameterElement(
                paramName, paramType, paramConfig, block);
            
            if (element != null) {
                // Bind to block parameter
                element.addChangeListener(value -> {
                    block.setParameter(paramName, value.getValue());
                    saveBlockToWorld(block);
                });
                
                gui.setElement(slot, element);
                slot += 2; // Space out elements
                
                if (slot >= 44) break; // Don't overflow into control area
            }
        }
    }
    
    /**
     * Creates parameter element based on type
     */
    private InteractiveGUIManager.InteractiveElement createParameterElement(String paramName, 
                                                                          String paramType, 
                                                                          org.bukkit.configuration.ConfigurationSection paramConfig,
                                                                          CodeBlock block) {
        Map<String, Object> properties = new HashMap<>();
        
        // Convert config to properties map
        for (String key : paramConfig.getKeys(false)) {
            properties.put(key, paramConfig.get(key));
        }
        
        // Set current value if exists
        Object currentValue = block.getParameter(paramName);
        if (currentValue != null) {
            properties.put("value", currentValue);
        }
        
        // Create element based on type
        switch (paramType.toLowerCase()) {
            case "material":
                return createMaterialSelector(paramName, properties);
            case "number":
            case "integer":
            case "double":
                return createNumberSlider(paramName, properties);
            case "boolean":
                return createBooleanToggle(paramName, properties);
            case "color":
                return createColorPicker(paramName, properties);
            case "item":
            case "itemstack":
                return createItemEditor(paramName, properties);
            case "text":
            case "string":
            default:
                return createTextInput(paramName, properties);
        }
    }
    
    /**
     * Creates material selector element
     */
    private InteractiveGUIManager.InteractiveElement createMaterialSelector(String paramName, Map<String, Object> properties) {
        // Setup available materials
        Object materialsObj = properties.get("options");
        if (materialsObj instanceof List) {
            properties.put("materials", materialsObj);
        } else {
            // Default materials for different parameter types
            switch (paramName.toLowerCase()) {
                case "block":
                case "material":
                    properties.put("materials", Arrays.asList(
                        Material.STONE, Material.DIRT, Material.GRASS_BLOCK,
                        Material.OAK_PLANKS, Material.COBBLESTONE, Material.SAND
                    ));
                    break;
                case "tool":
                    properties.put("materials", Arrays.asList(
                        Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
                        Material.DIAMOND_SWORD, Material.NETHERITE_SWORD
                    ));
                    break;
                default:
                    properties.put("materials", Arrays.asList(
                        Material.STONE, Material.DIRT, Material.OAK_PLANKS, Material.IRON_INGOT
                    ));
            }
        }
        
        return new InteractiveGUIManager.MaterialSelectorElement(paramName, properties);
    }
    
    /**
     * Creates number slider element
     */
    private InteractiveGUIManager.InteractiveElement createNumberSlider(String paramName, Map<String, Object> properties) {
        // Set defaults based on parameter name
        switch (paramName.toLowerCase()) {
            case "amount":
            case "count":
                properties.putIfAbsent("min", 1.0);
                properties.putIfAbsent("max", 64.0);
                properties.putIfAbsent("step", 1.0);
                break;
            case "time":
            case "delay":
                properties.putIfAbsent("min", 0.0);
                properties.putIfAbsent("max", 600.0);
                properties.putIfAbsent("step", 1.0);
                break;
            case "distance":
            case "radius":
                properties.putIfAbsent("min", 0.0);
                properties.putIfAbsent("max", 100.0);
                properties.putIfAbsent("step", 0.5);
                break;
            default:
                properties.putIfAbsent("min", 0.0);
                properties.putIfAbsent("max", 100.0);
                properties.putIfAbsent("step", 1.0);
        }
        
        return new InteractiveGUIManager.NumberSliderElement(paramName, properties);
    }
    
    /**
     * Creates boolean toggle element
     */
    private InteractiveGUIManager.InteractiveElement createBooleanToggle(String paramName, Map<String, Object> properties) {
        properties.putIfAbsent("modes", Arrays.asList("TRUE", "FALSE"));
        return new InteractiveGUIManager.ModeToggleElement(paramName, properties);
    }
    
    /**
     * Creates color picker element
     */
    private InteractiveGUIManager.InteractiveElement createColorPicker(String paramName, Map<String, Object> properties) {
        return new InteractiveGUIManager.ColorPickerElement(paramName, properties);
    }
    
    /**
     * Creates item editor element
     */
    private InteractiveGUIManager.InteractiveElement createItemEditor(String paramName, Map<String, Object> properties) {
        return new InteractiveGUIManager.ItemStackEditorElement(paramName, properties);
    }
    
    /**
     * Creates text input element
     */
    private InteractiveGUIManager.InteractiveElement createTextInput(String paramName, Map<String, Object> properties) {
        return new InteractiveGUIManager.TextInputElement(paramName, properties);
    }
    
    /**
     * Sets up generic parameter editors for unknown actions
     */
    private void setupGenericParameterEditors(InteractiveGUI gui, CodeBlock block) {
        // Common parameters for most actions
        
        // Message parameter (common for many actions)
        Map<String, Object> messageProps = new HashMap<>();
        messageProps.put("value", block.getParameterValue("message", String.class, ""));
        InteractiveGUIManager.TextInputElement messageInput = 
            new InteractiveGUIManager.TextInputElement("message", messageProps);
        messageInput.addChangeListener(value -> {
            block.setParameter("message", value.getValue());
            saveBlockToWorld(block);
        });
        gui.setElement(10, messageInput);
        
        // Amount parameter
        Map<String, Object> amountProps = new HashMap<>();
        amountProps.put("min", 1.0);
        amountProps.put("max", 64.0);
        amountProps.put("value", 1.0);
        InteractiveGUIManager.NumberSliderElement amountSlider = 
            new InteractiveGUIManager.NumberSliderElement("amount", amountProps);
        amountSlider.addChangeListener(value -> {
            block.setParameter("amount", value.getValue());
            saveBlockToWorld(block);
        });
        gui.setElement(12, amountSlider);
        
        // Enabled toggle
        Map<String, Object> enabledProps = new HashMap<>();
        enabledProps.put("modes", Arrays.asList("ENABLED", "DISABLED"));
        InteractiveGUIManager.ModeToggleElement enabledToggle = 
            new InteractiveGUIManager.ModeToggleElement("enabled", enabledProps);
        enabledToggle.addChangeListener(value -> {
            block.setParameter("enabled", "ENABLED".equals(value.getValue()));
            saveBlockToWorld(block);
        });
        gui.setElement(14, enabledToggle);
    }
    
    /**
     * Sets up control buttons with enhanced design
     */
    private void setupControlButtons(InteractiveGUI gui, CodeBlock block, Player player, Location blockLocation) {
        // Save button with enhanced visual design
        gui.getInventory().setItem(45, createSaveButton());
        
        // Cancel button with enhanced visual design
        gui.getInventory().setItem(53, createCancelButton());
        
        // Reset button with enhanced visual design
        gui.getInventory().setItem(49, createResetButton());
        
        // Help button with enhanced visual design
        gui.getInventory().setItem(48, createHelpButton(block.getAction()));
        
        // Add back button
        gui.getInventory().setItem(46, createBackButton());
    }
    
    /**
     * Helper methods for creating control buttons with enhanced design
     */
    
    private ItemStack createTitleItem(String actionId, CodeBlock block) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6🎆 Reference System Parameter Editor");
            List<String> lore = new ArrayList<>();
            lore.add("§7Action: §e" + actionId);
            lore.add("§7Block: §f" + block.getMaterial().name());
            lore.add("§7Parameters: §f" + block.getParameters().size());
            lore.add("");
            lore.add("§a✨ Real-time parameter editing");
            lore.add("§a🎆 Reference System-style interface");
            lore.add("§7Use the interactive elements below");
            lore.add("");
            lore.add("§f✨ Reference system-стиль: универсальные блоки");
            lore.add("§fс настройкой через GUI");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createSaveButton() {
        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a🎆 Save & Apply");
            List<String> lore = new ArrayList<>();
            lore.add("§7Save all parameter changes");
            lore.add("§7and apply them to the block");
            lore.add("");
            lore.add("§eChanges are auto-saved on edit");
            lore.add("");
            lore.add("§f✨ Reference system-стиль: универсальные блоки");
            lore.add("§fс настройкой через GUI");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§c erotik Close Editor");
            List<String> lore = new ArrayList<>();
            lore.add("§7Close the parameter editor");
            lore.add("§7Changes are already saved");
            lore.add("");
            lore.add("§f✨ Reference system-стиль: универсальные блоки");
            lore.add("§fс настройкой через GUI");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createResetButton() {
        ItemStack item = new ItemStack(Material.ORANGE_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6 erotik Reset Parameters");
            List<String> lore = new ArrayList<>();
            lore.add("§7Reset all parameters");
            lore.add("§7to their default values");
            lore.add("");
            lore.add("§cThis cannot be undone!");
            lore.add("");
            lore.add("§f✨ Reference system-стиль: универсальные блоки");
            lore.add("§fс настройкой через GUI");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createHelpButton(String actionId) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§b erotik Help: " + actionId);
            List<String> lore = new ArrayList<>();
            lore.add("§7Get help for this action type");
            lore.add("§7and its parameters");
            lore.add("");
            lore.add("§eClick for detailed help");
            lore.add("");
            lore.add("§f✨ Reference system-стиль: универсальные блоки");
            lore.add("§fс настройкой через GUI");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§c⬅ Назад");
            List<String> lore = new ArrayList<>();
            lore.add("§7Вернуться к предыдущему меню");
            lore.add("");
            lore.add("§f✨ Reference system-стиль: универсальные блоки");
            lore.add("§fс настройкой через GUI");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Helper methods
     */
    
    private CodeBlock getCodeBlock(Location location) {
        var placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
        return placementHandler.getCodeBlock(location);
    }
    
    private void saveBlockToWorld(CodeBlock block) {
        // Save the world to persist changes
        var worldManager = plugin.getWorldManager();
        Location blockLocation = new org.bukkit.Location(org.bukkit.Bukkit.getWorld(block.getWorldId()), block.getX(), block.getY(), block.getZ());
        com.megacreative.models.CreativeWorld world = worldManager.findCreativeWorldByBukkit(blockLocation.getWorld());
        if (world != null) {
            worldManager.saveWorld(world);
        }
    }
    
    /**
     * Opens the enhanced parameter editor
     */
    public void openParameterEditor(Player player, Location blockLocation, String actionId) {
        InteractiveGUI gui = createParameterEditor(player, blockLocation, actionId);
        if (gui != null) {
            gui.open();
        }
    }
    
    /**
     * Gets the interactive GUI manager
     */
    public InteractiveGUIManager getGUIManager() {
        return guiManager;
    }
}