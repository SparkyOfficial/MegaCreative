# End-to-End Integration Test Guide

This guide demonstrates the complete workflow of the enhanced BlockConfigManager with action selection GUI.

## Test Scenario: "Welcome Message Script"

### Objective
Create a script that sends a personalized welcome message when a player joins the server.

### Step-by-Step Test Process

#### 1. Block Placement ✅ WORKING
```
Location: Dev world, Line 1
1. Place DIAMOND_BLOCK at (0, 64, Z) - Blue line (EVENT block)
2. Place COBBLESTONE at (1, 64, Z) - Gray line (ACTION block)
```

**Expected Result**: 
- ✅ DIAMOND_BLOCK placed successfully (EVENT category on blue line)
- ✅ COBBLESTONE placed successfully (ACTION category on gray line)
- ✅ Auto-connection established between blocks
- ✅ Placement validation works correctly

#### 2. Action Selection GUI 🔄 NEW FEATURE
```
Interaction: Right-click DIAMOND_BLOCK
```

**Expected Workflow**:
1. Block has no action configured (action = "Настройка...")
2. [showActionSelectionGUI] opens with available EVENT actions:
   - onJoin (При входе игрока)
   - onLeave (При выходе игрока) 
   - onChat (При сообщении в чат)
   - etc.
3. Player selects "onJoin"
4. Block action updates to "onJoin"
5. Automatically opens parameter configuration GUI

#### 3. Parameter Configuration GUI 🔄 ENHANCED
```
Interaction: Configure COBBLESTONE block  
```

**Expected Workflow**:
1. Right-click COBBLESTONE block
2. Block needs action selection first
3. [showActionSelectionGUI] opens with ACTION category actions:
   - sendMessage (Отправить сообщение игроку)
   - teleport (Телепортировать игрока)
   - giveItem (Выдать предмет игроку)
   - etc.
4. Player selects "sendMessage"
5. [showParameterConfigGUI] opens with 27 slots
6. Placeholder items show parameter hints:
   - Slot 0: "Сообщение для отправки" (Paper placeholder)
7. Player places renamed Paper with "Welcome ${player:name}!"
8. GUI closes, parameters converted to DataValue

#### 4. Script Execution 🎯 TARGET
```
Event: Player joins server
Script: onJoin -> sendMessage("Welcome ${player:name}!")
```

**Expected Flow**:
1. Player joins dev world
2. Event system triggers "onJoin" blocks
3. ScriptExecutor processes the script:
   - Finds DIAMOND_BLOCK with action="onJoin"
   - Follows nextBlock connection to COBBLESTONE
   - Executes SendMessageAction with parameters
4. SendMessageAction reads DataValue parameters:
   - message = "Welcome ${player:name}!"
   - Resolves placeholder ${player:name}
5. Player receives: "Welcome PlayerName!"

## Technical Integration Points

### 1. BlockConfigManager Enhancement ✅ COMPLETED
```java
// New workflow in openConfigGUI
if (currentAction == null || currentAction.equals("Настройка...")) {
    showActionSelectionGUI(player, codeBlock, blockLocation);
} else {
    showParameterConfigGUI(player, codeBlock, blockLocation);
}
```

### 2. CodingActionGUI Integration ✅ EXISTING
```java
// Action selection callback
CodingActionGUI actionGUI = new CodingActionGUI(
    player, codeBlock.getMaterial(), blockLocation, availableActions,
    (selectedAction) -> {
        codeBlock.setAction(selectedAction);
        showParameterConfigGUI(player, codeBlock, blockLocation);
    }
);
```

### 3. DataValue Parameter Conversion ✅ IMPLEMENTED
```java
// ItemStack -> DataValue conversion
private DataValue convertItemStackToDataValue(ItemStack item) {
    // Smart detection based on item type and display name
    // Paper -> TextValue
    // Gold Nugget -> NumberValue  
    // Lime/Red Dye -> BooleanValue
    // Chest -> ListValue (future enhancement)
}
```

### 4. BlockConfigService Integration ✅ WORKING
```java
// Available actions from coding_blocks.yml
List<String> availableActions = blockConfigService.getAvailableActions(material);

// Block category validation  
String blockCategory = blockConfigService.getBlockCategory(material);
```

## Testing Checklist

### Phase 1: Block Placement ✅
- [x] EVENT blocks only on blue line (X=0)
- [x] ACTION blocks only on gray line (X>0) 
- [x] Proper error messages for wrong placement
- [x] Auto-connection between placed blocks

### Phase 2: GUI Integration 🔄 IN PROGRESS
- [x] Action selection GUI opens for unconfigured blocks
- [x] Available actions loaded from BlockConfigService
- [x] Action selection updates CodeBlock.action
- [x] Parameter GUI opens after action selection
- [ ] **TO TEST**: End-to-end GUI workflow

### Phase 3: Parameter Management 🔄 IN PROGRESS  
- [x] ItemStack to DataValue conversion
- [x] DataValue to ItemStack conversion
- [x] Placeholder item generation
- [x] Parameter persistence in CodeBlock
- [ ] **TO TEST**: Parameter resolution during execution

### Phase 4: Script Execution 🎯 NEXT MILESTONE
- [ ] ScriptExecutor integration with DataValue parameters
- [ ] Event triggering (onJoin detection)
- [ ] Action execution with resolved parameters
- [ ] Placeholder substitution (${player:name})

## Success Criteria

### ✅ Completed Successfully
1. **Block Categorization**: Position-based placement validation working
2. **Action Selection**: GUI integration with BlockConfigService
3. **Parameter Conversion**: ItemStack ↔ DataValue system implemented
4. **Service Integration**: Proper dependency injection through ServiceRegistry

### 🔄 Next Immediate Goals
1. **Test Complete Workflow**: Place blocks → Configure → Execute
2. **ScriptExecutor Enhancement**: Ensure DataValue parameter reading
3. **Event System Verification**: Confirm onJoin events trigger scripts
4. **Placeholder Resolution**: Test ${player:name} substitution

### 🎯 Final Target
A player can create a complete working script:
1. Place EVENT block on blue line
2. Place ACTION block on gray line  
3. Configure both blocks through GUI
4. Script executes automatically when event occurs
5. Parameters are properly resolved and used

## Key Implementation Files

### Enhanced Files ✅
- `BlockConfigManager.java` - Complete GUI workflow
- `coding_blocks.yml` - Block type categorization
- `BlockConfigService.java` - Category-based validation
- `AutoConnectionManager.java` - Placement validation

### Integration Points 🔄
- `ScriptExecutor.java` - DataValue parameter reading
- `SendMessageAction.java` - Parameter resolution
- `VariableManager.java` - Placeholder substitution
- Event system - Script triggering

The foundation is solid - now it's time to test and refine the complete user experience! 🚀