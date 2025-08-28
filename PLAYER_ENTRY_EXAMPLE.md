# Player Entry Action Example

This example demonstrates how to use the enhanced PlayerEntryAction with container-based item giving.

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
  - 1x Diamond
  - 5x Iron Ingots
  - 1x Golden Apple

### 5. Connect the Blocks
- Ensure the event block is connected to the action block

## How It Works

When a player joins the world and uses `/play`:

1. The "onJoin" event triggers
2. The "playerEntry" action executes
3. The system checks for a container above the action block
4. It finds the chest with the configured items
5. It gives all items from the chest to the player
6. The player receives a welcome message

## Fallback Behavior

If no chest is found above the action block:
1. The system falls back to parameter-based item giving
2. It gives the items specified in `itemName` and `itemAmount` parameters
3. If those parameters are not set, it just shows the welcome message

## Benefits

This approach provides:
- Visual configuration through in-game containers
- Easy item setup without complex parameter editing
- Flexibility to give multiple different items at once
- Familiar Minecraft interface for item selection