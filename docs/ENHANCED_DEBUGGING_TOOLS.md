# Enhanced Script Debugging Tools

## Overview

The Enhanced Script Debugging Tools provide advanced debugging capabilities for MegaCreative's visual programming system. These tools allow developers to inspect, trace, and analyze script execution in real-time with features like breakpoints, variable watching, execution tracing, and performance analysis.

## Features

### 1. Breakpoint Management

Set breakpoints at specific locations to pause script execution and inspect the current state:

- **Set breakpoints**: Place breakpoints at specific block locations
- **Conditional breakpoints**: Set breakpoints with conditions that must be met
- **List breakpoints**: View all active breakpoints
- **Remove breakpoints**: Clear specific breakpoints

### 2. Variable Watching

Monitor variable values during script execution:

- **Add watchers**: Watch specific variables or expressions
- **List watchers**: View all active variable watchers
- **Remove watchers**: Stop watching specific variables

### 3. Execution Tracing

Track the execution flow of scripts:

- **Start tracing**: Begin recording execution steps
- **Stop tracing**: End execution tracing
- **Show trace**: Display the recorded execution trace

### 4. Performance Analysis

Analyze script performance and identify bottlenecks:

- **Performance reporting**: Generate detailed performance reports
- **Execution timing**: Record execution times for individual blocks
- **Bottleneck identification**: Identify slow-performing blocks

### 5. Advanced Visualization

Visualize script execution with enhanced effects:

- **Block highlighting**: Highlight executing blocks with colored effects
- **Flow tracing**: Show execution flow between blocks
- **Performance mapping**: Color blocks based on performance data
- **Group mapping**: Visualize block group execution

## Command Reference

### Basic Debug Control

```
/debug                    - Toggle debug mode on/off
/debug on                 - Enable debug mode
/debug off                - Disable debug mode
/debug status             - Show debug status
/debug stats              - Show debug statistics
```

### Breakpoint Management

```
/debug breakpoint set [x y z] [condition]  - Set breakpoint at location (or current position)
/debug breakpoint remove <x> <y> <z>       - Remove breakpoint at location
/debug breakpoint list                     - List all breakpoints
```

### Variable Watching

```
/debug watch add <variable> [expression]   - Add variable watcher
/debug watch remove <variable>             - Remove variable watcher
/debug watch list                          - List all variable watchers
```

### Execution Tracing

```
/debug trace start [maxSteps]              - Start execution tracing
/debug trace stop                          - Stop execution tracing
/debug trace show                          - Show execution trace
```

### Performance Analysis

```
/debug performance report                  - Show performance analysis report
```

### Visualization

```
/debug visualize <mode> [on|off]           - Control visualization modes
```

Available visualization modes:
- `block` - Block highlighting
- `flow` - Flow tracing
- `performance` - Performance mapping
- `group` - Group mapping

## Usage Examples

### Setting a Breakpoint

```
# Set breakpoint at current position
/debug breakpoint set

# Set breakpoint at specific coordinates
/debug breakpoint set 10 64 20

# Set conditional breakpoint
/debug breakpoint set 10 64 20 player.health < 10
```

### Watching Variables

```
# Watch a simple variable
/debug watch add player_name

# Watch a complex expression
/debug watch add "player.inventory.size()"
```

### Tracing Execution

```
# Start tracing with default limit
/debug trace start

# Start tracing with custom limit
/debug trace start 500

# Show trace results
/debug trace show
```

### Performance Analysis

```
# Generate performance report
/debug performance report
```

### Visualization

```
# Enable block highlighting
/debug visualize block on

# Enable flow tracing
/debug visualize flow on

# Disable visualization
/debug visualize block off
```

## Technical Implementation

### Classes

- **VisualDebugger**: Main debugging interface with all debugging features
- **AdvancedVisualDebugger**: Enhanced visualization and performance analysis
- **DebugCommand**: Command handler for all debugging functionality
- **BreakpointManager**: Manages breakpoints for players
- **ExecutionTracer**: Tracks execution steps
- **VariableWatcher**: Monitors variable changes
- **PerformanceAnalyzer**: Analyzes script performance

### Integration Points

The debugging system integrates with existing MegaCreative components:

- **ScriptExecutor**: Provides execution data for tracing and performance analysis
- **CodeBlock**: Execution events trigger debugging actions
- **VariableManager**: Variable watching functionality
- **BlockGroupManager**: Group visualization support

## Best Practices

### Debugging Workflow

1. Enable debug mode with `/debug on`
2. Set breakpoints at critical locations
3. Add variable watchers for important values
4. Start execution tracing to track flow
5. Run your script and observe debug output
6. Analyze performance with performance reports
7. Disable debug mode when finished

### Performance Considerations

- Debug mode adds overhead to script execution
- Execution tracing consumes memory for recorded steps
- Visualization effects may impact client performance
- Use selective debugging features rather than enabling everything

## Troubleshooting

### Common Issues

1. **Breakpoints not triggering**: Ensure coordinates match block positions exactly
2. **Variable watchers not showing updates**: Check variable names and scopes
3. **Performance reports empty**: Ensure performance analysis was started during script execution
4. **Visualization not working**: Check that visualization mode is enabled

### Solutions

1. Use `/debug breakpoint list` to verify breakpoint positions
2. Use `/debug watch list` to verify watched variables
3. Ensure scripts are run with debug mode enabled
4. Check that visualization mode is properly set with `/debug visualize <mode> on`