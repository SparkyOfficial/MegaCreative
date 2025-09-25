package com.megacreative.gui.editors;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.managers.GUIManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * Abstract base class for all parameter editors
 * Provides a template for creating specialized GUI editors for different actions and conditions
 */
public abstract class AbstractParameterEditor implements GUIManager.ManagedGUIInterface {
    
    protected final MegaCreative plugin;
    protected final Player player;
    protected final CodeBlock codeBlock;
    protected final Inventory inventory;
    protected final String title;

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
        this.inventory = plugin.getServer().createInventory(null, size, title);
    }

    /**
     * This method should be implemented by each editor to populate the inventory with items
     */
    public abstract void populateItems();

    /**
     * Common method to open the GUI
     */
    public void open() {
        populateItems(); // Fill the inventory before opening
        player.openInventory(inventory);
        // Register the GUI in GUIManager to track clicks
        if (plugin.getGuiManager() != null) {
            plugin.getGuiManager().registerGUI(player, this, inventory);
        }
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
     * Handles inventory click events - to be overridden by subclasses
     * @param event The inventory click event
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        // Default implementation - subclasses should override this
        event.setCancelled(true);
    }
    
    /**
     * Handles inventory close events - to be overridden by subclasses if needed
     * @param event The inventory close event
     */
    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Default implementation - subclasses can override this if needed
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