package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.ScriptStructureChangedEvent;
import com.megacreative.models.CreativeWorld;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.logging.Logger;

/**
 * Script compiler for handling script compilation on structure changes.
 * This class listens to ScriptStructureChangedEvent and validates scripts when needed.
 */
public class ScriptCompiler implements Listener {
    private static final Logger log = Logger.getLogger(ScriptCompiler.class.getName());
    
    private final MegaCreative plugin;
    
    public ScriptCompiler(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles script structure change events and validates the affected scripts
     * @param event the script structure change event
     */
    @EventHandler
    public void onScriptStructureChanged(ScriptStructureChangedEvent event) {
        try {
            CreativeWorld creativeWorld = event.getCreativeWorld();
            if (creativeWorld == null) {
                log.warning("CreativeWorld is null in ScriptStructureChangedEvent");
                return;
            }
            
            log.info("Processing structure change in world: " + creativeWorld.getName() + 
                    " for block: " + event.getModifiedBlock().getId());
            
            // Get all scripts from the creative world
            List<CodeScript> scripts = creativeWorld.getScripts();
            if (scripts == null || scripts.isEmpty()) {
                log.info("No scripts found in world: " + creativeWorld.getName());
                return;
            }
            
            // Compile and validate all scripts in the world
            int compiledCount = 0;
            for (CodeScript script : scripts) {
                if (compileScript(script)) {
                    compiledCount++;
                }
            }
            
            log.info("Structure change processed for world: " + creativeWorld.getName() + 
                    " with " + scripts.size() + " scripts. Successfully compiled: " + compiledCount);
                    
        } catch (Exception e) {
            log.severe("Error handling ScriptStructureChangedEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Compiles and validates a script
     * @param script the script to compile
     * @return true if compilation was successful, false otherwise
     */
    private boolean compileScript(CodeScript script) {
        try {
            if (script == null || script.getRootBlock() == null) {
                log.warning("Cannot compile null script or script without root block");
                return false;
            }
            
            log.info("Compiling script: " + script.getName());
            
            // Validate script structure
            if (!validateScriptStructure(script)) {
                log.warning("Script validation failed for: " + script.getName());
                return false;
            }
            
            // Optimize script
            optimizeScript(script);
            
            // Pre-compile script for faster execution
            precompileScript(script);
            
            log.info("Successfully compiled script: " + script.getName());
            return true;
        } catch (Exception e) {
            log.severe("Error compiling script " + script.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validates the structure of a script
     * @param script the script to validate
     * @return true if valid, false otherwise
     */
    private boolean validateScriptStructure(CodeScript script) {
        try {
            CodeBlock rootBlock = script.getRootBlock();
            if (rootBlock == null) {
                log.warning("Script " + script.getName() + " has no root block");
                return false;
            }
            
            // Validate root block
            if (!isValidRootBlock(rootBlock)) {
                log.warning("Script " + script.getName() + " has invalid root block");
                return false;
            }
            
            // Validate all blocks in the script
            List<CodeBlock> allBlocks = script.getBlocks();
            for (CodeBlock block : allBlocks) {
                if (!validateBlock(block)) {
                    log.warning("Script " + script.getName() + " has invalid block: " + block.getAction());
                    return false;
                }
            }
            
            // Validate control flow structures
            if (!validateControlFlow(script)) {
                log.warning("Script " + script.getName() + " has invalid control flow");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.severe("Error validating script structure: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a block is a valid root block
     * @param block the block to check
     * @return true if valid, false otherwise
     */
    private boolean isValidRootBlock(CodeBlock block) {
        // Root blocks should be events or functions
        String action = block.getAction();
        return action != null && (action.startsWith("on") || "FUNCTION".equals(block.getEvent()));
    }
    
    /**
     * Validates a single block
     * @param block the block to validate
     * @return true if valid, false otherwise
     */
    private boolean validateBlock(CodeBlock block) {
        // Check if block has required parameters
        if (block.getAction() == null || "NOT_SET".equals(block.getAction())) {
            // It's okay for blocks to not have actions set yet
            return true;
        }
        
        // Add more validation logic here as needed
        return true;
    }
    
    /**
     * Validates control flow structures in a script
     * @param script the script to validate
     * @return true if valid, false otherwise
     */
    private boolean validateControlFlow(CodeScript script) {
        try {
            // Check for matching brackets
            List<CodeBlock> allBlocks = script.getBlocks();
            int openBrackets = 0;
            int closeBrackets = 0;
            
            for (CodeBlock block : allBlocks) {
                if (block.isBracket()) {
                    if (block.getBracketType() == CodeBlock.BracketType.OPEN) {
                        openBrackets++;
                    } else if (block.getBracketType() == CodeBlock.BracketType.CLOSE) {
                        closeBrackets++;
                    }
                }
            }
            
            if (openBrackets != closeBrackets) {
                log.warning("Script " + script.getName() + " has mismatched brackets: " + 
                           openBrackets + " open, " + closeBrackets + " close");
                return false;
            }
            
            // Check for proper ELSE block connections
            for (CodeBlock block : allBlocks) {
                if ("conditionalBranch".equals(block.getAction()) && block.getElseBlock() != null) {
                    // Verify that ELSE block exists in the script
                    if (!allBlocks.contains(block.getElseBlock())) {
                        log.warning("Script " + script.getName() + " has dangling ELSE block reference");
                        return false;
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            log.severe("Error validating control flow: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Optimizes a script by pre-processing common patterns
     * @param script the script to optimize
     */
    private void optimizeScript(CodeScript script) {
        try {
            // Currently just logging optimization info
            List<CodeBlock> allBlocks = script.getBlocks();
            log.info("Optimizing script " + script.getName() + " with " + allBlocks.size() + " blocks");
            
            // In a more advanced implementation, we could:
            // 1. Identify and cache frequently used variable references
            // 2. Pre-resolve common parameter patterns
            // 3. Identify and optimize loops
            // 4. Cache compiled expressions
        } catch (Exception e) {
            log.warning("Error optimizing script " + script.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Pre-compiles a script for faster execution
     * @param script the script to pre-compile
     */
    private void precompileScript(CodeScript script) {
        try {
            // Currently just logging pre-compilation info
            log.info("Pre-compiling script: " + script.getName());
            
            // In a more advanced implementation, we could:
            // 1. Pre-parse expressions and store compiled versions
            // 2. Pre-resolve variable references
            // 3. Cache action and condition handlers
            // 4. Generate optimized execution paths
        } catch (Exception e) {
            log.warning("Error pre-compiling script " + script.getName() + ": " + e.getMessage());
        }
    }
}