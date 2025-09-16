package com.megacreative.gui.coding.game_action;

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

class GameActionBlockGUITest {
    
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
    void testGameActionBlockGUICreation() {
        // Test that GameActionBlockGUI can be created for NETHERITE_BLOCK
        when(blockConfigService.getActionsForMaterial(Material.NETHERITE_BLOCK)).thenReturn(List.of(
            "setTime", "setWeather", "setBlock", "explosion", "playSound", "effect", 
            "playParticle", "createScoreboard", "setScore", "incrementScore", 
            "createTeam", "addPlayerToTeam", "saveLocation", "getLocation"
        ));
        
        GameActionBlockGUI gui = new GameActionBlockGUI(plugin, player, location, Material.NETHERITE_BLOCK);
        
        // Verify that the GUI was created successfully
        assertNotNull(gui, "GameActionBlockGUI should be created successfully for NETHERITE_BLOCK");
    }
    
    @Test
    void testGameActionBlockGUIFallbackActions() {
        // Test that when no actions are found, GameActionBlockGUI gets fallback actions
        when(blockConfigService.getActionsForMaterial(Material.NETHERITE_BLOCK)).thenReturn(List.of());
        
        GameActionBlockGUI gui = new GameActionBlockGUI(plugin, player, location, Material.NETHERITE_BLOCK);
        
        // The GUI should still be created with fallback actions
        assertNotNull(gui, "GameActionBlockGUI should be created with fallback actions for NETHERITE_BLOCK");
    }
    
    @Test
    void testGetGUITitle() {
        // Test that the GUI title is correct
        when(blockConfigService.getActionsForMaterial(Material.NETHERITE_BLOCK)).thenReturn(List.of());
        
        GameActionBlockGUI gui = new GameActionBlockGUI(plugin, player, location, Material.NETHERITE_BLOCK);
        
        String title = gui.getGUITitle();
        assertNotNull(title, "GUI title should not be null");
        assertTrue(title.contains("NETHERITE_BLOCK"), "GUI title should contain material name");
    }
}