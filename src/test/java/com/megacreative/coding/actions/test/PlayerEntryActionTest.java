package com.megacreative.coding.actions.test;

import com.megacreative.MegaCreative;
import com.megacreative.coding.actions.PlayerEntryAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.containers.BlockContainerManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for PlayerEntryAction
 */
public class PlayerEntryActionTest {
    
    private PlayerEntryAction playerEntryAction;
    private Player mockPlayer;
    private MegaCreative mockPlugin;
    private ServiceRegistry mockServiceRegistry;
    private VariableManager mockVariableManager;
    private CodeBlock mockBlock;
    private World mockWorld;
    private Location mockLocation;
    
    @BeforeEach
    public void setUp() {
        playerEntryAction = new PlayerEntryAction();
        mockPlayer = Mockito.mock(Player.class);
        mockPlugin = Mockito.mock(MegaCreative.class);
        mockServiceRegistry = Mockito.mock(ServiceRegistry.class);
        mockVariableManager = Mockito.mock(VariableManager.class);
        mockBlock = Mockito.mock(CodeBlock.class);
        mockWorld = Mockito.mock(World.class);
        mockLocation = Mockito.mock(Location.class);
        
        when(mockPlugin.getServiceRegistry()).thenReturn(mockServiceRegistry);
        when(mockPlugin.getVariableManager()).thenReturn(mockVariableManager);
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.getDisplayName()).thenReturn("TestPlayer");
        when(mockPlayer.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getName()).thenReturn("test_world");
        when(mockBlock.getLocation()).thenReturn(mockLocation);
        when(mockBlock.getId()).thenReturn(UUID.randomUUID());
        
        // Mock VariableManager to return the same value (no resolution needed for simple values)
        when(mockVariableManager.getVariable(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            return new AnyValue(null);
        });
    }
    
    @Test
    public void testPlayerEntryWithoutAutoGive() {
        // Setup
        ExecutionContext context = ExecutionContext.builder()
                .plugin(mockPlugin)
                .player(mockPlayer)
                .currentBlock(mockBlock)
                .build();
        
        when(mockBlock.getParameter("autoGiveItem")).thenReturn(null);
        
        // Execute
        playerEntryAction.execute(context);
        
        // Verify
        verify(mockPlayer).sendMessage(contains("Добро пожаловать"));
    }
    
    @Test
    public void testPlayerEntryWithAutoGive() {
        // Setup
        ExecutionContext context = ExecutionContext.builder()
                .plugin(mockPlugin)
                .player(mockPlayer)
                .currentBlock(mockBlock)
                .build();
        
        PlayerInventory mockInventory = Mockito.mock(PlayerInventory.class);
        when(mockPlayer.getInventory()).thenReturn(mockInventory);
        when(mockBlock.getParameter("autoGiveItem")).thenReturn(DataValue.fromObject(true));
        when(mockBlock.getParameter("itemName")).thenReturn(DataValue.fromObject("STONE"));
        when(mockBlock.getParameter("itemAmount")).thenReturn(DataValue.fromObject("5"));
        
        // Execute
        playerEntryAction.execute(context);
        
        // Verify
        verify(mockInventory).addItem(any());
        verify(mockPlayer).sendMessage(contains("Добро пожаловать"));
        verify(mockPlayer).sendMessage(contains("получили 5x STONE"));
    }
    
    @Test
    public void testPlayerEntryWithContainerItems() {
        // Setup
        ExecutionContext context = ExecutionContext.builder()
                .plugin(mockPlugin)
                .player(mockPlayer)
                .currentBlock(mockBlock)
                .build();
        
        // Mock the container manager
        BlockContainerManager mockContainerManager = Mockito.mock(BlockContainerManager.class);
        when(mockServiceRegistry.getContainerManager()).thenReturn(mockContainerManager);
        
        PlayerInventory mockInventory = Mockito.mock(PlayerInventory.class);
        when(mockPlayer.getInventory()).thenReturn(mockInventory);
        when(mockBlock.getParameter("autoGiveItem")).thenReturn(DataValue.fromObject(true));
        
        // Execute
        playerEntryAction.execute(context);
        
        // Verify that container manager was accessed
        verify(mockServiceRegistry).getContainerManager();
        verify(mockPlayer).sendMessage(contains("Добро пожаловать"));
    }
    
    // Simple implementation of AnyValue for testing
    private static class AnyValue implements DataValue {
        private final Object value;
        
        public AnyValue(Object value) {
            this.value = value;
        }
        
        @Override
        public ValueType getType() {
            return ValueType.ANY;
        }
        
        @Override
        public Object getValue() {
            return value;
        }
        
        @Override
        public void setValue(Object value) throws IllegalArgumentException {
            // Not implemented for test
        }
        
        @Override
        public String asString() {
            return value != null ? value.toString() : "";
        }
        
        @Override
        public Number asNumber() throws NumberFormatException {
            return 0;
        }
        
        @Override
        public boolean asBoolean() {
            return value instanceof Boolean ? (Boolean) value : false;
        }
        
        @Override
        public boolean isEmpty() {
            return value == null;
        }
        
        @Override
        public boolean isValid() {
            return true;
        }
        
        @Override
        public String getDescription() {
            return "Test value";
        }
        
        @Override
        public DataValue clone() {
            return new AnyValue(value);
        }
        
        @Override
        public java.util.Map<String, Object> serialize() {
            return new java.util.HashMap<>();
        }
    }
}