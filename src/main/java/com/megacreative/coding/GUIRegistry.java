package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.gui.editors.actions.SetVarEditor;
import com.megacreative.gui.editors.actions.GetVarEditor;
import com.megacreative.gui.editors.conditions.HasPermissionEditor;
import com.megacreative.gui.editors.conditions.IfVarEqualsEditor;
import com.megacreative.gui.editors.conditions.IfVarGreaterEditor;
import com.megacreative.gui.editors.events.PlayerJoinEventEditor;
import com.megacreative.gui.editors.events.BlockPlaceEventEditor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}

/**
 * Registry for GUI editors.
 * This class manages the registration and opening of GUI editors for different block actions.
 */
public class GUIRegistry {
    private final Map<String, TriFunction<MegaCreative, Player, CodeBlock, Object>> editors = new HashMap<>();
    
    public GUIRegistry() {
        // Register action editors
        register("setVar", SetVarEditor::new);
        register("getVar", GetVarEditor::new);
        
        // Register condition editors
        register("hasPermission", HasPermissionEditor::new);
        register("ifVarEquals", IfVarEqualsEditor::new);
        register("ifVarGreater", IfVarGreaterEditor::new);
        
        // Register event editors
        register("playerJoin", PlayerJoinEventEditor::new);
        register("blockPlace", BlockPlaceEventEditor::new);
    }
    
    /**
     * Registers a GUI editor for a specific action ID
     * @param actionId the action ID
     * @param factory the factory function that creates the GUI editor
     */
    public void register(String actionId, TriFunction<MegaCreative, Player, CodeBlock, Object> factory) {
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
            Object editor = editors.get(actionId).apply(plugin, player, codeBlock);
            // Try to call open() method on the editor if it has one
            try {
                editor.getClass().getMethod("open").invoke(editor);
            } catch (Exception e) {
                // Fallback to default editor
                openDefaultEditor(plugin, player, codeBlock);
            }
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
        new com.megacreative.coding.CodingParameterGUI(
            player, 
            codeBlock.getAction(), 
            new org.bukkit.Location(org.bukkit.Bukkit.getWorld(codeBlock.getWorldId()), codeBlock.getX(), codeBlock.getY(), codeBlock.getZ()), 
            parameters -> {
                // Apply parameters to the code block
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    codeBlock.setParameter(entry.getKey(), com.megacreative.coding.values.DataValue.fromObject(entry.getValue()));
                }
                player.sendMessage("§aПараметры сохранены!");
            },
            plugin.getServiceRegistry().getGuiManager()
        ).open();
    }
}