package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.actions.control.BreakAction;
import com.megacreative.coding.actions.control.ContinueAction;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test for loop control functionality
 */
public class ComprehensiveLoopControlTest {

    @Mock
    private MegaCreative plugin;
    
    @Mock
    private Player player;
    
    @Mock
    private VariableManager variableManager;
    
    @Mock
    private BlockConfigService blockConfigService;
    
    private BreakAction breakAction;
    private ContinueAction continueAction;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up plugin mock
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("ComprehensiveLoopControlTest"));
        
        // Create actions
        breakAction = new BreakAction(plugin);
        continueAction = new ContinueAction(plugin);
    }
    
    @Test
    void testBreakActionExecution() {
        // Create a code block
        CodeBlock block = mock(CodeBlock.class);
        
        // Create execution context
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .build();
        
        // Execute break action
        ExecutionResult result = breakAction.execute(block, context);
        
        // Verify results
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Break executed", result.getMessage());
        
        // Verify that break flag was set
        assertTrue(context.hasBreakFlag());
    }
    
    @Test
    void testContinueActionExecution() {
        // Create a code block
        CodeBlock block = mock(CodeBlock.class);
        
        // Create execution context
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .build();
        
        // Execute continue action
        ExecutionResult result = continueAction.execute(block, context);
        
        // Verify results
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Continue executed", result.getMessage());
        
        // Verify that continue flag was set
        assertTrue(context.hasContinueFlag());
    }
    
    @Test
    void testBreakFlagClearing() {
        // Create execution context
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .build();
        
        // Set break flag
        context.setBreakFlag(true);
        assertTrue(context.hasBreakFlag());
        
        // Clear break flag
        context.clearBreakFlag();
        assertFalse(context.hasBreakFlag());
    }
    
    @Test
    void testContinueFlagClearing() {
        // Create execution context
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .build();
        
        // Set continue flag
        context.setContinueFlag(true);
        assertTrue(context.hasContinueFlag());
        
        // Clear continue flag
        context.clearContinueFlag();
        assertFalse(context.hasContinueFlag());
    }
}