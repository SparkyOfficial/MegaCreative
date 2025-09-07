package com.megacreative.coding.variables;

import com.megacreative.coding.values.DataValue;

public class QuickTest {
    public static void main(String[] args) {
        System.out.println("=== Starting Quick Test ===");
        
        // Create a simple test plugin
        TestPlugin plugin = new TestPlugin();
        
        // Create variable manager
        VariableManager vm = new VariableManager(plugin);
        
        // Test local variables
        System.out.println("\nTesting local variables...");
        String context = "test-context";
        String varName = "test-var";
        DataValue value = DataValue.of("test-value");
        
        vm.setLocalVariable(context, varName, value);
        System.out.println("Set local variable: " + context + "." + varName + " = " + value.asString());
        
        DataValue result = vm.getLocalVariable(context, varName);
        System.out.println("Got local variable: " + (result != null ? result.asString() : "null"));
        
        // Test global variables
        System.out.println("\nTesting global variables...");
        String globalVar = "global-var";
        DataValue globalValue = DataValue.of(42);
        
        vm.setGlobalVariable(globalVar, globalValue);
        System.out.println("Set global variable: " + globalVar + " = " + globalValue.asInt());
        
        DataValue globalResult = vm.getGlobalVariable(globalVar);
        System.out.println("Got global variable: " + (globalResult != null ? globalResult.asInt() : "null"));
        
        System.out.println("\n=== Test Complete ===");
    }
    
    static class TestPlugin {
        public java.io.File getDataFolder() {
            return new java.io.File("test-data");
        }
        
        public void logInfo(String message) {
            System.out.println("[INFO] " + message);
        }
    }
}
