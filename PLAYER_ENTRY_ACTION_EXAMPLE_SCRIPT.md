# Player Entry Action Example Script

This example demonstrates how to use the enhanced PlayerEntryAction with container-based item giving in a complete script.

## Script Overview

This script creates a simple game starter that gives players a set of starting items when they join the world. The items are configured visually through a chest placed above the action block.

## Script Setup

### 1. Create the Event Block
- Place a DIAMOND_BLOCK at coordinates (0, 64, 0) in your dev world
- This represents the "onJoin" event

### 2. Create the Action Block
- Place a COBBLESTONE block at coordinates (1, 64, 0) in your dev world
- Set the action to "playerEntry"

### 3. Configure the Action Parameters
- Set `autoGiveItem` to `true`
- (Optional) Set `itemName` to `STONE` and `itemAmount` to `5` as fallback

### 4. Create the Container
- Place a CHEST at coordinates (1, 65, 0) - one block above the action block
- Place items you want to give in the chest:
  - 1x Diamond Sword (with enchantments if desired)
  - 5x Steak
  - 1x Golden Apple
  - 32x Cobblestone
  - 1x Compass

### 5. Connect the Blocks
- Ensure the event block is connected to the action block

## Script Code

Here's the complete script configuration:

```yaml
# Script: starter_kit
# Version: 1.0
# Description: Gives players a starter kit when they join

blocks:
  - id: event_block_1
    type: DIAMOND_BLOCK
    position: [0, 64, 0]
    event: "onJoin"
    
  - id: action_block_1
    type: COBBLESTONE
    position: [1, 64, 0]
    action: "playerEntry"
    parameters:
      autoGiveItem: true
      itemName: "STONE"
      itemAmount: 5
      
  - id: container_block_1
    type: CHEST
    position: [1, 65, 0]
    items:
      - slot: 0
        item: DIAMOND_SWORD
        amount: 1
        enchantments:
          - sharpness: 2
      - slot: 1
        item: STEAK
        amount: 5
      - slot: 2
        item: GOLDEN_APPLE
        amount: 1
      - slot: 3
        item: COBBLESTONE
        amount: 32
      - slot: 4
        item: COMPASS
        amount: 1

connections:
  - from: event_block_1
    to: action_block_1
```

## How It Works

When a player joins the world and uses `/play`:

1. The "onJoin" event triggers
2. The "playerEntry" action executes
3. The system checks for a container above the action block
4. It finds the chest with the configured items
5. It gives all items from the chest to the player
6. The player receives a welcome message: "✓ Добро пожаловать! Вы получили предметы из конфигурации."

## Fallback Behavior

If no chest is found above the action block:
1. The system falls back to parameter-based item giving
2. It gives 5x STONE (as configured in `itemName` and `itemAmount` parameters)
3. If those parameters are not set, it just shows the welcome message

## Benefits

This approach provides:
- Visual configuration through in-game containers
- Easy item setup without complex parameter editing
- Flexibility to give multiple different items at once
- Familiar Minecraft interface for item selection
- Support for enchanted items and custom item properties
- Graceful fallback to parameter-based configuration

## Advanced Configuration

### Multiple Item Stacks
You can place multiple stacks of the same item in different slots of the chest to give different quantities:

- Slot 0: 1x Diamond Sword
- Slot 1: 10x Arrows
- Slot 2: 10x Arrows
- Slot 3: 10x Arrows

This would give the player 1 Diamond Sword and 30 Arrows.

### Enchanted Items
Place enchanted items in the chest to give enchanted items to players:

- Diamond Sword with Sharpness II
- Bow with Power III
- Helmet with Protection I

### Custom Named Items
Place custom named items in the chest to give named items to players:

- "Starter Sword" (Diamond Sword with custom name)
- "Magic Compass" (Compass with custom name)
- "Building Blocks" (Cobblestone with custom name)

## Integration with Other Actions

The PlayerEntryAction can be combined with other actions to create more complex starter experiences:

```yaml
# Extended script with multiple actions
blocks:
  - id: event_block_1
    type: DIAMOND_BLOCK
    position: [0, 64, 0]
    event: "onJoin"
    
  - id: action_block_1
    type: COBBLESTONE
    position: [1, 64, 0]
    action: "playerEntry"
    parameters:
      autoGiveItem: true
      
  - id: action_block_2
    type: STONE
    position: [2, 64, 0]
    action: "sendMessage"
    parameters:
      message: "Welcome to our custom world! Check your inventory for your starter kit."
      
  - id: action_block_3
    type: DIRT
    position: [3, 64, 0]
    action: "teleport"
    parameters:
      location: [100, 65, 100]
      
  - id: container_block_1
    type: CHEST
    position: [1, 65, 0]
    items:
      - item: DIAMOND_SWORD
        amount: 1
      - item: STEAK
        amount: 10

connections:
  - from: event_block_1
    to: action_block_1
  - from: action_block_1
    to: action_block_2
  - from: action_block_2
    to: action_block_3
```

This extended script would:
1. Give the player items from the chest
2. Send them a welcome message
3. Teleport them to a specific location

## Best Practices

### Container Management
- Use descriptive names for your chests to easily identify their purpose
- Keep starter kit chests in a consistent location in your dev world
- Document what items are in each chest for future reference

### Item Balance
- Consider game balance when selecting starter items
- Don't give items that are too powerful for new players
- Provide a mix of tools, resources, and consumables

### Testing
- Test your script in a separate test world before deploying
- Verify that all items are given correctly
- Check that fallback behavior works as expected

### Performance
- Avoid placing too many items in containers as it may impact performance
- Use the minimum number of items needed for the starter experience
- Consider using templates for common starter kits

## Troubleshooting

### Common Issues

1. **No items given to player**
   - Check that `autoGiveItem` is set to `true`
   - Verify that a container exists one block above the action block
   - Ensure the container has items in it
   - Check that the player has inventory space

2. **Wrong items given**
   - Verify the container contents are correct
   - Check that the container is positioned correctly (one block above)
   - Ensure no other actions are interfering

3. **Error messages in console**
   - Look for null pointer exceptions which may indicate missing dependencies
   - Check that all required services are properly initialized
   - Verify that the ServiceRegistry is accessible

### Debugging Tips

1. Use the `/debug` command to enable script debugging
2. Check the server logs for warning messages from PlayerEntryAction
3. Test with simple items first before using complex enchanted items
4. Use the fallback parameters to verify the basic functionality works

## Extending the Functionality

### Custom Container Types
Future enhancements could support:
- Barrels for compact item storage
- Shulker boxes for portable container configurations
- Custom container types with special properties

### Dynamic Item Selection
Future enhancements could support:
- Variable-based item selection
- Conditional item giving based on player properties
- Random item selection from a pool

### Integration with Game Systems
Future enhancements could support:
- Integration with quest systems
- Integration with economy systems
- Integration with progression systems

This example demonstrates the power and flexibility of the enhanced PlayerEntryAction, providing an intuitive way for players to configure automatic item giving through visual container-based configuration.