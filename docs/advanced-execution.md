# 🎆 FrameLand-Style Advanced Execution Engine Documentation

MegaCreative now features a sophisticated execution engine that provides multiple execution modes for optimal performance and resource management, inspired by FrameLand's advanced execution capabilities.

## Execution Modes

### 1. Synchronous Execution (SYNCHRONOUS)
- **Purpose**: Real-time operations requiring immediate execution
- **Thread**: Main server thread (thread-safe with Bukkit)
- **Use Cases**: Player movement, combat events, critical UI updates
- **Performance**: Fastest for simple operations, can cause lag if overused

```java
// Execute immediately on main thread
enhancedEngine.executeScript(script, player, 
    ExecutionMode.SYNCHRONOUS, Priority.CRITICAL, "player_damage");
```

### 2. Asynchronous Execution (ASYNCHRONOUS)
- **Purpose**: Heavy computations that don't require main thread access
- **Thread**: Dedicated thread pool (4 threads by default)
- **Use Cases**: Complex calculations, file I/O, network requests
- **Performance**: Doesn't block server, ideal for intensive operations

```java
// Execute on background thread pool
enhancedEngine.executeScript(script, player, 
    ExecutionMode.ASYNCHRONOUS, Priority.NORMAL, "data_processing");
```

### 3. Delayed Execution (DELAYED)
- **Purpose**: Timed operations and scheduled events
- **Thread**: Main thread after specified delay
- **Use Cases**: Cooldowns, delayed actions, timed events
- **Performance**: Efficient scheduling with Bukkit's task system

```java
// Execute after 60 ticks (3 seconds)
enhancedEngine.executeScriptDelayed(script, player, 60L, "cooldown_expire");
```

### 4. Batch Execution (BATCH)
- **Purpose**: Processing multiple scripts together efficiently
- **Thread**: Main thread with optimized batching
- **Use Cases**: Mass operations, bulk updates, synchronized actions
- **Performance**: Reduces overhead by grouping operations

```java
// Execute multiple scripts in a single batch
CodeScript[] scripts = {script1, script2, script3};
enhancedEngine.executeScriptsBatch(scripts, player, "bulk_operation");
```

### 5. Prioritized Execution (PRIORITIZED)
- **Purpose**: Advanced scheduling based on operation importance
- **Thread**: Priority-based queue system
- **Use Cases**: Complex systems with mixed priority operations
- **Performance**: Ensures critical operations execute first

```java
// Execute with high priority in queue
enhancedEngine.executeScript(script, player, 
    ExecutionMode.PRIORITIZED, Priority.HIGH, "important_update");
```

## Priority Levels

### Priority.CRITICAL (Level 0)
- **Maximum Instructions**: 2000 per tick
- **Max Tasks per Cycle**: 20
- **Use Cases**: Combat damage, death events, critical UI
- **Execution**: Immediate processing, highest priority

### Priority.HIGH (Level 1)
- **Maximum Instructions**: 1000 per tick
- **Max Tasks per Cycle**: 15
- **Use Cases**: Player join/leave, important events
- **Execution**: Fast processing after critical tasks

### Priority.NORMAL (Level 2)
- **Maximum Instructions**: 500 per tick
- **Max Tasks per Cycle**: 10
- **Use Cases**: Standard gameplay events, chat, commands
- **Execution**: Regular processing queue

### Priority.LOW (Level 3)
- **Maximum Instructions**: 250 per tick
- **Max Tasks per Cycle**: 5
- **Use Cases**: Background tasks, statistics, cleanup
- **Execution**: Processed when resources available

### Priority.IDLE (Level 4)
- **Maximum Instructions**: 125 per tick
- **Max Tasks per Cycle**: 2
- **Use Cases**: Non-essential operations, analytics
- **Execution**: Lowest priority, processed during idle time

## Performance Features

### Automatic Load Balancing
The execution engine automatically adjusts based on server load:

```java
// Check if engine is overloaded
if (enhancedEngine.isOverloaded()) {
    // Reduce non-essential operations
    // Switch to lower priority modes
}

// Monitor current throughput
double opsPerSecond = enhancedEngine.getCurrentThroughput();
```

### Execution Statistics
Comprehensive performance monitoring and analytics:

```java
ExecutionStatistics stats = enhancedEngine.getExecutionStatistics();

// Key metrics
long totalExecutions = stats.getTotalExecutions();
long successfulExecutions = stats.getSuccessfulExecutions();
double successRate = stats.getSuccessRate();
long averageTime = stats.getAverageExecutionTime();
int activeSessions = stats.getActiveSessions();
double throughput = stats.getThroughput();
```

### Resource Management
- **Thread Pool**: 4 dedicated async threads
- **Instruction Limits**: Per-priority instruction caps prevent infinite loops
- **Timeout Protection**: 5-second maximum execution time per script
- **Memory Management**: Automatic cleanup of completed executions

## Enhanced Script Engine Interface

### Convenience Methods
The enhanced engine provides convenient methods for common use cases:

```java
// Critical operations (immediate execution)
enhancedEngine.executeScriptCritical(script, player, "damage_calculation");

// Background operations (async, low priority)
enhancedEngine.executeScriptBackground(script, player, "statistics_update");

// Async operations (async, normal priority)
enhancedEngine.executeScriptAsync(script, player, "data_processing");
```

### Execution Context Enhancement
ExecutionContext now supports advanced execution modes:

