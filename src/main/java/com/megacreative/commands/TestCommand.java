package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.testing.ScriptTestRunner;
import com.megacreative.testing.SampleTestSuite;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command for running script tests
 * Allows players to run unit tests, integration tests, and performance tests
 * on their MegaCreative scripts
 */
public class TestCommand implements CommandExecutor, TabCompleter {
    private final MegaCreative plugin;
    
    public TestCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "run":
                handleRunCommand(player, args);
                break;
            case "list":
                handleListCommand(player, args);
                break;
            case "create":
                handleCreateCommand(player, args);
                break;
            case "sample":
                handleSampleCommand(player, args);
                break;
            case "help":
                showHelp(player);
                break;
            default:
                player.sendMessage("§cUnknown subcommand: " + subCommand);
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private void handleRunCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /test run <suite> [testcase]");
            return;
        }
        
        String suiteName = args[1];
        ScriptTestRunner testRunner = plugin.getTestRunner();
        
        if (testRunner == null) {
            player.sendMessage("§cTest runner is not available!");
            return;
        }
        
        if (args.length >= 3) {
            // Run specific test case
            String testCaseName = args[2];
            player.sendMessage("§eRunning test case: " + testCaseName + " in suite: " + suiteName);
            
            CompletableFuture<com.megacreative.testing.ScriptTestRunner.TestResult> future = 
                testRunner.runTestCase(suiteName, testCaseName);
                
            future.thenAccept(result -> {
                if (result != null) {
                    player.sendMessage(result.isPassed() ? "§a✓ Test passed" : "§c✗ Test failed");
                    player.sendMessage("§7" + result.getMessage());
                } else {
                    player.sendMessage("§cTest case not found: " + testCaseName);
                }
            }).exceptionally(throwable -> {
                player.sendMessage("§cError running test: " + throwable.getMessage());
                return null;
            });
        } else {
            // Run entire test suite
            player.sendMessage("§eRunning test suite: " + suiteName);
            
            CompletableFuture<List<com.megacreative.testing.ScriptTestRunner.TestResult>> future = 
                testRunner.runTestSuite(suiteName);
                
            future.thenAccept(results -> {
                int passed = 0;
                int failed = 0;
                
                for (com.megacreative.testing.ScriptTestRunner.TestResult result : results) {
                    if (result.isPassed()) {
                        passed++;
                    } else {
                        failed++;
                    }
                }
                
                player.sendMessage("§a=== Test Suite Results ===");
                player.sendMessage("§7Suite: " + suiteName);
                player.sendMessage("§7Passed: " + passed);
                player.sendMessage("§7Failed: " + failed);
                player.sendMessage("§7Total: " + results.size());
                
                if (failed > 0) {
                    player.sendMessage("§cFailed tests:");
                    for (com.megacreative.testing.ScriptTestRunner.TestResult result : results) {
                        if (!result.isPassed()) {
                            player.sendMessage("§c  ✗ " + result.getTestName() + ": " + result.getMessage());
                        }
                    }
                }
            }).exceptionally(throwable -> {
                player.sendMessage("§cError running test suite: " + throwable.getMessage());
                return null;
            });
        }
    }
    
    private void handleListCommand(Player player, String[] args) {
        ScriptTestRunner testRunner = plugin.getTestRunner();
        
        if (testRunner == null) {
            player.sendMessage("§cTest runner is not available!");
            return;
        }
        
        player.sendMessage("§e=== Available Test Suites ===");
        // In a real implementation, we would list the available test suites
        player.sendMessage("§7No test suites available yet. Create one with /test create");
    }
    
    private void handleCreateCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /test create <suite>");
            return;
        }
        
        String suiteName = args[1];
        ScriptTestRunner testRunner = plugin.getTestRunner();
        
        if (testRunner == null) {
            player.sendMessage("§cTest runner is not available!");
            return;
        }
        
        // Create a new test suite
        testRunner.createTestSuite(suiteName);
        player.sendMessage("§aCreated test suite: " + suiteName);
    }
    
    private void handleSampleCommand(Player player, String[] args) {
        ScriptTestRunner testRunner = plugin.getTestRunner();
        
        if (testRunner == null) {
            player.sendMessage("§cTest runner is not available!");
            return;
        }
        
        // Create sample test suites
        SampleTestSuite.createSampleTests(plugin, testRunner);
        player.sendMessage("§aCreated sample test suites!");
        player.sendMessage("§7Available suites: VariableOperations, MathOperations, ControlFlow");
        player.sendMessage("§7Run them with: /test run <suite>");
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§e=== MegaCreative Test Command ===");
        player.sendMessage("§7/test run <suite> [testcase] §8- Run tests");
        player.sendMessage("§7/test list §8- List available test suites");
        player.sendMessage("§7/test create <suite> §8- Create a new test suite");
        player.sendMessage("§7/test sample §8- Create sample test suites");
        player.sendMessage("§7/test help §8- Show this help");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("run");
            completions.add("list");
            completions.add("create");
            completions.add("sample");
            completions.add("help");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "run":
                case "create":
                    // In a real implementation, we would provide available test suites
                    completions.add("example-suite");
                    break;
            }
        }
        
        return completions;
    }
}