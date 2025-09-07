import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.values.DataValue;

public class TestVariableManager {
    public static void main(String[] args) {
        System.out.println("=== Testing VariableManager ===");
        
        // Create a simple test plugin
        TestPlugin plugin = new TestPlugin();
        
        // Create variable manager
        VariableManager vm = new VariableManager(plugin);
        
        // Test local variables
        System.out.println("\n=== Testing Local Variables ===");
        testLocalVariables(vm);
        
        // Test global variables
        System.out.println("\n=== Testing Global Variables ===");
        testGlobalVariables(vm);
        
        System.out.println("\n=== Test Complete ===");
    }
    
    private static void testLocalVariables(VariableManager vm) {
        String context = "test-context";
        String varName = "test-var";
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
        String varName = "global-var";
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
    
    static class TestPlugin implements com.megacreative.MegaCreative {
        @Override
        public java.io.File getDataFolder() {
            return new java.io.File("test-data");
        }
        
        @Override
        public org.bukkit.Server getServer() {
            return null; // Not needed for this test
        }
        
        @Override
        public java.util.logging.Logger getLogger() {
            return java.util.logging.Logger.getLogger("TestPlugin");
        }
    }
}
