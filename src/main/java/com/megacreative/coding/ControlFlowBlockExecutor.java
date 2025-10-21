package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.variables.VariableManager;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executor for control flow blocks
 * This executor handles CONTROL type blocks (if-else, while, for-each, etc.)
 */
public class ControlFlowBlockExecutor implements BlockExecutor {
    private static final Logger LOGGER = java.util.logging.Logger.getLogger(ControlFlowBlockExecutor.class.getName());
    private static final int MAX_LOOP_ITERATIONS = 10000; 
    
    private final ActionFactory actionFactory;
    private final ConditionFactory conditionFactory;
    
    public ControlFlowBlockExecutor(ActionFactory actionFactory, ConditionFactory conditionFactory) {
        this.actionFactory = actionFactory;
        this.conditionFactory = conditionFactory;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            LOGGER.warning("Attempted to execute null control flow block");
            return ExecutionResult.success("Block is null");
        }
        
        String controlAction = block.getAction();
        if (controlAction == null || controlAction.isEmpty()) {
            LOGGER.warning("Control flow block has null or empty action");
            return ExecutionResult.error("Control action is null or empty");
        }
        
        
        LOGGER.info("Processing control action: " + controlAction + " for player: " + 
                   (context.getPlayer() != null ? context.getPlayer().getName() : "unknown"));
        
        
        switch (controlAction) {
            case "conditionalBranch":
                return handleConditionalBranch(block, context);
            case "else":
                
                LOGGER.fine("ELSE block processed");
                return ExecutionResult.success("ELSE block processed");
            case "whileLoop":
                return handleWhileLoop(block, context);
            case "forEach":
                return handleForEachLoop(block, context);
            case "break":
                LOGGER.fine("BREAK statement processed");
                ExecutionResult breakResult = ExecutionResult.success("BREAK statement processed");
                breakResult.setTerminated(true);
                return breakResult;
            case "continue":
                LOGGER.fine("CONTINUE statement processed");
                ExecutionResult continueResult = ExecutionResult.success("CONTINUE statement processed");
                continueResult.setTerminated(true);
                return continueResult;
            default:
                LOGGER.warning("Unknown control action: " + controlAction);
                return ExecutionResult.error("Unknown control action: " + controlAction);
        }
    }
    
    private ExecutionResult handleConditionalBranch(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue conditionValue = block.getParameter("condition");
            if (conditionValue == null || conditionValue.isEmpty()) {
                LOGGER.warning("Conditional branch has no condition parameter");
                return ExecutionResult.error("Conditional branch has no condition parameter");
            }
            
            String conditionId = conditionValue.asString();
            // According to static analysis, conditionId is never null
            // This is a false positive - we need this check for safety
            // Removed redundant null check - static analysis flagged it as always non-null when this method is called
            
            
            BlockCondition conditionHandler = conditionFactory.createCondition(conditionId);
            if (conditionHandler == null) {
                LOGGER.warning("No condition handler found for: " + conditionId);
                return ExecutionResult.error("Condition handler not found for: " + conditionId);
            }
            
            boolean conditionResult = conditionHandler.evaluate(block, context);
            LOGGER.fine("Conditional branch condition " + conditionId + " evaluated to " + conditionResult);
            
            
            context.setVariable("_condition_result", conditionResult);
            
            
            return new ExecutionResult.Builder()
                .success(true)
                .message("Conditional branch processed, condition " + conditionId + " = " + conditionResult)
                .addDetail("condition_result", conditionResult)
                .build();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception during conditional branch processing: " + e.getMessage(), e);
            return ExecutionResult.error("Exception during conditional branch processing: " + e.getMessage());
        }
    }
    
    private ExecutionResult handleWhileLoop(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue conditionValue = block.getParameter("condition");
            if (conditionValue == null || conditionValue.isEmpty()) {
                LOGGER.warning("While loop has no condition parameter");
                return ExecutionResult.error("While loop has no condition parameter");
            }
            
            String conditionId = conditionValue.asString();
            // According to static analysis, conditionId is never null
            // This is a false positive - we need this check for safety
            // Removed redundant null check - static analysis flagged it as always non-null when this method is called
            
            
            int maxIterations = block.getParameterValue("maxIterations", Integer.class, MAX_LOOP_ITERATIONS);
            
            LOGGER.fine("Processing while loop with condition: " + conditionId + ", max iterations: " + maxIterations);
            
            
            
            return new ExecutionResult.Builder()
                .success(true)
                .message("While loop initialized with condition " + conditionId)
                .addDetail("condition_id", conditionId)
                .addDetail("max_iterations", maxIterations)
                .addDetail("loop_type", "while")
                .build();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception during while loop processing: " + e.getMessage(), e);
            return ExecutionResult.error("Exception during while loop processing: " + e.getMessage());
        }
    }
    
    private ExecutionResult handleForEachLoop(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue conditionValue = block.getParameter("condition");
            if (conditionValue == null || conditionValue.isEmpty()) {
                LOGGER.warning("For-each loop has no condition parameter");
                return ExecutionResult.error("For-each loop has no condition parameter");
            }
            
            String conditionId = conditionValue.asString();
            // Removed redundant null check - static analysis flagged it as always non-null when this method is called
            
            DataValue collectionValue = block.getParameter("collection");
            if (collectionValue == null || collectionValue.isEmpty()) {
                LOGGER.warning("For-each loop has no collection parameter");
                return ExecutionResult.error("For-each loop has no collection parameter");
            }
            
            String collectionName = collectionValue.asString();
            // Removed redundant null check - static analysis flagged it as always non-null when this method is called
            
            DataValue variableValue = block.getParameter("variable");
            if (variableValue == null || variableValue.isEmpty()) {
                LOGGER.warning("For-each loop has no variable parameter");
                return ExecutionResult.error("For-each loop has no variable parameter");
            }
            
            String variableName = variableValue.asString();
            // Removed redundant null check - static analysis flagged it as always non-null when this method is called
            
            LOGGER.fine("Processing for-each loop over collection: " + collectionName + " with variable: " + variableName);
            
            
            
            return new ExecutionResult.Builder()
                .success(true)
                .message("For-each loop initialized over collection " + collectionName)
                .addDetail("collection_name", collectionName)
                .addDetail("variable_name", variableName)
                .addDetail("loop_type", "foreach")
                .build();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception during for-each loop processing: " + e.getMessage(), e);
            return ExecutionResult.error("Exception during for-each loop processing: " + e.getMessage());
        }
    }
}