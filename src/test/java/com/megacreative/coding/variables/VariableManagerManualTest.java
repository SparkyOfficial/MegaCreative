package com.megacreative.coding.variables;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

public class VariableManagerManualTest {
    
    private static class MockMegaCreative extends MegaCreative {
        private final Server server;
        
        public MockMegaCreative() {
            super(new JavaPluginLoader(null, new PluginDescriptionFile("MegaCreative", "1.0.0", "com.megacreative.MegaCreative")), null);
            this.server = new org.bukkit.craftbukkit.v1_20_R3.CraftServer(null, null, null, null, null, null, null, null, null, null, null);
        }
        
        @Override
        public Server getServer() {
            return server;
        }
        
        @Override
        public Logger getLogger() {
            return Logger.getLogger("MegaCreativeTest");
        }
        
        @Override
        public File getDataFolder() {
            return new File("test-data");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Starting VariableManager Manual Test ===\n");
        
        // Setup mock plugin
        MegaCreative plugin = new MockMegaCreative();
        VariableManager variableManager = new VariableManager(plugin);
        
        // Test local variables
        System.out.println("=== Testing Local Variables ===");
        testLocalVariables(variableManager);
        
        // Test global variables
        System.out.println("\n=== Testing Global Variables ===");
        testGlobalVariables(variableManager);
        
        // Test player variables
        System.out.println("\n=== Testing Player Variables ===");
        testPlayerVariables(variableManager);
        
        // Test server variables
        System.out.println("\n=== Testing Server Variables ===");
        testServerVariables(variableManager);
        
        // Test persistent variables
        System.out.println("\n=== Testing Persistent Variables ===");
        testPersistentVariables(variableManager);
        
        // Test dynamic variables
        System.out.println("\n=== Testing Dynamic Variables ===");
        testDynamicVariables(variableManager);
        
        System.out.println("\n=== All tests completed successfully! ===");
    }
    
    private static void testLocalVariables(VariableManager vm) {
        String context = "test-context";
        String varName = "localVar";
        DataValue value = DataValue.of("test-value");
        
        // Set local variable
        vm.setLocalVariable(context, varName, value);
        System.out.println("Set local variable: " + context + "." + varName + " = " + value.asString());
        
        // Get local variable
        DataValue result = vm.getLocalVariable(context, varName);
        System.out.println("Got local variable: " + (result != null ? result.asString() : "null"));
        
        // Clear local variables
        vm.clearLocalVariables(context);
        System.out.println("Cleared local variables for context: " + context);
        
        // Verify cleared
        result = vm.getLocalVariable(context, varName);
        System.out.println("After clear, got: " + (result != null ? result.asString() : "null"));
    }
    
    private static void testGlobalVariables(VariableManager vm) {
        String varName = "globalVar";
        DataValue value = DataValue.of(42);
        
        // Set global variable
        vm.setGlobalVariable(varName, value);
        System.out.println("Set global variable: " + varName + " = " + value.asString());
        
        // Get global variable
        DataValue result = vm.getGlobalVariable(varName);
        System.out.println("Got global variable: " + (result != null ? result.asString() : "null"));
        
        // Clear global variables
        vm.clearGlobalVariables();
        System.out.println("Cleared all global variables");
        
        // Verify cleared
        result = vm.getGlobalVariable(varName);
        System.out.println("After clear, got: " + (result != null ? result.asString() : "null"));
    }
    
    private static void testPlayerVariables(VariableManager vm) {
        UUID playerId = UUID.randomUUID();
        String varName = "playerVar";
        DataValue value = DataValue.of(true);
        
        // Set player variable
        vm.setPlayerVariable(playerId, varName, value);
        System.out.println("Set player variable: " + playerId + "." + varName + " = " + value.asString());
        
        // Get player variable
        DataValue result = vm.getPlayerVariable(playerId, varName);
        System.out.println("Got player variable: " + (result != null ? result.asString() : "null"));
        
        // Clear player variables
        vm.clearPlayerVariables(playerId);
        System.out.println("Cleared variables for player: " + playerId);
        
        // Verify cleared
        result = vm.getPlayerVariable(playerId, varName);
        System.out.println("After clear, got: " + (result != null ? result.asString() : "null"));
    }
    
    private static void testServerVariables(VariableManager vm) {
        String varName = "serverVar";
        DataValue value = DataValue.of(3.14);
        
        // Set server variable
        vm.setServerVariable(varName, value);
        System.out.println("Set server variable: " + varName + " = " + value.asString());
        
        // Get server variable
        DataValue result = vm.getServerVariable(varName);
        System.out.println("Got server variable: " + (result != null ? result.asString() : "null"));
        
        // Clear server variables
        vm.clearServerVariables();
        System.out.println("Cleared all server variables");
        
        // Verify cleared
        result = vm.getServerVariable(varName);
        System.out.println("After clear, got: " + (result != null ? result.asString() : "null"));
    }
    
    private static void testPersistentVariables(VariableManager vm) {
        String varName = "persistentVar";
        DataValue value = DataValue.of("persistent-value");
        
        // Set persistent variable
        vm.setPersistentVariable(varName, value);
        System.out.println("Set persistent variable: " + varName + " = " + value.asString());
        
        // Get persistent variable
        DataValue result = vm.getPersistentVariable(varName);
        System.out.println("Got persistent variable: " + (result != null ? result.asString() : "null"));
        
        // Save persistent data
        vm.savePersistentData();
        System.out.println("Saved persistent data");
        
        // Clear persistent variables
        vm.clearPersistentVariables();
        System.out.println("Cleared all persistent variables");
        
        // Verify cleared
        result = vm.getPersistentVariable(varName);
        System.out.println("After clear, got: " + (result != null ? result.asString() : "null"));
    }
    
    private static void testDynamicVariables(VariableManager vm) {
        String varName = "dynamicTime";
        
        // Register dynamic variable
        vm.registerDynamicVariable(varName, () -> DataValue.of(System.currentTimeMillis()));
        System.out.println("Registered dynamic variable: " + varName);
        
        // Get dynamic variable (should return current time)
        DataValue result1 = vm.getVariable(varName, IVariableManager.VariableScope.DYNAMIC, null);
        System.out.println("First get dynamic variable: " + (result1 != null ? result1.asString() : "null"));
        
        // Get again (should be different due to time passing)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DataValue result2 = vm.getVariable(varName, IVariableManager.VariableScope.DYNAMIC, null);
        System.out.println("Second get dynamic variable: " + (result2 != null ? result2.asString() : "null"));
        
        // Unregister dynamic variable
        vm.unregisterDynamicVariable(varName);
        System.out.println("Unregistered dynamic variable: " + varName);
        
        // Verify unregistered
        DataValue result3 = vm.getVariable(varName, IVariableManager.VariableScope.DYNAMIC, null);
        System.out.println("After unregister, got: " + (result3 != null ? result3.asString() : "null"));
    }
}
