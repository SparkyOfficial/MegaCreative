package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.configs.WorldCode;
import com.megacreative.services.CodeCompiler;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.logging.Logger;

/**
 * 🎆 Reference System-Style Compilation Listener
 * 
 * Automatically compiles code when players leave dev worlds, similar to FrameLand's runCompileListener.
 * This ensures that code is compiled and ready for execution when players switch to play mode.
 */
public class CompilationListener implements Listener {
    
    private final MegaCreative plugin;
    private final Logger logger;
    
    public CompilationListener(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        logger.info("🎆 Compilation listener initialized for automatic code compilation");
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChangingWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        
        // Check if player is leaving a dev world
        if (fromWorld.getName().contains("-code")) {
            compileWorldCode(fromWorld);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        // Check if player is leaving a dev world
        if (world.getName().contains("-code")) {
            compileWorldCode(world);
        }
    }
    
    /**
     * Compiles code in a world and saves it to WorldCode configuration
     */
    private void compileWorldCode(World world) {
        try {
            logger.info("Starting automatic compilation for world: " + world.getName());
            
            // Get the CodeCompiler service
            CodeCompiler codeCompiler = plugin.getServiceRegistry().getCodeCompiler();
            if (codeCompiler == null) {
                logger.severe("CodeCompiler service not available for automatic compilation!");
                return;
            }
            
            // Compile world to code strings
            List<String> codeStrings = codeCompiler.compileWorldToCodeStrings(world);
            logger.info("Compiled " + codeStrings.size() + " lines of code from world: " + world.getName());
            
            // Save compiled code to WorldCode configuration
            String worldId = world.getName().replace("-code", "");
            codeCompiler.saveCompiledCode(worldId, codeStrings);
            
            logger.info("Successfully compiled and saved code for world: " + worldId);
            
        } catch (Exception e) {
            logger.severe("Failed to automatically compile world code: " + e.getMessage());
            e.printStackTrace();
        }
    }
}