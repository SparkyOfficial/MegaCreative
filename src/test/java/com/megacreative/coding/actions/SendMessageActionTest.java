package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.values.DataValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
public class SendMessageActionTest {

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
    public void setUp() {
        action = new SendMessageAction();
        
        lenient().when(context.getPlayer()).thenReturn(player);
        lenient().when(context.getPlugin()).thenReturn(plugin);
        lenient().when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        lenient().when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
        lenient().when(block.getAction()).thenReturn("send_message");
        lenient().when(messageItem.hasItemMeta()).thenReturn(true);
        lenient().when(messageItem.getItemMeta()).thenReturn(itemMeta);
    }

    @Test
    public void testExecuteWithNullPlayer() {
        when(context.getPlayer()).thenReturn(null);
        ExecutionResult result = action.execute(block, context);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No player found"));
    }

    @Test
    public void testGetMessageFromContainerWithNoSlotResolver() {
        // Make all stubbings lenient to avoid unnecessary stubbing exceptions
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(null);
        lenient().when(block.getParameter("message")).thenReturn(null);
        lenient().when(block.getConfigItem(anyInt())).thenReturn(null);
        
        ExecutionResult result = action.execute(block, context);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Message is not configured"));
    }

    @Test
    public void testGetMessageFromContainerWithNullMessageItem() {
        Function<String, Integer> slotResolver = mock(Function.class);
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(slotResolver);
        lenient().when(slotResolver.apply("message_slot")).thenReturn(0);
        lenient().when(block.getConfigItem(0)).thenReturn(null);
        lenient().when(block.getParameter("message")).thenReturn(null);
        
        ExecutionResult result = action.execute(block, context);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Message is not configured"));
    }
    
    @Test
    public void testExecuteWithEmptyMessage() {
        // Test the case where message is empty
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(null);
        DataValue emptyDataValue = mock(DataValue.class);
        lenient().when(emptyDataValue.isEmpty()).thenReturn(true);
        lenient().when(block.getParameter("message")).thenReturn(emptyDataValue);
        lenient().when(block.getConfigItem(anyInt())).thenReturn(null);
        
        ExecutionResult result = action.execute(block, context);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Message is not configured"));
    }
    
    @Test
    public void testExecuteWithValidMessageFromParameter() {
        // Test the case where message is provided via parameter
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(null);
        DataValue messageDataValue = mock(DataValue.class);
        lenient().when(messageDataValue.isEmpty()).thenReturn(false);
        lenient().when(messageDataValue.asString()).thenReturn("Test message");
        lenient().when(block.getParameter("message")).thenReturn(messageDataValue);
        lenient().when(block.getConfigItem(anyInt())).thenReturn(null);
        
        ExecutionResult result = action.execute(block, context);
        assertTrue(result.isSuccess());
        assertEquals("Message sent successfully", result.getMessage());
        verify(player).sendMessage("Test message");
    }
    
    @Test
    public void testExecuteWithValidMessageFromItem() {
        // Test the case where message is provided via item
        Function<String, Integer> slotResolver = mock(Function.class);
        lenient().when(blockConfigService.getSlotResolver(anyString())).thenReturn(slotResolver);
        lenient().when(slotResolver.apply("message_slot")).thenReturn(0);
        lenient().when(block.getConfigItem(0)).thenReturn(messageItem);
        lenient().when(itemMeta.getDisplayName()).thenReturn("Test message from item");
        lenient().when(block.getParameter("message")).thenReturn(null);
        
        ExecutionResult result = action.execute(block, context);
        assertTrue(result.isSuccess());
        assertEquals("Message sent successfully", result.getMessage());
        verify(player).sendMessage("Test message from item");
    }
}