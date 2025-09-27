package com.megacreative.interfaces;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.events.EventPublisher;

import java.util.Map;

/**
 * Interface for creating block actions using annotation-based discovery
 */
public interface IActionFactory extends EventPublisher {
    
    /**
     * Scans for annotated actions and registers them
     */
    void registerAllActions();
    
    /**
     * Creates an action by ID
     * @param actionId Action ID
     * @return BlockAction or null if not found
     */
    BlockAction createAction(String actionId);
    
    /**
     * Gets the display name for an action
     * 
     * @param actionId The action ID
     * @return The display name, or the action ID if no display name is set
     */
    String getActionDisplayName(String actionId);
    
    /**
     * Gets all registered action display names
     * 
     * @return A map of action IDs to display names
     */
    Map<String, String> getActionDisplayNames();
    
    /**
     * Gets the action count
     * @return Number of registered actions
     */
    int getActionCount();
}