package com.megacreative.services;

import com.megacreative.MegaCreative;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockConfigServiceComprehensiveTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private Logger logger;

    private BlockConfigService blockConfigService;

    @BeforeEach
    void setUp() {
        // Set up the plugin mock to return a logger
        lenient().when(plugin.getLogger()).thenReturn(logger);
        
        // Create a temporary directory for testing
        File dataFolder = new File("target/test-data");
        dataFolder.mkdirs();
        lenient().when(plugin.getDataFolder()).thenReturn(dataFolder);
        
        // Copy the resource file to the test directory
        File configFile = new File(dataFolder, "coding_blocks.yml");
        if (!configFile.exists()) {
            // Create a minimal config for testing
            String minimalConfig = 
                "blocks:\n" +
                "  DIAMOND_BLOCK:\n" +
                "    name: \"Event Block\"\n" +
                "    type: \"EVENT\"\n" +
                "    default_action: \"onJoin\"\n" +
                "  COBBLESTONE:\n" +
                "    name: \"Action Block\"\n" +
                "    type: \"ACTION\"\n" +
                "    default_action: \"sendMessage\"\n" +
                "  PISTON:\n" +
                "    name: \"Bracket Block\"\n" +
                "    type: \"CONTROL\"\n" +
                "    default_action: \"openBracket\"\n" +
                "\n" +
                "action_configurations:\n" +
                "  sendMessage:\n" +
                "    slots:\n" +
                "      0:\n" +
                "        slot_name: \"message_slot\"\n" +
                "  giveItems:\n" +
                "    item_groups:\n" +
                "      items_to_give:\n" +
                "        slots: [0, 1, 2, 3, 4, 5, 6, 7, 8]\n";
            
            try {
                java.nio.file.Files.write(configFile.toPath(), minimalConfig.getBytes());
            } catch (Exception e) {
                // Ignore for now
            }
        }
        
        // Create the BlockConfigService instance
        blockConfigService = new BlockConfigService(plugin);
    }

    @Test
    void testConstructorLoadsBlockConfigs() {
        // Verify that the service was created successfully
        assertNotNull(blockConfigService);
        
        // Verify that block configs were loaded
        assertFalse(blockConfigService.getAllBlockConfigs().isEmpty());
    }

    @Test
    void testGetBlockConfig() {
        // Test getting a specific block config
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig("onJoin");
        
        // Should return null for non-existent configs (since we're using action names, not block IDs)
        assertNull(config);
        
        // Test getting a block config by block ID
        BlockConfigService.BlockConfig diamondConfig = blockConfigService.getBlockConfig("DIAMOND_BLOCK");
        assertNotNull(diamondConfig);
        assertEquals("DIAMOND_BLOCK", diamondConfig.getId());
        assertEquals(Material.DIAMOND_BLOCK, diamondConfig.getMaterial());
        assertEquals("EVENT", diamondConfig.getType());
        assertEquals("onJoin", diamondConfig.getDefaultAction());
    }

    @Test
    void testGetBlockConfigsForMaterial() {
        // Test getting block configs for a material
        List<BlockConfigService.BlockConfig> configs = blockConfigService.getBlockConfigsForMaterial(Material.DIAMOND_BLOCK);
        
        // Should have at least one config for diamond block
        assertFalse(configs.isEmpty());
        assertEquals("DIAMOND_BLOCK", configs.get(0).getId());
    }

    @Test
    void testGetAvailableActions() {
        // Test getting available actions for a material
        List<String> actions = blockConfigService.getActionsForMaterial(Material.DIAMOND_BLOCK);
        
        // Should have at least one action for diamond block
        assertFalse(actions.isEmpty());
        // Check that it contains actual actions, not just block IDs
        assertTrue(actions.contains("onJoin"));
    }

    @Test
    void testIsCodeBlock() {
        // Test checking if a material is a code block
        assertTrue(blockConfigService.isCodeBlock(Material.DIAMOND_BLOCK));
        assertTrue(blockConfigService.isCodeBlock(Material.COBBLESTONE));
        assertTrue(blockConfigService.isCodeBlock(Material.PISTON));
        assertFalse(blockConfigService.isCodeBlock(Material.STONE));
    }

    @Test
    void testGetCodeBlockMaterials() {
        // Test getting all code block materials
        var materials = blockConfigService.getCodeBlockMaterials();
        
        // Should contain our test materials
        assertTrue(materials.contains(Material.DIAMOND_BLOCK));
        assertTrue(materials.contains(Material.COBBLESTONE));
        assertTrue(materials.contains(Material.PISTON));
    }

    @Test
    void testGetBlockConfigByDisplayName() {
        // Test getting block config by display name
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByDisplayName("Event Block");
        
        // Should find the diamond block config
        assertNotNull(config);
        assertEquals("DIAMOND_BLOCK", config.getId());
    }

    @Test
    void testGetFirstBlockConfig() {
        // Test getting the first block config for a material
        BlockConfigService.BlockConfig config = blockConfigService.getFirstBlockConfig(Material.DIAMOND_BLOCK);
        
        // Should return the diamond block config
        assertNotNull(config);
        assertEquals("DIAMOND_BLOCK", config.getId());
    }

    @Test
    void testGetBlockConfigByMaterial() {
        // Test getting block config by material (alias for getFirstBlockConfig)
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(Material.DIAMOND_BLOCK);
        
        // Should return the diamond block config
        assertNotNull(config);
        assertEquals("DIAMOND_BLOCK", config.getId());
    }

    @Test
    void testIsControlOrEventBlock() {
        // Test checking if a block type is control or event
        assertTrue(blockConfigService.isControlOrEventBlock("DIAMOND_BLOCK"));
        assertFalse(blockConfigService.isControlOrEventBlock("COBBLESTONE"));
        assertTrue(blockConfigService.isControlOrEventBlock("PISTON"));
        assertFalse(blockConfigService.isControlOrEventBlock("NON_EXISTENT"));
        assertFalse(blockConfigService.isControlOrEventBlock(null));
    }

    @Test
    void testGetActionConfigurations() {
        // Test getting action configurations
        var actionConfigurations = blockConfigService.getActionConfigurations();
        
        // Should not be null
        assertNotNull(actionConfigurations);
    }

    @Test
    void testGetSlotResolver() {
        // Test getting slot resolver for sendMessage action
        Function<String, Integer> slotResolver = blockConfigService.getSlotResolver("sendMessage");
        
        // Should not be null
        assertNotNull(slotResolver);
        
        // Should resolve known slot names
        Integer messageSlot = slotResolver.apply("message_slot");
        assertNotNull(messageSlot);
        assertEquals(0, messageSlot.intValue());
    }

    @Test
    void testGetSlotResolverForNonExistentAction() {
        // Test getting slot resolver for non-existent action
        Function<String, Integer> slotResolver = blockConfigService.getSlotResolver("nonExistentAction");
        
        // Should return null
        assertNull(slotResolver);
    }

    @Test
    void testGetGroupSlotsResolver() {
        // Test getting group slots resolver for giveItems action
        Function<String, int[]> groupSlotsResolver = blockConfigService.getGroupSlotsResolver("giveItems");
        
        // Should not be null
        assertNotNull(groupSlotsResolver);
        
        // Should resolve known group names
        int[] itemsToGiveSlots = groupSlotsResolver.apply("items_to_give");
        assertNotNull(itemsToGiveSlots);
        assertEquals(9, itemsToGiveSlots.length);
        // Check that slots are 0-8
        for (int i = 0; i < 9; i++) {
            assertEquals(i, itemsToGiveSlots[i]);
        }
    }

    @Test
    void testGetGroupSlotsResolverForNonExistentAction() {
        // Test getting group slots resolver for non-existent action
        Function<String, int[]> groupSlotsResolver = blockConfigService.getGroupSlotsResolver("nonExistentAction");
        
        // Should return null
        assertNull(groupSlotsResolver);
    }

    @Test
    void testReload() {
        // Test reloading the configuration
        assertDoesNotThrow(() -> blockConfigService.reload());
    }
}