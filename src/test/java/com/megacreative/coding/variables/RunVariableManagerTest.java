package com.megacreative.coding.variables;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.UUID;

public class RunVariableManagerTest {
    
    public static void main(String[] args) {
        System.out.println("Starting VariableManager tests...");
        
        // Setup mock plugin
        MegaCreative plugin = mock(MegaCreative.class);
        Server server = mock(Server.class);
        
        // Configure mocks
        when(plugin.getDataFolder()).thenReturn(new File("test-data"));
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestLogger"));
        when(plugin.getServer()).thenReturn(server);
        
        // Create test data directory
        File testDataDir = new File("test-data");
        if (!testDataDir.exists()) {
            testDataDir.mkdirs();
        }
        
        // Run tests
        runBasicTests(plugin);
        
        System.out.println("All tests completed!");
    }
    
    private static void runBasicTests(MegaCreative plugin) {
        System.out.println("Running basic tests...");
        VariableManager variableManager = new VariableManager(plugin);
        
        // Test 1: Local variables
        System.out.println("\nTest 1: Local variables");
        String context = "test-context";
        String varName = "test-var";
        DataValue value = DataValue.of(42);
        
        // Set local variable
        variableManager.setLocalVariable(context, varName, value);
        System.out.println("Set local variable: " + varName + " = " + value.getValue());
        
        // Get local variable
        DataValue result = variableManager.getLocalVariable(context, varName);
        System.out.println("Got local variable: " + (result != null ? result.getValue() : "null"));
        
        // Test 2: Global variables
        System.out.println("\nTest 2: Global variables");
        String globalVarName = "global-var";
        DataValue globalValue = DataValue.of("test-value");
        
        // Set global variable
        variableManager.setGlobalVariable(globalVarName, globalValue);
        System.out.println("Set global variable: " + globalVarName + " = " + globalValue.getValue());
        
        // Get global variable
        DataValue globalResult = variableManager.getGlobalVariable(globalVarName);
        System.out.println("Got global variable: " + (globalResult != null ? globalResult.getValue() : "null"));
        
        // Test 3: Player variables
        System.out.println("\nTest 3: Player variables");
        UUID playerId = UUID.randomUUID();
        String playerVarName = "player-var";
        DataValue playerValue = DataValue.of(true);
        
        // Set player variable
        variableManager.setPlayerVariable(playerId, playerVarName, playerValue);
        System.out.println("Set player variable: " + playerVarName + " = " + playerValue.getValue());
        
        // Get player variable
        DataValue playerResult = variableManager.getPlayerVariable(playerId, playerVarName);
        System.out.println("Got player variable: " + (playerResult != null ? playerResult.getValue() : "null"));
        
        // Test 4: Increment player variable
        System.out.println("\nTest 4: Increment player variable");
        String counterVarName = "counter";
        
        // Increment new variable
        variableManager.incrementPlayerVariable(playerId, counterVarName, 5.0);
        DataValue counterResult = variableManager.getPlayerVariable(playerId, counterVarName);
        System.out.println("After first increment: " + counterResult.getValue());
        
        // Increment existing variable
        variableManager.incrementPlayerVariable(playerId, counterVarName, 3.5);
        counterResult = variableManager.getPlayerVariable(playerId, counterVarName);
        System.out.println("After second increment: " + counterResult.getValue());
    }
    
    // Simple mock method since we're not using Mockito
    @SuppressWarnings("unchecked")
    private static <T> T mock(Class<T> clazz) {
        try {
            return (T) java.lang.reflect.Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                (proxy, method, args) -> {
                    if (method.getReturnType().isPrimitive()) {
                        if (method.getReturnType() == boolean.class) return false;
                        if (method.getReturnType() == int.class) return 0;
                        if (method.getReturnType() == long.class) return 0L;
                        if (method.getReturnType() == double.class) return 0.0;
                        if (method.getReturnType() == float.class) return 0.0f;
                        if (method.getReturnType() == char.class) return '\0';
                        if (method.getReturnType() == byte.class) return (byte) 0;
                        if (method.getReturnType() == short.class) return (short) 0;
                    }
                    return null;
                });
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock for " + clazz.getName(), e);
        }
    }
    
    // Simple when method
    private static <T> void when(T methodCall) {
        // No-op, just for compilation
    }
}
