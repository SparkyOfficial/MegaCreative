package com.megacreative.gui.interactive;

import com.megacreative.managers.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🎆 Reference System-Style Interactive GUI Implementation
 * 
 * A dynamic GUI that can contain interactive elements with real-time updates.
 * Supports element positioning, data binding, and event handling.
 */
public class InteractiveGUI implements GUIManager.ManagedGUIInterface {
    
    private final InteractiveGUIManager manager;
    private final Player player;
    private final Inventory inventory;
    private final String title;
    
    
    private final Map<Integer, InteractiveGUIManager.InteractiveElement> slotElements = new ConcurrentHashMap<>();
    private final Map<String, InteractiveGUIManager.InteractiveElement> namedElements = new ConcurrentHashMap<>();
    
    
    private final Set<Integer> dirtySlots = ConcurrentHashMap.newKeySet();
    private boolean needsRefresh = false;
    
    public InteractiveGUI(InteractiveGUIManager manager, Player player, String title, int size) {
        this.manager = manager;
        this.player = player;
        this.title = title;
        this.inventory = Bukkit.createInventory(null, size, title);
        
        
        setupAutoRefresh();
    }
    
    /**
     * Adds an interactive element to a specific slot
     */
    public void setElement(int slot, InteractiveGUIManager.InteractiveElement element) {
        
        if (slotElements.containsKey(slot)) {
            InteractiveGUIManager.InteractiveElement existing = slotElements.get(slot);
            namedElements.remove(existing.getId());
        }
        
        
        slotElements.put(slot, element);
        namedElements.put(element.getId(), element);
        
        
        element.addChangeListener(value -> {
            markSlotDirty(slot);
            scheduleRefresh();
        });
        
        
        markSlotDirty(slot);
        scheduleRefresh();
    }
    
    /**
     * Gets an element by ID
     */
    public InteractiveGUIManager.InteractiveElement getElement(String id) {
        return namedElements.get(id);
    }
    
    /**
     * Gets an element by slot
     */
    public InteractiveGUIManager.InteractiveElement getElement(int slot) {
        return slotElements.get(slot);
    }
    
    /**
     * Removes an element from a slot
     */
    public void removeElement(int slot) {
        InteractiveGUIManager.InteractiveElement element = slotElements.remove(slot);
        if (element != null) {
            namedElements.remove(element.getId());
            inventory.setItem(slot, null);
        }
    }
    
    /**
     * Handles inventory clicks
     */
    public void handleClick(int slot, ClickType clickType, ItemStack clickedItem) {
        InteractiveGUIManager.InteractiveElement element = slotElements.get(slot);
        if (element != null) {
            element.handleClick(clickType);
            
            
            markSlotDirty(slot);
            refreshNow();
            
            
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        }
    }
    
    /**
     * Opens the GUI for the player
     */
    public void open() {
        refreshAll();
        player.openInventory(inventory);
        
        
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 0.7f, 1.2f);
    }
    
    /**
     * Marks a slot as needing update
     */
    private void markSlotDirty(int slot) {
        dirtySlots.add(slot);
    }
    
    /**
     * Schedules a refresh
     */
    private void scheduleRefresh() {
        needsRefresh = true;
    }
    
    /**
     * Performs immediate refresh of dirty slots
     */
    private void refreshNow() {
        if (dirtySlots.isEmpty()) return;
        
        for (int slot : dirtySlots) {
            InteractiveGUIManager.InteractiveElement element = slotElements.get(slot);
            if (element != null) {
                ItemStack displayItem = element.createDisplayItem();
                inventory.setItem(slot, displayItem);
            }
        }
        
        dirtySlots.clear();
        needsRefresh = false;
    }
    
    /**
     * Refreshes all elements
     */
    private void refreshAll() {
        for (Map.Entry<Integer, InteractiveGUIManager.InteractiveElement> entry : slotElements.entrySet()) {
            int slot = entry.getKey();
            InteractiveGUIManager.InteractiveElement element = entry.getValue();
            
            ItemStack displayItem = element.createDisplayItem();
            inventory.setItem(slot, displayItem);
        }
        
        dirtySlots.clear();
        needsRefresh = false;
    }
    
    /**
     * Sets up automatic refresh for dynamic elements
     */
    private void setupAutoRefresh() {
        
        Bukkit.getScheduler().runTaskTimer(
            manager.getPlugin(), 
            () -> {
                if (needsRefresh && player.getOpenInventory().getTopInventory().equals(inventory)) {
                    refreshNow();
                }
            }, 
            1L, 
            2L  
        );
    }
    
    /**
     * Called when the GUI is closed
     */
    public void onClose() {
        
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_CLOSE, 0.7f, 0.8f);
        
        
        manager.removeActiveGUI(player);
    }
    
    /**
     * Gets the underlying inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Gets all elements in the GUI
     */
    public Collection<InteractiveGUIManager.InteractiveElement> getAllElements() {
        return namedElements.values();
    }
    
    /**
     * Checks if the GUI has any elements
     */
    public boolean hasElements() {
        return !slotElements.isEmpty();
    }
    
    /**
     * Gets the number of elements
     */
    public int getElementCount() {
        return slotElements.size();
    }
    
    
    
    @Override
    public String getGUITitle() {
        return title;
    }
    
    @Override
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) return;
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        handleClick(event.getSlot(), event.getClick(), event.getCurrentItem());
    }
    
    @Override
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (!event.getPlayer().equals(player)) return;
        if (!event.getInventory().equals(inventory)) return;
        
        onClose();
    }
    
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        if (!event.getPlayer().equals(player)) return;
        
        
        manager.removeActiveGUI(player);
    }
}