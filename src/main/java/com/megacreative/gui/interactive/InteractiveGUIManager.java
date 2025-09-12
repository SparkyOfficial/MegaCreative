package com.megacreative.gui.interactive;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 🎆 FrameLand-Style Interactive GUI Manager
 * 
 * Provides dynamic GUI elements with real-time updates:
 * - Material selection with live preview
 * - Mode toggles with state persistence
 * - Dynamic button states and animations
 * - Real-time data binding
 * - Custom GUI element types
 */
public class InteractiveGUIManager implements Listener {
    
    private final MegaCreative plugin;
    private final GUIManager guiManager;
    
    // Active interactive GUIs
    private final Map<UUID, InteractiveGUI> activeGUIs = new ConcurrentHashMap<>();
    
    // Interactive element types
    private final Map<String, InteractiveElementFactory> elementFactories = new ConcurrentHashMap<>();
    
    public InteractiveGUIManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
        
        // Register default interactive elements
        registerDefaultElements();
        
        // Register event listeners
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        plugin.getLogger().info("🎆 Interactive GUI Manager initialized with FrameLand-style elements");
    }
    
    /**
     * Gets the plugin instance
     */
    public MegaCreative getPlugin() {
        return plugin;
    }
    
    /**
     * Creates an interactive GUI for a player
     */
    public InteractiveGUI createInteractiveGUI(Player player, String title, int size) {
        InteractiveGUI gui = new InteractiveGUI(this, player, title, size);
        activeGUIs.put(player.getUniqueId(), gui);
        return gui;
    }
    
    /**
     * Gets an active interactive GUI for a player
     */
    public InteractiveGUI getActiveGUI(Player player) {
        return activeGUIs.get(player.getUniqueId());
    }
    
    /**
     * Removes an active GUI
     */
    public void removeActiveGUI(Player player) {
        activeGUIs.remove(player.getUniqueId());
    }
    
    /**
     * Registers an interactive element factory
     */
    public void registerElement(String type, InteractiveElementFactory factory) {
        elementFactories.put(type, factory);
        plugin.getLogger().info("🎆 Registered interactive element: " + type);
    }
    
    /**
     * Creates an interactive element
     */
    public InteractiveElement createElement(String type, String id, Map<String, Object> properties) {
        InteractiveElementFactory factory = elementFactories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown interactive element type: " + type);
        }
        return factory.create(id, properties);
    }
    
    /**
     * Registers default interactive elements
     */
    private void registerDefaultElements() {
        // Material selector
        registerElement("material_selector", (id, props) -> 
            new MaterialSelectorElement(id, props));
        
        // Mode toggle
        registerElement("mode_toggle", (id, props) -> 
            new ModeToggleElement(id, props));
        
        // Number slider
        registerElement("number_slider", (id, props) -> 
            new NumberSliderElement(id, props));
        
        // Text input
        registerElement("text_input", (id, props) -> 
            new TextInputElement(id, props));
        
        // Color picker
        registerElement("color_picker", (id, props) -> 
            new ColorPickerElement(id, props));
        
        // Item stack editor
        registerElement("item_editor", (id, props) -> 
            new ItemStackEditorElement(id, props));
        
        plugin.getLogger().info("🎆 Registered 6 default interactive elements");
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        InteractiveGUI gui = getActiveGUI(player);
        
        if (gui != null && event.getInventory().equals(gui.getInventory())) {
            event.setCancelled(true);
            gui.handleClick(event.getSlot(), event.getClick(), event.getCurrentItem());
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        InteractiveGUI gui = getActiveGUI(player);
        
        if (gui != null && event.getInventory().equals(gui.getInventory())) {
            gui.onClose();
            removeActiveGUI(player);
        }
    }
    
    /**
     * Shutdown method
     */
    public void shutdown() {
        activeGUIs.clear();
        elementFactories.clear();
        plugin.getLogger().info("🎆 Interactive GUI Manager shut down");
    }
    
    /**
     * Factory interface for creating interactive elements
     */
    @FunctionalInterface
    public interface InteractiveElementFactory {
        InteractiveElement create(String id, Map<String, Object> properties);
    }
    
    /**
     * Base class for interactive elements
     */
    public abstract static class InteractiveElement {
        protected final String id;
        protected final Map<String, Object> properties;
        protected DataValue value;
        protected List<Consumer<DataValue>> changeListeners = new ArrayList<>();
        
        public InteractiveElement(String id, Map<String, Object> properties) {
            this.id = id;
            this.properties = new HashMap<>(properties);
            this.value = DataValue.of("");
        }
        
        public String getId() { return id; }
        public DataValue getValue() { return value; }
        
        public void setValue(DataValue value) {
            DataValue oldValue = this.value;
            this.value = value;
            notifyListeners(oldValue, value);
        }
        
        public void addChangeListener(Consumer<DataValue> listener) {
            changeListeners.add(listener);
        }
        
        protected void notifyListeners(DataValue oldValue, DataValue newValue) {
            for (Consumer<DataValue> listener : changeListeners) {
                try {
                    listener.accept(newValue);
                } catch (Exception e) {
                    // Log error but continue
                }
            }
        }
        
        public abstract ItemStack createDisplayItem();
        public abstract void handleClick(org.bukkit.event.inventory.ClickType clickType);
        public abstract List<ItemStack> getAdditionalItems();
        
        protected Object getProperty(String key, Object defaultValue) {
            return properties.getOrDefault(key, defaultValue);
        }
    }
    
    /**
     * Material selector element
     */
    public static class MaterialSelectorElement extends InteractiveElement {
        private final List<Material> availableMaterials;
        private int currentIndex = 0;
        
        @SuppressWarnings("unchecked")
        public MaterialSelectorElement(String id, Map<String, Object> properties) {
            super(id, properties);
            
            Object materialsObj = properties.get("materials");
            if (materialsObj instanceof List) {
                this.availableMaterials = new ArrayList<>();
                for (Object obj : (List<?>) materialsObj) {
                    if (obj instanceof String) {
                        try {
                            Material material = Material.valueOf((String) obj);
                            availableMaterials.add(material);
                        } catch (IllegalArgumentException ignored) {}
                    } else if (obj instanceof Material) {
                        availableMaterials.add((Material) obj);
                    }
                }
            } else {
                // Default materials
                this.availableMaterials = Arrays.asList(
                    Material.STONE, Material.DIRT, Material.GRASS_BLOCK,
                    Material.OAK_PLANKS, Material.IRON_BLOCK, Material.GOLD_BLOCK,
                    Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK
                );
            }
            
            if (!availableMaterials.isEmpty()) {
                setValue(DataValue.of(availableMaterials.get(0).name()));
            }
        }
        
        @Override
        public ItemStack createDisplayItem() {
            if (availableMaterials.isEmpty()) {
                return new ItemStack(Material.BARRIER);
            }
            
            Material current = availableMaterials.get(currentIndex);
            ItemStack item = new ItemStack(current);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName("§6🎆 Material: §e" + current.name());
                meta.setLore(Arrays.asList(
                    "§7Current: §f" + current.name(),
                    "§7Index: §f" + (currentIndex + 1) + "/" + availableMaterials.size(),
                    "",
                    "§eLeft Click: §7Next material",
                    "§eRight Click: §7Previous material",
                    "§eShift Click: §7Open material browser"
                ));
                item.setItemMeta(meta);
            }
            
            return item;
        }
        
        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType) {
            if (availableMaterials.isEmpty()) return;
            
            switch (clickType) {
                case LEFT:
                    currentIndex = (currentIndex + 1) % availableMaterials.size();
                    break;
                case RIGHT:
                    currentIndex = (currentIndex - 1 + availableMaterials.size()) % availableMaterials.size();
                    break;
                case SHIFT_LEFT:
                case SHIFT_RIGHT:
                    // Open material browser - could implement in future
                    break;
            }
            
            Material newMaterial = availableMaterials.get(currentIndex);
            setValue(DataValue.of(newMaterial.name()));
        }
        
        @Override
        public List<ItemStack> getAdditionalItems() {
            return new ArrayList<>(); // No additional items for basic selector
        }
    }
    
    /**
     * Mode toggle element
     */
    public static class ModeToggleElement extends InteractiveElement {
        private final List<String> modes;
        private int currentModeIndex = 0;
        
        @SuppressWarnings("unchecked")
        public ModeToggleElement(String id, Map<String, Object> properties) {
            super(id, properties);
            
            Object modesObj = properties.get("modes");
            if (modesObj instanceof List) {
                this.modes = new ArrayList<>();
                for (Object obj : (List<?>) modesObj) {
                    if (obj instanceof String) {
                        modes.add((String) obj);
                    }
                }
            } else {
                this.modes = Arrays.asList("ON", "OFF");
            }
            
            if (!modes.isEmpty()) {
                setValue(DataValue.of(modes.get(0)));
            }
        }
        
        @Override
        public ItemStack createDisplayItem() {
            if (modes.isEmpty()) {
                return new ItemStack(Material.BARRIER);
            }
            
            String currentMode = modes.get(currentModeIndex);
            Material material = getMaterialForMode(currentMode);
            
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName("§6🎆 Mode: §e" + currentMode);
                meta.setLore(Arrays.asList(
                    "§7Current Mode: §f" + currentMode,
                    "§7Available: §f" + String.join(", ", modes),
                    "",
                    "§eClick: §7Toggle mode"
                ));
                item.setItemMeta(meta);
            }
            
            return item;
        }
        
        private Material getMaterialForMode(String mode) {
            switch (mode.toUpperCase()) {
                case "ON":
                case "TRUE":
                case "ENABLED":
                    return Material.LIME_CONCRETE;
                case "OFF":
                case "FALSE":
                case "DISABLED":
                    return Material.RED_CONCRETE;
                case "AUTO":
                case "AUTOMATIC":
                    return Material.YELLOW_CONCRETE;
                default:
                    return Material.BLUE_CONCRETE;
            }
        }
        
        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType) {
            if (modes.isEmpty()) return;
            
            currentModeIndex = (currentModeIndex + 1) % modes.size();
            String newMode = modes.get(currentModeIndex);
            setValue(DataValue.of(newMode));
        }
        
        @Override
        public List<ItemStack> getAdditionalItems() {
            return new ArrayList<>();
        }
    }
    
    /**
     * Number slider element
     */
    public static class NumberSliderElement extends InteractiveElement {
        private final double min;
        private final double max;
        private final double step;
        private double currentValue;
        
        public NumberSliderElement(String id, Map<String, Object> properties) {
            super(id, properties);
            
            this.min = ((Number) getProperty("min", 0.0)).doubleValue();
            this.max = ((Number) getProperty("max", 100.0)).doubleValue();
            this.step = ((Number) getProperty("step", 1.0)).doubleValue();
            this.currentValue = ((Number) getProperty("value", min)).doubleValue();
            
            setValue(DataValue.of(currentValue));
        }
        
        @Override
        public ItemStack createDisplayItem() {
            ItemStack item = new ItemStack(Material.COMPARATOR);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName("§6🎆 Value: §e" + currentValue);
                meta.setLore(Arrays.asList(
                    "§7Current: §f" + currentValue,
                    "§7Range: §f" + min + " - " + max,
                    "§7Step: §f" + step,
                    "",
                    "§eLeft Click: §7Increase (+1)",
                    "§eRight Click: §7Decrease (-1)",
                    "§eShift Left: §7Increase (+" + step + ")",
                    "§eShift Right: §7Decrease (-" + step + ")"
                ));
                item.setItemMeta(meta);
            }
            
            return item;
        }
        
        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType) {
            double change = 0;
            
            switch (clickType) {
                case LEFT:
                    change = 1.0;
                    break;
                case RIGHT:
                    change = -1.0;
                    break;
                case SHIFT_LEFT:
                    change = step;
                    break;
                case SHIFT_RIGHT:
                    change = -step;
                    break;
            }
            
            currentValue = Math.max(min, Math.min(max, currentValue + change));
            setValue(DataValue.of(currentValue));
        }
        
        @Override
        public List<ItemStack> getAdditionalItems() {
            return new ArrayList<>();
        }
    }
    
    /**
     * Text input element (simplified)
     */
    public static class TextInputElement extends InteractiveElement {
        private String currentText;
        
        public TextInputElement(String id, Map<String, Object> properties) {
            super(id, properties);
            this.currentText = (String) getProperty("value", "");
            setValue(DataValue.of(currentText));
        }
        
        @Override
        public ItemStack createDisplayItem() {
            ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName("§6🎆 Text: §e" + (currentText.isEmpty() ? "Empty" : currentText));
                meta.setLore(Arrays.asList(
                    "§7Current Text: §f" + currentText,
                    "",
                    "§eClick: §7Edit text (anvil GUI)",
                    "§cNote: §7Text editing requires anvil GUI"
                ));
                item.setItemMeta(meta);
            }
            
            return item;
        }
        
        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType) {
            // Text editing would require anvil GUI or chat input - simplified for now
            // In a full implementation, this would open an anvil GUI or prompt for chat input
        }
        
        @Override
        public List<ItemStack> getAdditionalItems() {
            return new ArrayList<>();
        }
    }
    
    /**
     * Color picker element
     */
    public static class ColorPickerElement extends InteractiveElement {
        private final List<Material> colorMaterials;
        private int currentColorIndex = 0;
        
        public ColorPickerElement(String id, Map<String, Object> properties) {
            super(id, properties);
            
            this.colorMaterials = Arrays.asList(
                Material.WHITE_CONCRETE, Material.LIGHT_GRAY_CONCRETE,
                Material.GRAY_CONCRETE, Material.BLACK_CONCRETE,
                Material.RED_CONCRETE, Material.ORANGE_CONCRETE,
                Material.YELLOW_CONCRETE, Material.LIME_CONCRETE,
                Material.GREEN_CONCRETE, Material.CYAN_CONCRETE,
                Material.LIGHT_BLUE_CONCRETE, Material.BLUE_CONCRETE,
                Material.PURPLE_CONCRETE, Material.MAGENTA_CONCRETE,
                Material.PINK_CONCRETE, Material.BROWN_CONCRETE
            );
            
            setValue(DataValue.of(colorMaterials.get(0).name()));
        }
        
        @Override
        public ItemStack createDisplayItem() {
            Material currentColor = colorMaterials.get(currentColorIndex);
            ItemStack item = new ItemStack(currentColor);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName("§6🎆 Color: §e" + getColorName(currentColor));
                meta.setLore(Arrays.asList(
                    "§7Current: §f" + getColorName(currentColor),
                    "§7Index: §f" + (currentColorIndex + 1) + "/" + colorMaterials.size(),
                    "",
                    "§eLeft Click: §7Next color",
                    "§eRight Click: §7Previous color"
                ));
                item.setItemMeta(meta);
            }
            
            return item;
        }
        
        private String getColorName(Material material) {
            return material.name().replace("_CONCRETE", "").replace("_", " ");
        }
        
        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType) {
            switch (clickType) {
                case LEFT:
                    currentColorIndex = (currentColorIndex + 1) % colorMaterials.size();
                    break;
                case RIGHT:
                    currentColorIndex = (currentColorIndex - 1 + colorMaterials.size()) % colorMaterials.size();
                    break;
            }
            
            Material newColor = colorMaterials.get(currentColorIndex);
            setValue(DataValue.of(newColor.name()));
        }
        
        @Override
        public List<ItemStack> getAdditionalItems() {
            return new ArrayList<>();
        }
    }
    
    /**
     * Item stack editor element
     */
    public static class ItemStackEditorElement extends InteractiveElement {
        private ItemStack currentItem;
        
        public ItemStackEditorElement(String id, Map<String, Object> properties) {
            super(id, properties);
            this.currentItem = new ItemStack(Material.STONE);
            setValue(DataValue.of(currentItem));
        }
        
        @Override
        public ItemStack createDisplayItem() {
            ItemStack display = currentItem.clone();
            ItemMeta meta = display.getItemMeta();
            
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("§6🎆 Item Editor");
                lore.add("§7Material: §f" + currentItem.getType().name());
                lore.add("§7Amount: §f" + currentItem.getAmount());
                
                if (meta.hasDisplayName()) {
                    lore.add("§7Name: §f" + meta.getDisplayName());
                }
                
                lore.add("");
                lore.add("§eLeft Click: §7Edit material");
                lore.add("§eRight Click: §7Edit amount");
                lore.add("§eShift Click: §7Edit name/lore");
                
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            
            return display;
        }
        
        @Override
        public void handleClick(org.bukkit.event.inventory.ClickType clickType) {
            // Item editing would require more complex GUI - simplified for now
            switch (clickType) {
                case LEFT:
                    // Cycle through common materials
                    Material[] materials = {Material.STONE, Material.DIRT, Material.OAK_PLANKS, Material.IRON_INGOT};
                    Material current = currentItem.getType();
                    for (int i = 0; i < materials.length; i++) {
                        if (materials[i] == current) {
                            currentItem.setType(materials[(i + 1) % materials.length]);
                            break;
                        }
                    }
                    break;
                case RIGHT:
                    // Adjust amount
                    int newAmount = currentItem.getAmount() + 1;
                    if (newAmount > 64) newAmount = 1;
                    currentItem.setAmount(newAmount);
                    break;
            }
            
            setValue(DataValue.of(currentItem));
        }
        
        @Override
        public List<ItemStack> getAdditionalItems() {
            return new ArrayList<>();
        }
    }
}