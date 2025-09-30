package com.megacreative.coding;

import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import java.util.logging.Logger;

/**
 * Executor for control flow blocks
 * This executor handles CONTROL type blocks (if-else, while, for-each, etc.)
 */
public class ControlFlowBlockExecutor implements BlockExecutor {
    private static final Logger LOGGER = java.util.logging.Logger.getLogger(ControlFlowBlockExecutor.class.getName());
    
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
        
        // Log the control action being processed
        LOGGER.info("Processing control action: " + controlAction + " for player: " + 
                   (context.getPlayer() != null ? context.getPlayer().getName() : "unknown"));
        
        // Handle different control flow actions
        switch (controlAction) {
            case "conditionalBranch":
                return handleConditionalBranch(block, context);
            case "else":
                // ELSE block - just continue to next block
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
            // Get the condition to evaluate
            DataValue conditionValue = block.getParameter("condition");
            if (conditionValue == null || conditionValue.isEmpty()) {
                LOGGER.warning("Conditional branch has no condition parameter");
                return ExecutionResult.error("Conditional branch has no condition parameter");
            }
            
            String conditionId = conditionValue.asString();
            if (conditionId == null || conditionId.isEmpty()) {
                LOGGER.warning("Conditional branch has empty condition parameter");
                return ExecutionResult.error("Conditional branch has empty condition parameter");
            }
            
            // Evaluate the condition
            BlockCondition conditionHandler = conditionFactory.createCondition(conditionId);
            if (conditionHandler == null) {
                LOGGER.warning("No condition handler found for: " + conditionId);
                return ExecutionResult.error("Condition handler not found for: " + conditionId);
            }
            
            boolean conditionResult = conditionHandler.evaluate(block, context);
            LOGGER.fine("Conditional branch condition " + conditionId + " evaluated to " + conditionResult);
            
            // Store the condition result in the execution context for use by the ScriptEngine
            context.setVariable("_condition_result", conditionResult);
            
            // Return result with condition evaluation
            return new ExecutionResult.Builder()
                .success(true)
                .message("Conditional branch processed, condition " + conditionId + " = " + conditionResult)
                .addDetail("condition_result", conditionResult)
                .build();
        } catch (Exception e) {
            LOGGER.severe("Exception during conditional branch processing: " + e.getMessage());
            e.printStackTrace();
            return ExecutionResult.error("Exception during conditional branch processing: " + e.getMessage());
        }
    }
    
    private ExecutionResult handleWhileLoop(CodeBlock block, ExecutionContext context) {
        try {
            // Get the condition to evaluate
            DataValue conditionValue = block.getParameter("condition");
            if (conditionValue == null || conditionValue.isEmpty()) {
                LOGGER.warning("While loop has no condition parameter");
                return ExecutionResult.error("While loop has no condition parameter");
            }
            
            String conditionId = conditionValue.asString();
            if (conditionId == null || conditionId.isEmpty()) {
                LOGGER.warning("While loop has empty condition parameter");
                return ExecutionResult.error("While loop has empty condition parameter");
            }
            
            // Get maximum iterations to prevent infinite loops
            int maxIterations = block.getParameterValue("maxIterations", Integer.class, 1000);
            
            LOGGER.fine("Processing while loop with condition: " + conditionId + ", max iterations: " + maxIterations);
            
            // Return result indicating while loop was processed
            return new ExecutionResult.Builder()
                .success(true)
                .message("While loop processed with condition " + conditionId)
                .addDetail("condition_id", conditionId)
                .addDetail("max_iterations", maxIterations)
                .build();
        } catch (Exception e) {
            LOGGER.severe("Exception during while loop processing: " + e.getMessage());
            e.printStackTrace();
            return ExecutionResult.error("Exception during while loop processing: " + e.getMessage());
        }
    }
    
    private ExecutionResult handleForEachLoop(CodeBlock block, ExecutionContext context) {
        try {
            // Get the collection to iterate over
            DataValue collectionValue = block.getParameter("collection");
            if (collectionValue == null || collectionValue.isEmpty()) {
                LOGGER.warning("For-each loop has no collection parameter");
                return ExecutionResult.error("For-each loop has no collection parameter");
            }
            
            String collectionName = collectionValue.asString();
            if (collectionName == null || collectionName.isEmpty()) {
                LOGGER.warning("For-each loop has empty collection parameter");
                return ExecutionResult.error("For-each loop has empty collection parameter");
            }
            
            // Get the variable name to store each item
            DataValue variableValue = block.getParameter("variable");
            if (variableValue == null || variableValue.isEmpty()) {
                LOGGER.warning("For-each loop has no variable parameter");
                return ExecutionResult.error("For-each loop has no variable parameter");
            }
            
            String variableName = variableValue.asString();
            if (variableName == null || variableName.isEmpty()) {
                LOGGER.warning("For-each loop has empty variable parameter");
                return ExecutionResult.error("For-each loop has empty variable parameter");
            }
            
            LOGGER.fine("Processing for-each loop over collection: " + collectionName + " with variable: " + variableName);
            
            // Return result indicating for-each loop was processed
            return new ExecutionResult.Builder()
                .success(true)
                .message("For-each loop processed over collection " + collectionName)
                .addDetail("collection_name", collectionName)
                .addDetail("variable_name", variableName)
                .build();
        } catch (Exception e) {
            LOGGER.severe("Exception during for-each loop processing: " + e.getMessage());
            e.printStackTrace();
            return ExecutionResult.error("Exception during for-each loop processing: " + e.getMessage());
        }
    }
}