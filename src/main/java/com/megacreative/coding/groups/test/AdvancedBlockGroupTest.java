package com.megacreative.coding.groups.test;

import com.megacreative.coding.groups.AdvancedBlockGroup;
import com.megacreative.coding.groups.BlockGroupManager;
import com.megacreative.coding.groups.GroupTemplateManager;
import com.megacreative.coding.CodeBlock;
import com.megacreative.interfaces.IPlayerManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for advanced block group features
 */
public class AdvancedBlockGroupTest {
    
    private BlockGroupManager groupManager;
    private Plugin mockPlugin;
    private IPlayerManager mockPlayerManager;
    
    @BeforeEach
    public void setUp() {
        mockPlugin = Mockito.mock(Plugin.class);
        mockPlayerManager = Mockito.mock(IPlayerManager.class);
        groupManager = new BlockGroupManager(mockPlugin, mockPlayerManager);
    }
    
    @Test
    public void testAdvancedBlockGroupCreation() {
        // Create test data
        UUID ownerId = UUID.randomUUID();
        Map<Location, CodeBlock> blocks = new HashMap<>();
        
        // Create location and mock code block
        World mockWorld = Mockito.mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        
        Location location = new Location(mockWorld, 0, 0, 0);
        CodeBlock mockBlock = Mockito.mock(CodeBlock.class);
        blocks.put(location, mockBlock);
        
        BlockGroupManager.GroupBounds bounds = new BlockGroupManager.GroupBounds(0, 0, 0, 0, 0, 0);
        
        // Create advanced block group
        AdvancedBlockGroup group = new AdvancedBlockGroup(UUID.randomUUID(), "TestGroup", ownerId, blocks, bounds);
        
        // Verify group creation
        assertNotNull(group);
        assertEquals("TestGroup", group.getName());
        assertEquals(ownerId, group.getOwner());
        assertEquals(1, group.getBlocks().size());
        assertFalse(group.isTemplate());
        assertEquals(AdvancedBlockGroup.ExecutionMode.SEQUENTIAL, group.getExecutionMode());
    }
    
    @Test
    public void testAdvancedBlockGroupFeatures() {
        // Create test data
        UUID ownerId = UUID.randomUUID();
        Map<Location, CodeBlock> blocks = new HashMap<>();
        BlockGroupManager.GroupBounds bounds = new BlockGroupManager.GroupBounds(0, 0, 0, 0, 0, 0);
        
        // Create advanced block group
        AdvancedBlockGroup group = new AdvancedBlockGroup(UUID.randomUUID(), "TestGroup", ownerId, blocks, bounds);
        
        // Test metadata functionality
        group.setMetadata("testKey", "testValue");
        assertEquals("testValue", group.getMetadata("testKey", String.class));
        
        // Test tag functionality
        group.addTag("important");
        group.addTag("utility");
        assertTrue(group.hasTag("important"));
        assertTrue(group.hasTag("utility"));
        assertFalse(group.hasTag("nonexistent"));
        
        // Test nested groups
        UUID nestedGroupId = UUID.randomUUID();
        group.addNestedGroup(nestedGroupId);
        assertTrue(group.getNestedGroups().contains(nestedGroupId));
        
        // Test dependencies
        UUID dependencyId = UUID.randomUUID();
        group.addDependency(dependencyId);
        assertTrue(group.getDependencies().contains(dependencyId));
        
        // Test execution control
        group.setExecutionLimit(5);
        assertEquals(5, group.getExecutionLimit());
        
        // Test canExecute method
        assertTrue(group.canExecute()); // Should be true initially
        
        // Test execution counting
        group.incrementExecutionCount();
        assertEquals(1, group.getExecutionCount());
        
        // Test locking
        group.setLocked(true);
        assertTrue(group.isLocked());
        assertFalse(group.canExecute()); // Should be false when locked
    }
    
    @Test
    public void testTemplateFunctionality() {
        // Create test data
        UUID ownerId = UUID.randomUUID();
        Map<Location, CodeBlock> blocks = new HashMap<>();
        BlockGroupManager.GroupBounds bounds = new BlockGroupManager.GroupBounds(0, 0, 0, 0, 0, 0);
        
        // Create advanced block group
        AdvancedBlockGroup group = new AdvancedBlockGroup(UUID.randomUUID(), "TemplateSource", ownerId, blocks, bounds);
        group.addTag("template-source");
        group.setMetadata("author", "test-user");
        
        // Create template from group
        GroupTemplateManager templateManager = groupManager.getTemplateManager();
        GroupTemplateManager.GroupTemplate template = templateManager.createTemplateFromGroup(group, "TestTemplate", "A test template");
        
        // Verify template creation
        assertNotNull(template);
        assertEquals("TestTemplate", template.getName());
        assertEquals("A test template", template.getDescription());
        assertTrue(template.hasTag("template-source"));
        assertEquals("test-user", template.getMetadata("author", String.class));
        
        // Test template instantiation
        UUID newOwnerId = UUID.randomUUID();
        AdvancedBlockGroup instantiatedGroup = template.instantiate("InstantiatedGroup", newOwnerId);
        
        // Verify instantiation
        assertNotNull(instantiatedGroup);
        assertEquals("InstantiatedGroup", instantiatedGroup.getName());
        assertEquals(newOwnerId, instantiatedGroup.getOwner());
        assertTrue(instantiatedGroup.hasTag("template-source"));
        assertEquals("test-user", instantiatedGroup.getMetadata("author", String.class));
    }
    
    @Test
    public void testGroupCopying() {
        // Create test data
        UUID ownerId = UUID.randomUUID();
        Map<Location, CodeBlock> blocks = new HashMap<>();
        BlockGroupManager.GroupBounds bounds = new BlockGroupManager.GroupBounds(0, 0, 0, 0, 0, 0);
        
        // Create advanced block group
        AdvancedBlockGroup originalGroup = new AdvancedBlockGroup(UUID.randomUUID(), "OriginalGroup", ownerId, blocks, bounds);
        originalGroup.addTag("original");
        originalGroup.setMetadata("version", "1.0");
        originalGroup.setExecutionMode(AdvancedBlockGroup.ExecutionMode.PARALLEL);
        originalGroup.setExecutionLimit(10);
        
        // Test copying
        UUID newOwnerId = UUID.randomUUID();
        AdvancedBlockGroup copiedGroup = originalGroup.copy("CopiedGroup", newOwnerId);
        
        // Verify copy
        assertNotNull(copiedGroup);
        assertEquals("CopiedGroup", copiedGroup.getName());
        assertEquals(newOwnerId, copiedGroup.getOwner());
        assertTrue(copiedGroup.hasTag("original"));
        assertEquals("1.0", copiedGroup.getMetadata("version", String.class));
        assertEquals(AdvancedBlockGroup.ExecutionMode.PARALLEL, copiedGroup.getExecutionMode());
        assertEquals(10, copiedGroup.getExecutionLimit());
    }
}