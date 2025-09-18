package com.megacreative.coding.variables;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VariableScopeResolutionTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private Player player;

    private VariableManager variableManager;
    private UUID playerUUID;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        variableManager = new VariableManager(plugin);
        playerUUID = UUID.randomUUID();
        
        when(player.getUniqueId()).thenReturn(playerUUID);
    }

    @Test
    void testEnhancedVariableResolution() {
        // Set up variables in different scopes
        String varName = "testVar";
        String localValue = "localValue";
        String playerValue = "playerValue";
        String globalValue = "globalValue";
        String serverValue = "serverValue";
        
        // Set variables in different scopes
        variableManager.setLocalVariable("testContext", varName, DataValue.of(localValue));
        variableManager.setPlayerVariable(playerUUID, varName, DataValue.of(playerValue));
        variableManager.setGlobalVariable(varName, DataValue.of(globalValue));
        variableManager.setServerVariable(varName, DataValue.of(serverValue));
        
        // Test resolution with player context - should find player variable first
        DataValue resolved = variableManager.resolveVariable(varName, playerUUID.toString());
        assertNotNull(resolved);
        assertEquals(playerValue, resolved.asString());
        
        // Test resolution with different context - should fall back to global
        resolved = variableManager.resolveVariable(varName, "differentContext");
        assertNotNull(resolved);
        assertEquals(globalValue, resolved.asString());
    }

    @Test
    void testExplicitScopeResolution() {
        // Set up variables in different scopes
        String varName = "explicitVar";
        String localValue = "localValue";
        String globalValue = "globalValue";
        
        variableManager.setLocalVariable("testContext", varName, DataValue.of(localValue));
        variableManager.setGlobalVariable(varName, DataValue.of(globalValue));
        
        // Test explicit scope resolution
        DataValue resolved = variableManager.resolveVariableWithScopes(
            varName, 
            "testContext", 
            IVariableManager.VariableScope.LOCAL, 
            IVariableManager.VariableScope.GLOBAL
        );
        assertNotNull(resolved);
        assertEquals(localValue, resolved.asString());
        
        // Test with different order
        resolved = variableManager.resolveVariableWithScopes(
            varName, 
            "testContext", 
            IVariableManager.VariableScope.GLOBAL,
            IVariableManager.VariableScope.LOCAL
        );
        assertNotNull(resolved);
        assertEquals(globalValue, resolved.asString());
    }

    @Test
    void testGetAllVariables() {
        // Set up variables in different scopes
        String var1 = "var1";
        String var2 = "var2";
        String var3 = "var3";
        
        variableManager.setLocalVariable("testContext", var1, DataValue.of("local1"));
        variableManager.setPlayerVariable(playerUUID, var2, DataValue.of("player2"));
        variableManager.setGlobalVariable(var3, DataValue.of("global3"));
        
        // Test getting all variables for player context
        Map<String, DataValue> allVars = variableManager.getAllVariables(playerUUID.toString());
        assertNotNull(allVars);
        assertTrue(allVars.containsKey(var1));
        assertTrue(allVars.containsKey(var2));
        assertTrue(allVars.containsKey(var3));
        assertEquals("local1", allVars.get(var1).asString());
        assertEquals("player2", allVars.get(var2).asString());
        assertEquals("global3", allVars.get(var3).asString());
    }

    @Test
    void testDynamicVariables() {
        // Register a dynamic variable
        variableManager.registerDynamicVariable("dynamicTest", () -> DataValue.of("dynamicValue"));
        
        // Test resolution of dynamic variable
        DataValue resolved = variableManager.resolveVariable("dynamicTest", "anyContext");
        assertNotNull(resolved);
        assertEquals("dynamicValue", resolved.asString());
        
        // Test that dynamic variables appear in getAllVariables
        Map<String, DataValue> allVars = variableManager.getAllVariables("anyContext");
        assertTrue(allVars.containsKey("dynamicTest"));
        assertEquals("dynamicValue", allVars.get("dynamicTest").asString());
    }
}