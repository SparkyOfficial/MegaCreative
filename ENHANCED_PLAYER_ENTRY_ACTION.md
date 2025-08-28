# Enhanced PlayerEntryAction Documentation

## Overview
The enhanced PlayerEntryAction implements the workflow you described where players can configure automatic item giving through containers (chests) placed above code blocks in the dev world.

## Workflow Implementation

### 1. Dev Mode Entry
When a player enters dev mode, they can place code blocks to create their game logic.

### 2. Blue Block Event
The player places a blue block (likely a LAPIS_LAZULI_BLOCK or similar) to represent a player entry event.

### 3. Player Action Configuration
The player adds a "playerEntry" action block that can be configured with parameters:
- `autoGiveItem`: Boolean flag to enable automatic item giving
- `itemName`: The name of the item to give (fallback when no container is found)
- `itemAmount`: The quantity of items to give (fallback when no container is found)

### 4. Container-Based Item Configuration
The key enhancement is the integration with the container system:
1. When a player enables `autoGiveItem`, the system first checks for a container (chest, barrel, etc.) placed one block above the action block
2. If a container is found, it extracts all items from that container and gives them to the player
3. If no container is found, it falls back to the parameter-based item giving

### 5. Play Mode Activation
When the player uses the `/play` command, the script execution engine processes all the configured blocks, including the playerEntry action.

## Technical Implementation

### Container Integration
The PlayerEntryAction now integrates with the BlockContainerManager through the ServiceRegistry:

```java
// Get the service registry from the plugin
ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();

// Get the container manager
BlockContainerManager containerManager = serviceRegistry.getContainerManager();

// Get items from the container inventory directly
List<ItemStack> itemsToGive = getItemsFromContainer(containerManager, context.getCurrentBlock().getLocation());
```

### Item Extraction
The system extracts items directly from container inventories:

```java
private List<ItemStack> getItemsFromContainer(BlockContainerManager containerManager, 
                                              org.bukkit.Location blockLocation) {
    List<ItemStack> items = new ArrayList<>();
    
    try {
        // Get the container for this block
        // The container is placed one block above the code block
        org.bukkit.Location containerLocation = blockLocation.clone().add(0, 1, 0);
        
        // Get the container block (chest, barrel, etc.)
        org.bukkit.block.Block containerBlock = containerLocation.getBlock();
        if (containerBlock.getState() instanceof Container containerState) {
            Inventory inventory = containerState.getInventory();
            
            // Add all non-null items from the inventory
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    items.add(item);
                }
            }
        }
    } catch (Exception e) {
        logger.warning("Error extracting items from container: " + e.getMessage());
    }
    
    return items;
}
```

## Usage Instructions

### For Players
1. Enter dev mode with `/dev`
2. Place a player entry event block (blue/diamond block)
3. Place a player entry action block (cobblestone)
4. Configure the action with `autoGiveItem` set to true
5. Place a chest one block above the action block
6. Put items you want to give in the chest
7. Switch to play mode with `/play`

### For Developers
The PlayerEntryAction follows the same pattern as other actions in the system:
- Implements the BlockAction interface
- Receives an ExecutionContext with all necessary context
- Uses the ServiceRegistry for dependency injection
- Handles errors gracefully with appropriate logging

## Testing

The implementation includes comprehensive tests that verify:
1. Player entry without auto-give functionality
2. Player entry with parameter-based auto-give functionality
3. Player entry with container-based item giving

## Future Enhancements

Potential future enhancements could include:
1. Support for multiple container types (barrels, shulker boxes, etc.)
2. Container-based configuration for other action parameters
3. Visual feedback when containers are detected
4. Template system for pre-configured containers