package com.megacreative.coding.variables;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VariableManagerTest {
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private Server server;
    
    private VariableManager variableManager;
    
    @BeforeEach
    public void setUp() {
        // Setup plugin mock
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        when(plugin.getDataFolder()).thenReturn(new File("test-data"));
        // Make this lenient since not all tests use it
        lenient().when(plugin.getServer()).thenReturn(server);
        
        // Create test data directory if it doesn't exist
        File testDataDir = new File("test-data");
        if (!testDataDir.exists()) {
            testDataDir.mkdirs();
        }
        
        variableManager = new VariableManager(plugin);
    }
    
    @Test
    public void testSetAndGetLocalVariable() {
        String context = "test-context";
        String varName = "test-var";
        DataValue value = DataValue.of(42);
        
        // Test setting a local variable
        variableManager.setLocalVariable(context, varName, value);
        
        // Test getting the local variable
        DataValue result = variableManager.getLocalVariable(context, varName);
        assertNotNull(result);
        assertEquals(42, result.getValue());
        
        // Test getting non-existent variable
        assertNull(variableManager.getLocalVariable("nonexistent-context", varName));
        assertNull(variableManager.getLocalVariable(context, "nonexistent-var"));
    }
    
    @Test
    public void testSetAndGetGlobalVariable() {
        String varName = "global-var";
        DataValue value = DataValue.of("test-value");
        
        // Test setting a global variable
        variableManager.setGlobalVariable(varName, value);
        
        // Test getting the global variable
        DataValue result = variableManager.getGlobalVariable(varName);
        assertNotNull(result);
        assertEquals("test-value", result.getValue());
    }
    
    @Test
    public void testPlayerVariables() {
        UUID playerId = UUID.randomUUID();
        String varName = "player-var";
        DataValue value = DataValue.of(true);
        
        // Test setting a player variable
        variableManager.setPlayerVariable(playerId, varName, value);
        
        // Test getting the player variable
        DataValue result = variableManager.getPlayerVariable(playerId, varName);
        assertNotNull(result);
        assertTrue((Boolean) result.getValue());
        
        // Test getting all player variables
        var playerVars = variableManager.getPlayerVariables(playerId);
        assertFalse(playerVars.isEmpty());
        assertTrue(playerVars.containsKey(varName));
    }
    
    @Test
    public void testIncrementPlayerVariable() {
        UUID playerId = UUID.randomUUID();
        String varName = "counter";
        
        // Test incrementing a new variable
        variableManager.incrementPlayerVariable(playerId, varName, 5.0);
        DataValue result = variableManager.getPlayerVariable(playerId, varName);
        assertNotNull(result);
        assertEquals(5.0, result.getValue());
        
        // Test incrementing existing variable
        variableManager.incrementPlayerVariable(playerId, varName, 3.5);
        result = variableManager.getPlayerVariable(playerId, varName);
        assertEquals(8.5, result.getValue());
    }
    
    @Test
    public void testDynamicVariables() {
        String varName = "dynamic-var";
        
        // Register a dynamic variable
        variableManager.registerDynamicVariable(varName, () -> DataValue.of("dynamic-value"));
        
        // Test getting the dynamic variable
        DataValue result = variableManager.getVariable(varName, IVariableManager.VariableScope.DYNAMIC, null);
        assertNotNull(result);
        assertEquals("dynamic-value", result.getValue());
        
        // Test unregistering the dynamic variable
        variableManager.unregisterDynamicVariable(varName);
        assertNull(variableManager.getVariable(varName, IVariableManager.VariableScope.DYNAMIC, null));
    }
    
    @Test
    public void testVariableMetadata() {
        String varName = "metadata-var";
        DataValue value = DataValue.of(3.14);
        
        // Set a variable to generate metadata
        variableManager.setGlobalVariable(varName, value);
        
        // Test getting variable metadata
        IVariableManager.VariableMetadata metadata = variableManager.getVariableMetadata("global_" + varName);
        assertNotNull(metadata);
        assertEquals(IVariableManager.VariableScope.GLOBAL, metadata.getScope());
        assertEquals(com.megacreative.coding.values.ValueType.NUMBER, metadata.getType());
        
        // Test getting all metadata
        var allMetadata = variableManager.getAllVariableMetadata();
        assertFalse(allMetadata.isEmpty());
        assertTrue(allMetadata.containsKey("global_" + varName));
    }
}