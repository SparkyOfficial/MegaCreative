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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.ArrayList;

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
        plugin.getServiceRegistry().getGuiManager().unregisterGUI(player);
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
        
        
        event.setCancelled(true);
        event.getPlayer().getServer().getScheduler().runTask(
            plugin, 
            () -> {
                
                GUIManager guiManager = plugin.getServiceRegistry().getGuiManager();
                if (guiManager != null) {
                    
                    Boolean awaitingTextInput = guiManager.getPlayerMetadata(player, "awaiting_text_input", Boolean.class);
                    if (Boolean.TRUE.equals(awaitingTextInput)) {
                        
                        
                        InteractiveGUIManager.TextInputElement element = guiManager.getPlayerMetadata(player, "pending_text_input_element", InteractiveGUIManager.TextInputElement.class);
                        if (element != null) {
                            
                            if (message.equalsIgnoreCase("cancel")) {
                                player.sendMessage("§cText input cancelled");
                                guiManager.setPlayerMetadata(player, "awaiting_text_input", false);
                                guiManager.setPlayerMetadata(player, "pending_text_input_element", null);
                                return;
                            }
                            
                            
                            element.setValue(DataValue.of(message));
                            player.sendMessage("§aText input accepted: §f" + message);
                            
                            
                            guiManager.setPlayerMetadata(player, "awaiting_text_input", false);
                            guiManager.setPlayerMetadata(player, "pending_text_input_element", null);
                            
                            
                            InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                            if (interactiveGUI != null) {
                                interactiveGUI.refreshGUI(player);
                            }
                            return;
                        }
                    }
                    
                    
                    Boolean awaitingAmountInput = guiManager.getPlayerMetadata(player, "awaiting_amount_input", Boolean.class);
                    if (Boolean.TRUE.equals(awaitingAmountInput)) {
                        
                        
                        InteractiveGUIManager.ItemStackEditorElement element = guiManager.getPlayerMetadata(player, "pending_amount_element", InteractiveGUIManager.ItemStackEditorElement.class);
                        if (element != null) {
                            
                            if (message.equalsIgnoreCase("cancel")) {
                                player.sendMessage("§cAmount input cancelled");
                                guiManager.setPlayerMetadata(player, "awaiting_amount_input", false);
                                guiManager.setPlayerMetadata(player, "pending_amount_element", null);
                                
                                
                                InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                                if (interactiveGUI != null) {
                                    interactiveGUI.refreshGUI(player);
                                }
                                return;
                            }
                            
                            
                            try {
                                int amount = Integer.parseInt(message);
                                if (amount >= 1 && amount <= 64) {
                                    
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
                            
                            
                            guiManager.setPlayerMetadata(player, "awaiting_amount_input", false);
                            guiManager.setPlayerMetadata(player, "pending_amount_element", null);
                            
                            
                            InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                            if (interactiveGUI != null) {
                                interactiveGUI.refreshGUI(player);
                            }
                            return;
                        }
                    }
                    
                    
                    Boolean awaitingNameInput = guiManager.getPlayerMetadata(player, "awaiting_name_input", Boolean.class);
                    if (Boolean.TRUE.equals(awaitingNameInput)) {
                        
                        
                        InteractiveGUIManager.ItemStackEditorElement element = guiManager.getPlayerMetadata(player, "pending_name_element", InteractiveGUIManager.ItemStackEditorElement.class);
                        if (element != null) {
                            
                            if (message.equalsIgnoreCase("cancel")) {
                                player.sendMessage("§cName input cancelled");
                                guiManager.setPlayerMetadata(player, "awaiting_name_input", false);
                                guiManager.setPlayerMetadata(player, "pending_name_element", null);
                                
                                
                                InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                                if (interactiveGUI != null) {
                                    interactiveGUI.refreshGUI(player);
                                }
                                return;
                            }
                            
                            
                            ItemStack currentItem = (ItemStack) element.getValue().getValue();
                            if (currentItem != null) {
                                ItemMeta meta = currentItem.getItemMeta();
                                if (meta != null) {
                                    
                                    String coloredName = message.replace('&', '§');
                                    meta.setDisplayName(coloredName);
                                    currentItem.setItemMeta(meta);
                                    element.setValue(DataValue.of(currentItem));
                                    player.sendMessage("§aName set to: §f" + coloredName);
                                }
                            }
                            
                            
                            guiManager.setPlayerMetadata(player, "awaiting_name_input", false);
                            guiManager.setPlayerMetadata(player, "pending_name_element", null);
                            
                            
                            InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                            if (interactiveGUI != null) {
                                interactiveGUI.refreshGUI(player);
                            }
                            return;
                        }
                    }
                    
                    
                    Boolean awaitingLoreInput = guiManager.getPlayerMetadata(player, "awaiting_lore_input", Boolean.class);
                    if (Boolean.TRUE.equals(awaitingLoreInput)) {
                        
                        
                        InteractiveGUIManager.ItemStackEditorElement element = guiManager.getPlayerMetadata(player, "pending_lore_element", InteractiveGUIManager.ItemStackEditorElement.class);
                        if (element != null) {
                            
                            if (message.equalsIgnoreCase("cancel")) {
                                player.sendMessage("§cLore input cancelled");
                                guiManager.setPlayerMetadata(player, "awaiting_lore_input", false);
                                guiManager.setPlayerMetadata(player, "pending_lore_element", null);
                                guiManager.setPlayerMetadata(player, "current_lore_lines", null);
                                
                                
                                InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                                if (interactiveGUI != null) {
                                    interactiveGUI.refreshGUI(player);
                                }
                                return;
                            }
                            
                            
                            if (message.equalsIgnoreCase("done")) {
                                
                                @SuppressWarnings("unchecked")
                                List<String> loreLines = guiManager.getPlayerMetadata(player, "current_lore_lines", List.class);
                                if (loreLines != null) {
                                    
                                    ItemStack currentItem = (ItemStack) element.getValue().getValue();
                                    if (currentItem != null) {
                                        ItemMeta meta = currentItem.getItemMeta();
                                        if (meta != null) {
                                            
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
                                
                                
                                guiManager.setPlayerMetadata(player, "awaiting_lore_input", false);
                                guiManager.setPlayerMetadata(player, "pending_lore_element", null);
                                guiManager.setPlayerMetadata(player, "current_lore_lines", null);
                                
                                
                                InteractiveGUIManager interactiveGUI = guiManager.getInteractiveGUIManager();
                                if (interactiveGUI != null) {
                                    interactiveGUI.refreshGUI(player);
                                }
                                return;
                            }
                            
                            
                            @SuppressWarnings("unchecked")
                            List<String> loreLines = guiManager.getPlayerMetadata(player, "current_lore_lines", List.class);
                            if (loreLines == null) {
                                loreLines = new ArrayList<>();
                            }
                            loreLines.add(message);
                            guiManager.setPlayerMetadata(player, "current_lore_lines", loreLines);
                            player.sendMessage("§aLine added to lore. Type 'done' when finished or 'cancel' to abort.");
                            return;
                        }
                    }
                }
                
                
                player.chat(message);
            }
        );
    }
}