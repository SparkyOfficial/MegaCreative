package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.managers.GUIManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for GUI editors.
 * This class manages the registration and opening of GUI editors for different block actions.
 */
public class GUIRegistry {
    private final Map<String, Object> editors = new HashMap<>();
    
    /**
     * Registers a GUI editor for a specific action ID
     * @param actionId the action ID
     * @param factory the factory function that creates the GUI editor
     */
    public void register(String actionId, Object factory) {
        editors.put(actionId, factory);
    }
    
    /**
     * Opens a GUI editor for a specific action ID
     * @param actionId the action ID
     * @param plugin the MegaCreative plugin instance
     * @param player the player to open the GUI for
     * @param codeBlock the code block to edit
     */
    public void open(String actionId, MegaCreative plugin, Player player, CodeBlock codeBlock) {
        if (editors.containsKey(actionId)) {
            EditorContext context = new EditorContext(plugin, player, codeBlock);
            // For now, we'll just send a message since we're simplifying the implementation
            player.sendMessage("GUI editor would open for action: " + codeBlock.getAction());
        } else {
            // Open default parameter editor
            openDefaultEditor(plugin, player, codeBlock);
        }
    }
    
    /**
     * Opens the default parameter editor for a code block
     * @param plugin the MegaCreative plugin instance
     * @param player the player to open the GUI for
     * @param codeBlock the code block to edit
     */
    private void openDefaultEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        // Implementation would depend on existing default editor classes
        // This is a placeholder for the default editor
        player.sendMessage("Default editor not implemented yet for action: " + codeBlock.getAction());
    }
    
    /**
     * Context class for passing parameters to GUI editor factories
     */
    public static class EditorContext {
        private final MegaCreative plugin;
        private final Player player;
        private final CodeBlock codeBlock;
        
        public EditorContext(MegaCreative plugin, Player player, CodeBlock codeBlock) {
            this.plugin = plugin;
            this.player = player;
            this.codeBlock = codeBlock;
        }
        
        public MegaCreative getPlugin() {
            return plugin;
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public CodeBlock getCodeBlock() {
            return codeBlock;
        }
    }
}