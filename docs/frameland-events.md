# 🎆 FrameLand-Style Comprehensive Events Documentation

MegaCreative now includes a comprehensive event system inspired by FrameLand, providing extensive event coverage for enhanced scripting capabilities.

## Event Categories

### 1. Player Movement & Interaction Events

#### Player Movement
- `onPlayerMove` - Player moves between blocks
- `onPlayerTeleport` - Player teleports (includes cause and distance)
- `onPlayerToggleSprint` - Player starts/stops sprinting
- `onPlayerToggleSneak` - Player starts/stops sneaking

#### Player Interaction
- `onPlayerInteract` - Player right/left clicks blocks or air
- `onPlayerItemHeld` - Player changes held item slot
- `onPlayerCommand` - Player executes a command

### 2. Combat & Damage Events

#### Combat Events
- `onPlayerDamage` - Player takes damage from any source
- `onPlayerDamaged` - Player receives damage (victim perspective)
- `onPlayerDamagePlayer` - Player damages another player (attacker perspective)
- `onPlayerDeath` - Player dies
- `onPlayerRespawn` - Player respawns

#### Event Data Available
- Damage amount (initial and final)
- Damage cause (fall, entity, fire, etc.)
- Attacker information (if applicable)
- Death message and drops
- Respawn location and type

### 3. Inventory & Item Events

#### Inventory Management
- `onInventoryClick` - Player clicks in inventory
- `onInventoryDrag` - Player drags items in inventory
- `onPlayerDropItem` - Player drops an item
- `onPlayerPickupItem` - Player picks up an item
- `onPlayerItemConsume` - Player eats/drinks an item

#### Event Data Available
- Slot information and types
- Click types and actions
- Item details and quantities
- Cursor item information

### 4. Block & World Events

#### Block Events
- `onBlockPlace` - Player places a block
- `onBlockBreak` - Player breaks a block

#### World Events
- `onWeatherChange` - Weather changes in world
- `onTimeSkip` - Time advances in world

#### Event Data Available
- Block type and location
- Item used for placement
- Weather state changes
- Time skip amount and reason

### 5. Connection Events

#### Player Connection
- `onPlayerJoin` - Player joins the server
- `onPlayerQuit` - Player leaves the server
- `onPlayerKick` - Player is kicked from server

#### World Changes
- `onPlayerChangedWorld` - Player switches between worlds
- `onPlayerPortal` - Player uses a portal

#### Event Data Available
- Join/quit messages
- First-time player detection
- World names (from/to)
- Portal locations and causes

### 6. Experience & Progression Events

#### Level & Experience
- `onPlayerLevelChange` - Player's level changes
- `onPlayerExpChange` - Player gains/loses experience

#### Event Data Available
- Old and new levels
- Experience amounts
- Level differences

### 7. Vehicle & Entity Events

#### Vehicle Interaction
- `onVehicleEnter` - Player enters a vehicle (boat, minecart, etc.)
- `onVehicleExit` - Player exits a vehicle

#### Event Data Available
- Vehicle type and entity
- Vehicle location

## Custom FrameLand Events

### 8. Variable Events

#### Variable Monitoring
- `onVariableChange` - Any monitored variable changes
- `onVariable{VariableName}` - Specific variable changes

#### Usage Example
```java
// Monitor a variable
eventManager.monitorVariable(player, "score", 100);

// When variable changes, events are triggered automatically
```

### 9. Region Events

#### Region Detection
- `onRegionEnter` - Player enters a defined region
- `onRegionLeave` - Player leaves a defined region
- `onRegionEnter{RegionName}` - Specific region enter events
- `onRegionLeave{RegionName}` - Specific region leave events

#### Usage Example
```java
// Define a region
Location corner1 = new Location(world, 0, 0, 0);
Location corner2 = new Location(world, 100, 100, 100);
eventManager.defineRegion("spawn_area", corner1, corner2);

// Events will trigger when players enter/leave the region
```

### 10. Function Events

#### Function Calls
- `onFunctionCall` - Any function is called
- `onFunction{FunctionName}` - Specific function calls

#### Usage Example
```java
// Trigger function call event
eventManager.triggerFunctionCall(player, "calculateDamage", new Object[]{weapon, target});
```

### 11. Score Events

#### Score Tracking
- `onScoreChange` - Any score type changes
- `onScore{ScoreType}` - Specific score type changes

#### Usage Example
```java
// Trigger score change
eventManager.triggerScoreChange(player, "kills", 5, 6, "monster_kill");
```

### 12. Timer Events

#### Timer System
- `onTimerExpire` - Any timer expires
- `onTimer{TimerName}` - Specific timer expiration

#### Usage Example
```java
// Start a timer (20 ticks = 1 second)
eventManager.startTimer(player, "cooldown", 60L, "sword_ability");

// Timer will trigger event when it expires
```

### 13. Custom Action Events

