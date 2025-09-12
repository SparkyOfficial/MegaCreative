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
 * 🎆 ENHANCED: Test suite for FrameLand-style placeholder system
 */
public class FrameLandPlaceholderResolverTest {
    
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
    void testFrameLandVariablePlaceholder() {
        // Setup variable
        when(variableManager.getPlayerVariable(any(UUID.class), eq("score")))
            .thenReturn(DataValue.of("1500"));
            
        String text = "Your score is apple[score]~ points!";
        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);
        
        assertEquals("Your score is 1500 points!", result);
    }
    
    @Test\n    void testFrameLandVariableWithDefault() {\n        // No variable set\n        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"missing\")))\n            .thenReturn(null);\n            \n        String text = \"Value: apple[missing|default_value]~\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        assertEquals(\"Value: default_value\", result);\n    }\n    \n    @Test\n    void testPlayerPlaceholders() {\n        String text = \"Hello player[name]~! You are in world player[world]~ at level player[level]~.\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        assertEquals(\"Hello TestPlayer! You are in world TestWorld at level 30.\", result);\n    }\n    \n    @Test\n    void testLocationPlaceholders() {\n        String text = \"You are at location[x]~, location[y]~, location[z]~ in location[world]~\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        assertEquals(\"You are at 100, 64, 200 in TestWorld\", result);\n    }\n    \n    @Test\n    void testWorldPlaceholders() {\n        String text = \"World: world[name]~, Time: world[time]~, Weather: world[weather]~\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        assertEquals(\"World: TestWorld, Time: 6000, Weather: clear\", result);\n    }\n    \n    @Test\n    void testMathPlaceholders() {\n        String text = \"5 + 3 = math[5+3]~, 10 * 2 = math[10*2]~\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        assertEquals(\"5 + 3 = 8.0, 10 * 2 = 20.0\", result);\n    }\n    \n    @Test\n    void testRandomPlaceholders() {\n        String text = \"Random 1-10: random[1-10]~, Random 100: random[100]~\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        // Check that result contains numbers in expected ranges\n        assertTrue(result.matches(\"Random 1-10: [1-9]\\\\d*, Random 100: \\\\d+\"));\n    }\n    \n    @Test\n    void testColorPlaceholders() {\n        String text = \"color[red]~Hello color[green]~World color[reset]~!\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        assertEquals(\"§cHello §aWorld §r!\", result);\n    }\n    \n    @Test\n    void testTimePlaceholders() {\n        String text = \"Current time: time[HH:mm]~, Date: time[date]~\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        // Check format (time will vary)\n        assertTrue(result.matches(\"Current time: \\\\d{2}:\\\\d{2}, Date: \\\\d{4}-\\\\d{2}-\\\\d{2}\"));\n    }\n    \n    @Test\n    void testFormatPlaceholders() {\n        // Setup variable for formatting\n        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"money\")))\n            .thenReturn(DataValue.of(\"1234.567\"));\n            \n        String text = \"Money: format[apple[money]~|currency]~, Rounded: format[apple[money]~|2]~\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        assertEquals(\"Money: $1234.57, Rounded: 1234.57\", result);\n    }\n    \n    @Test\n    void testMixedPlaceholderFormats() {\n        // Setup variable\n        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"score\")))\n            .thenReturn(DataValue.of(\"1500\"));\n            \n        String text = \"FrameLand: apple[score]~, Modern: ${player_name}, Classic: %world%\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        assertEquals(\"FrameLand: 1500, Modern: TestPlayer, Classic: TestWorld\", result);\n    }\n    \n    @Test\n    void testNestedPlaceholders() {\n        // Setup variables\n        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"base_score\")))\n            .thenReturn(DataValue.of(\"100\"));\n        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"multiplier\")))\n            .thenReturn(DataValue.of(\"5\"));\n            \n        String text = \"Total: math[apple[base_score]~*apple[multiplier]~]~\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        assertEquals(\"Total: 500.0\", result);\n    }\n    \n    @Test\n    void testComplexExample() {\n        // Setup variables\n        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"kills\")))\n            .thenReturn(DataValue.of(\"25\"));\n        when(variableManager.getPlayerVariable(any(UUID.class), eq(\"deaths\")))\n            .thenReturn(DataValue.of(\"5\"));\n            \n        String text = \"color[gold]~=== player[name]~'s Stats ===\\n\" +\n                     \"color[green]~❤ Health: player[health]~/player[max_health]~\\n\" +\n                     \"color[blue]~🗡 K/D Ratio: format[math[apple[kills]~/apple[deaths]~]|2]~\\n\" +\n                     \"color[yellow]~📍 Location: location[formatted]~\\n\" +\n                     \"color[cyan]~🌍 World: world[name]~ (time[short]~)\\n\" +\n                     \"color[reset]~Last updated: time[medium]~\";\n                     \n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        assertTrue(result.contains(\"§6=== TestPlayer's Stats ===\"));\n        assertTrue(result.contains(\"§a❤ Health: 20/20\"));\n        assertTrue(result.contains(\"§9🗡 K/D Ratio: 5.00\"));\n        assertTrue(result.contains(\"§e📍 Location: 100, 64, 200\"));\n        assertTrue(result.contains(\"§b🌍 World: TestWorld\"));\n        assertTrue(result.contains(\"§r\"));\n    }\n    \n    @Test\n    void testInvalidPlaceholders() {\n        String text = \"Valid: player[name]~, Invalid: invalid[unknown]~, Missing: apple[missing]~\";\n        String result = FrameLandPlaceholderResolver.resolvePlaceholders(text, context);\n        \n        // Valid placeholder should resolve, invalid should remain, missing should be empty\n        assertTrue(result.contains(\"Valid: TestPlayer\"));\n        assertTrue(result.contains(\"Invalid: invalid[unknown]~\")); // Should remain unchanged\n        assertTrue(result.contains(\"Missing: \")); // Should be empty string\n    }\n    \n    @Test\n    void testEmptyAndNullInputs() {\n        assertEquals(\"\", FrameLandPlaceholderResolver.resolvePlaceholders(\"\", context));\n        assertEquals(null, FrameLandPlaceholderResolver.resolvePlaceholders(null, context));\n    }\n}\n