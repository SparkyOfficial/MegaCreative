package com.megacreative.coding.variables;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SimpleVariableManagerTest {
    
    private VariableManager variableManager;
    
    @Mock
    private MegaCreative mockPlugin;
    
    @Mock
    private Server mockServer;
    
    @BeforeEach
    public void setUp() {
        // Properly mock the getLogger method
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));
        // Mock getDataFolder to avoid NPE
        when(mockPlugin.getDataFolder()).thenReturn(new File("test-data"));
        // Mock getServer
        when(mockPlugin.getServer()).thenReturn(mockServer);
        
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
        
        assertNotNull(result, "Local variable should exist");
        assertEquals(value.asString(), result.asString(), "Local variable value should match");
        
        // Test clearing local variables
        variableManager.clearLocalVariables(context);
        result = variableManager.getLocalVariable(context, varName);
        assertNull(result, "Local variable should be cleared");
    }
    
    @Test
    public void testGlobalVariables() {
        String varName = "global-var";
        DataValue value = DataValue.of(42);
        
        // Test setting and getting a global variable
        variableManager.setGlobalVariable(varName, value);
        DataValue result = variableManager.getGlobalVariable(varName);
        
        assertNotNull(result, "Global variable should exist");
        assertEquals(value.asNumber().intValue(), result.asNumber().intValue(), "Global variable value should match");
        
        // Test getting all global variables
        var allGlobals = variableManager.getAllGlobalVariables();
        assertTrue(allGlobals.containsKey(varName), "Global variables should contain the test variable");
        
        // Test clearing global variables
        variableManager.clearGlobalVariables();
        result = variableManager.getGlobalVariable(varName);
        assertNull(result, "Global variable should be cleared");
    }
    
    @Test
    public void testPlayerVariables() {
        UUID playerId = UUID.randomUUID();
        String varName = "player-var";
        DataValue value = DataValue.of(true);
        
        // Test setting and getting a player variable
        variableManager.setPlayerVariable(playerId, varName, value);
        DataValue result = variableManager.getPlayerVariable(playerId, varName);
        
        assertNotNull(result, "Player variable should exist");
        assertTrue(result.asBoolean(), "Player variable value should be true");
        
        // Test getting all player variables
        var playerVars = variableManager.getPlayerVariables(playerId);
        assertTrue(playerVars.containsKey(varName), "Player variables should contain the test variable");
        
        // Test incrementing a player variable
        String counterName = "counter";
        variableManager.incrementPlayerVariable(playerId, counterName, 5.0);
        DataValue counter = variableManager.getPlayerVariable(playerId, counterName);
        assertEquals(5.0, counter.asNumber().doubleValue(), 0.001, "Counter should be 5.0");
        
        // Test clearing player variables
        variableManager.clearPlayerVariables(playerId);
        result = variableManager.getPlayerVariable(playerId, varName);
        assertNull(result, "Player variable should be cleared");
    }
    
    @Test
    public void testServerVariables() {
        String varName = "server-var";
        DataValue value = DataValue.of(3.14);
        
        // Test setting and getting a server variable
        variableManager.setServerVariable(varName, value);
        DataValue result = variableManager.getServerVariable(varName);
        
        assertNotNull(result, "Server variable should exist");
        assertEquals(value.asNumber().doubleValue(), result.asNumber().doubleValue(), 0.001, "Server variable value should match");
        
        // Test getting all server variables
        var serverVars = variableManager.getServerVariables();
        assertTrue(serverVars.containsKey(varName), "Server variables should contain the test variable");
        
        // Test clearing server variables
        variableManager.clearServerVariables();
        result = variableManager.getServerVariable(varName);
        assertNull(result, "Server variable should be cleared");
    }
    
    @Test
    public void testPersistentVariables() {
        String varName = "persistent-var";
        DataValue value = DataValue.of("persistent-value");
        
        // Test setting and getting a persistent variable
        variableManager.setPersistentVariable(varName, value);
        DataValue result = variableManager.getPersistentVariable(varName);
        
        assertNotNull(result, "Persistent variable should exist");
        assertEquals(value.asString(), result.asString(), "Persistent variable value should match");
        
        // Test getting all persistent variables
        var persistentVars = variableManager.getAllPersistentVariables();
        assertTrue(persistentVars.containsKey(varName), "Persistent variables should contain the test variable");
        
        // Test clearing persistent variables
        variableManager.clearPersistentVariables();
        result = variableManager.getPersistentVariable(varName);
        assertNull(result, "Persistent variable should be cleared");
    }
    
    @Test
    public void testDynamicVariables() {
        String varName = "dynamic-time";
        
        // Test registering a dynamic variable
        variableManager.registerDynamicVariable(varName, () -> DataValue.of(System.currentTimeMillis()));
        
        // Test getting a dynamic variable
        DataValue result1 = variableManager.getVariable(varName, IVariableManager.VariableScope.DYNAMIC, null);
        assertNotNull(result1, "Dynamic variable should exist");
        
        // Test unregistering a dynamic variable
        variableManager.unregisterDynamicVariable(varName);
        DataValue result2 = variableManager.getVariable(varName, IVariableManager.VariableScope.DYNAMIC, null);
        assertNull(result2, "Dynamic variable should be unregistered");
    }
}