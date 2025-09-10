package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.logging.Logger;

import static org.mockito.Mockito.*;

class ActionParameterGUITest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private Player player;

    @Mock
    private Location location;

    @Mock
    private BlockConfigService blockConfigService;

    @Mock
    private World world;

    @Mock
    private PluginManager pluginManager;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mocks
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getServiceRegistry().getBlockConfigService()).thenReturn(blockConfigService);
        when(location.getWorld()).thenReturn(world);
        when(Bukkit.getPluginManager()).thenReturn(pluginManager);
    }

    @Test
    void testActionParameterGUICreation() {
        // Test that the ActionParameterGUI can be created without errors
        String actionId = "sendMessage";
        ActionParameterGUI gui = new ActionParameterGUI(plugin, player, location, actionId);
        
        // Verify that the GUI was created successfully
        assert gui != null;
    }
}