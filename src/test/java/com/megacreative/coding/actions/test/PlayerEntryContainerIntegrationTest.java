package com.megacreative.coding.actions.test;

import com.megacreative.MegaCreative;
import com.megacreative.coding.actions.PlayerEntryAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.containers.BlockContainerManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.block.Chest;
import org.bukkit.block.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration test for PlayerEntryAction container integration
 * This test demonstrates the full workflow of giving items from containers
 */
public class PlayerEntryContainerIntegrationTest {
    
    private PlayerEntryAction playerEntryAction;
    private Player mockPlayer;
    private MegaCreative mockPlugin;
    private ServiceRegistry mockServiceRegistry;
    private VariableManager mockVariableManager;
    private CodeBlock mockBlock;
    private World mockWorld;
    private Location mockBlockLocation;
    private Location mockContainerLocation;
    
    @BeforeEach
    public void setUp() {
        playerEntryAction = new PlayerEntryAction();
        mockPlayer = Mockito.mock(Player.class);
        mockPlugin = Mockito.mock(MegaCreative.class);
        mockServiceRegistry = Mockito.mock(ServiceRegistry.class);
        mockVariableManager = Mockito.mock(VariableManager.class);
        mockBlock = Mockito.mock(CodeBlock.class);
        mockWorld = Mockito.mock(World.class);
        mockBlockLocation = Mockito.mock(Location.class);
        mockContainerLocation = Mockito.mock(Location.class);
        
        // Mock CodeBlock ID to prevent NPE
        when(mockBlock.getId()).thenReturn(UUID.randomUUID());
        
        when(mockPlugin.getServiceRegistry()).thenReturn(mockServiceRegistry);
        when(mockPlugin.getVariableManager()).thenReturn(mockVariableManager);
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.getDisplayName()).thenReturn("TestPlayer");
        when(mockPlayer.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getName()).thenReturn("test_world");
        when(mockBlock.getLocation()).thenReturn(mockBlockLocation);
    }
    
    @Test
    public void testContainerIntegrationWorkflow() {
        // Setup
        ExecutionContext context = ExecutionContext.builder()
                .plugin(mockPlugin)
                .player(mockPlayer)
                .currentBlock(mockBlock)
                .build();
        
        when(mockBlock.getParameter("autoGiveItem")).thenReturn(DataValue.fromObject(true));
        
        // Execute
        playerEntryAction.execute(context);
        
        // Verify that the service registry was accessed
        verify(mockPlugin).getServiceRegistry();
    }
    
    @Test
    public void testGetItemsFromContainerWithValidChest() {
        // This would require a more complex setup with actual Bukkit mocks
        // For now, we'll test that the method handles the case properly
        
        // Mock the container manager
        BlockContainerManager mockContainerManager = Mockito.mock(BlockContainerManager.class);
        when(mockServiceRegistry.getContainerManager()).thenReturn(mockContainerManager);
        
        // Mock locations
        Location clonedLocation = Mockito.mock(Location.class);
        when(mockBlockLocation.clone()).thenReturn(clonedLocation);
        when(clonedLocation.add(0, 1, 0)).thenReturn(mockContainerLocation);
        
        // Mock container block
        Block mockContainerBlock = Mockito.mock(Block.class);
        when(mockContainerLocation.getBlock()).thenReturn(mockContainerBlock);
        
        // Mock block state as a chest
        Chest mockChest = Mockito.mock(Chest.class);
        when(mockContainerBlock.getState()).thenReturn(mockChest);
        
        // Mock inventory
        Inventory mockInventory = Mockito.mock(Inventory.class);
        when(mockChest.getInventory()).thenReturn(mockInventory);
        
        // Mock inventory contents
        ItemStack[] contents = new ItemStack[2];
        contents[0] = new ItemStack(Material.DIAMOND, 1);
        contents[1] = new ItemStack(Material.IRON_INGOT, 5);
        when(mockInventory.getContents()).thenReturn(contents);
        
        PlayerInventory mockPlayerInventory = Mockito.mock(PlayerInventory.class);
        when(mockPlayer.getInventory()).thenReturn(mockPlayerInventory);
        
        // Setup execution context
        ExecutionContext context = ExecutionContext.builder()
                .plugin(mockPlugin)
                .player(mockPlayer)
                .currentBlock(mockBlock)
                .build();
        
        when(mockBlock.getParameter("autoGiveItem")).thenReturn(DataValue.fromObject(true));
        
        // Execute
        playerEntryAction.execute(context);
        
        // Verify that items were added to player inventory
        verify(mockPlayerInventory, atLeastOnce()).addItem(any(ItemStack.class));
        verify(mockPlayer).sendMessage(contains("получили предметы из конфигурации"));
    }
}