package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ScriptTest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command to test script compilation and execution
 */
public class TestCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public TestCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "compile":
                    ScriptTest.testScriptCompilation(plugin, player.getWorld());
                    player.sendMessage("§aScript compilation test completed! Check console for details.");
                    break;
                    
                case "actions":
                    ScriptTest.testActionRegistration(plugin);
                    player.sendMessage("§aAction registration test completed! Check console for details.");
                    break;
                    
                case "conditions":
                    ScriptTest.testConditionRegistration(plugin);
                    player.sendMessage("§aCondition registration test completed! Check console for details.");
                    break;
                    
                default:
                    sendHelp(player);
                    break;
            }
        } else {
            sendHelp(player);
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§8§m                    §r §6§l/Test Command Help §8§m                    ");
        player.sendMessage("§7/test compile §8- §fTest script compilation");
        player.sendMessage("§7/test actions §8- §fTest action registration");
        player.sendMessage("§7/test conditions §8- §fTest condition registration");
        player.sendMessage("§8§m                                                        ");
    }
}