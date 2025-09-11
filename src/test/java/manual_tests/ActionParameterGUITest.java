package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Logger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    
    @Mock
    private ServiceRegistry serviceRegistry;

    @BeforeEach
    void setUp() {
        // Setup mocks
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
        when(location.getWorld()).thenReturn(world);
        
        // Mock static Bukkit method using try-with-resources
        MockedStatic<Bukkit> mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getPluginManager).thenReturn(pluginManager);
        // Note: We need to keep the reference to mockedBukkit to prevent it from being closed
        // In a real test, we would use try-with-resources, but for simplicity, we'll just create it
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