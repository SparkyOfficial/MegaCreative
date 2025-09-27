package com.megacreative.core;

import com.megacreative.MegaCreative;
import com.megacreative.coding.DefaultScriptEngine;
import com.megacreative.interfaces.IScriptEngine;
import com.megacreative.interfaces.IActionFactory;
import com.megacreative.interfaces.IConditionFactory;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test to verify that dependency injection is working correctly
 */
@ExtendWith(MockitoExtension.class)
public class DependencyInjectionTest {
    
    @Mock
    private MegaCreative plugin;
    
    /**
     * Test that ServiceRegistry can be created and provides services correctly
     */
    @Test
    public void testServiceRegistryCreation() {
        // Create dependency container
        DependencyContainer container = new DependencyContainer();
        
        // Create service registry
        ServiceRegistry serviceRegistry = new ServiceRegistry(plugin, container);
        
        // Verify service registry was created
        assertNotNull(serviceRegistry);
    }
    
    /**
     * Test that ServiceRegistry provides the correct script engine interface
     */
    @Test
    public void testScriptEngineInterfaceResolution() {
        // Create dependency container
        DependencyContainer container = new DependencyContainer();
        
        // Create service registry
        ServiceRegistry serviceRegistry = new ServiceRegistry(plugin, container);
        
        // Initialize services
        serviceRegistry.initializeServices();
        
        // Get script engine interface
        IScriptEngine scriptEngine = serviceRegistry.getScriptEngineInterface();
        
        // Verify we got the correct interface
        assertNotNull(scriptEngine);
        assertTrue(scriptEngine instanceof DefaultScriptEngine);
    }
    
    /**
     * Test that ServiceRegistry provides the correct factory interfaces
     */
    @Test
    public void testFactoryInterfaceResolution() {
        // Create dependency container
        DependencyContainer container = new DependencyContainer();
        
        // Create service registry
        ServiceRegistry serviceRegistry = new ServiceRegistry(plugin, container);
        
        // Initialize services
        serviceRegistry.initializeServices();
        
        // Get action factory interface
        IActionFactory actionFactory = serviceRegistry.getActionFactory();
        
        // Verify we got the correct interface
        assertNotNull(actionFactory);
        
        // Get condition factory interface
        IConditionFactory conditionFactory = serviceRegistry.getConditionFactory();
        
        // Verify we got the correct interface
        assertNotNull(conditionFactory);
    }
}