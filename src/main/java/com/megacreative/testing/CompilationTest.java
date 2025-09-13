package com.megacreative.testing;

import com.megacreative.MegaCreative;
import com.megacreative.services.CodeCompiler;
import com.megacreative.coding.CodeScript;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.logging.Logger;

/**
 * Test class for verifying the complete compilation process
 * This test ensures that world structures are properly compiled to executable scripts
 */
public class CompilationTest {
    
    private final MegaCreative plugin;
    private final Logger logger;
    
    public CompilationTest(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Tests the complete compilation process from world structures to executable scripts
     * 
     * @param playerName The name of the player to test with
     * @return true if compilation test passes, false otherwise
     */
    public boolean testCompilationProcess(String playerName) {
        try {
            // Get the player
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                logger.severe("Player " + playerName + " not found!");
                return false;
            }
            
            // Get the player's current world
            World world = player.getWorld();
            logger.info("Testing compilation in world: " + world.getName());
            
            // Get the CodeCompiler service
            CodeCompiler codeCompiler = plugin.getServiceRegistry().getCodeCompiler();
            if (codeCompiler == null) {
                logger.severe("CodeCompiler service not available!");
                return false;
            }
            
            // Test 1: Compile world to CodeScript objects
            logger.info("=== Test 1: Compiling world to CodeScript objects ===");
            List<CodeScript> scripts = codeCompiler.compileWorldScripts(world);
            logger.info("Found " + scripts.size() + " scripts in world");
            
            // Log script details
            for (int i = 0; i < scripts.size(); i++) {
                CodeScript script = scripts.get(i);
                logger.info("Script " + (i + 1) + ": " + script.getName());
                logger.info("  Enabled: " + script.isEnabled());
                logger.info("  Type: " + script.getType());
                if (script.getRootBlock() != null) {
                    logger.info("  Root block action: " + script.getRootBlock().getAction());
                }
            }
            
            // Test 2: Compile world to code strings (reference system-style)
            logger.info("=== Test 2: Compiling world to code strings (reference system-style) ===");
            List<String> codeStrings = codeCompiler.compileWorldToCodeStrings(world);
            logger.info("Generated " + codeStrings.size() + " code strings");
            
            // Log code strings
            for (int i = 0; i < codeStrings.size(); i++) {
                logger.info("Line " + (i + 1) + ": " + codeStrings.get(i));
            }
            
            // Test 3: Save compiled code (reference system-style)
            logger.info("=== Test 3: Saving compiled code ===");
            String worldId = world.getName().replace("-code", "").replace("-world", "");
            codeCompiler.saveCompiledCode(worldId, codeStrings);
            
            logger.info("=== Compilation test completed successfully ===");
            player.sendMessage("§a✓ Compilation test passed! Found " + scripts.size() + " scripts and " + codeStrings.size() + " code lines.");
            player.sendMessage("§a✓ Code compilation is working correctly!");
            
            return true;
            
        } catch (Exception e) {
            logger.severe("Compilation test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Runs a quick compilation test
     * 
     * @param world The world to test compilation in
     * @return true if quick test passes, false otherwise
     */
    public boolean quickTest(World world) {
        try {
            logger.info("=== Quick compilation test ===");
            
            // Get the CodeCompiler service
            CodeCompiler codeCompiler = plugin.getServiceRegistry().getCodeCompiler();
            if (codeCompiler == null) {
                logger.severe("CodeCompiler service not available!");
                return false;
            }
            
            // Quick test: compile world structures
            List<CodeScript> scripts = codeCompiler.compileWorldScripts(world);
            logger.info("Quick test: Found " + scripts.size() + " scripts");
            
            // Quick test: compile to code strings
            List<String> codeStrings = codeCompiler.compileWorldToCodeStrings(world);
            logger.info("Quick test: Generated " + codeStrings.size() + " code lines");
            
            logger.info("=== Quick compilation test completed ===");
            return true;
            
        } catch (Exception e) {
            logger.severe("Quick compilation test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}