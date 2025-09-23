package com.megacreative.coding.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import com.megacreative.core.ServiceRegistry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RegionDetectionSystemTest {
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private CustomEventManager eventManager;
    
    @Mock
    private Player player;
    
    @Mock
    private World world;
    
    @Mock
    private ServiceRegistry serviceRegistry;
    
    private RegionDetectionSystem regionDetectionSystem;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));
        regionDetectionSystem = new RegionDetectionSystem(plugin, eventManager);
    }
    
    @Test
    void testDefineRectangularRegion() {
        // Arrange
        String regionId = "test_region";
        String worldName = "world";
        Location minPoint = new Location(world, 0, 0, 0);
        Location maxPoint = new Location(world, 10, 10, 10);
        String description = "Test Region";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");
        
        // Act
        regionDetectionSystem.defineRegion(regionId, worldName, minPoint, maxPoint, description, metadata);
        
        // Assert
        Region region = regionDetectionSystem.getRegion(regionId);
        assertNotNull(region);
        assertEquals(regionId, region.getId());
        assertEquals(worldName, region.getWorldName());
        assertFalse(region.isCircular());
        assertEquals(description, region.getDescription());
        assertEquals("value", region.getMetadata().get("key"));
    }
    
    @Test
    void testDefineCircularRegion() {
        // Arrange
        String regionId = "circular_region";
        String worldName = "world";
        Location center = new Location(world, 5, 5, 5);
        double radius = 10.0;
        String description = "Circular Test Region";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "circular");
        
        // Act
        regionDetectionSystem.defineCircularRegion(regionId, worldName, center, radius, description, metadata);
        
        // Assert
        Region region = regionDetectionSystem.getRegion(regionId);
        assertNotNull(region);
        assertEquals(regionId, region.getId());
        assertEquals(worldName, region.getWorldName());
        assertTrue(region.isCircular());
        assertEquals(description, region.getDescription());
        assertEquals(radius, region.getRadius());
        assertEquals("circular", region.getMetadata().get("type"));
    }
    
    @Test
    void testRegionContainsLocation() {
        // Arrange
        String regionId = "test_region";
        String worldName = "world";
        Location minPoint = new Location(world, 0, 0, 0);
        Location maxPoint = new Location(world, 10, 10, 10);
        
        regionDetectionSystem.defineRegion(regionId, worldName, minPoint, maxPoint, "Test Region", new HashMap<>());
        
        // Act & Assert
        Location insideLocation = new Location(world, 5, 5, 5);
        Location outsideLocation = new Location(world, 15, 15, 15);
        Location wrongWorldLocation = new Location(mock(World.class), 5, 5, 5);
        
        assertTrue(regionDetectionSystem.getRegion(regionId).contains(insideLocation));
        assertFalse(regionDetectionSystem.getRegion(regionId).contains(outsideLocation));
        assertFalse(regionDetectionSystem.getRegion(regionId).contains(wrongWorldLocation));
    }
    
    @Test
    void testCircularRegionContainsLocation() {
        // Arrange
        String regionId = "circular_region";
        String worldName = "world";
        Location center = new Location(world, 0, 0, 0);
        double radius = 5.0;
        
        regionDetectionSystem.defineCircularRegion(regionId, worldName, center, radius, "Circular Test Region", new HashMap<>());
        
        // Act & Assert
        Location insideLocation = new Location(world, 3, 0, 4); // Distance = 5, within radius
        Location outsideLocation = new Location(world, 4, 0, 4); // Distance = 5.66, outside radius
        
        assertTrue(regionDetectionSystem.getRegion(regionId).contains(insideLocation));
        assertFalse(regionDetectionSystem.getRegion(regionId).contains(outsideLocation));
    }
    
    @Test
    void testRemoveRegion() {
        // Arrange
        String regionId = "test_region";
        String worldName = "world";
        Location minPoint = new Location(world, 0, 0, 0);
        Location maxPoint = new Location(world, 10, 10, 10);
        
        regionDetectionSystem.defineRegion(regionId, worldName, minPoint, maxPoint, "Test Region", new HashMap<>());
        
        // Act
        regionDetectionSystem.removeRegion(regionId);
        
        // Assert
        assertNull(regionDetectionSystem.getRegion(regionId));
    }
    
    @Test
    void testGetRegionsAtLocation() {
        // Arrange
        String region1Id = "region1";
        String region2Id = "region2";
        String worldName = "world";
        Location minPoint1 = new Location(world, 0, 0, 0);
        Location maxPoint1 = new Location(world, 10, 10, 10);
        Location minPoint2 = new Location(world, 5, 5, 5);
        Location maxPoint2 = new Location(world, 15, 15, 15);
        
        regionDetectionSystem.defineRegion(region1Id, worldName, minPoint1, maxPoint1, "Region 1", new HashMap<>());
        regionDetectionSystem.defineRegion(region2Id, worldName, minPoint2, maxPoint2, "Region 2", new HashMap<>());
        
        // Act
        List<String> regionsAtLocation = regionDetectionSystem.getRegionsAtLocation(worldName, new Location(world, 7, 7, 7));
        List<String> regionsAtOutsideLocation = regionDetectionSystem.getRegionsAtLocation(worldName, new Location(world, 20, 20, 20));
        
        // Assert
        assertEquals(2, regionsAtLocation.size());
        assertTrue(regionsAtLocation.contains(region1Id));
        assertTrue(regionsAtLocation.contains(region2Id));
        assertTrue(regionsAtOutsideLocation.isEmpty());
    }
}