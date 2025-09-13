package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.testing.CompilationTest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to test the compilation process
 */
public class TestCompileCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public TestCompileCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        // Run compilation test
        player.sendMessage("§eRunning compilation test...");
        plugin.getLogger().info("Player " + player.getName() + " initiated compilation test");
        
        try {
            CompilationTest test = new CompilationTest(plugin);
            boolean result = test.testCompilationProcess(player.getName());
            
            if (result) {
                player.sendMessage("§a✓ Compilation test completed successfully!");
                player.sendMessage("§aThe CodeCompiler is working correctly!");
            } else {
                player.sendMessage("§c✗ Compilation test failed!");
                player.sendMessage("§cCheck the console for error details.");
            }
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Compilation test failed with exception: " + e.getMessage());
            plugin.getLogger().severe("Compilation test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
}