package com.megacreative.coding;

import com.megacreative.coding.values.DataValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.World;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParameterResolverTest {

    @Mock
    private ExecutionContext context;
    
    @Mock
    private Player player;
    
    @Mock
    private World world;
    
    private ParameterResolver resolver;

    @BeforeEach
    public void setUp() {
        resolver = new ParameterResolver(context);
    }

    @Test
    public void testResolveWithNoPlaceholders() {
        DataValue input = DataValue.of("Hello World");
        DataValue result = resolver.resolve(context, input);
        assertEquals("Hello World", result.asString());
    }

    @Test
    public void testResolveWithPlayerNamePlaceholder() {
        when(context.getPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("TestPlayer");
        
        DataValue input = DataValue.of("Hello ${player_name}!");
        DataValue result = resolver.resolve(context, input);
        assertEquals("Hello TestPlayer!", result.asString());
    }

    @Test
    public void testResolveWithPlayerDisplayNamePlaceholder() {
        when(context.getPlayer()).thenReturn(player);
        when(player.getDisplayName()).thenReturn("TestPlayerDisplay");
        
        DataValue input = DataValue.of("Hello ${player_display_name}!");
        DataValue result = resolver.resolve(context, input);
        assertEquals("Hello TestPlayerDisplay!", result.asString());
    }

    @Test
    public void testResolveWithPlayerUUIDPlaceholder() {
        when(context.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(java.util.UUID.randomUUID());
        
        DataValue input = DataValue.of("UUID: ${player_uuid}");
        DataValue result = resolver.resolve(context, input);
        assertTrue(result.asString().startsWith("UUID: "));
    }

    @Test
    public void testResolveWithPlayerWorldPlaceholder() {
        when(context.getPlayer()).thenReturn(player);
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("TestWorld");
        
        DataValue input = DataValue.of("World: ${player_world}");
        DataValue result = resolver.resolve(context, input);
        assertEquals("World: TestWorld", result.asString());
    }

    @Test
    public void testResolveWithPlayerCoordinatesPlaceholders() {
        when(context.getPlayer()).thenReturn(player);
        Location location = new Location(world, 10.5, 20.7, 30.9);
        when(player.getLocation()).thenReturn(location);
        
        DataValue input = DataValue.of("Coords: ${player_x}, ${player_y}, ${player_z}");
        DataValue result = resolver.resolve(context, input);
        assertEquals("Coords: 10.5, 20.7, 30.9", result.asString());
    }

    @Test
    public void testResolveWithBlockCoordinatesPlaceholders() {
        Location blockLocation = new Location(world, 100.0, 50.0, 200.0);
        when(context.getBlockLocation()).thenReturn(blockLocation);
        when(blockLocation.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("BlockWorld");
        
        DataValue input = DataValue.of("Block: ${block_x}, ${block_y}, ${block_z}, ${block_world}");
        DataValue result = resolver.resolve(context, input);
        assertEquals("Block: 100.0, 50.0, 200.0, BlockWorld", result.asString());
    }

    @Test
    public void testResolveWithTimestampPlaceholder() {
        DataValue input = DataValue.of("Time: ${timestamp}");
        DataValue result = resolver.resolve(context, input);
        assertTrue(result.asString().startsWith("Time: "));
        assertTrue(Long.parseLong(result.asString().substring(6)) > 0);
    }

    @Test
    public void testResolveWithRandomPlaceholder() {
        DataValue input = DataValue.of("Random: ${random}");
        DataValue result = resolver.resolve(context, input);
        assertTrue(result.asString().startsWith("Random: "));
        assertTrue(Double.parseDouble(result.asString().substring(8)) >= 0);
        assertTrue(Double.parseDouble(result.asString().substring(8)) <= 1);
    }

    @Test
    public void testResolveWithUnknownPlaceholder() {
        DataValue input = DataValue.of("Hello ${unknown_placeholder}!");
        DataValue result = resolver.resolve(context, input);
        assertEquals("Hello ${unknown_placeholder}!", result.asString());
    }

    @Test
    public void testResolveWithNullInput() {
        DataValue result = resolver.resolve(context, null);
        assertNull(result);
    }

    @Test
    public void testResolveWithEmptyInput() {
        DataValue input = DataValue.of("");
        DataValue result = resolver.resolve(context, input);
        assertEquals("", result.asString());
    }
}