# Migration Guide: ScriptExecutor to ScriptEngine

This guide will help you migrate your code from the deprecated `ScriptExecutor` to the new `ScriptEngine` system.

## Key Changes

1. **New Interface**: `ScriptEngine` is now the main interface for script execution
2. **Improved API**: More consistent method names and better error handling
3. **Enhanced Debugging**: Built-in support for step debugging and breakpoints
4. **Better Performance**: Optimized execution with modern Java features

## Migration Steps

### 1. Update Dependencies

Replace any direct dependencies on `ScriptExecutor` with `ScriptEngine`:

```java
// Old
import com.megacreative.coding.ScriptExecutor;

// New
import com.megacreative.coding.ScriptEngine;
```

### 2. Getting the ScriptEngine Instance

```java
// Old
ScriptExecutor executor = plugin.getCodingManager().getScriptExecutor();

// New
ScriptEngine engine = plugin.getServiceRegistry().getScriptEngine();
```

### 3. Executing Scripts

```java
// Old
executor.execute(script, context, "eventName");

// New
engine.executeScript(script, player, "eventName")
    .thenAccept(result -> {
        if (!result.isSuccess()) {
            player.sendMessage("Error: " + result.getError());
        }
    });
```

### 4. Registering Actions and Conditions

```java
// Old
executor.registerAction("action_name", action);
executor.registerCondition("condition_name", condition);

// New
engine.registerAction(BlockType.ACTION_NAME, action);
engine.registerCondition(BlockType.CONDITION_NAME, condition);
```

### 5. Debugging Support

The new system includes enhanced debugging features:

```java
// Pause execution
engine.pauseExecution(executionId);

// Step through code
engine.stepExecution(executionId);

// Resume execution
engine.resumeExecution(executionId);

// Stop execution
engine.stopExecution(executionId);
```

## New Features

### 1. Asynchronous Execution
All script execution is now asynchronous by default, using `CompletableFuture`:

```java
engine.executeScript(script, player, "eventName")
    .thenRun(() -> player.sendMessage("Script completed!"))
    .exceptionally(e -> {
        player.sendMessage("Error: " + e.getMessage());
        return null;
    });
```

### 2. Better Error Handling
Errors now include more context and can be handled more gracefully:

```java
try {
    ExecutionResult result = engine.executeScript(script, player, "eventName").get();
    if (!result.isSuccess()) {
        // Handle error
    }
} catch (Exception e) {
    // Handle exception
}
```

## Deprecated Features

The following features from `ScriptExecutor` have been removed:

1. Direct synchronous execution - use `get()` on the returned `CompletableFuture`
2. Legacy action/condition registration - use the new type-safe methods
3. Direct access to execution threads - use the new control methods instead

## Troubleshooting

### Common Issues

1. **Missing Dependencies**
   Ensure all required dependencies are included in your `pom.xml` or `build.gradle`.

2. **Class Not Found**
   If you see `ClassNotFoundException` for `ScriptExecutor`, update your imports to use `ScriptEngine`.

3. **Method Not Found**
   Some method signatures have changed. Refer to the examples above for the new patterns.

## Need Help?

If you encounter any issues during migration, please:
1. Check the JavaDocs for the new API
2. Review the example code in the documentation
3. Open an issue on our GitHub repository if you need further assistance
