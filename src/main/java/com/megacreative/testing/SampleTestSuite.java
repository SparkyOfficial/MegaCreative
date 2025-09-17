package com.megacreative.testing;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.actions.SetVarAction;
import com.megacreative.coding.conditions.VarEqualsCondition;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;

import java.util.UUID;

/**
 * Sample test suite demonstrating how to create and run tests for MegaCreative scripts
 * This class shows examples of unit tests, integration tests, and performance tests
 */
public class SampleTestSuite {
    
    /**
     * Creates a sample test suite with various test cases
     */
    public static void createSampleTests(MegaCreative plugin, ScriptTestRunner testRunner) {
        // Create a test suite for basic variable operations
        ScriptTestRunner.TestSuite variableSuite = testRunner.createTestSuite("VariableOperations");
        
        // Test case: Setting and getting a variable
        variableSuite.createTestCase("SetAndGetVariable")
            .withScript(createSetVariableScript())
            .addAssertion(ScriptTestRunner.Assertions.variableEquals("testVar", "Hello World"))
            .addAssertion(ScriptTestRunner.Assertions.successful());
            
        // Test case: Variable comparison
        variableSuite.createTestCase("VariableComparison")
            .withScript(createComparisonScript())
            .withVariable("compareVar", 42)
            .addAssertion(ScriptTestRunner.Assertions.successful());
            
        // Create a test suite for math operations
        ScriptTestRunner.TestSuite mathSuite = testRunner.createTestSuite("MathOperations");
        
        // Test case: Basic arithmetic
        mathSuite.createTestCase("BasicArithmetic")
            .withScript(createArithmeticScript())
            .withVariable("num1", 10)
            .withVariable("num2", 5)
            .addAssertion(ScriptTestRunner.Assertions.variableEquals("result", 15))
            .addAssertion(ScriptTestRunner.Assertions.successful());
            
        // Create a test suite for control flow
        ScriptTestRunner.TestSuite controlSuite = testRunner.createTestSuite("ControlFlow");
        
        // Test case: Conditional execution
        controlSuite.createTestCase("ConditionalExecution")
            .withScript(createConditionalScript())
            .withVariable("conditionVar", true)
            .addAssertion(ScriptTestRunner.Assertions.variableEquals("executed", true))
            .addAssertion(ScriptTestRunner.Assertions.successful());
    }
    
    /**
     * Creates a script that sets a variable
     */
    private static CodeScript createSetVariableScript() {
        CodeBlock setVarBlock = new CodeBlock();
        setVarBlock.setId(UUID.randomUUID());
        setVarBlock.setAction("setVar");
        setVarBlock.setMaterial(Material.COBBLESTONE);
        setVarBlock.setParameter("variableName", "testVar");
        setVarBlock.setParameter("variableValue", "Hello World");
        
        return new CodeScript("Set Variable Test", true, setVarBlock);
    }
    
    /**
     * Creates a script that compares variables
     */
    private static CodeScript createComparisonScript() {
        CodeBlock compareBlock = new CodeBlock();
        compareBlock.setId(UUID.randomUUID());
        compareBlock.setAction("varEquals");
        compareBlock.setMaterial(Material.OAK_PLANKS);
        compareBlock.setParameter("variableName", "compareVar");
        compareBlock.setParameter("compareValue", "42");
        
        return new CodeScript("Variable Comparison Test", true, compareBlock);
    }
    
    /**
     * Creates a script that performs arithmetic operations
     */
    private static CodeScript createArithmeticScript() {
        // Add block
        CodeBlock addBlock = new CodeBlock();
        addBlock.setId(UUID.randomUUID());
        addBlock.setAction("addVar");
        addBlock.setMaterial(Material.COBBLESTONE);
        addBlock.setParameter("variableName", "result");
        addBlock.setParameter("value", "%num1%");
        
        // Add another block to add num2
        CodeBlock addBlock2 = new CodeBlock();
        addBlock2.setId(UUID.randomUUID());
        addBlock2.setAction("addVar");
        addBlock2.setMaterial(Material.COBBLESTONE);
        addBlock2.setParameter("variableName", "result");
        addBlock2.setParameter("value", "%num2%");
        
        // Chain the blocks
        addBlock.setNextBlock(addBlock2);
        
        return new CodeScript("Arithmetic Test", true, addBlock);
    }
    
    /**
     * Creates a script with conditional execution
     */
    private static CodeScript createConditionalScript() {
        // Condition block
        CodeBlock conditionBlock = new CodeBlock();
        conditionBlock.setId(UUID.randomUUID());
        conditionBlock.setAction("varEquals");
        conditionBlock.setMaterial(Material.OAK_PLANKS);
        conditionBlock.setParameter("variableName", "conditionVar");
        conditionBlock.setParameter("compareValue", "true");
        
        // Action block to execute if condition is true
        CodeBlock actionBlock = new CodeBlock();
        actionBlock.setId(UUID.randomUUID());
        actionBlock.setAction("setVar");
        actionBlock.setMaterial(Material.COBBLESTONE);
        actionBlock.setParameter("variableName", "executed");
        actionBlock.setParameter("variableValue", "true");
        
        // Chain the blocks
        conditionBlock.setNextBlock(actionBlock);
        
        return new CodeScript("Conditional Test", true, conditionBlock);
    }
}