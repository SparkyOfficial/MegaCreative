package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultScriptEngineTest {

    @Mock
    private MegaCreative plugin;
    
    @Mock
    private VariableManager variableManager;
    
    @Mock
    private VisualDebugger debugger;
    
    @Mock
    private BlockConfigService blockConfigService;
    
    @Mock
    private Player player;
    
    private DefaultScriptEngine scriptEngine;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scriptEngine = new DefaultScriptEngine(plugin, variableManager, debugger, blockConfigService);
    }
    
    @Test
    void testConstructorInitialization() {
        assertNotNull(scriptEngine, "ScriptEngine should be initialized");
    }
    
    @Test
    void testRegisterAndGetAction() {
        BlockAction testAction = mock(BlockAction.class);
        scriptEngine.registerAction(BlockType.ACTION_SEND_MESSAGE, testAction);
        
        BlockAction retrievedAction = scriptEngine.getAction(BlockType.ACTION_SEND_MESSAGE);
        assertSame(testAction, retrievedAction, "Retrieved action should be the same as registered");
    }
    
    @Test
    void testRegisterAndGetCondition() {
        BlockCondition testCondition = mock(BlockCondition.class);
        scriptEngine.registerCondition(BlockType.CONDITION_HAS_ITEM, testCondition);
        
        BlockCondition retrievedCondition = scriptEngine.getCondition(BlockType.CONDITION_HAS_ITEM);
        assertSame(testCondition, retrievedCondition, "Retrieved condition should be the same as registered");
    }
    
    @Test
    void testProcessBlockWithAction() {
        // Setup test
        CodeBlock block = new CodeBlock(Material.STONE, "test_action");
        ExecutionContext context = mock(ExecutionContext.class);
        BlockAction mockAction = mock(BlockAction.class);
        
        when(context.isCancelled()).thenReturn(false);
        when(mockAction.execute(any(CodeBlock.class), any(ExecutionContext.class)))
            .thenReturn(ExecutionResult.success("Test success"));
        
        scriptEngine.registerAction(BlockType.fromMaterialAndAction(block.getMaterial(), block.getAction()), mockAction);
        
        // Execute
        scriptEngine.processBlock(block, context);
        
        // Verify
        verify(mockAction, times(1)).execute(eq(block), eq(context));
    }
    
    @Test
    void testProcessBlockWithCondition() {
        // Setup test
        CodeBlock block = new CodeBlock(Material.LEVER, "test_condition");
        ExecutionContext context = mock(ExecutionContext.class);
        BlockCondition mockCondition = mock(BlockCondition.class);
        
        when(context.isCancelled()).thenReturn(false);
        when(mockCondition.evaluate(any(CodeBlock.class), any(ExecutionContext.class)))
            .thenReturn(true);
        
        scriptEngine.registerCondition(BlockType.fromMaterialAndAction(block.getMaterial(), block.getAction()), mockCondition);
        
        // Execute
        scriptEngine.processBlock(block, context);
        
        // Verify
        verify(mockCondition, times(1)).evaluate(eq(block), eq(context));
    }
    
    @Test
    void testSetAndGetBlockConfigService() {
        BlockConfigService newConfigService = mock(BlockConfigService.class);
        scriptEngine.setBlockConfigService(newConfigService);
        
        // Verify the service was set correctly
        assertSame(newConfigService, scriptEngine.getBlockConfigService(), 
                 "BlockConfigService should be updated");
    }
    
    @Test
    void testGetActionCount() {
        // Initially should be 0
        assertEquals(0, scriptEngine.getActionCount(), "Initial action count should be 0");
        
        // Register an action and verify count increases
        scriptEngine.registerAction(BlockType.ACTION_SEND_MESSAGE, mock(BlockAction.class));
        assertEquals(1, scriptEngine.getActionCount(), "Action count should be 1 after registration");
    }
    
    @Test
    void testGetConditionCount() {
        // Initially should be 0
        assertEquals(0, scriptEngine.getConditionCount(), "Initial condition count should be 0");
        
        // Register a condition and verify count increases
        scriptEngine.registerCondition(BlockType.CONDITION_HAS_ITEM, mock(BlockCondition.class));
        assertEquals(1, scriptEngine.getConditionCount(), "Condition count should be 1 after registration");
    }
}