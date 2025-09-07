package com.megacreative.coding.variables;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VariableManagerTestMain {
    
    public static void main(String[] args) {
        System.out.println("=== VariableManager Test ===\n");
        
        // Create a test plugin instance
        TestPlugin plugin = new TestPlugin();
        
        // Create a VariableManager instance
        VariableManager variableManager = new VariableManager(plugin);
        
        // Test local variables
        testLocalVariables(variableManager);
        
        // Test global variables
        testGlobalVariables(variableManager);
        
        // Test player variables
        testPlayerVariables(variableManager);
        
        System.out.println("\n=== Test Complete ===");
    }
    
    private static void testLocalVariables(VariableManager vm) {
        System.out.println("\n--- Testing Local Variables ---");
        String context = "test-context";
        String varName = "local-var";
        DataValue value = DataValue.of("test-value");
        
        // Set local variable
        System.out.println("Setting local variable: " + context + "." + varName + " = " + value.asString());
        vm.setLocalVariable(context, varName, value);
        
        // Get local variable
        DataValue result = vm.getLocalVariable(context, varName);
        System.out.println("Retrieved local variable: " + (result != null ? result.asString() : "null"));
        
        // Clear local variables
        System.out.println("Clearing local variables for context: " + context);
        vm.clearLocalVariables(context);
        
        // Verify cleared
        result = vm.getLocalVariable(context, varName);
        System.out.println("After clear, got: " + (result != null ? result.asString() : "null"));
    }
    
    private static void testGlobalVariables(VariableManager vm) {
        System.out.println("\n--- Testing Global Variables ---");
        String varName = "global-var";
        DataValue value = DataValue.of(42);
        
        // Set global variable
        System.out.println("Setting global variable: " + varName + " = " + value.asString());
        vm.setGlobalVariable(varName, value);
        
        // Get global variable
        DataValue result = vm.getGlobalVariable(varName);
        System.out.println("Retrieved global variable: " + (result != null ? result.asString() : "null"));
        
        // Clear global variables
        System.out.println("Clearing all global variables");
        vm.clearGlobalVariables();
        
        // Verify cleared
        result = vm.getGlobalVariable(varName);
        System.out.println("After clear, got: " + (result != null ? result.asString() : "null"));
    }
    
    private static void testPlayerVariables(VariableManager vm) {
        System.out.println("\n--- Testing Player Variables ---");
        UUID playerId = UUID.randomUUID();
        String varName = "player-var";
        DataValue value = DataValue.of(true);
        
        // Set player variable
        System.out.println("Setting player variable: " + playerId + "." + varName + " = " + value.asString());
        vm.setPlayerVariable(playerId, varName, value);
        
        // Get player variable
        DataValue result = vm.getPlayerVariable(playerId, varName);
        System.out.println("Retrieved player variable: " + (result != null ? result.asString() : "null"));
        
        // Test incrementing a number
        String counterName = "counter";
        System.out.println("Incrementing player counter by 5.0");
        vm.incrementPlayerVariable(playerId, counterName, 5.0);
        
        // Get the counter value
        DataValue counter = vm.getPlayerVariable(playerId, counterName);
        System.out.println("Counter value: " + (counter != null ? counter.asString() : "null"));
        
        // Clear player variables
        System.out.println("Clearing variables for player: " + playerId);
        vm.clearPlayerVariables(playerId);
        
        // Verify cleared
        result = vm.getPlayerVariable(playerId, varName);
        System.out.println("After clear, got: " + (result != null ? result.asString() : "null"));
    }
    
    // Simple test implementation of MegaCreative interface
    private static class TestPlugin implements MegaCreative {
        private final File dataFolder;
        private final Logger logger;
        
        public TestPlugin() {
            this.dataFolder = new File("test-data");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            this.logger = Logger.getLogger("TestPlugin");
            this.logger.setLevel(Level.ALL);
        }
        
        @Override
        public File getDataFolder() {
            return dataFolder;
        }
        
        @Override
        public org.bukkit.Server getServer() {
            // Return null as we don't need server for basic tests
            return null;
        }
        
        @Override
        public Logger getLogger() {
            return logger;
        }
    }
}