```java
ExecutionContext context = new ExecutionContext.Builder()
    .plugin(plugin)
    .player(player)
    .currentBlock(block)
    .executionMode(ExecutionMode.ASYNCHRONOUS)
    .priority(Priority.HIGH)
    .maxInstructions(1500)
    .build();

// Check execution properties
if (context.isTimedOut()) {
    // Handle timeout
}

if (context.isInstructionLimitExceeded()) {
    // Handle instruction limit
}
```

## Command Interface

Use `/execution` command to test and monitor the execution engine:

### Testing Execution Modes
```
/execution test sync       # Test synchronous execution
/execution test async      # Test asynchronous execution
/execution test delayed    # Test delayed execution (2 seconds)
/execution test batch      # Test batch execution (5 scripts)
/execution test critical   # Test critical priority
/execution test background # Test background priority
```

### Performance Monitoring
```
/execution stats           # Show detailed execution statistics
/execution benchmark       # Run performance benchmark (100 operations)
/execution cancel          # Cancel all your active executions
```

### Example Output
```
🎆 Execution Statistics
▪ Total Executions: 1,250
▪ Successful: 1,248 (99.8%)
▪ Failed: 2
▪ Average Time: 12ms
▪ Active Sessions: 3
▪ Active Threads: 2
▪ Throughput: 45.32 ops/sec
✓ Engine performance is normal
```

## Best Practices

### When to Use Each Mode

#### Synchronous (SYNCHRONOUS)
- ✅ Player movement responses
- ✅ Combat damage calculations
- ✅ UI updates and inventory changes
- ❌ Heavy calculations
- ❌ File operations
- ❌ Network requests

#### Asynchronous (ASYNCHRONOUS)
- ✅ Complex math calculations
- ✅ Data processing and analysis
- ✅ File I/O operations
- ✅ Database queries
- ❌ Bukkit API calls (unless thread-safe)
- ❌ Player inventory modifications

#### Delayed (DELAYED)
- ✅ Cooldown timers
- ✅ Scheduled announcements
- ✅ Delayed effects
- ✅ Cleanup operations

#### Batch (BATCH)
- ✅ Mass player updates
- ✅ Bulk data processing
- ✅ Synchronized operations
- ✅ Server startup tasks

### Priority Selection Guidelines

1. **CRITICAL**: Only for operations that must execute immediately
   - Player damage/death
   - Critical error handling
   - Security operations

2. **HIGH**: Important but not critical operations
   - Player join/leave events
   - Important notifications
   - Achievement unlocks

3. **NORMAL**: Standard gameplay operations
   - Chat messages
   - Standard commands
   - Regular events

4. **LOW**: Background operations
   - Statistics collection
   - Data cleanup
   - Non-essential updates

5. **IDLE**: Operations that can wait
   - Analytics
   - Log processing
   - Performance monitoring

### Performance Optimization

#### Script Design
```java
// Good: Light operations in sync mode
enhancedEngine.executeScript(script, player, 
    ExecutionMode.SYNCHRONOUS, Priority.NORMAL, "chat_message");

// Better: Heavy operations in async mode
enhancedEngine.executeScript(script, player, 
    ExecutionMode.ASYNCHRONOUS, Priority.LOW, "complex_calculation");

// Best: Batch related operations
enhancedEngine.executeScriptsBatch(relatedScripts, player, "bulk_update");
```

#### Resource Management
```java
// Monitor performance
if (enhancedEngine.isOverloaded()) {
    // Reduce script frequency
    // Switch to lower priorities
    // Defer non-essential operations
}

// Set appropriate limits
enhancedEngine.setMaxExecutionTime(3000); // 3 seconds
enhancedEngine.setMaxInstructionsPerTick(500); // Reduce for lower-end servers
```

## Migration Guide

### From Basic ScriptEngine
The enhanced engine is fully backward compatible:

```java
// Old way (still works)
scriptEngine.executeScript(script, player, "trigger")
    .thenAccept(result -> handleResult(result));

// New way (enhanced performance)
enhancedEngine.executeScript(script, player, 
    ExecutionMode.ASYNCHRONOUS, Priority.NORMAL, "trigger")
    .thenAccept(result -> handleResult(result));
```

### Updating Existing Scripts
1. **Identify script types**: Categorize scripts by complexity and priority
2. **Choose appropriate modes**: Match execution mode to script requirements
3. **Set proper priorities**: Use priority levels effectively
4. **Monitor performance**: Use execution statistics to optimize

## Troubleshooting

### Common Issues

#### High CPU Usage
- **Cause**: Too many synchronous executions
- **Solution**: Switch heavy operations to async mode
- **Monitoring**: Check throughput and active sessions

#### Script Timeouts
- **Cause**: Infinite loops or excessive calculations
- **Solution**: Add instruction limits and timeout checks
- **Prevention**: Use proper loop controls

#### Memory Leaks
- **Cause**: Accumulating active sessions
- **Solution**: Ensure scripts complete properly
- **Monitoring**: Watch active sessions count

### Debug Commands
```java
// Check execution status
ExecutionStatistics stats = enhancedEngine.getExecutionStatistics();

// Cancel problematic executions
enhancedEngine.cancelPlayerExecutions(player);

// Monitor performance
double throughput = enhancedEngine.getCurrentThroughput();
boolean overloaded = enhancedEngine.isOverloaded();
```

This advanced execution system brings enterprise-level performance and scalability to MegaCreative, enabling sophisticated script execution patterns while maintaining optimal server performance.