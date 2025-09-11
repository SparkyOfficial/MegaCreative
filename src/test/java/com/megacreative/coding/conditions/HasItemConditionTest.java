package com.megacreative.coding.conditions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import com.megacreative.core.ServiceRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HasItemConditionTest {

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
    private PlayerInventory inventory;
    
    private HasItemCondition condition;

    @BeforeEach
    public void setUp() {
        condition = new HasItemCondition();
        
        when(context.getPlayer()).thenReturn(player);
        when(context.getPlugin()).thenReturn(plugin);
        when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
        when(player.getInventory()).thenReturn(inventory);
    }

    @Test
    public void testEvaluateWithNullPlayer() {
        when(context.getPlayer()).thenReturn(null);
        boolean result = condition.evaluate(block, context);
        assertFalse(result);
    }

    @Test
    public void testEvaluateWithNoSlotResolver() {
        when(blockConfigService.getSlotResolver(anyString())).thenReturn(null);
        boolean result = condition.evaluate(block, context);
        assertFalse(result);
    }

    @Test
    public void testEvaluateWithNullItem() {
        Function<String, Integer> slotResolver = mock(Function.class);
        when(blockConfigService.getSlotResolver(anyString())).thenReturn(slotResolver);
        when(slotResolver.apply("item_slot")).thenReturn(0);
        when(block.getConfigItem(0)).thenReturn(null);
        
        boolean result = condition.evaluate(block, context);
        assertFalse(result);
    }
}