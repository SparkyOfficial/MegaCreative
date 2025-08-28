# 🧪 MegaCreative Testing Manual: Visual Programming System

## 📋 **Testing Overview**

This manual provides step-by-step instructions for testing the implemented visual programming system components. Follow these tests to validate that the system is working correctly.

## 🔧 **Setup Requirements**

### Environment Setup
1. **Server**: PaperMC 1.20.4 server
2. **Java**: JDK 21 installed
3. **Plugin**: MegaCreative JAR placed in plugins folder
4. **World**: Dev world with coding platforms at Y=64

### Build & Deploy
```bash
# Build the plugin
mvn clean package

# Copy JAR to server
cp target/megacreative-1.0.0.jar /path/to/server/plugins/

# Start/restart server
```

## 🎯 **Test Suite 1: Block Categorization & Placement Validation**

### Test 1.1: EVENT Block Placement ✅
**Objective**: Verify EVENT blocks can only be placed on blue lines (X=0)

**Steps**:
1. Join dev world: `/dev` or `/world create dev_world DEV`
2. Navigate to coding platform (Y=64)
3. Try to place DIAMOND_BLOCK at coordinates:
   - ✅ **Should SUCCEED**: (0, 64, any_Z) - Blue line
   - ❌ **Should FAIL**: (1, 64, any_Z) - Gray line

**Expected Results**:
- ✅ Success: "Code block placed and auto-connected!"
- ❌ Failure: "Этот тип блока можно ставить только на серые линии!"

### Test 1.2: ACTION Block Placement ✅
**Objective**: Verify ACTION blocks can only be placed on gray lines (X>0)

**Steps**:
1. Try to place COBBLESTONE at coordinates:
   - ❌ **Should FAIL**: (0, 64, any_Z) - Blue line
   - ✅ **Should SUCCEED**: (1, 64, any_Z) - Gray line

**Expected Results**:
- ❌ Failure: "Этот тип блока можно ставить только в начало линии (на синий блок)!"
- ✅ Success: "Code block placed and auto-connected!"

### Test 1.3: Auto-Connection Verification ✅
**Objective**: Verify blocks automatically connect when placed correctly

**Steps**:
1. Place DIAMOND_BLOCK at (0, 64, 100)
2. Place COBBLESTONE at (1, 64, 100)
3. Check server logs for connection messages

**Expected Results**:
- Log: "Auto-connected CodeBlock at [coordinates] for player [name]"
- Log: "Connected horizontal: [from] -> [to]"

## 🎯 **Test Suite 2: Action Selection GUI**

### Test 2.1: New Block Action Selection 🔄
**Objective**: Verify action selection GUI opens for unconfigured blocks

**Steps**:
1. Place DIAMOND_BLOCK (should have action = "Настройка...")
2. Right-click the block
3. Verify GUI opens with available EVENT actions

**Expected Results**:
- GUI title: "§bВыберите действие"
- Available actions for DIAMOND_BLOCK: onJoin, onLeave, onChat, etc.
- Action icons displayed correctly (emerald for onJoin, etc.)

### Test 2.2: Action Selection Callback 🔄
**Objective**: Verify action selection updates the block

**Steps**:
1. Right-click unconfigured DIAMOND_BLOCK
2. Select "onJoin" from GUI
3. Verify block action is updated
4. Right-click again to open parameter GUI

**Expected Results**:
- Message: "§aВыбрано действие: §eonJoin"
- Block action internally set to "onJoin"
- Parameter configuration GUI opens automatically

### Test 2.3: Different Block Types Actions 🔄
**Objective**: Verify different block types show appropriate actions

**Steps**:
1. Place and configure different block types:
   - DIAMOND_BLOCK: Should show EVENT actions
   - COBBLESTONE: Should show ACTION actions  
   - OAK_PLANKS: Should show CONDITION actions

**Expected Results**:
- Each block type shows only its category-appropriate actions
- No ACTION actions in EVENT block GUI, etc.

## 🎯 **Test Suite 3: Parameter Configuration GUI**

### Test 3.1: Parameter GUI Opening 🔄
**Objective**: Verify parameter configuration GUI opens correctly

**Steps**:
1. Configure COBBLESTONE with "sendMessage" action
2. Right-click the block
3. Verify parameter GUI opens with 27 slots

**Expected Results**:
- GUI title: "§8Настройка: sendMessage"
- 27 inventory slots available
- Placeholder items in appropriate slots

### Test 3.2: ItemStack to DataValue Conversion 🔄
**Objective**: Verify parameter conversion works correctly

**Steps**:
1. Open sendMessage parameter GUI
2. Place items in slots:
   - Paper with custom name "Hello ${player:name}!" in slot 0
   - Gold nugget with name "5" in slot 1
   - Lime dye (true) or Red dye (false) in slot 2
3. Close GUI
4. Check conversion results

**Expected Results**:
- Message: "§a✓ Конфигурация блока сохранена! (X предметов)"
- Parameters stored as DataValue objects in CodeBlock
- Type detection works: Paper→TextValue, Gold→NumberValue, Dye→BooleanValue

### Test 3.3: Parameter Persistence 🔄
**Objective**: Verify parameters persist after GUI close

