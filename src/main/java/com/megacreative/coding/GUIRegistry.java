package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Registry for GUI editors to eliminate the large switch-case in BlockPlacementHandler.
 * This allows for dynamic registration of editors without modifying the core handler.
 */
public class GUIRegistry {
    private final Map<String, BiFunction<MegaCreative, Player, CodeBlock>> editorFactories = new HashMap<>();
    
    /**
     * Registers an editor factory for a specific action ID
     */
    public void registerEditor(String actionId, BiFunction<MegaCreative, Player, CodeBlock> factory) {
        editorFactories.put(actionId, factory);
    }
    
    /**
     * Opens the appropriate editor for an action ID
     */
    public void openEditor(String actionId, MegaCreative plugin, Player player, CodeBlock codeBlock) {
        BiFunction<MegaCreative, Player, CodeBlock> factory = editorFactories.get(actionId);
        if (factory != null) {
            // Create and open the editor
            // Note: This is a simplified approach - in reality, you'd want to pass the codeBlock to the editor
            // and have the editor handle its own opening
            try {
                // This is a placeholder implementation
                // In a real implementation, you would instantiate the editor and call its open() method
                Object editor = factory.apply(plugin, player);
                if (editor != null) {
                    // Call open method if it exists
                    editor.getClass().getMethod("open").invoke(editor);
                }
            } catch (Exception e) {
                player.sendMessage("§cError opening editor for " + actionId);
                plugin.getLogger().severe("Error opening editor for " + actionId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Checks if an editor is registered for an action ID
     */
    public boolean hasEditor(String actionId) {
        return editorFactories.containsKey(actionId);
    }
}