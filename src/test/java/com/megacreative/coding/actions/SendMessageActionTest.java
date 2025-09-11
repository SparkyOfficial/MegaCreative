package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.PluginMain;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import com.megacreative.services.ServiceRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.bukkit.entity.Player;

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
    private PluginMain plugin;
    
    @Mock
    private ServiceRegistry serviceRegistry;
    
    @Mock
    private BlockConfigService blockConfigService;
    
    @Mock
    private Player player;
    
    private SendMessageAction action;

    @BeforeEach
    public void setUp() {
        action = new SendMessageAction();
        
        when(context.getPlayer()).thenReturn(player);
        when(context.getPlugin()).thenReturn(plugin);
        when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
    }

    @Test
    public void testExecuteWithNullPlayer() {
        when(context.getPlayer()).thenReturn(null);
        ExecutionResult result = action.execute(block, context);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("No player found"));
    }

    @Test
    public void testGetMessageFromContainerWithNoSlotResolver() {
        when(blockConfigService.getSlotResolver(anyString())).thenReturn(null);
        
        ExecutionResult result = action.execute(block, context);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Message is not configured"));
    }

    @Test
    public void testGetMessageFromContainerWithNullMessageItem() {
        Function<String, Integer> slotResolver = mock(Function.class);
        when(blockConfigService.getSlotResolver(anyString())).thenReturn(slotResolver);
        when(slotResolver.apply("message_slot")).thenReturn(0);
        when(block.getConfigItem(0)).thenReturn(null);
        
        ExecutionResult result = action.execute(block, context);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Message is not configured"));
    }
}