**Steps**:
1. Configure block with parameters
2. Close GUI
3. Reopen parameter GUI
4. Verify items are still there
5. Restart server and check persistence

**Expected Results**:
- Parameters remain in GUI after reopen
- Parameters survive server restart
- Block configuration saved to world data

## 🎯 **Test Suite 4: Script Execution Integration**

### Test 4.1: Simple Script Creation 🔄
**Objective**: Create a working "Welcome Message" script

**Setup Script**:
```
Line 1: [DIAMOND_BLOCK:onJoin] → [COBBLESTONE:sendMessage("Welcome ${player:name}!")]
```

**Steps**:
1. Place DIAMOND_BLOCK at (0, 64, 200)
2. Configure action: "onJoin"
3. Place COBBLESTONE at (1, 64, 200) 
4. Configure action: "sendMessage"
5. Set message parameter: "Welcome ${player:name}!"
6. Save script: `/savescript welcome_test`

**Expected Results**:
- Both blocks configured successfully
- Script saved without errors
- Auto-connection established between blocks

### Test 4.2: Script Execution Test 🎯
**Objective**: Verify the script executes when player joins

**Steps**:
1. Create the welcome script (Test 4.1)
2. Have a test player leave and rejoin the world
3. Check if welcome message is sent

**Expected Results**:
- Player receives: "Welcome [PlayerName]!"
- Placeholder ${player:name} correctly resolved
- Message sent immediately upon join

### Test 4.3: Parameter Resolution Test 🎯
**Objective**: Verify DataValue parameters are read correctly by actions

**Steps**:
1. Create script with multiple parameter types:
   - Text parameter with placeholders
   - Number parameter for delay/amount
   - Boolean parameter for conditions
2. Execute script and verify parameter usage

**Expected Results**:
- Text parameters resolve placeholders correctly
- Number parameters converted to correct values
- Boolean parameters interpreted correctly

## 🎯 **Test Suite 5: Advanced Features**

### Test 5.1: Block Type Categories 🔄
**Objective**: Verify all block types have correct categories

**Check List**:
- ✅ DIAMOND_BLOCK → EVENT category
- ✅ EMERALD_BLOCK → CONTROL category  
- ✅ LAPIS_BLOCK → FUNCTION category
- ✅ COBBLESTONE → ACTION category
- ✅ IRON_BLOCK → ACTION category
- ✅ OAK_PLANKS → CONDITION category
- ✅ OBSIDIAN → CONDITION category

### Test 5.2: Error Handling 🔄
**Objective**: Verify proper error handling and user feedback

**Test Cases**:
1. Try to place wrong block type on wrong line
2. Try to configure block with no available actions
3. Try to open GUI on non-code block
4. Try invalid parameter values

**Expected Results**:
- Clear, helpful error messages in Russian
- No crashes or exceptions
- Graceful fallback behavior

### Test 5.3: Service Integration 🔄
**Objective**: Verify all services work together correctly

**Verification Points**:
- ✅ BlockConfigService loads categories from YAML
- ✅ AutoConnectionManager validates placement
- ✅ BlockPlacementHandler creates CodeBlocks  
- ✅ BlockConfigManager handles GUI workflow
- ✅ ServiceRegistry provides dependencies

## 📊 **Test Results Summary**

### ✅ **Currently Working** (Confirmed)
1. **Block Categorization**: Type-based placement validation
2. **BlockConfigService**: Configuration loading from YAML
3. **AutoConnectionManager**: Position-based rule enforcement
4. **BlockConfigManager**: Enhanced GUI workflow
5. **DataValue Integration**: Type-safe parameter system

### 🔄 **Ready for Testing** (Implementation Complete)
1. **Action Selection GUI**: CodingActionGUI integration
2. **Parameter Configuration**: ItemStack ↔ DataValue conversion
3. **Service Integration**: ServiceRegistry coordination
4. **Error Handling**: User-friendly feedback system

### 🎯 **Needs Validation** (Next Priority)
1. **Script Execution**: End-to-end workflow testing
2. **Event Triggering**: onJoin detection and script activation
3. **Placeholder Resolution**: ${player:name} substitution
4. **Data Persistence**: Cross-restart parameter preservation

## 🚀 **Deployment Checklist**

### Pre-Deployment
- [ ] All core components compile successfully
- [ ] Basic placement validation works
- [ ] GUI workflow functions correctly
- [ ] No critical errors in logs

### Post-Deployment Testing
- [ ] Complete Test Suite 1 (Block Placement)
- [ ] Complete Test Suite 2 (Action Selection)  
- [ ] Complete Test Suite 3 (Parameter Configuration)
- [ ] Complete Test Suite 4 (Script Execution)
- [ ] Complete Test Suite 5 (Advanced Features)

### Production Readiness
- [ ] All test suites pass
- [ ] Performance acceptable
- [ ] User experience smooth
- [ ] Documentation complete

## 🎯 **Next Development Phase**

Based on testing results:

1. **Fix any discovered issues** from testing
2. **Enhance GUI experience** based on user feedback
3. **Add advanced features**: loops, functions, debugging
4. **Performance optimization** for complex scripts
5. **User documentation** and tutorials

---

**The system is now ready for comprehensive testing! 🧪✨**