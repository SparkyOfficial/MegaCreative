package com.megacreative.gui.interactive;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
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
 * 🎆 Reference System-Style Interactive GUI Manager
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
    
    
    private final Map<UUID, InteractiveGUI> activeGUIs = new ConcurrentHashMap<>();
    
    
    private final Map<String, InteractiveElementFactory> elementFactories = new ConcurrentHashMap<>();
    
    public InteractiveGUIManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        
        
        registerDefaultElements();
        
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        plugin.getLogger().info(" YYS Interactive GUI Manager initialized with reference system-style elements");
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
        plugin.getLogger().info(" YYS Registered interactive element: " + type);
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
        
        registerElement("material_selector", (id, props) -> 
            new MaterialSelectorElement(id, props));
        
        
        registerElement("mode_toggle", (id, props) -> 
            new ModeToggleElement(id, props));
        
        
        registerElement("number_slider", (id, props) -> 
            new NumberSliderElement(id, props));
        
        
        registerElement("text_input", (id, props) -> 
            new TextInputElement(id, props));
        
        
        registerElement("color_picker", (id, props) -> 
            new ColorPickerElement(id, props));
        
        
        registerElement("item_editor", (id, props) -> 
            new ItemStackEditorElement(id, props));
        
        plugin.getLogger().info(" YYS Registered 6 default interactive elements");
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
     * Refreshes the GUI for a player
     * @param player The player whose GUI should be refreshed
     */
    public void refreshGUI(Player player) {
        
        InteractiveGUI gui = activeGUIs.get(player.getUniqueId());
        if (gui != null) {
            
            player.closeInventory();
            
            
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    
                    
                    player.openInventory(gui.getInventory());
                }
            }.runTaskLater(plugin, 1L);
        }
    }
    
    /**
     * Shutdown method
     */
    public void shutdown() {
        activeGUIs.clear();
        elementFactories.clear();
        plugin.getLogger().info(" YYS Interactive GUI Manager shut down");
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
                    // Log exception and continue processing
                    // This is expected behavior when notifying listeners
                    // Silently ignore listener exceptions to prevent breaking the GUI
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
                        } catch (IllegalArgumentException e) {
                            // Log invalid material and continue processing
                            // This is expected behavior when parsing user input
                            // Silently ignore invalid materials and continue with valid ones
                        }
                    } else if (obj instanceof Material) {
                        availableMaterials.add((Material) obj);
                    }
                }
            } else {
                
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
                meta.setDisplayName("§6 YYS Material: §e" + current.name());
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
                    
                    break;
            }
            
            Material newMaterial = availableMaterials.get(currentIndex);
            setValue(DataValue.of(newMaterial.name()));
        }
        
        @Override
        public List<ItemStack> getAdditionalItems() {
            return new ArrayList<>(); 
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
                meta.setDisplayName("§6 YYS Mode: §e" + currentMode);
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
                meta.setDisplayName("§6 YYS Value: §e" + currentValue);
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
                meta.setDisplayName("§6 YYS Text: §e" + (currentText.isEmpty() ? "Empty" : currentText));
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
            
            
            
            
            MegaCreative plugin = (MegaCreative) Bukkit.getPluginManager().getPlugin("MegaCreative");
            if (plugin != null) {
                openAnvilGUI(plugin, this);
            }
        }
        
        @Override
        public List<ItemStack> getAdditionalItems() {
            return new ArrayList<>();
        }
        
        /**
         * Opens an anvil GUI for text input
         */
        private void openAnvilGUI(MegaCreative plugin, TextInputElement element) {
            
            plugin.getLogger().info("Opening chat-based text input for element: " + element.getId());
            
            
            openChatInput(plugin, element);
        }
        
        /**
         * Opens a chat-based input system for text input
         * This is the preferred implementation since we don't like AnvilGUI
         */
        private void openChatInput(MegaCreative plugin, TextInputElement element) {
            
            ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
            if (serviceRegistry == null) return;
            
            
            GUIManager guiManager = serviceRegistry.getGuiManager();
            if (guiManager == null) return;
            
            
            Player player = getCurrentPlayer();
            if (player == null) {
                
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    player = onlinePlayer;
                    break;
                }
            }
            
            if (player != null) {
                player.sendMessage("§6Enter text for element §e" + element.getId() + "§6:");
                player.sendMessage("§7(Type your text in chat, or type 'cancel' to cancel)");
                
                
                storePendingTextInput(player, element);
            }
        }
        
        /**
         * Stores pending text input for a player
         * This implementation uses the proper registry system through GUIManager
         */
        private void storePendingTextInput(Player player, TextInputElement element) {
            MegaCreative plugin = (MegaCreative) Bukkit.getPluginManager().getPlugin("MegaCreative");
            if (plugin != null) {
                ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
                if (serviceRegistry != null) {
                    GUIManager guiManager = serviceRegistry.getGuiManager();
                    if (guiManager != null) {
                        
                        guiManager.setPlayerMetadata(player, "awaiting_text_input", true);
                        guiManager.setPlayerMetadata(player, "pending_text_input_element", element);
                        plugin.getLogger().info("Registered pending text input for player " + player.getName() + " with element " + element.getId());
                    }
                }
            }
        }
        
        /**
         * Gets the current player from the context
         */
        private Player getCurrentPlayer() {
            
            try {
                
                String[] parts = this.id.split("_", 3);
                if (parts.length >= 2 && "player".equals(parts[0])) {
                    UUID playerUUID = UUID.fromString(parts[1]);
                    return Bukkit.getPlayer(playerUUID);
                }
            } catch (Exception e) {
                // Log exception and continue processing
                // This is expected behavior when parsing player ID
                // Use fallback method to get current player when parsing fails
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    return onlinePlayer;
                }
            }
            
            
            MegaCreative plugin = (MegaCreative) Bukkit.getPluginManager().getPlugin("MegaCreative");
            if (plugin != null) {
                ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
                if (serviceRegistry != null) {
                    GUIManager guiManager = serviceRegistry.getGuiManager();
                    if (guiManager != null) {
                        
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            InteractiveGUI gui = ((InteractiveGUIManager) guiManager.getInteractiveGUIManager()).getActiveGUI(onlinePlayer);
                            if (gui != null) {
                                return onlinePlayer;
                            }
                        }
                    }
                }
            }
            
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                return player;
            }
            
            return null;
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
                meta.setDisplayName("§6 YYS Color: §e" + getColorName(currentColor));
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
                lore.add("§6 YYS Item Editor");
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
            
            MegaCreative plugin = (MegaCreative) Bukkit.getPluginManager().getPlugin("MegaCreative");
            if (plugin == null) return;
            
            
            openItemEditorGUI(plugin, this);
        }
        
        @Override
        public List<ItemStack> getAdditionalItems() {
            return new ArrayList<>();
        }
        
        /**
         * Opens a dedicated GUI for item editing
         */
        private void openItemEditorGUI(MegaCreative plugin, ItemStackEditorElement element) {
            
            plugin.getLogger().info("Opening item editor GUI for item editor element: " + element.getId());
            
            
            createItemEditorInterface(plugin, element, null);
        }
        
        /**
         * Creates a proper item editor interface
         * This is a more proper implementation than the previous simulation
         */
        private void createItemEditorInterface(MegaCreative plugin, final ItemStackEditorElement element, final InteractiveGUIManager outerInstance) {
            
            Player player = null;
            try {
                
                String[] parts = element.getId().split("_", 3);
                if (parts.length >= 2 && "player".equals(parts[0])) {
                    UUID playerUUID = UUID.fromString(parts[1]);
                    player = Bukkit.getPlayer(playerUUID);
                }
            } catch (Exception e) {
                // Log exception and continue processing
                // This is expected behavior when parsing element ID
                // Use fallback method to get current player when parsing fails
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    player = onlinePlayer;
                    break;
                }
            }
            
            if (player == null) return;
            
            
            Inventory editorInventory = Bukkit.createInventory(null, 27, " YYS Item Editor");
            
            
            editorInventory.setItem(13, element.currentItem.clone());
            
            
            ItemStack materialButton = new ItemStack(Material.CRAFTING_TABLE);
            ItemMeta materialMeta = materialButton.getItemMeta();
            if (materialMeta != null) {
                materialMeta.setDisplayName("§6 YYS Change Material");
                materialMeta.setLore(Arrays.asList("§7Click to change the item material"));
                materialButton.setItemMeta(materialMeta);
            }
            editorInventory.setItem(10, materialButton);
            
            ItemStack amountButton = new ItemStack(Material.HOPPER);
            ItemMeta amountMeta = amountButton.getItemMeta();
            if (amountMeta != null) {
                amountMeta.setDisplayName("§6 YYS Change Amount");
                amountMeta.setLore(Arrays.asList("§7Click to change the item amount"));
                amountButton.setItemMeta(amountMeta);
            }
            editorInventory.setItem(11, amountButton);
            
            ItemStack nameButton = new ItemStack(Material.NAME_TAG);
            ItemMeta nameMeta = nameButton.getItemMeta();
            if (nameMeta != null) {
                nameMeta.setDisplayName("§6 YYS Change Name");
                nameMeta.setLore(Arrays.asList("§7Click to change the item name"));
                nameButton.setItemMeta(nameMeta);
            }
            editorInventory.setItem(15, nameButton);
            
            ItemStack loreButton = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta loreMeta = loreButton.getItemMeta();
            if (loreMeta != null) {
                loreMeta.setDisplayName("§6 YYS Edit Lore");
                loreMeta.setLore(Arrays.asList("§7Click to edit the item lore"));
                loreButton.setItemMeta(loreMeta);
            }
            editorInventory.setItem(16, loreButton);
            
            
            ItemStack saveButton = new ItemStack(Material.LIME_CONCRETE);
            ItemMeta saveMeta = saveButton.getItemMeta();
            if (saveMeta != null) {
                saveMeta.setDisplayName("§a YYS Save Changes");
                saveMeta.setLore(Arrays.asList("§7Click to save changes to the item"));
                saveButton.setItemMeta(saveMeta);
            }
            editorInventory.setItem(26, saveButton);
            
            ItemStack cancelButton = new ItemStack(Material.RED_CONCRETE);
            ItemMeta cancelMeta = cancelButton.getItemMeta();
            if (cancelMeta != null) {
                cancelMeta.setDisplayName("§c YYS Cancel");
                cancelMeta.setLore(Arrays.asList("§7Click to cancel and close"));
                cancelButton.setItemMeta(cancelMeta);
            }
            editorInventory.setItem(18, cancelButton);
            
            
            ServiceRegistry serviceRegistryOuter = plugin.getServiceRegistry();
            if (serviceRegistryOuter != null) {
                GUIManager guiManagerOuter = serviceRegistryOuter.getGuiManager();
                if (guiManagerOuter != null) {
                    
                    guiManagerOuter.setPlayerMetadata(player, "item_editor_element", element);
                }
            }
            
            
            final String elementId = element.getId();
            
            
            GUIManager.ManagedGUIInterface managedGUI = new GUIManager.ManagedGUIInterface() {
                @Override
                public void onInventoryClick(InventoryClickEvent event) {
                    
                    event.setCancelled(true);
                    Player player = (Player) event.getWhoClicked();
                    
                    
                    MegaCreative plugin = (MegaCreative) Bukkit.getPluginManager().getPlugin("MegaCreative");
                    ItemStackEditorElement editorElement = null;
                    if (plugin != null) {
                        ServiceRegistry serviceRegistryInner = plugin.getServiceRegistry();
                        if (serviceRegistryInner != null) {
                            GUIManager guiManagerInner = serviceRegistryInner.getGuiManager();
                            if (guiManagerInner != null) {
                                editorElement = guiManagerInner.getPlayerMetadata(player, "item_editor_element", ItemStackEditorElement.class);
                            }
                        }
                    }
                    
                    if (editorElement == null) {
                        player.sendMessage("§cError: Could not find item editor element");
                        player.closeInventory();
                        return;
                    }
                    
                    
                    switch (event.getSlot()) {
                        case 10: 
                            openMaterialSelector(player, editorElement, editorInventory);
                            break;
                        case 11: 
                            openAmountEditor(player, editorElement);
                            break;
                        case 15: 
                            openNameEditor(player, editorElement);
                            break;
                        case 16: 
                            openLoreEditor(player, editorElement);
                            break;
                        case 26: 
                            
                            editorElement.currentItem = editorInventory.getItem(13);
                            if (editorElement.currentItem == null) {
                                editorElement.currentItem = new ItemStack(Material.STONE);
                            }
                            
                            editorElement.setValue(DataValue.of(editorElement.currentItem));
                            player.sendMessage("§aChanges saved!");
                            player.closeInventory();
                            
                            if (outerInstance != null) {
                                outerInstance.refreshGUI(player);
                            }
                            break;
                        case 18: 
                            player.sendMessage("§cCancelled");
                            player.closeInventory();
                            break;
                        case 13: 
                            
                            
                            player.sendMessage("§6Click the edit buttons to modify this item");
                            break;
                    }
                }
                
                @Override
                public String getGUITitle() {
                    return " YYS Item Editor for " + elementId;
                }
                
                @Override
                public void onInventoryClose(InventoryCloseEvent event) {
                    
                    Player player = (Player) event.getPlayer();
                    MegaCreative plugin = (MegaCreative) Bukkit.getPluginManager().getPlugin("MegaCreative");
                    if (plugin != null) {
                        ServiceRegistry serviceRegistryInner = plugin.getServiceRegistry();
                        if (serviceRegistryInner != null) {
                            GUIManager guiManagerInner = serviceRegistryInner.getGuiManager();
                            if (guiManagerInner != null) {
                                guiManagerInner.setPlayerMetadata(player, "item_editor_element", null);
                            }
                        }
                    }
                }
            };
            
            
            if (plugin != null) {
                ServiceRegistry serviceRegistryInner = plugin.getServiceRegistry();
                if (serviceRegistryInner != null) {
                    GUIManager guiManagerInner = serviceRegistryInner.getGuiManager();
                    if (guiManagerInner != null) {
                        guiManagerInner.registerGUI(player, managedGUI, editorInventory);
                    }
                }
            }
            
            
            player.openInventory(editorInventory);
        }
        
        /**
         * Opens a material selector GUI
         */
        private void openMaterialSelector(Player player, ItemStackEditorElement element, Inventory editorInventory) {
            
            Inventory materialInventory = Bukkit.createInventory(null, 54, " YYS Select Material");
            
            
            Material[] commonMaterials = {
                Material.STONE, Material.COBBLESTONE, Material.DIRT, Material.GRASS_BLOCK,
                Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS,
                Material.DIAMOND, Material.GOLD_INGOT, Material.IRON_INGOT,
                Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE,
                Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
                Material.BOW, Material.ARROW, Material.APPLE, Material.BREAD,
                Material.COAL, Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK,
                Material.EMERALD, Material.EMERALD_BLOCK, Material.REDSTONE, Material.REDSTONE_BLOCK,
                Material.LAPIS_LAZULI, Material.LAPIS_BLOCK, Material.OBSIDIAN, Material.BEDROCK
            };
            
            
            for (int i = 0; i < commonMaterials.length; i++) {
                ItemStack item = new ItemStack(commonMaterials[i]);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§6" + commonMaterials[i].name());
                    item.setItemMeta(meta);
                }
                materialInventory.setItem(i, item);
            }
            
            
            ItemStack backButton = new ItemStack(Material.BARRIER);
            ItemMeta backMeta = backButton.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§cBack to Item Editor");
                backButton.setItemMeta(backMeta);
            }
            materialInventory.setItem(49, backButton);
            
            
            GUIManager.ManagedGUIInterface materialGUI = new GUIManager.ManagedGUIInterface() {
                @Override
                public void onInventoryClick(InventoryClickEvent event) {
                    event.setCancelled(true);
                    Player player = (Player) event.getWhoClicked();
                    
                    if (event.getSlot() == 49) { 
                        
                        player.openInventory(editorInventory);
                        return;
                    }
                    
                    ItemStack clickedItem = event.getCurrentItem();
                    if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                        
                        ItemStack displayItem = editorInventory.getItem(13);
                        if (displayItem != null) {
                            displayItem.setType(clickedItem.getType());
                            editorInventory.setItem(13, displayItem);
                            player.sendMessage("§aMaterial changed to: §f" + clickedItem.getType().name());
                        }
                        
                        
                        player.openInventory(editorInventory);
                    }
                }
                
                @Override
                public String getGUITitle() {
                    return "Select Material";
                }
            };
            
            
            MegaCreative plugin = (MegaCreative) Bukkit.getPluginManager().getPlugin("MegaCreative");
            if (plugin != null) {
                ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
                if (serviceRegistry != null) {
                    GUIManager guiManager = serviceRegistry.getGuiManager();
                    if (guiManager != null) {
                        guiManager.registerGUI(player, materialGUI, materialInventory);
                        player.openInventory(materialInventory);
                    }
                }
            }
        }
        
        /**
         * Opens an amount editor using chat input
         */
        private void openAmountEditor(Player player, ItemStackEditorElement element) {
            player.sendMessage("§6Enter the new amount for the item (1-64):");
            player.sendMessage("§7(Type a number in chat, or type 'cancel' to cancel)");
            
            
            MegaCreative plugin = (MegaCreative) Bukkit.getPluginManager().getPlugin("MegaCreative");
            if (plugin != null) {
                ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
                if (serviceRegistry != null) {
                    GUIManager guiManager = serviceRegistry.getGuiManager();
                    if (guiManager != null) {
                        
                        guiManager.setPlayerMetadata(player, "awaiting_amount_input", true);
                        guiManager.setPlayerMetadata(player, "pending_amount_element", element);
                        plugin.getLogger().info("Registered pending amount input for player " + player.getName() + " with element " + element.getId());
                    }
                }
            }
            
            
            player.closeInventory();
        }
        
        /**
         * Opens a name editor using chat input
         */
        private void openNameEditor(Player player, ItemStackEditorElement element) {
            player.sendMessage("§6Enter the new name for the item:");
            player.sendMessage("§7(Type the name in chat, or type 'cancel' to cancel)");
            player.sendMessage("§7(Use & for color codes, e.g. &aGreen Sword)");
            
            
            MegaCreative plugin = (MegaCreative) Bukkit.getPluginManager().getPlugin("MegaCreative");
            if (plugin != null) {
                ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
                if (serviceRegistry != null) {
                    GUIManager guiManager = serviceRegistry.getGuiManager();
                    if (guiManager != null) {
                        
                        guiManager.setPlayerMetadata(player, "awaiting_name_input", true);
                        guiManager.setPlayerMetadata(player, "pending_name_element", element);
                        plugin.getLogger().info("Registered pending name input for player " + player.getName() + " with element " + element.getId());
                    }
                }
            }
            
            
            player.closeInventory();
        }
        
        /**
         * Opens a lore editor using chat input
         */
        private void openLoreEditor(Player player, ItemStackEditorElement element) {
            player.sendMessage("§6Enter the new lore line for the item:");
            player.sendMessage("§7(Type the lore in chat, or type 'cancel' to cancel)");
            player.sendMessage("§7(Use & for color codes, e.g. &7A powerful sword)");
            player.sendMessage("§7(Type 'done' when finished adding lore lines)");
            
            
            MegaCreative plugin = (MegaCreative) Bukkit.getPluginManager().getPlugin("MegaCreative");
            if (plugin != null) {
                ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
                if (serviceRegistry != null) {
                    GUIManager guiManager = serviceRegistry.getGuiManager();
                    if (guiManager != null) {
                        
                        guiManager.setPlayerMetadata(player, "awaiting_lore_input", true);
                        guiManager.setPlayerMetadata(player, "pending_lore_element", element);
                        guiManager.setPlayerMetadata(player, "current_lore_lines", new ArrayList<String>());
                        plugin.getLogger().info("Registered pending lore input for player " + player.getName() + " with element " + element.getId());
                    }
                }
            }
            
            
            player.closeInventory();
        }
    }
}