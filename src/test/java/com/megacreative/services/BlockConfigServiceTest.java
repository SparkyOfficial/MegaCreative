package com.megacreative.services;

import com.megacreative.MegaCreative;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

class BlockConfigServiceTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(plugin.getLogger()).thenReturn(logger);
    }

    @Test
    void testBlockConfigServiceLoadsActionConfigurations() {
        // Create BlockConfigService
        BlockConfigService blockConfigService = new BlockConfigService(plugin);
        
        // Verify that action configurations are loaded
        var actionConfigurations = blockConfigService.getActionConfigurations();
        
        // Should not be null
        assert actionConfigurations != null;
        
        // Should contain configurations for known actions
        assert actionConfigurations.getConfigurationSection("sendMessage") != null;
        assert actionConfigurations.getConfigurationSection("sendTitle") != null;
        assert actionConfigurations.getConfigurationSection("giveItems") != null;
        assert actionConfigurations.getConfigurationSection("setArmor") != null;
    }

    @Test
    void testBlockConfigServiceGetSlotResolver() {
        // Create BlockConfigService
        BlockConfigService blockConfigService = new BlockConfigService(plugin);
        
        // Test slot resolver for sendMessage action
        var slotResolver = blockConfigService.getSlotResolver("sendMessage");
        
        // Should not be null
        assert slotResolver != null;
        
        // Should resolve known slot names
        Integer messageSlot = slotResolver.apply("message_slot");
        assert messageSlot != null;
        assert messageSlot == 0;
    }

    @Test
    void testBlockConfigServiceGetGroupSlotsResolver() {
        // Create BlockConfigService
        BlockConfigService blockConfigService = new BlockConfigService(plugin);
        
        // Test group slots resolver for giveItems action
        var groupSlotsResolver = blockConfigService.getGroupSlotsResolver("giveItems");
        
        // Should not be null
        assert groupSlotsResolver != null;
        
        // Should resolve known group names
        int[] itemsToGiveSlots = groupSlotsResolver.apply("items_to_give");
        assert itemsToGiveSlots != null;
        assert itemsToGiveSlots.length == 9;
        // Check that slots are 0-8
        for (int i = 0; i < 9; i++) {
            assert itemsToGiveSlots[i] == i;
        }
    }
}