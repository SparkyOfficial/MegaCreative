package com.megacreative.coding.actions.base;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenericActionTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private Player player;

    @Mock
    private World world;

    @Mock
    private PlayerInventory inventory;

    @Mock
    private CreativeWorld creativeWorld;

    @Mock
    private Server server;

    @Mock
    private PluginManager pluginManager;

    @Mock
    private BukkitScheduler scheduler;

    private GenericAction genericAction;
    private CodeBlock codeBlock;
    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        genericAction = new GenericAction();
        codeBlock = mock(CodeBlock.class);
        
        // Set up player
        UUID playerUUID = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenReturn(new Location(world, 0, 0, 0));
        when(player.getInventory()).thenReturn(inventory);
        when(player.getServer()).thenReturn(server);
        
        // Set up world
        when(world.getName()).thenReturn("world");
        when(world.getUID()).thenReturn(UUID.randomUUID());
        
        // Set up server
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getWorld("world")).thenReturn(world);
        
        // Set up plugin
        when(plugin.getServer()).thenReturn(server);
        
        // Create execution context
        context = new ExecutionContext(plugin, player, creativeWorld, mock(Event.class), 
            new Location(world, 0, 0, 0), mock(CodeBlock.class));
    }

    @Test
    void testSendMessageAction() {
        // Set up the code block for sendMessage action
        when(codeBlock.getAction()).thenReturn("sendMessage");
        Map<String, DataValue> params = new HashMap<>();
        params.put("message", DataValue.of("Hello, World!"));
        when(codeBlock.getParameters()).thenReturn(params);
        
        // Execute the action
        ExecutionResult result = genericAction.execute(codeBlock, context);
        
        // Verify the result
        assertTrue(result.isSuccess());
        verify(player).sendMessage("Hello, World!");
    }

    @Test
    void testGiveItemAction() {
        // Set up the code block for giveItem action
        when(codeBlock.getAction()).thenReturn("giveItem");
        Map<String, DataValue> params = new HashMap<>();
        params.put("material", DataValue.of("STONE"));
        params.put("amount", DataValue.of(5));
        when(codeBlock.getParameters()).thenReturn(params);
        
        // Execute the action
        ExecutionResult result = genericAction.execute(codeBlock, context);
        
        // Verify the result
        assertTrue(result.isSuccess());
        verify(inventory).addItem(any(ItemStack.class));
    }

    @Test
    void testUnsupportedAction() {
        // Set up the code block for an unsupported action
        when(codeBlock.getAction()).thenReturn("unsupportedAction");
        when(codeBlock.getParameters()).thenReturn(new HashMap<>());
        
        // Execute the action
        ExecutionResult result = genericAction.execute(codeBlock, context);
        
        // Verify the result
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Unknown action"));
    }

    @Test
    void testIsSupported() {
        // Test supported actions
        assertTrue(GenericAction.isSupported("sendMessage"));
        assertTrue(GenericAction.isSupported("giveItem"));
        assertTrue(GenericAction.isSupported("setHealth"));
        
        // Test unsupported action
        assertFalse(GenericAction.isSupported("unsupportedAction"));
    }

    @Test
    void testRegisterActionHandler() {
        // Register a custom action handler
        GenericAction.registerActionHandler("customAction", (ctx, params) -> {
            // Custom action implementation
        });
        
        // Verify the action is now supported
        assertTrue(GenericAction.isSupported("customAction"));
    }

    @Test
    void testParseLocationString() {
        // Test parsing location string with world
        String locationString = "world:10.5,20.0,30.7";
        Location location = GenericAction.parseLocationString(locationString, context);
        
        assertNotNull(location);
        assertEquals(10.5, location.getX());
        assertEquals(20.0, location.getY());
        assertEquals(30.7, location.getZ());
        assertEquals(world, location.getWorld());
        
        // Test parsing location string without world
        String locationStringNoWorld = "15.0,25.5,35.2";
        Location locationNoWorld = GenericAction.parseLocationString(locationStringNoWorld, context);
        
        assertNotNull(locationNoWorld);
        assertEquals(15.0, locationNoWorld.getX());
        assertEquals(25.5, locationNoWorld.getY());
        assertEquals(35.2, locationNoWorld.getZ());
        assertEquals(world, locationNoWorld.getWorld());
    }
}