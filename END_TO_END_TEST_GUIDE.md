# 🚀 Complete End-to-End Testing Guide: MegaCreative Visual Programming

## 🎯 **Objective: Create and Test Your First Working Script**

Create a welcome message script that:
1. **Triggers** when a player joins the world
2. **Sends** a personalized message with player name
3. **Sets** a variable to track visits
4. **Shows** the visit count in the message

---

## 📋 **Prerequisites**

### 🔧 **Server Setup**
1. **PaperMC 1.20.4** server running
2. **MegaCreative JAR** placed in `plugins/` folder
3. **Java 21** runtime
4. Server **started** and plugin loaded successfully

### ✅ **Plugin Status Check**
Run these commands in server console:
```
/plugins
/megacreative status
```
Expected output: `MegaCreative v1.0.0 - ENABLED`

---

## 🏗️ **Step 1: Create Development World**

### 1.1 Create Dev World
```
/create dev
```
**Expected Result**: New development world created with coding platforms

### 1.2 Enter Dev Mode  
```
/dev
```
**Expected Result**: 
- Teleported to dev world
- Received developer inventory with code blocks
- Message: "§aDeveloper mode activated"

### 1.3 Verify Block Types
Check your inventory contains:
- **DIAMOND_BLOCK** - "Событие игрока" (EVENT)
- **COBBLESTONE** - "Действие игрока" (ACTION) 
- **IRON_BLOCK** - "Переменная" (ACTION)

---

## 🧱 **Step 2: Place and Configure Blocks**

### 2.1 Place EVENT Block (DIAMOND_BLOCK)
**Location**: Blue line at coordinates `(0, 64, Z)`

**Action**:
1. Place DIAMOND_BLOCK on blue line
2. **Right-click** the placed block
3. **Action Selection GUI** should open
4. Select **"onJoin"** from the list
5. GUI closes automatically

**Expected Messages**:
- "§a✓ Блок кода размещен: Событие игрока"
- "§7Кликните правой кнопкой для настройки"
- "§aВыбрано действие: §eonJoin"

### 2.2 Place ACTION Block (COBBLESTONE)  
**Location**: Gray line at coordinates `(1, 64, Z)`

**Action**:
1. Place COBBLESTONE next to DIAMOND_BLOCK
2. **Right-click** the placed block
3. **Action Selection GUI** opens
4. Select **"sendMessage"** from the list
5. **Parameter Configuration GUI** opens automatically

**Expected Messages**:
- "§a✓ Блок кода размещен: Действие игрока"
- "§aВыбрано действие: §esendMessage"
- "§e§l!§r §eНастройте блок, поместив предметы в инвентарь."

### 2.3 Configure sendMessage Parameters
**In the Parameter Configuration GUI**:

1. **Slot 0** (message parameter):
   - Place **PAPER** item
   - Rename it to: `"Welcome ${player:name}! Visit #${local:visits}"`
   - Close GUI

**Expected Messages**:
- "§a✓ Конфигурация блока сохранена! (1 предметов)"

### 2.4 Place VARIABLE Block (IRON_BLOCK)
**Location**: Gray line at coordinates `(2, 64, Z)`

**Action**:
1. Place IRON_BLOCK next to COBBLESTONE
2. **Right-click** the placed block  
3. Select **"setVar"** action
4. **Parameter Configuration GUI** opens

### 2.5 Configure setVar Parameters
**In the Parameter Configuration GUI**:

1. **Slot 0** (var parameter):
   - Place **PAPER** item
   - Rename it to: `"visits"`
   
2. **Slot 1** (value parameter):
   - Place **GOLD_NUGGET** item  
   - Rename it to: `"1"`

3. Close GUI

**Expected Messages**:
- "§a✓ Конфигурация блока сохранена! (2 предметов)"

---

## 🔗 **Step 3: Verify Auto-Connection**

### 3.1 Check Block Chain
The blocks should be **automatically connected** in sequence:
```
DIAMOND_BLOCK(onJoin) → COBBLESTONE(sendMessage) → IRON_BLOCK(setVar)
```

### 3.2 Verify Connections
**Visual Check**: Look for connection indicators (particles, lines, or signs)

**Console Check**: Check server logs for:
```
[INFO] Auto-connected CodeBlock at (X,Y,Z) for player PlayerName
```

---

## 💾 **Step 4: Save Script**

### 4.1 Save the Script
```
/savescript welcome_script
```

