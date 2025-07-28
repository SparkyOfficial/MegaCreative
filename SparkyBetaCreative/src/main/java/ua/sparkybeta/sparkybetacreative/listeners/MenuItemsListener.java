package ua.sparkybeta.sparkybetacreative.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import ua.sparkybeta.sparkybetacreative.gui.ConfirmationMenu;
import ua.sparkybeta.sparkybetacreative.gui.WorldsBrowserMenu;
import ua.sparkybeta.sparkybetacreative.gui.BlockArgumentsMenu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuItemsListener implements Listener {

    private static final Map<UUID, Object> openMenus = new HashMap<>();

    public static void setOpenMenu(Player player, Object menu) {
        openMenus.put(player.getUniqueId(), menu);
    }
    
    public static void clearOpenMenu(Player player) {
        openMenus.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Object menu = openMenus.get(player.getUniqueId());

        if (menu instanceof WorldsBrowserMenu browserMenu) {
            event.setCancelled(true);
            browserMenu.onInventoryClick(player, event.getSlot(), event.getClick());
        } else if (menu instanceof ConfirmationMenu confirmationMenu) {
            event.setCancelled(true);
            confirmationMenu.onInventoryClick(event.getSlot());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        // Clear the menu from the map when the player closes it
        // This is important to prevent memory leaks and handle argument saving
        Object menu = openMenus.get(player.getUniqueId());
        if (menu instanceof BlockArgumentsMenu) {
            // The argument saving is handled in CodeBlockInteractListener
            return;
        }
        
        if (menu != null) {
            clearOpenMenu(player);
        }
    }
} 