package com.megacreative.coding.variables;

import com.megacreative.coding.values.DataValue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class SimpleVariableManagerTest {
    
    private VariableManager variableManager;
    private MegaCreative mockPlugin;
    
    @Before
    public void setUp() {
        // Create a mock plugin
        mockPlugin = Mockito.mock(MegaCreative.class);
        when(mockPlugin.getDataFolder()).thenReturn(new java.io.File("test-data"));
        
        // Create the variable manager
        variableManager = new VariableManager(mockPlugin);
    }
    
    @Test
    public void testLocalVariables() {
        String context = "test-context";
        String varName = "test-var";
        DataValue value = DataValue.of("test-value");
        
        // Test setting and getting a local variable
        variableManager.setLocalVariable(context, varName, value);
        DataValue result = variableManager.getLocalVariable(context, varName);
        
        assertNotNull("Local variable should exist", result);
        assertEquals("Local variable value should match", value.asString(), result.asString());
        
        // Test clearing local variables
        variableManager.clearLocalVariables(context);
        result = variableManager.getLocalVariable(context, varName);
        assertNull("Local variable should be cleared", result);
    }
    
    @Test
    public void testGlobalVariables() {
        String varName = "global-var";
        DataValue value = DataValue.of(42);
        
        // Test setting and getting a global variable
        variableManager.setGlobalVariable(varName, value);
        DataValue result = variableManager.getGlobalVariable(varName);
        
        assertNotNull("Global variable should exist", result);
        assertEquals("Global variable value should match", value.asInt(), result.asInt());
        
        // Test getting all global variables
        var allGlobals = variableManager.getAllGlobalVariables();
        assertTrue("Global variables should contain the test variable", allGlobals.containsKey(varName));
        
        // Test clearing global variables
        variableManager.clearGlobalVariables();
        result = variableManager.getGlobalVariable(varName);
        assertNull("Global variable should be cleared", result);
    }
    
    @Test
    public void testPlayerVariables() {
        UUID playerId = UUID.randomUUID();
        String varName = "player-var";
        DataValue value = DataValue.of(true);
        
        // Test setting and getting a player variable
        variableManager.setPlayerVariable(playerId, varName, value);
        DataValue result = variableManager.getPlayerVariable(playerId, varName);
        
        assertNotNull("Player variable should exist", result);
        assertTrue("Player variable value should be true", result.asBoolean());
        
        // Test getting all player variables
        var playerVars = variableManager.getPlayerVariables(playerId);
        assertTrue("Player variables should contain the test variable", playerVars.containsKey(varName));
        
        // Test incrementing a player variable
        String counterName = "counter";
        variableManager.incrementPlayerVariable(playerId, counterName, 5.0);
        DataValue counter = variableManager.getPlayerVariable(playerId, counterName);
        assertEquals("Counter should be 5.0", 5.0, counter.asDouble(), 0.001);
        
        // Test clearing player variables
        variableManager.clearPlayerVariables(playerId);
        result = variableManager.getPlayerVariable(playerId, varName);
        assertNull("Player variable should be cleared", result);
    }
    
    @Test
    public void testServerVariables() {
        String varName = "server-var";
        DataValue value = DataValue.of(3.14);
        
        // Test setting and getting a server variable
        variableManager.setServerVariable(varName, value);
        DataValue result = variableManager.getServerVariable(varName);
        
        assertNotNull("Server variable should exist", result);
        assertEquals("Server variable value should match", value.asDouble(), result.asDouble(), 0.001);
        
        // Test getting all server variables
        var serverVars = variableManager.getServerVariables();
        assertTrue("Server variables should contain the test variable", serverVars.containsKey(varName));
        
        // Test clearing server variables
        variableManager.clearServerVariables();
        result = variableManager.getServerVariable(varName);
        assertNull("Server variable should be cleared", result);
    }
    
    @Test
    public void testPersistentVariables() {
        String varName = "persistent-var";
        DataValue value = DataValue.of("persistent-value");
        
        // Test setting and getting a persistent variable
        variableManager.setPersistentVariable(varName, value);
        DataValue result = variableManager.getPersistentVariable(varName);
        
        assertNotNull("Persistent variable should exist", result);
        assertEquals("Persistent variable value should match", value.asString(), result.asString());
        
        // Test getting all persistent variables
        var persistentVars = variableManager.getAllPersistentVariables();
        assertTrue("Persistent variables should contain the test variable", persistentVars.containsKey(varName));
        
        // Test clearing persistent variables
        variableManager.clearPersistentVariables();
        result = variableManager.getPersistentVariable(varName);
        assertNull("Persistent variable should be cleared", result);
    }
    
    @Test
    public void testDynamicVariables() {
        String varName = "dynamic-time";
        
        // Test registering a dynamic variable
        variableManager.registerDynamicVariable(varName, () -> DataValue.of(System.currentTimeMillis()));
        
        // Test getting a dynamic variable
        DataValue result1 = variableManager.getVariable(varName, IVariableManager.VariableScope.DYNAMIC, null);
        assertNotNull("Dynamic variable should exist", result1);
        
        // Test unregistering a dynamic variable
        variableManager.unregisterDynamicVariable(varName);
        DataValue result2 = variableManager.getVariable(varName, IVariableManager.VariableScope.DYNAMIC, null);
        assertNull("Dynamic variable should be unregistered", result2);
    }
}
