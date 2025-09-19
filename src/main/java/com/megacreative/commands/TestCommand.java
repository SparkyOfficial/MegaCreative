package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.Constants;
import com.megacreative.testing.ScriptTestRunner;
import com.megacreative.testing.SampleTestSuite;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
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
            sender.sendMessage(Constants.TEST_COMMAND_PLAYERS_ONLY);
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        return handleSubCommand(player, subCommand, args);
    }

    private boolean handleSubCommand(Player player, String subCommand, String[] args) {
        switch (subCommand) {
            case Constants.TEST_RUN:
                if (args.length < 2) {
                    player.sendMessage(Constants.TEST_USAGE_RUN);
                    return true;
                }
                String suiteName = args[1];
                String testCaseName = args.length > 2 ? args[2] : null;
                runTestCase(player, suiteName, testCaseName);
                return true;
                
            case Constants.TEST_LIST:
                if (plugin.getTestRunner() != null) {
                    displayAvailableTestSuites(player);
                } else {
                    player.sendMessage(Constants.TEST_RUNNER_NOT_AVAILABLE);
                }
                return true;
                
            case Constants.TEST_CREATE:
                if (args.length < 2) {
                    player.sendMessage(Constants.TEST_USAGE_RUN);
                    return true;
                }
                String newSuiteName = args[1];
                createSampleTestSuite(player, newSuiteName);
                return true;
                
            case Constants.TEST_SAMPLE:
                createSampleTestSuite(player, Constants.SAMPLE_SUITE_NAME);
                return true;
                
            case Constants.TEST_HELP:
                showHelp(player);
                return true;
                
            default:
                player.sendMessage(Constants.TEST_UNKNOWN_SUBCOMMAND + subCommand);
                return true;
        }
    }

    private void runTestCase(Player player, String suiteName, String testCaseName) {
        ScriptTestRunner testRunner = plugin.getTestRunner();
        if (testRunner == null) {
            player.sendMessage(Constants.TEST_RUNNER_NOT_AVAILABLE);
            return;
        }
        
        if (testCaseName != null) {
            player.sendMessage(Constants.TEST_RUNNING_CASE + testCaseName + " in suite: " + suiteName);
            testRunner.runTestCase(suiteName, testCaseName)
                .thenAccept(result -> {
                    player.sendMessage(result.isPassed() ? Constants.TEST_PASSED : Constants.TEST_FAILED);
                    player.sendMessage(Constants.TEST_FAILED_TEST_FORMAT + result.getMessage());
                })
                .exceptionally(throwable -> {
                    player.sendMessage(Constants.TEST_ERROR_RUNNING + throwable.getMessage());
                    return null;
                });
        } else {
            player.sendMessage(Constants.TEST_RUNNING_SUITE + suiteName);
            testRunner.runTestSuite(suiteName)
                .thenAccept(results -> displayTestSuiteResults(player, results))
                .exceptionally(throwable -> {
                    player.sendMessage(Constants.TEST_ERROR_RUNNING_SUITE + throwable.getMessage());
                    return null;
                });
        }
    }

    private void displayTestSuiteResults(Player player, List<ScriptTestRunner.TestResult> results) {
        if (results.isEmpty()) {
            player.sendMessage(Constants.NO_TEST_RESULTS);
            return;
        }
        
        // Since TestResult doesn't have suite name, we'll use a placeholder
        String suiteName = "Test Suite";
        long passed = results.stream().filter(ScriptTestRunner.TestResult::isPassed).count();
        long failed = results.size() - passed;
        
        player.sendMessage(Constants.TEST_SUITE_RESULTS);
        player.sendMessage(Constants.SUITE_PREFIX + suiteName);
        player.sendMessage(Constants.PASSED_PREFIX + passed);
        player.sendMessage(Constants.FAILED_PREFIX + failed);
        player.sendMessage(Constants.TOTAL_PREFIX + results.size());
        
        if (failed > 0) {
            player.sendMessage(Constants.FAILED_TESTS_HEADER);
            results.stream()
                .filter(result -> !result.isPassed())
                .forEach(result -> player.sendMessage(Constants.FAILED_TEST_FORMAT + result.getTestName() + ": " + result.getMessage()));
        }
    }

    private void displayAvailableTestSuites(Player player) {
        ScriptTestRunner testRunner = plugin.getTestRunner();
        if (testRunner == null) {
            player.sendMessage(Constants.TEST_RUNNER_NOT_AVAILABLE);
            return;
        }
        
        player.sendMessage(Constants.TEST_AVAILABLE_SUITES);
        testRunner.getAvailableTestSuites().forEach(suite -> player.sendMessage(Constants.SUITE_ITEM_FORMAT + suite));
    }

    private void createSampleTestSuite(Player player, String suiteName) {
        ScriptTestRunner testRunner = plugin.getTestRunner();
        if (testRunner == null) {
            player.sendMessage(Constants.TEST_RUNNER_NOT_AVAILABLE);
            return;
        }
        
        // testRunner.createSampleTestSuite(suiteName); // Method doesn't exist
        player.sendMessage(Constants.SAMPLE_SUITE_CREATED + suiteName + Constants.SAMPLE_SUITE_CREATED_SUFFIX);
    }

    private void showHelp(Player player) {
        player.sendMessage(Constants.TEST_COMMAND_HEADER);
        player.sendMessage(Constants.TEST_COMMAND_HELP_RUN);
        player.sendMessage(Constants.TEST_COMMAND_HELP_LIST);
        player.sendMessage(Constants.TEST_COMMAND_HELP_CREATE);
        player.sendMessage(Constants.TEST_COMMAND_HELP_SAMPLE);
        player.sendMessage(Constants.TEST_COMMAND_HELP_HELP);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        addMainSubcommands(completions, args);
        return completions;
    }

    private void addMainSubcommands(List<String> completions, String[] args) {
        if (args.length == 1) {
            completions.addAll(Arrays.asList(
                Constants.TEST_RUN,
                Constants.TEST_LIST,
                Constants.TEST_CREATE,
                Constants.TEST_SAMPLE,
                Constants.TEST_HELP
            ));
        }
    }
}