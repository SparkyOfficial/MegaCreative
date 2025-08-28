# Comprehensive PlayerEntryAction Documentation

## Overview
The enhanced PlayerEntryAction implements a sophisticated workflow that allows players to configure automatic item giving through containers (chests) placed above code blocks in the dev world. This creates an intuitive visual programming experience where players can simply place items in a chest to configure what they'll receive when entering their world.

## Workflow Implementation

### 1. Dev Mode Entry
When a player enters dev mode using `/dev`, they can place code blocks to create their game logic.

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
List<ItemStack> getItemsFromContainer(BlockContainerManager containerManager, 
                                      Location blockLocation) {
    List<ItemStack> items = new ArrayList<>();
    
    try {
        // Check if blockLocation is null
        if (blockLocation == null) {
            logger.warning("Block location is null in getItemsFromContainer");
            return items;
        }
        
        // Clone the location to avoid modifying the original
        Location clonedLocation = blockLocation.clone();
        if (clonedLocation == null) {
            logger.warning("Cloned location is null in getItemsFromContainer");
            return items;
        }
        
        // Get the container for this block
        // The container is placed one block above the code block
        Location containerLocation = clonedLocation.add(0, 1, 0);
        
        // Get the container block (chest, barrel, etc.)
        Block containerBlock = containerLocation.getBlock();
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
4. Error handling for null locations and other edge cases

## Future Enhancements

Potential future enhancements could include:
1. Support for multiple container types (barrels, shulker boxes, etc.)
2. Container-based configuration for other action parameters
3. Visual feedback when containers are detected
4. Template system for pre-configured containers
5. Support for enchanted items and custom item properties
6. Integration with the variable system for dynamic item configuration

## API Reference

### PlayerEntryAction Class
- **Package**: `com.megacreative.coding.actions`
- **Implements**: `BlockAction`
- **Methods**:
  - `execute(ExecutionContext context)`: Main execution method
  - `tryGiveItemsFromContainer(ExecutionContext context, Player player)`: Attempts to give items from a container above the block
  - `getItemsFromContainer(BlockContainerManager containerManager, Location blockLocation)`: Extracts items from a container

### Key Dependencies
- `ServiceRegistry`: For accessing the container manager
- `BlockContainerManager`: For container operations
- `VariableManager`: For parameter resolution
- `ParameterResolver`: For resolving parameter values

## Error Handling
The PlayerEntryAction includes comprehensive error handling:
- Null checks for player, block, and location objects
- Graceful fallback to parameter-based item giving when containers are not found
- Logging of all errors and warnings for debugging
- Exception handling for container access operations

## Performance Considerations
- The action only accesses containers when `autoGiveItem` is enabled
- Container access is optimized with proper null checking
- Item extraction is efficient with early returns for empty containers
- Memory usage is minimized with proper collection management

## Integration with Existing Systems
The PlayerEntryAction integrates seamlessly with:
- The existing action system through the BlockAction interface
- The container system through BlockContainerManager
- The service registry for dependency injection
- The variable system for parameter resolution
- The execution context for accessing game state

## Example Implementation
Here's a complete example of how the PlayerEntryAction works in practice:

```java
public class PlayerEntryAction implements BlockAction {
    private static final Logger logger = Logger.getLogger(PlayerEntryAction.class.getName());
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        // Null checks
        if (player == null || block == null) {
            logger.warning("Player or block is null in PlayerEntryAction");
            return;
        }
        
        // Get variable manager
        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) {
            logger.warning("VariableManager is null in PlayerEntryAction");
            return;
        }
        
        // Resolve parameters
        ParameterResolver resolver = new ParameterResolver(variableManager);
        DataValue autoGiveItem = block.getParameter("autoGiveItem");
        
        if (autoGiveItem != null && resolver.resolve(context, autoGiveItem).asBoolean()) {
            // Try container-based item giving first
            if (tryGiveItemsFromContainer(context, player)) {
                player.sendMessage("§a✓ Добро пожаловать! Вы получили предметы из конфигурации.");
                return;
            }
            
            // Fallback to parameter-based item giving
            // ... (implementation details)
        } else {
            // Standard entry message
            player.sendMessage("§a✓ Добро пожаловать в мир творчества!");
        }
    }
    
    // ... (other methods)
}
```

This implementation provides a robust, flexible, and user-friendly way for players to configure automatic item giving in their custom Minecraft worlds.