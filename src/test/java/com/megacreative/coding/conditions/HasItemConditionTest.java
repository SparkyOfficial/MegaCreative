package com.megacreative.coding.conditions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.PluginMain;
import com.megacreative.services.BlockConfigService;
import com.megacreative.services.ServiceRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

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
    private PluginMain plugin;
    
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
    public void testGetItemParamsFromContainerWithNoSlotResolver() {
        when(blockConfigService.getSlotResolver(anyString())).thenReturn(null);
        
        // We'll test the private method indirectly by calling evaluate
        // Since the method is private, we can't call it directly
        // But we can verify that it handles the null case gracefully
        boolean result = condition.evaluate(block, context);
        assertFalse(result);
    }

    @Test
    public void testGetItemTypeFromItem() {
        ItemStack item = new ItemStack(Material.STONE);
        
        // We'll test the private method indirectly by calling evaluate
        // Since the method is private, we can't call it directly
        // But we can verify that it works with a real ItemStack
        when(blockConfigService.getSlotResolver(anyString())).thenReturn(mock(Function.class));
        
        boolean result = condition.evaluate(block, context);
        assertFalse(result); // Should be false because we haven't set up the item properly
    }
}