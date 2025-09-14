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
 * 🎆 Улучшенный графический интерфейс параметров действий в стиле Reference System
 * 
 * Объединяет мощность InteractiveGUI с настройкой параметров действий.
 * Обеспечивает динамическое редактирование параметров в реальном времени с визуальной обратной связью.
 *
 * 🎆 Enhanced Reference System-Style Action Parameter GUI
 * 
 * Combines the power of InteractiveGUI with action parameter configuration.
 * Provides dynamic, real-time parameter editing with visual feedback.
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
     * Инициализирует улучшенный графический интерфейс параметров действий
     * @param plugin Ссылка на основной плагин
     *
     * Initializes enhanced action parameter GUI
     * @param plugin Reference to main plugin
     *
     * Initialisiert die erweiterte Aktionsparameter-GUI
     * @param plugin Referenz zum Haupt-Plugin
     */
    public EnhancedActionParameterGUI(MegaCreative plugin) {
        this.plugin = plugin;
        this.guiManager = new InteractiveGUIManager(plugin);
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
    }
    
    /**
     * Создает улучшенный редактор параметров для блока кода
     *
     * Creates an enhanced parameter editor for a code block
     *
     * Erstellt einen erweiterten Parameter-Editor für einen Codeblock
     */
    public InteractiveGUI createParameterEditor(Player player, Location blockLocation, String actionId) {
        // Get the code block
        CodeBlock block = getCodeBlock(blockLocation);
        if (block == null) {
            player.sendMessage("§cError: Code block not found at location");
            return null;
        }
        
        // Create interactive GUI
        InteractiveGUI gui = guiManager.createInteractiveGUI(player, 
            "🎆 " + actionId + " Parameters", 54);
        
        // Add title display
        gui.getInventory().setItem(4, createTitleItem(actionId, block));
        
        // Setup parameter editors based on action type
        setupParameterEditors(gui, actionId, block, player);
        
        // Add control buttons
        setupControlButtons(gui, block, player, blockLocation);
        
        return gui;
    }
    
    /**
     * Настраивает редакторы параметров на основе конфигурации действия
     *
     * Sets up parameter editors based on action configuration
     *
     * Richtet Parameter-Editoren basierend auf der Aktionskonfiguration ein
     */
    private void setupParameterEditors(InteractiveGUI gui, String actionId, CodeBlock block, Player player) {
        // Get action configuration
        var actionConfigurations = blockConfigService.getActionConfigurations();
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
     * Настраивает редакторы параметров из конфигурации
     *
     * Sets up parameter editors from configuration
     *
     * Richtet Parameter-Editoren aus der Konfiguration ein
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
     * Создает элемент параметра на основе типа
     *
     * Creates parameter element based on type
     *
     * Erstellt Parameterelement basierend auf dem Typ
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
     * Создает элемент выбора материала
     *
     * Creates material selector element
     *
     * Erstellt Materialauswahlelement
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
     * Создает элемент ползунка числа
     *
     * Creates number slider element
     *
     * Erstellt Zahlenschieberelement
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
     * Создает элемент переключателя булевого значения
     *
     * Creates boolean toggle element
     *
     * Erstellt Boolesches Umschaltelement
     */
    private InteractiveGUIManager.InteractiveElement createBooleanToggle(String paramName, Map<String, Object> properties) {
        properties.putIfAbsent("modes", Arrays.asList("TRUE", "FALSE"));
        return new InteractiveGUIManager.ModeToggleElement(paramName, properties);
    }
    
    /**
     * Создает элемент выбора цвета
     *
     * Creates color picker element
     *
     * Erstellt Farbauswahlelement
     */
    private InteractiveGUIManager.InteractiveElement createColorPicker(String paramName, Map<String, Object> properties) {
        return new InteractiveGUIManager.ColorPickerElement(paramName, properties);
    }
    
    /**
     * Создает элемент редактора предмета
     *
     * Creates item editor element
     *
     * Erstellt Artikel-Editorelement
     */
    private InteractiveGUIManager.InteractiveElement createItemEditor(String paramName, Map<String, Object> properties) {
        return new InteractiveGUIManager.ItemStackEditorElement(paramName, properties);
    }
    
    /**
     * Создает элемент ввода текста
     *
     * Creates text input element
     *
     * Erstellt Texteingabeelement
     */
    private InteractiveGUIManager.InteractiveElement createTextInput(String paramName, Map<String, Object> properties) {
        return new InteractiveGUIManager.TextInputElement(paramName, properties);
    }
    
    /**
     * Настраивает общие редакторы параметров для неизвестных действий
     *
     * Sets up generic parameter editors for unknown actions
     *
     * Richtet generische Parameter-Editoren für unbekannte Aktionen ein
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
     * Настраивает кнопки управления (сохранить, отменить и т.д.)
     *
     * Sets up control buttons (save, cancel, etc.)
     *
     * Richtet Steuerschaltflächen ein (speichern, abbrechen, etc.)
     */
    private void setupControlButtons(InteractiveGUI gui, CodeBlock block, Player player, Location blockLocation) {
        // Save button
        gui.getInventory().setItem(45, createSaveButton());
        
        // Cancel button
        gui.getInventory().setItem(53, createCancelButton());
        
        // Reset button
        gui.getInventory().setItem(49, createResetButton());
        
        // Help button
        gui.getInventory().setItem(48, createHelpButton(block.getAction()));
    }
    
    /**
     * Вспомогательные методы для создания кнопок управления
     *
     * Helper methods for creating control buttons
     *
     * Hilfsmethoden zum Erstellen von Steuerschaltflächen
     */
    
    private ItemStack createTitleItem(String actionId, CodeBlock block) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6🎆 Reference System Parameter Editor");
            meta.setLore(Arrays.asList(
                "§7Action: §e" + actionId,
                "§7Block: §f" + block.getMaterial().name(),
                "§7Parameters: §f" + block.getParameters().size(),
                "",
                "§a✨ Real-time parameter editing",
                "§a🎆 Reference System-style interface",
                "§7Use the interactive elements below"
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createSaveButton() {
        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a🎆 Save & Apply");
            meta.setLore(Arrays.asList(
                "§7Save all parameter changes",
                "§7and apply them to the block",
                "",
                "§eChanges are auto-saved on edit"
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§c🎆 Close Editor");
            meta.setLore(Arrays.asList(
                "§7Close the parameter editor",
                "§7Changes are already saved"
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createResetButton() {
        ItemStack item = new ItemStack(Material.ORANGE_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6🎆 Reset Parameters");
            meta.setLore(Arrays.asList(
                "§7Reset all parameters",
                "§7to their default values",
                "",
                "§cThis cannot be undone!"
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createHelpButton(String actionId) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§b🎆 Help: " + actionId);
            meta.setLore(Arrays.asList(
                "§7Get help for this action type",
                "§7and its parameters",
                "",
                "§eClick for detailed help"
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Вспомогательные методы
     *
     * Helper methods
     *
     * Hilfsmethoden
     */
    
    private CodeBlock getCodeBlock(Location location) {
        var placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
        return placementHandler.getCodeBlock(location);
    }
    
    private void saveBlockToWorld(CodeBlock block) {
        // Save the world to persist changes
        var worldManager = plugin.getWorldManager();
        var world = worldManager.findCreativeWorldByBukkit(block.getLocation().getWorld());
        if (world != null) {
            worldManager.saveWorld(world);
        }
    }
    
    /**
     * Открывает улучшенный редактор параметров
     *
     * Opens the enhanced parameter editor
     *
     * Öffnet den erweiterten Parameter-Editor
     */
    public void openParameterEditor(Player player, Location blockLocation, String actionId) {
        InteractiveGUI gui = createParameterEditor(player, blockLocation, actionId);
        if (gui != null) {
            gui.open();
        }
    }
    
    /**
     * Получает менеджер интерактивного графического интерфейса
     *
     * Gets the interactive GUI manager
     *
     * Ruft den interaktiven GUI-Manager ab
     */
    public InteractiveGUIManager getGUIManager() {
        return guiManager;
    }
}