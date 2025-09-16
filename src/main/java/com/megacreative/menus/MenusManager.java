package com.megacreative.menus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active menus in the MegaCreative plugin
 * Provides registration and unregistration of menus for proper cleanup
 */
public class MenusManager {
    private static MenusManager instance;
    private final Map<Integer, InventoryMenu> activeMenus;
    
    private MenusManager() {
        this.activeMenus = new ConcurrentHashMap<>();
    }
    
    /**
     * Gets the singleton instance of MenusManager
     * @return The MenusManager instance
     */
    public static synchronized MenusManager getInstance() {
        if (instance == null) {
            instance = new MenusManager();
        }
        return instance;
    }
    
    /**
     * Registers a menu as active
     * @param menu The menu to register
     */
    public void registerMenu(InventoryMenu menu) {
        if (menu != null) {
            activeMenus.put(System.identityHashCode(menu), menu);
        }
    }
    
    /**
     * Unregisters a menu
     * @param menu The menu to unregister
     */
    public void unregisterMenu(InventoryMenu menu) {
        if (menu != null) {
            activeMenus.remove(System.identityHashCode(menu));
        }
    }
    
    /**
     * Gets an active menu by its hash code
     * @param hashCode The hash code of the menu
     * @return The menu, or null if not found
     */
    public InventoryMenu getMenu(int hashCode) {
        return activeMenus.get(hashCode);
    }
    
    /**
     * Gets the number of active menus
     * @return The count of active menus
     */
    public int getActiveMenuCount() {
        return activeMenus.size();
    }
    
    /**
     * Clears all active menus
     */
    public void clearAllMenus() {
        activeMenus.clear();
    }
}