package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.services.BlockConfigService;
import com.megacreative.interfaces.IActionFactory;
import com.megacreative.interfaces.IConditionFactory;
import com.megacreative.coding.ActionFactory;
import com.megacreative.coding.ConditionFactory;
import org.bukkit.World;
import org.bukkit.Material;
import java.util.List;
import java.util.ArrayList;

/**
 * Test class to verify script compilation and execution
 */
public class ScriptTest {
    
    /**
     * Tests the compilation of scripts in a world
     */
    public static void testScriptCompilation(MegaCreative plugin, World world) {
        try {
            // Create the script compiler
            ScriptCompiler compiler = new ScriptCompiler(
                plugin,
                plugin.getServiceRegistry().getBlockConfigService(),
                plugin.getServiceRegistry().getBlockPlacementHandler()
            );
            
            // Compile all scripts in the world
            List<CodeScript> scripts = compiler.compileWorldScripts(world);
            
            plugin.getLogger().info("Successfully compiled " + scripts.size() + " scripts");
            
            // Print script structure for debugging
            compiler.printScriptStructure(scripts);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error testing script compilation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tests action registration
     */
    public static void testActionRegistration(MegaCreative plugin) {
        try {
            IActionFactory iActionFactory = plugin.getServiceRegistry().getActionFactory();
            ActionFactory actionFactory = (ActionFactory) iActionFactory;
            int actionCount = actionFactory.getActionCount();
            plugin.getLogger().info("Registered " + actionCount + " actions");
            
            // Test creating a few actions
            BlockAction sendMessageAction = actionFactory.createAction("sendMessage");
            if (sendMessageAction != null) {
                plugin.getLogger().info("Successfully created sendMessage action");
            } else {
                plugin.getLogger().warning("Failed to create sendMessage action");
            }
            
            BlockAction teleportAction = actionFactory.createAction("teleport");
            if (teleportAction != null) {
                plugin.getLogger().info("Successfully created teleport action");
            } else {
                plugin.getLogger().warning("Failed to create teleport action");
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error testing action registration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tests condition registration
     */
    public static void testConditionRegistration(MegaCreative plugin) {
        try {
            IConditionFactory iConditionFactory = plugin.getServiceRegistry().getConditionFactory();
            ConditionFactory conditionFactory = (ConditionFactory) iConditionFactory;
            int conditionCount = conditionFactory.getConditionCount();
            plugin.getLogger().info("Registered " + conditionCount + " conditions");
            
            // Test creating a few conditions
            BlockCondition hasItemCondition = conditionFactory.createCondition("hasItem");
            if (hasItemCondition != null) {
                plugin.getLogger().info("Successfully created hasItem condition");
            } else {
                plugin.getLogger().warning("Failed to create hasItem condition");
            }
            
            BlockCondition isOpCondition = conditionFactory.createCondition("isOp");
            if (isOpCondition != null) {
                plugin.getLogger().info("Successfully created isOp condition");
            } else {
                plugin.getLogger().warning("Failed to create isOp condition");
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error testing condition registration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}