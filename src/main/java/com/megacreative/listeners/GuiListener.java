package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.gui.interactive.InteractiveGUIManager;
import com.megacreative.gui.interactive.InteractiveGUIManager.TextInputElement;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for GUI-related events
 *
 * Слушатель для событий, связанных с GUI
 *
 * Listener für GUI-bezogene Ereignisse
 */
public class GuiListener implements Listener {
    
    private final MegaCreative plugin;
    
    /**
     * Constructor for GuiListener
     * @param plugin the main plugin
     *
     * Конструктор для GuiListener
     * @param plugin основной плагин
     *
     * Konstruktor für GuiListener
     * @param plugin das Haupt-Plugin
     */
    public GuiListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles player quit events
     * @param event the player quit event
     *
     * Обрабатывает события выхода игрока
     * @param event событие выхода игрока
     *
     * Verarbeitet Spieler-Verlassen-Ereignisse
     * @param event das Spieler-Verlassen-Ereignis
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getGuiManager().unregisterGUI(player);
    }
    
    /**
     * Handles player chat events
     * @param event the async player chat event
     *
     * Обрабатывает события чата игроков
     * @param event асинхронное событие чата игрока
     *
     * Verarbeitet Spieler-Chat-Ereignisse
     * @param event das asynchrone Spieler-Chat-Ereignis
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();
        
        // Handle text input for InteractiveGUIManager
        GUIManager guiManager = plugin.getGuiManager();
        if (guiManager != null) {
            // Check if player is waiting for text input
            Boolean awaitingTextInput = guiManager.getPlayerMetadata(player, "awaiting_text_input", Boolean.class);
            if (Boolean.TRUE.equals(awaitingTextInput)) {
                event.setCancelled(true);
                
                // Get the text input element
                InteractiveGUIManager.TextInputElement element = guiManager.getPlayerMetadata(player, "pending_text_input_element", InteractiveGUIManager.TextInputElement.class);
                if (element != null) {
                    // Handle cancel command
                    if (message.equalsIgnoreCase("cancel")) {
                        player.sendMessage("§cText input cancelled");
                        guiManager.setPlayerMetadata(player, "awaiting_text_input", false);
                        guiManager.setPlayerMetadata(player, "pending_text_input_element", null);
                        return;
                    }
                    
                    // Set the value in the element
                    element.setValue(DataValue.of(message));
                    player.sendMessage("§aText input accepted: §f" + message);
                    
                    // Clear the pending input state
                    guiManager.setPlayerMetadata(player, "awaiting_text_input", false);
                    guiManager.setPlayerMetadata(player, "pending_text_input_element", null);
                    
                    // Update the GUI if it's still open
                    InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                    if (interactiveGUI != null) {
                        interactiveGUI.refreshGUI(player);
                    }
                    return;
                }
            }
            
            // Check if player is waiting for amount input
            Boolean awaitingAmountInput = guiManager.getPlayerMetadata(player, "awaiting_amount_input", Boolean.class);
            if (Boolean.TRUE.equals(awaitingAmountInput)) {
                event.setCancelled(true);
                
                // Get the item stack editor element
                InteractiveGUIManager.ItemStackEditorElement element = guiManager.getPlayerMetadata(player, "pending_amount_element", InteractiveGUIManager.ItemStackEditorElement.class);
                if (element != null) {
                    // Handle cancel command
                    if (message.equalsIgnoreCase("cancel")) {
                        player.sendMessage("§cAmount input cancelled");
                        guiManager.setPlayerMetadata(player, "awaiting_amount_input", false);
                        guiManager.setPlayerMetadata(player, "pending_amount_element", null);
                        
                        // Reopen the item editor GUI
                        InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                        if (interactiveGUI != null) {
                            interactiveGUI.refreshGUI(player);
                        }
                        return;
                    }
                    
                    // Parse and validate the amount
                    try {
                        int amount = Integer.parseInt(message);
                        if (amount >= 1 && amount <= 64) {
                            // Update the item amount
                            ItemStack currentItem = (ItemStack) element.getValue().getValue();
                            if (currentItem != null) {
                                currentItem.setAmount(amount);
                                element.setValue(DataValue.of(currentItem));
                                player.sendMessage("§aAmount set to: §f" + amount);
                            }
                        } else {
                            player.sendMessage("§cAmount must be between 1 and 64");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cPlease enter a valid number");
                        return;
                    }
                    
                    // Clear the pending input state
                    guiManager.setPlayerMetadata(player, "awaiting_amount_input", false);
                    guiManager.setPlayerMetadata(player, "pending_amount_element", null);
                    
                    // Reopen the item editor GUI
                    InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                    if (interactiveGUI != null) {
                        interactiveGUI.refreshGUI(player);
                    }
                    return;
                }
            }
            
            // Check if player is waiting for name input
            Boolean awaitingNameInput = guiManager.getPlayerMetadata(player, "awaiting_name_input", Boolean.class);
            if (Boolean.TRUE.equals(awaitingNameInput)) {
                event.setCancelled(true);
                
                // Get the item stack editor element
                InteractiveGUIManager.ItemStackEditorElement element = guiManager.getPlayerMetadata(player, "pending_name_element", InteractiveGUIManager.ItemStackEditorElement.class);
                if (element != null) {
                    // Handle cancel command
                    if (message.equalsIgnoreCase("cancel")) {
                        player.sendMessage("§cName input cancelled");
                        guiManager.setPlayerMetadata(player, "awaiting_name_input", false);
                        guiManager.setPlayerMetadata(player, "pending_name_element", null);
                        
                        // Reopen the item editor GUI
                        InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                        if (interactiveGUI != null) {
                            interactiveGUI.refreshGUI(player);
                        }
                        return;
                    }
                    
                    // Update the item name (with color code support)
                    ItemStack currentItem = (ItemStack) element.getValue().getValue();
                    if (currentItem != null) {
                        ItemMeta meta = currentItem.getItemMeta();
                        if (meta != null) {
                            // Replace & with § for color codes
                            String coloredName = message.replace('&', '§');
                            meta.setDisplayName(coloredName);
                            currentItem.setItemMeta(meta);
                            element.setValue(DataValue.of(currentItem));
                            player.sendMessage("§aName set to: §f" + coloredName);
                        }
                    }
                    
                    // Clear the pending input state
                    guiManager.setPlayerMetadata(player, "awaiting_name_input", false);
                    guiManager.setPlayerMetadata(player, "pending_name_element", null);
                    
                    // Reopen the item editor GUI
                    InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                    if (interactiveGUI != null) {
                        interactiveGUI.refreshGUI(player);
                    }
                    return;
                }
            }
            
            // Check if player is waiting for lore input
            Boolean awaitingLoreInput = guiManager.getPlayerMetadata(player, "awaiting_lore_input", Boolean.class);
            if (Boolean.TRUE.equals(awaitingLoreInput)) {
                event.setCancelled(true);
                
                // Get the item stack editor element
                InteractiveGUIManager.ItemStackEditorElement element = guiManager.getPlayerMetadata(player, "pending_lore_element", InteractiveGUIManager.ItemStackEditorElement.class);
                if (element != null) {
                    // Handle cancel command
                    if (message.equalsIgnoreCase("cancel")) {
                        player.sendMessage("§cLore input cancelled");
                        guiManager.setPlayerMetadata(player, "awaiting_lore_input", false);
                        guiManager.setPlayerMetadata(player, "pending_lore_element", null);
                        guiManager.setPlayerMetadata(player, "current_lore_lines", null);
                        
                        // Reopen the item editor GUI
                        InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                        if (interactiveGUI != null) {
                            interactiveGUI.refreshGUI(player);
                        }
                        return;
                    }
                    
                    // Handle "done" command to finish lore input
                    if (message.equalsIgnoreCase("done")) {
                        // Get the current lore lines
                        @SuppressWarnings("unchecked")
                        List<String> loreLines = guiManager.getPlayerMetadata(player, "current_lore_lines", List.class);
                        if (loreLines != null) {
                            // Update the item lore
                            ItemStack currentItem = (ItemStack) element.getValue().getValue();
                            if (currentItem != null) {
                                ItemMeta meta = currentItem.getItemMeta();
                                if (meta != null) {
                                    // Apply color codes to all lore lines
                                    List<String> coloredLore = new ArrayList<>();
                                    for (String line : loreLines) {
                                        coloredLore.add(line.replace('&', '§'));
                                    }
                                    meta.setLore(coloredLore);
                                    currentItem.setItemMeta(meta);
                                    element.setValue(DataValue.of(currentItem));
                                    player.sendMessage("§aLore updated with " + loreLines.size() + " lines");
                                }
                            }
                        }
                        
                        // Clear the pending input state
                        guiManager.setPlayerMetadata(player, "awaiting_lore_input", false);
                        guiManager.setPlayerMetadata(player, "pending_lore_element", null);
                        guiManager.setPlayerMetadata(player, "current_lore_lines", null);
                        
                        // Reopen the item editor GUI
                        InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                        if (interactiveGUI != null) {
                            interactiveGUI.refreshGUI(player);
                        }
                        return;
                    }
                    
                    // Add the line to the current lore
                    @SuppressWarnings("unchecked")
                    List<String> loreLines = guiManager.getPlayerMetadata(player, "current_lore_lines", List.class);
                    if (loreLines == null) {
                        loreLines = new ArrayList<>();
                    }
                    loreLines.add(message);
                    guiManager.setPlayerMetadata(player, "current_lore_lines", loreLines);
                    player.sendMessage("§aLore line added. Type 'done' when finished or 'cancel' to cancel.");
                    return;
                }
            }
        }
        
        if (message.equalsIgnoreCase("УДАЛИТЬ")) {
            event.setCancelled(true);
            
            if (guiManager.isAwaitingDeleteConfirmation(player)) {
                new org.bukkit.scheduler.BukkitRunnable() {
                    @Override
                    public void run() {
                        String worldId = guiManager.getDeleteConfirmationWorldId(player);
                        var world = plugin.getWorldManager().getWorld(worldId);
                        if (world != null) {
                            plugin.getWorldManager().deleteWorld(world.getId(), player);
                            player.sendMessage("§aWorld deleted successfully!");
                        } else {
                            player.sendMessage("§cWorld not found!");
                        }
                        guiManager.clearDeleteConfirmation(player);
                    }
                }.runTask(plugin);
            } else {
                // Check legacy delete confirmation system
                java.util.Map<java.util.UUID, String> deleteConfirmations = plugin.getDeleteConfirmations();
                if (deleteConfirmations.containsKey(player.getUniqueId())) {
                    new org.bukkit.scheduler.BukkitRunnable() {
                        @Override
                        public void run() {
                            String worldId = deleteConfirmations.get(player.getUniqueId());
                            var world = plugin.getWorldManager().getWorld(worldId);
                            if (world != null) {
                                plugin.getWorldManager().deleteWorld(world.getId(), player);
                                player.sendMessage("§aWorld deleted successfully!");
                            } else {
                                player.sendMessage("§cWorld not found!");
                            }
                            deleteConfirmations.remove(player.getUniqueId());
                        }
                    }.runTask(plugin);
                } else {
                    // Try to find world from current player location as fallback
                    CreativeWorld currentWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
                    if (currentWorld != null) {
                        new org.bukkit.scheduler.BukkitRunnable() {
                            @Override
                            public void run() {
                                plugin.getWorldManager().deleteWorld(currentWorld.getId(), player);
                                player.sendMessage("§aWorld deleted successfully!");
                            }
                        }.runTask(plugin);
                    } else {
                        player.sendMessage("§cNo deletion confirmation pending.");
                    }
                }
            }
        }
    }
}