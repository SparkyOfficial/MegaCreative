package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.gui.ModuleGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class ModuleGuiTest {
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private Player player;
    
    private ModuleGui moduleGui;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        moduleGui = new ModuleGui(plugin, player, "Test GUI", 3);
    }
    
    @Test
    public void testCreateCategory() {
        // Create a category
        ModuleGui.Category category = moduleGui.addCategory(
            "test_category", 
            "Test Category", 
            Material.DIAMOND, 
            "This is a test category"
        );
        
        // Verify the category was created
        assert category != null;
        assert category.getId().equals("test_category");
        assert category.getDisplayName().equals("Test Category");
        assert category.getMaterial() == Material.DIAMOND;
        assert category.getDescription().equals("This is a test category");
    }
    
    @Test
    public void testAddItemToCategory() {
        // Create a category
        ModuleGui.Category category = moduleGui.addCategory(
            "test_category", 
            "Test Category", 
            Material.DIAMOND, 
            "This is a test category"
        );
        
        // Add an item to the category
        category.addItem(
            Material.STONE, 
            "Test Item", 
            Arrays.asList("This is a test item"), 
            event -> {
                // Handle click
                event.getPlayer().sendMessage("Clicked test item!");
            }
        );
        
        // Verify the item was added
        assert category.getItems().size() == 1;
    }
}