package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.MegaCreative;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeleportToLocationActionTest {
    
    private TeleportToLocationAction action;
    private CodeBlock block;
    private ExecutionContext context;
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private Player player;
    
    @Mock
    private World world;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        action = new TeleportToLocationAction();
        block = new CodeBlock();
        context = mock(ExecutionContext.class);
        
        when(context.getPlayer()).thenReturn(player);
        when(context.getPlugin()).thenReturn(plugin);
        when(player.getWorld()).thenReturn(world);
        when(player.getServer()).thenReturn(mock(org.bukkit.Server.class));
        when(player.getServer().getWorld("world")).thenReturn(world);
    }
    
    @Test
    void testExecuteWithValidLocation() {
        // Arrange
        block.setParameter("location", DataValue.of("10,20,30"));
        when(player.teleport(any(Location.class))).thenReturn(true);
        
        // Act
        ExecutionResult result = action.execute(block, context);
        
        // Assert
        assertTrue(result.isSuccess());
        verify(player).teleport(any(Location.class));
    }
    
    @Test
    void testExecuteWithValidLocationWithWorld() {
        // Arrange
        block.setParameter("location", DataValue.of("world:10,20,30"));
        when(player.teleport(any(Location.class))).thenReturn(true);
        
        // Act
        ExecutionResult result = action.execute(block, context);
        
        // Assert
        assertTrue(result.isSuccess());
        verify(player).teleport(any(Location.class));
    }
    
    @Test
    void testExecuteWithMissingLocation() {
        // Act
        ExecutionResult result = action.execute(block, context);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Location parameter is required for teleportation", result.getMessage());
    }
    
    @Test
    void testExecuteWithInvalidLocationFormat() {
        // Arrange
        block.setParameter("location", DataValue.of("invalid_format"));
        
        // Act
        ExecutionResult result = action.execute(block, context);
        
        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid location format"));
    }
    
    @Test
    void testExecuteWithNullPlayer() {
        // Arrange
        when(context.getPlayer()).thenReturn(null);
        
        // Act
        ExecutionResult result = action.execute(block, context);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("No player available for teleportation", result.getMessage());
    }
    
    @Test
    void testExecuteWithCustomMessage() {
        // Arrange
        block.setParameter("location", DataValue.of("10,20,30"));
        block.setParameter("message", DataValue.of("Teleported successfully!"));
        when(player.teleport(any(Location.class))).thenReturn(true);
        
        // Act
        ExecutionResult result = action.execute(block, context);
        
        // Assert
        assertTrue(result.isSuccess());
        verify(player).sendMessage("Teleported successfully!");
    }
    
    @Test
    void testExecuteWithoutEffects() {
        // Arrange
        block.setParameter("location", DataValue.of("10,20,30"));
        block.setParameter("effects", DataValue.of(false));
        when(player.teleport(any(Location.class))).thenReturn(true);
        
        // Act
        ExecutionResult result = action.execute(block, context);
        
        // Assert
        assertTrue(result.isSuccess());
        verify(player).teleport(any(Location.class));
    }
}