#### Custom Actions
- `onCustomAction` - Any custom action is triggered
- `onCustom{ActionName}` - Specific custom actions

#### Usage Example
```java
Map<String, Object> actionData = new HashMap<>();
actionData.put("item", "magic_sword");
actionData.put("power", 75);

eventManager.triggerCustomAction(player, "spell_cast", actionData);
```

### 14. World Mode Events

#### World Mode Changes
- `onWorldModeChange` - World switches between dev/play modes
- `onWorldMode{ModeName}` - Specific mode activation

#### Usage Example
```java
// Triggered when switching between dev/play worlds
eventManager.triggerWorldModeChange(player, worldId, "DEV", "PLAY");
```

## Event Data Context

Each event provides contextual data that can be accessed in scripts:

### Standard Event Data
- `player` - The player who triggered the event
- `world` - The world where the event occurred
- `location` - The location of the event (if applicable)
- `timestamp` - When the event occurred

### Event-Specific Data
- Combat events include damage amounts, causes, and attackers
- Inventory events include items, slots, and click types
- Movement events include locations and distances
- Custom events include user-defined data

## Performance Features

### High-Performance Event Handling
- **O(1) Event Lookup** - Events are cached by type for instant script finding
- **Async Execution** - Non-critical events execute asynchronously
- **Thread-Safe Operations** - Concurrent event handling without conflicts
- **Event Statistics** - Built-in performance monitoring and analytics

### Event Caching System
```java
// Events are automatically cached for optimal performance
// Cache is rebuilt when worlds/scripts change
frameLandEventManager.refreshEventCaches();
```

### Event Analytics
```java
// Get detailed event execution statistics
Map<String, Object> stats = eventManager.getEventStatistics();

// Example output:
// {
//   "onPlayerJoin": {
//     "count": 150,
//     "total_time_ns": 50000000,
//     "avg_time_ns": 333333,
//     "avg_time_ms": 0.333
//   }
// }
```

## Configuration & Setup

### Automatic Initialization
The FrameLand event system initializes automatically when MegaCreative starts:

```java
// Event manager is available through service registry
FrameLandEventManager eventManager = plugin.getServiceRegistry().getFrameLandEventManager();
```

### Event Registration
Events are automatically registered for all creative worlds. Scripts with matching event blocks will execute when events occur.

### Custom Event Triggering
```java
// Trigger custom events programmatically
eventManager.triggerCustomAction(player, "special_ability", data);
eventManager.triggerScoreChange(player, "points", oldScore, newScore, "quest_complete");
```

## Script Integration

### Event Block Types
Create code blocks with these event actions to respond to events:
- Use diamond blocks for `onPlayerJoin`
- Use redstone blocks for `onPlayerQuit`
- Use TNT blocks for `onPlayerDamage`
- Use chest blocks for `onInventoryClick`
- And many more...

### Event Context in Scripts
Scripts automatically receive event context data through the FrameLand placeholder system:

```
player[name]~ joined the server!
You took apple[damage]~ damage from apple[damage_cause]~
Your score changed from apple[old_score]~ to apple[new_score]~
```

## Best Practices

### Performance Optimization
1. Use specific event types rather than broad ones when possible
2. Keep event scripts lightweight for high-frequency events (onPlayerMove, onTick)
3. Use async execution for non-critical operations
4. Monitor event statistics to identify performance bottlenecks

### Event Organization
1. Group related events together in the same world
2. Use descriptive script names for event handlers
3. Document complex event interactions
4. Test event sequences thoroughly

### Security Considerations
1. Validate event data before processing
2. Limit custom event triggering to authorized players
3. Monitor event execution times and frequencies
4. Implement rate limiting for player-triggered events

## Migration from Basic Events

### Upgrading Existing Scripts
Existing scripts continue to work without changes. New FrameLand events provide additional capabilities:

```
// Old basic event
onJoin -> sendMessage[Welcome!]

// New FrameLand event with context
onPlayerJoin -> sendMessage[Welcome apple[player_name]~! First time: apple[first_play]~]
```

### Enhanced Event Data
FrameLand events provide much richer context data than basic events, enabling more sophisticated script logic and better user experiences.

## Troubleshooting

### Common Issues
1. **Events not triggering**: Check if scripts are enabled and event blocks are correct
2. **Performance issues**: Review event statistics and optimize high-frequency handlers
3. **Custom events not working**: Verify event manager initialization and triggering code

### Debug Tools
```java
// Get event statistics for debugging
Map<String, Object> stats = eventManager.getEventStatistics();

// Reset statistics to track specific issues
eventManager.resetStatistics();

// Check if regions are defined correctly
boolean inRegion = eventManager.isLocationInRegion(location, "region_name");
```

This comprehensive event system brings FrameLand-level functionality to MegaCreative, enabling sophisticated interactive experiences and script-driven gameplay mechanics.