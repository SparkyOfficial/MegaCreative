package com.megacreative.coding.placeholders;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 🎆 ENHANCED: Test suite for reference system-style placeholder system
 */
public class ReferenceSystemPlaceholderResolverTest {
    
    @Mock
    private ExecutionContext context;
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private VariableManager variableManager;
    
    @Mock
    private Player player;
    
    @Mock
    private World world;
    
    @Mock
    private Location location;
    
    @Mock
    private CreativeWorld creativeWorld;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup basic mocks
        when(context.getPlugin()).thenReturn(plugin);
        when(plugin.getServiceRegistry()).thenReturn(mock(com.megacreative.core.ServiceRegistry.class));
        when(plugin.getServiceRegistry().getVariableManager()).thenReturn(variableManager);
        when(context.getPlayer()).thenReturn(player);
        when(context.getCreativeWorld()).thenReturn(creativeWorld);
        
        // Setup player mocks
        when(player.getName()).thenReturn("TestPlayer");
        when(player.getDisplayName()).thenReturn("§aTestPlayer");
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenReturn(location);
        when(player.getHealth()).thenReturn(20.0);
        when(player.getMaxHealth()).thenReturn(20.0);
        when(player.getFoodLevel()).thenReturn(20);
        when(player.getLevel()).thenReturn(30);
        when(player.getExp()).thenReturn(0.5f);
        
        // Setup world mocks
        when(world.getName()).thenReturn("TestWorld");
        when(world.getTime()).thenReturn(6000L);
        when(world.hasStorm()).thenReturn(false);
        
        // Setup location mocks
        when(location.getBlockX()).thenReturn(100);
        when(location.getBlockY()).thenReturn(64);
        when(location.getBlockZ()).thenReturn(200);
        when(location.getYaw()).thenReturn(90.0f);
        when(location.getPitch()).thenReturn(0.0f);
        when(location.getWorld()).thenReturn(world);
    }
    
    @Test
    void testReferenceSystemVariablePlaceholder() {
        // Setup variable
        when(variableManager.getPlayerVariable(any(UUID.class), eq("score")))
            .thenReturn(DataValue.of("1500"));
            
        String text = "Your score is apple[score]~ points!";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertEquals("Your score is 1500 points!", result);
    }
    
    @Test
    void testReferenceSystemVariableWithDefault() {
        // No variable set
        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"missing\")))
            .thenReturn(null);
            
        String text = \"Value: apple[missing|default_value]~\";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertEquals(\"Value: default_value\", result);
    }
    
    @Test
    void testPlayerPlaceholders() {
        String text = \"Hello player[name]~! You are in world player[world]~ at level player[level]~.\";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertEquals(\"Hello TestPlayer! You are in world TestWorld at level 30.\", result);
    }
    
    @Test
    void testLocationPlaceholders() {
        String text = \"You are at location[x]~, location[y]~, location[z]~ in location[world]~\";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertEquals(\"You are at 100, 64, 200 in TestWorld\", result);
    }
    
    @Test
    void testWorldPlaceholders() {
        String text = \"World: world[name]~, Time: world[time]~, Weather: world[weather]~\";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertEquals(\"World: TestWorld, Time: 6000, Weather: clear\", result);
    }
    
    @Test
    void testMathPlaceholders() {
        String text = \"5 + 3 = math[5+3]~, 10 * 2 = math[10*2]~\";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertEquals(\"5 + 3 = 8.0, 10 * 2 = 20.0\", result);
    }
    
    @Test
    void testRandomPlaceholders() {
        String text = \"Random 1-10: random[1-10]~, Random 100: random[100]~\";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        // Check that result contains numbers in expected ranges
        assertTrue(result.matches(\"Random 1-10: [1-9]\\\\d*, Random 100: \\\\d+\"));
    }
    
    @Test
    void testColorPlaceholders() {
        String text = \"color[red]~Hello color[green]~World color[reset]~!\";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertEquals(\"§cHello §aWorld §r!\", result);
    }
    
    @Test
    void testTimePlaceholders() {
        String text = \"Current time: time[HH:mm]~, Date: time[date]~\";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        // Check format (time will vary)
        assertTrue(result.matches(\"Current time: \\\\d{2}:\\\\d{2}, Date: \\\\d{4}-\\\\d{2}-\\\\d{2}\"));
    }
    
    @Test
    void testFormatPlaceholders() {
        // Setup variable for formatting
        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"money\")))
            .thenReturn(DataValue.of(\"1234.567\"));
            
        String text = \"Money: format[apple[money]~|currency]~, Rounded: format[apple[money]~|2]~\";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertEquals(\"Money: $1234.57, Rounded: 1234.57\", result);
    }
    
    @Test
    void testMixedPlaceholderFormats() {
        // Setup variable
        when(variableManager.getPlayerVariable(any(UUID.class), eq("score")))
            .thenReturn(DataValue.of("1500"));
            
        String text = "Reference System: apple[score]~, Modern: ${player_name}, Classic: %world%";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertEquals("Reference System: 1500, Modern: TestPlayer, Classic: TestWorld", result);
    }
    
    @Test
    void testNestedPlaceholders() {
        // Setup variables
        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"base_score\")))
            .thenReturn(DataValue.of(\"100\"));
        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"multiplier\")))
            .thenReturn(DataValue.of(\"5\"));
            
        String text = \"Total: math[apple[base_score]~*apple[multiplier]~]~\";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertEquals(\"Total: 500.0\", result);
    }
    
    @Test
    void testComplexExample() {
        // Setup variables
        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"kills\")))
            .thenReturn(DataValue.of(\"25\"));
        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"deaths\")))
            .thenReturn(DataValue.of(\"5\"));
            
        String text = \"color[gold]~=== player[name]~'s Stats ===\n\" +
                     \"color[green]~❤ Health: player[health]~/player[max_health]~\n\" +
                     \"color[blue]~🗡 K/D Ratio: format[math[apple[kills]~/apple[deaths]~]|2]~\n\" +
                     \"color[yellow]~📍 Location: location[formatted]~\n\" +
                     \"color[cyan]~🌍 World: world[name]~ (time[short]~)\n\" +
                     \"color[reset]~Last updated: time[medium]~\";
                     
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertTrue(result.contains(\"§6=== TestPlayer's Stats ===\"));
        assertTrue(result.contains(\"§a❤ Health: 20/20\"));
        assertTrue(result.contains(\"§9🗡 K/D Ratio: 5.00\"));
        assertTrue(result.contains(\"§e📍 Location: 100, 64, 200\"));
        assertTrue(result.contains(\"§b🌍 World: TestWorld\"));
        assertTrue(result.contains(\"§r\"));
    }
    
    @Test
    void testInvalidPlaceholders() {
        String text = \"Valid: player[name]~, Invalid: invalid[unknown]~, Missing: apple[missing]~\";
        String result = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        // Valid placeholder should resolve, invalid should remain, missing should be empty
        assertTrue(result.contains(\"Valid: TestPlayer\"));
        assertTrue(result.contains(\"Invalid: invalid[unknown]~\")); // Should remain unchanged
        assertTrue(result.contains(\"Missing: \")); // Should be empty string
    }
    
    @Test
    void testEmptyAndNullInputs() {
        assertEquals("", ReferenceSystemPlaceholderResolver.resolvePlaceholders("", context));
        assertEquals(null, ReferenceSystemPlaceholderResolver.resolvePlaceholders(null, context));
    }
}
