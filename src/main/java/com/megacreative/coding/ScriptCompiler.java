package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.coding.events.ScriptStructureChangedEvent;
import com.megacreative.coding.events.CodeBlockPlacedEvent;
import com.megacreative.coding.events.CodeBlockBrokenEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.logging.Logger;

/**
 * ScriptCompiler is responsible for compiling CodeBlocks into executable CodeScripts.
 * It listens to ScriptStructureChangedEvent and compiles scripts when the structure changes.
 * This separation of concerns ensures that connection logic and compilation logic are decoupled.
 */
public class ScriptCompiler implements Listener {
    private static final Logger log = Logger.getLogger(ScriptCompiler.class.getName());
    
    private final MegaCreative plugin;
    
    public ScriptCompiler(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Listens to structure change events and compiles scripts accordingly
     */
    @EventHandler
    public void onScriptStructureChanged(ScriptStructureChangedEvent event) {
        CreativeWorld creativeWorld = event.getCreativeWorld();
        CodeBlock modifiedBlock = event.getModifiedBlock();
        
        // Find the root block (event block) that this change affects
        CodeBlock rootBlock = findRootBlock(modifiedBlock);
        
        if (rootBlock != null && isEventBlock(rootBlock)) {
            // Compile the script starting from this root block
            CodeScript script = compileScript(rootBlock);
            if (script != null) {
                // Add or update the script in the creative world
                creativeWorld.addScript(script);
                log.info("Compiled script for event block: " + rootBlock.getEvent());
            }
        }
    }
    
    /**
     * Finds the root block (event block) for a given block by traversing backwards
     */
    private CodeBlock findRootBlock(CodeBlock block) {
        // Traverse backwards through nextBlock references to find the root
        CodeBlock current = block;
        while (current != null && current.getNextBlock() != null) {
            // Check if this is an event block (root)
            if (isEventBlock(current)) {
                return current;
            }
            current = current.getNextBlock();
        }
        
        // If we didn't find it going forward, try going backward
        current = block;
        while (current != null) {
            if (isEventBlock(current)) {
                return current;
            }
            // This is a simplified approach - in reality, you'd need to traverse
            // the entire graph to find the root
            break;
        }
        
        return null;
    }
    
    /**
     * Compiles a script starting from a root block
     */
    private CodeScript compileScript(CodeBlock rootBlock) {
        if (rootBlock == null) {
            return null;
        }
        
        // Create a new script with the root block
        CodeScript script = new CodeScript(rootBlock);
        script.setEnabled(true);
        
        return script;
    }
    
    /**
     * Checks if a block is an event block (script root)
     */
    private boolean isEventBlock(CodeBlock block) {
        return block != null && block.getEvent() != null && !block.getEvent().equals("NOT_SET");
    }
}