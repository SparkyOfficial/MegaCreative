package com.megacreative.gui.editors;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.managers.GUIManager;
import com.megacreative.coding.containers.BlockContainerManager;
import com.megacreative.core.ServiceRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.Location;

/**
 * Abstract base class for all parameter editors
 * Provides a template for creating specialized GUI editors for different actions and conditions
 * Implements drag-and-drop container system for parameter configuration
 */
public abstract class AbstractParameterEditor implements GUIManager.ManagedGUIInterface {
    
    protected final MegaCreative plugin;
    protected final Player player;
    protected final CodeBlock codeBlock;
    protected final Inventory inventory;
    protected final String title;
    protected final Location blockLocation;
    protected BlockContainerManager containerManager;

    /**
     * Constructor for AbstractParameterEditor
     * @param plugin The main plugin instance
     * @param player The player using the editor
     * @param codeBlock The code block being edited
     * @param size The size of the inventory (must be multiple of 9, max 54)
     * @param title The title of the inventory
     */
    public AbstractParameterEditor(MegaCreative plugin, Player player, CodeBlock codeBlock, int size, String title) {
        this.plugin = plugin;
        this.player = player;
        this.codeBlock = codeBlock;
        this.title = title;
        this.blockLocation = codeBlock.getLocation();
        this.inventory = plugin.getServer().createInventory(null, size, title);
        
        // Get container manager from service registry
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry != null) {
            this.containerManager = serviceRegistry.getBlockContainerManager();
        }
    }

    /**
     * This method should be implemented by each editor to populate the inventory with items
     */
    public abstract void populateItems();

    /**
     * Common method to open the GUI
     */
    public void open() {
        // First try to load items from container if it exists
        loadContainerItems();
        
        // Then populate the inventory with GUI items
        populateItems();
        
        // Open the inventory
        player.openInventory(inventory);
        
        // Register the GUI in GUIManager to track clicks
        if (plugin.getGuiManager() != null) {
            plugin.getGuiManager().registerGUI(player, this, inventory);
        }
    }
    
    /**
     * Load items from container above the code block
     */
    protected void loadContainerItems() {
        if (containerManager == null || blockLocation == null) {
            return;
        }
        
        try {
            // Get container location (one block above)
            Location containerLocation = blockLocation.clone().add(0, 1, 0);
            
            // Check if container exists
            if (containerLocation.getBlock().getState() instanceof org.bukkit.block.Container containerState) {
                // Load items from container inventory
                org.bukkit.inventory.Inventory containerInventory = containerState.getInventory();
                
                // Load items into editor inventory (implementation depends on specific editor)
                onLoadContainerItems(containerInventory);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error loading container items: " + e.getMessage());
        }
    }
    
    /**
     * Save items from editor to container above the code block
     */
    protected void saveContainerItems() {
        if (containerManager == null || blockLocation == null) {
            return;
        }
        
        try {
            // Get container location (one block above)
            Location containerLocation = blockLocation.clone().add(0, 1, 0);
            
            // Check if container exists
            if (containerLocation.getBlock().getState() instanceof org.bukkit.block.Container containerState) {
                // Get container inventory
                org.bukkit.inventory.Inventory containerInventory = containerState.getInventory();
                
                // Clear container inventory
                containerInventory.clear();
                
                // Save items from editor inventory to container (implementation depends on specific editor)
                onSaveContainerItems(containerInventory);
                
                player.sendMessage("§a✓ Параметры сохранены в контейнер!");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error saving container items: " + e.getMessage());
            player.sendMessage("§cОшибка при сохранении параметров: " + e.getMessage());
        }
    }
    
    /**
     * Called when loading items from container - to be overridden by subclasses
     * @param containerInventory The container inventory to load items from
     */
    protected void onLoadContainerItems(org.bukkit.inventory.Inventory containerInventory) {
        // Default implementation - subclasses should override this
    }
    
    /**
     * Called when saving items to container - to be overridden by subclasses
     * @param containerInventory The container inventory to save items to
     */
    protected void onSaveContainerItems(org.bukkit.inventory.Inventory containerInventory) {
        // Default implementation - subclasses should override this
    }
    
    /**
     * Gets the inventory associated with this editor
     * @return The inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Gets the player using this editor
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Gets the code block being edited
     * @return The code block
     */
    public CodeBlock getCodeBlock() {
        return codeBlock;
    }
    
    /**
     * Gets the block location being edited
     * @return The block location
     */
    public Location getBlockLocation() {
        return blockLocation;
    }
    
    /**
     * Handles inventory click events - to be overridden by subclasses
     * @param event The inventory click event
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        // Default implementation - subclasses should override this
        event.setCancelled(true);
    }
    
    /**
     * Handles inventory close events - saves container items and calls cleanup
     * @param event The inventory close event
     */
    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Save items to container when GUI is closed
        saveContainerItems();
        
        // Call cleanup
        onCleanup();
    }
    
    /**
     * Called when GUI is being cleaned up
     */
    @Override
    public void onCleanup() {
        // Default implementation - subclasses can override this if needed
    }
    
    /**
     * Gets the GUI title for debugging
     * @return The GUI title
     */
    @Override
    public String getGUITitle() {
        return "Parameter Editor: " + title;
    }
}