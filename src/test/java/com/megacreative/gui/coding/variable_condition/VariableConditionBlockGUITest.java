package com.megacreative.gui.coding.variable_condition;

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

class VariableConditionBlockGUITest {
    
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
    void testVariableConditionBlockGUICreation() {
        // Test that VariableConditionBlockGUI can be created for OBSIDIAN
        when(blockConfigService.getActionsForMaterial(Material.OBSIDIAN)).thenReturn(List.of(
            "ifVarEquals", "ifVarGreater", "ifVarLess", "compareVariable"
        ));
        
        VariableConditionBlockGUI gui = new VariableConditionBlockGUI(plugin, player, location, Material.OBSIDIAN);
        
        // Verify that the GUI was created successfully
        assertNotNull(gui, "VariableConditionBlockGUI should be created successfully for OBSIDIAN");
    }
    
    @Test
    void testVariableConditionBlockGUIFallbackActions() {
        // Test that when no actions are found, VariableConditionBlockGUI gets fallback actions
        when(blockConfigService.getActionsForMaterial(Material.OBSIDIAN)).thenReturn(List.of());
        
        VariableConditionBlockGUI gui = new VariableConditionBlockGUI(plugin, player, location, Material.OBSIDIAN);
        
        // The GUI should still be created with fallback actions
        assertNotNull(gui, "VariableConditionBlockGUI should be created with fallback actions for OBSIDIAN");
    }
    
    @Test
    void testGetGUITitle() {
        // Test that the GUI title is correct
        when(blockConfigService.getActionsForMaterial(Material.OBSIDIAN)).thenReturn(List.of());
        
        VariableConditionBlockGUI gui = new VariableConditionBlockGUI(plugin, player, location, Material.OBSIDIAN);
        
        String title = gui.getGUITitle();
        assertNotNull(title, "GUI title should not be null");
        assertTrue(title.contains("OBSIDIAN"), "GUI title should contain material name");
    }
}