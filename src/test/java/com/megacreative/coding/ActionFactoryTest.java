package com.megacreative.coding;

import com.megacreative.core.DependencyContainer;
import com.megacreative.coding.actions.BlockAction;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.types.ListValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ActionFactory
 */
public class ActionFactoryTest {
    
    @Mock
    private DependencyContainer dependencyContainer;
    
    private ActionFactory actionFactory;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        actionFactory = new ActionFactory(dependencyContainer);
    }
    
    @Test
    public void testActionFactoryCreation() {
        assertNotNull(actionFactory);
        assertTrue(actionFactory.getActionCount() > 0);
    }
    
    @Test
    public void testCreateAction() {
        // Test creating a known action
        BlockAction action = actionFactory.createAction("sendMessage");
        assertNotNull(action);
        
        // Test creating an unknown action
        BlockAction unknownAction = actionFactory.createAction("unknownAction");
        assertNull(unknownAction);
    }
    
    @Test
    public void testParseListString() {
        // Test parsing a simple list
        ListValue list1 = actionFactory.parseListString("item1,item2,item3");
        assertNotNull(list1);
        assertEquals(3, list1.size());
        
        // Test parsing a list with brackets
        ListValue list2 = actionFactory.parseListString("[item1,item2,item3]");
        assertNotNull(list2);
        assertEquals(3, list2.size());
        
        // Test parsing a list with numbers
        ListValue list3 = actionFactory.parseListString("1,2,3");
        assertNotNull(list3);
        assertEquals(3, list3.size());
        
        // Test parsing an empty list
        ListValue list4 = actionFactory.parseListString("");
        assertNotNull(list4);
        assertEquals(0, list4.size());
    }
    
    @Test
    public void testParseRawList() {
        // Test parsing a raw list
        java.util.List<String> rawList = java.util.Arrays.asList("item1", "item2", "item3");
        ListValue list = actionFactory.parseRawList(rawList);
        assertNotNull(list);
        assertEquals(3, list.size());
        
        // Test parsing a null list
        ListValue nullList = actionFactory.parseRawList(null);
        assertNotNull(nullList);
        assertEquals(0, nullList.size());
    }
    
    @Test
    public void testManagerGetters() {
        // Test that managers can be retrieved (will be null since we're using mocks)
        assertNull(actionFactory.getGuiManager());
        assertNull(actionFactory.getInteractiveGuiManager());
        assertNull(actionFactory.getEventManager());
    }
}