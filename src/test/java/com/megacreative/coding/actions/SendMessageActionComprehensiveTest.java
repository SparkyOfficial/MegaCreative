package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.values.DataValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendMessageActionComprehensiveTest {

    @Mock
    private CodeBlock block;
    
    @Mock
    private ExecutionContext context;
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private ServiceRegistry serviceRegistry;
    
    @Mock
    private BlockConfigService blockConfigService;
    
    @Mock
    private Player player;
    
    @Mock
    private ItemStack messageItem;
    
    @Mock
    private ItemMeta itemMeta;
    
    private SendMessageAction action;

    @BeforeEach
    void setUp() {
        action = new SendMessageAction();
        
        // Set up all mocks with lenient stubbings to avoid unnecessary stubbing exceptions
        lenient().when(context.getPlayer()).thenReturn(player);
        lenient().when(context.getPlugin()).thenReturn(plugin);
        lenient().when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        lenient().when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
        lenient().when(block.getAction()).thenReturn("sendMessage");
        lenient().when(messageItem.hasItemMeta()).thenReturn(true);
        lenient().when(messageItem.getItemMeta()).thenReturn(itemMeta);
    }

    @Test
    void testConstructor() {
        // Test that the constructor creates a valid instance
        assertNotNull(action);
    }

    @Test
    void testExecuteWithValidPlayerAndMessageFromParameter() {
        // Set up the scenario where message is provided via parameter
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(null);
        DataValue messageDataValue = mock(DataValue.class);
        lenient().when(messageDataValue.isEmpty()).thenReturn(false);
        lenient().when(messageDataValue.asString()).thenReturn("Test message");
        lenient().when(block.getParameter("message")).thenReturn(messageDataValue);
        lenient().when(block.getConfigItem(anyInt())).thenReturn(null);
        
        // Execute the action
        ExecutionResult result = action.execute(block, context);
        
        // Verify success
        assertTrue(result.isSuccess());
        assertEquals("Message sent successfully", result.getMessage());
        verify(player).sendMessage("Test message");
    }

    @Test
    void testExecuteWithValidPlayerAndMessageFromItem() {
        // Set up the scenario where message is provided via item
        Function<String, Integer> slotResolver = mock(Function.class);
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(slotResolver);
        lenient().when(slotResolver.apply("message_slot")).thenReturn(0);
        lenient().when(block.getConfigItem(0)).thenReturn(messageItem);
        lenient().when(itemMeta.getDisplayName()).thenReturn("Test message from item");
        lenient().when(block.getParameter("message")).thenReturn(null);
        
        // Execute the action
        ExecutionResult result = action.execute(block, context);
        
        // Verify success
        assertTrue(result.isSuccess());
        assertEquals("Message sent successfully", result.getMessage());
        verify(player).sendMessage("Test message from item");
    }

    @Test
    void testExecuteWithNullPlayer() {
        // Set up the scenario where player is null
        when(context.getPlayer()).thenReturn(null);
        
        // Execute the action
        ExecutionResult result = action.execute(block, context);
        
        // Verify failure
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No player found"));
    }

    @Test
    void testExecuteWithNullMessageParameterAndItem() {
        // Set up the scenario where both message parameter and item are null
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(null);
        lenient().when(block.getParameter("message")).thenReturn(null);
        lenient().when(block.getConfigItem(anyInt())).thenReturn(null);
        
        // Execute the action
        ExecutionResult result = action.execute(block, context);
        
        // Verify failure
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Message is not configured"));
    }

    @Test
    void testExecuteWithEmptyMessageParameter() {
        // Set up the scenario where message parameter is empty
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(null);
        DataValue emptyDataValue = mock(DataValue.class);
        lenient().when(emptyDataValue.isEmpty()).thenReturn(true);
        lenient().when(block.getParameter("message")).thenReturn(emptyDataValue);
        lenient().when(block.getConfigItem(anyInt())).thenReturn(null);
        
        // Execute the action
        ExecutionResult result = action.execute(block, context);
        
        // Verify failure
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Message is not configured"));
    }

    @Test
    void testExecuteWithNullMessageItem() {
        // Set up the scenario where slot resolver exists but item is null
        Function<String, Integer> slotResolver = mock(Function.class);
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(slotResolver);
        lenient().when(slotResolver.apply("message_slot")).thenReturn(0);
        lenient().when(block.getConfigItem(0)).thenReturn(null);
        lenient().when(block.getParameter("message")).thenReturn(null);
        
        // Execute the action
        ExecutionResult result = action.execute(block, context);
        
        // Verify failure
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Message is not configured"));
    }

    @Test
    void testExecuteWithMessageItemWithoutMeta() {
        // Set up the scenario where message item has no meta
        Function<String, Integer> slotResolver = mock(Function.class);
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(slotResolver);
        lenient().when(slotResolver.apply("message_slot")).thenReturn(0);
        ItemStack itemWithoutMeta = mock(ItemStack.class);
        lenient().when(itemWithoutMeta.hasItemMeta()).thenReturn(false);
        lenient().when(block.getConfigItem(0)).thenReturn(itemWithoutMeta);
        lenient().when(block.getParameter("message")).thenReturn(null);
        
        // Execute the action
        ExecutionResult result = action.execute(block, context);
        
        // Verify failure
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Message is not configured"));
    }

    @Test
    void testExecuteWithMessageItemWithoutDisplayName() {
        // Set up the scenario where message item has meta but no display name
        Function<String, Integer> slotResolver = mock(Function.class);
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(slotResolver);
        lenient().when(slotResolver.apply("message_slot")).thenReturn(0);
        lenient().when(block.getConfigItem(0)).thenReturn(messageItem);
        lenient().when(itemMeta.getDisplayName()).thenReturn("");
        lenient().when(block.getParameter("message")).thenReturn(null);
        
        // Execute the action
        ExecutionResult result = action.execute(block, context);
        
        // Verify failure
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Message is not configured"));
    }
}