**Expected Result**:
- "§a✓ Script saved successfully: welcome_script"
- Script file created in plugin data folder

### 4.2 List Scripts (Optional)
```
/scripts
```
**Expected Result**: GUI showing your saved script

---

## 🎮 **Step 5: Test Script Execution**

### 5.1 Switch to Play Mode
```
/play
```
**Expected Result**:
- Teleported to play area
- Development inventory removed
- Message: "§aPlay mode activated"

### 5.2 Test Script Trigger
**Method 1**: Re-join world
```
/hub
/join <your-dev-world-name>
```

**Method 2**: Disconnect and reconnect to server

### 5.3 Expected Script Execution
**Expected Messages** (in order):
1. **Auto-execution message**: Script triggers on join
2. **Welcome message**: "Welcome YourPlayerName! Visit #1"
3. **Variable set message**: "§a✓ Переменная 'visits' (LOCAL, Text) установлена в: 1"

---

## 🔍 **Step 6: Verification and Debugging**

### 6.1 Check Script Status
```
/debug status
```
**Expected Output**: Shows active scripts and execution statistics

### 6.2 Test Multiple Joins
**Action**: Re-join the world multiple times
**Expected Result**: Visit counter should increment each time

### 6.3 Check Variable Storage
```
/debug stats
```
**Expected Output**: Should show stored variables and their values

---

## ✅ **Success Criteria**

### 🎯 **Complete Success Indicators**

1. **Block Placement**: ✅ All blocks placed without validation errors
2. **Action Selection**: ✅ GUI opens and allows action selection
3. **Parameter Configuration**: ✅ GUI accepts and saves parameters  
4. **Auto-Connection**: ✅ Blocks automatically connect in sequence
5. **Script Saving**: ✅ Script successfully saved to file
6. **Event Triggering**: ✅ onJoin event detects player join
7. **Script Execution**: ✅ All actions execute in correct order
8. **Parameter Resolution**: ✅ ${player:name} resolves to actual player name
9. **Variable Management**: ✅ Variables set and retrieved correctly
10. **Message Display**: ✅ Personalized message displayed to player

### 🏆 **Expected Final Output**
```
[Join Event] → [Script Triggers] → [Message Sent] → [Variable Set]
Player sees: "Welcome YourPlayerName! Visit #1"
```

---

## 🛠️ **Troubleshooting**

### ❌ **Common Issues**

#### Issue: "Block placement validation failed"
**Solution**: 
- EVENT blocks must be on blue line (X=0)
- ACTION blocks must be on gray line (X>0)

#### Issue: "Action selection GUI doesn't open"
**Solution**:
- Ensure you're in dev world
- Check you have trusted player permissions
- Verify block is properly placed

#### Issue: "Parameters not saving"
**Solution**:
- Use renamed items (PAPER for text, GOLD_NUGGET for numbers)
- Ensure proper item placement in correct slots
- Check inventory close event triggers save

#### Issue: "Script doesn't execute on join"
**Solution**:
- Verify script is saved with `/savescript`
- Check DIAMOND_BLOCK has "onJoin" action
- Ensure blocks are connected in sequence

#### Issue: "Variables not resolving"
**Solution**:
- Check variable names are correct
- Verify ParameterResolver is working
- Use debug mode to trace execution

### 🔧 **Debug Commands**
```bash
/debug on          # Enable debug mode
/debug stats       # Show execution statistics  
/status            # Check world mode and settings
/scripts           # List all saved scripts
```

---

## 🎉 **Congratulations!**

If you've successfully completed all steps and see the personalized welcome message, you have:

✅ **Built a complete visual programming script using blocks**  
✅ **Verified the entire GUI → Parameter → Execution workflow**  
✅ **Confirmed that the DataValue parameter system works**  
✅ **Tested event triggering and script execution**  
✅ **Validated variable management and placeholder resolution**

**Your MegaCreative visual programming system is fully operational!** 🚀

---

## 📈 **Next Steps**

Now that your core system works, you can:

1. **Create more complex scripts** with multiple conditions
2. **Test different event types** (onChat, onBlockBreak, etc.)
3. **Experiment with different data types** (numbers, booleans, lists)
4. **Build reusable functions** using saveFunction/callFunction blocks
5. **Create script templates** for common patterns
6. **Share scripts** with other players using the template system

**Happy coding with blocks!** 🎮