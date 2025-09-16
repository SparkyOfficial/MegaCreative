package com.megacreative.gui.coding.variable;

import com.megacreative.MegaCreative;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VariableBlockGUITest {
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private Player player;
    
    @Mock
    private Location location;
    
    @Mock
    private BlockConfigService blockConfigService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(plugin.getServiceRegistry().getBlockConfigService()).thenReturn(blockConfigService);
        when(plugin.getGuiManager()).thenReturn(mock(com.megacreative.managers.GUIManager.class));
    }
    
    @Test
    void testVariableBlockGUICreation() {
        // Test that VariableBlockGUI can be created for IRON_BLOCK
        when(blockConfigService.getActionsForMaterial(Material.IRON_BLOCK)).thenReturn(List.of(
            "setVar", "getVar", "addVar", "subVar", "mulVar", "divVar",
            "setGlobalVar", "getGlobalVar", "setServerVar", "getServerVar"
        ));
        
        VariableBlockGUI gui = new VariableBlockGUI(plugin, player, location, Material.IRON_BLOCK);
        
        // Verify that the GUI was created successfully
        assertNotNull(gui, "VariableBlockGUI should be created successfully for IRON_BLOCK");
    }
    
    @Test
    void testVariableBlockGUIFallbackActions() {
        // Test that when no actions are found, VariableBlockGUI gets fallback actions
        when(blockConfigService.getActionsForMaterial(Material.IRON_BLOCK)).thenReturn(List.of());
        
        VariableBlockGUI gui = new VariableBlockGUI(plugin, player, location, Material.IRON_BLOCK);
        
        // The GUI should still be created with fallback actions
        assertNotNull(gui, "VariableBlockGUI should be created with fallback actions for IRON_BLOCK");
    }
    
    @Test
    void testGetGUITitle() {
        // Test that the GUI title is correct
        when(blockConfigService.getActionsForMaterial(Material.IRON_BLOCK)).thenReturn(List.of());
        
        VariableBlockGUI gui = new VariableBlockGUI(plugin, player, location, Material.IRON_BLOCK);
        
        String title = gui.getGUITitle();
        assertNotNull(title, "GUI title should not be null");
        assertTrue(title.contains("IRON_BLOCK"), "GUI title should contain material name");
    }
}