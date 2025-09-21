package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.values.DataValue;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class TestScriptCommand implements CommandExecutor {
    private final MegaCreative plugin;

    public TestScriptCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        try {
            // Get the script engine from service registry
            ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
            if (serviceRegistry == null) {
                player.sendMessage("Service registry not available!");
                return true;
            }
            
            ScriptEngine scriptEngine = serviceRegistry.getScriptEngine();
            if (scriptEngine == null) {
                player.sendMessage("Script engine not available!");
                return true;
            }
            
            // Create a simple test script
            CodeBlock rootBlock = new CodeBlock(Material.DIAMOND_BLOCK, "sendMessage");
            
            Map<String, DataValue> parameters = new HashMap<>();
            parameters.put("message", DataValue.fromObject("Hello from test script!"));
            rootBlock.setParameters(parameters);
            
            CodeScript testScript = new CodeScript("test_script_" + UUID.randomUUID().toString(), true, rootBlock);
            
            // Execute the script
            scriptEngine.executeScript(testScript, player, "test_command")
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        player.sendMessage("Test script executed successfully!");
                    } else {
                        player.sendMessage("Test script failed: " + result.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    player.sendMessage("Test script error: " + throwable.getMessage());
                    return null;
                });
                
        } catch (Exception e) {
            player.sendMessage("Error executing test script: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
}