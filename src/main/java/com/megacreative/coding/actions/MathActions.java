package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;

import java.util.Random;

/**
 * Mathematical operations and calculations
 * Includes basic arithmetic, advanced math functions, and random number generation
 */
@BlockMeta(id = "mathActions", displayName = "§eMath Actions", type = BlockType.ACTION)
public class MathActions implements BlockAction {
    
    private static final Random RANDOM = new Random();
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get operation type
            DataValue operationValue = block.getParameter("operation");
            if (operationValue == null) {
                return ExecutionResult.error("Operation parameter is required");
            }
            
            ParameterResolver resolver = new ParameterResolver(context);
            String operation = resolver.resolve(context, operationValue).asString();
            
            switch (operation.toLowerCase()) {
                case "add":
                    return performAddition(block, context, resolver);
                    
                case "subtract":
                    return performSubtraction(block, context, resolver);
                    
                case "multiply":
                    return performMultiplication(block, context, resolver);
                    
                case "divide":
                    return performDivision(block, context, resolver);
                    
                case "power":
                    return performPower(block, context, resolver);
                    
                case "sqrt":
                    return performSquareRoot(block, context, resolver);
                    
                case "abs":
                    return performAbsolute(block, context, resolver);
                    
                case "round":
                    return performRound(block, context, resolver);
                    
                case "floor":
                    return performFloor(block, context, resolver);
                    
                case "ceil":
                    return performCeil(block, context, resolver);
                    
                case "random":
                    return generateRandom(block, context, resolver);
                    
                case "min":
                    return findMinimum(block, context, resolver);
                    
                case "max":
                    return findMaximum(block, context, resolver);
                    
                case "sin":
                    return performSin(block, context, resolver);
                    
                case "cos":
                    return performCos(block, context, resolver);
                    
                case "tan":
                    return performTan(block, context, resolver);
                    
                case "log":
                    return performLog(block, context, resolver);
                    
                case "ln":
                    return performLn(block, context, resolver);
                    
                default:
                    return ExecutionResult.error("Unknown math operation: " + operation);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error in math operation: " + e.getMessage());
        }
    }
    
    /**
     * Performs addition operation
     */
    private ExecutionResult performAddition(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue aValue = block.getParameter("a");
            DataValue bValue = block.getParameter("b");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (aValue == null || bValue == null) {
                return ExecutionResult.error("Both operands (a, b) are required for addition");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double a = resolver.resolve(context, aValue).asNumber().doubleValue();
            double b = resolver.resolve(context, bValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = a + b;
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Addition result: " + a + " + " + b + " = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in addition: " + e.getMessage());
        }
    }
    
    /**
     * Performs subtraction operation
     */
    private ExecutionResult performSubtraction(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue aValue = block.getParameter("a");
            DataValue bValue = block.getParameter("b");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (aValue == null || bValue == null) {
                return ExecutionResult.error("Both operands (a, b) are required for subtraction");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double a = resolver.resolve(context, aValue).asNumber().doubleValue();
            double b = resolver.resolve(context, bValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = a - b;
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Subtraction result: " + a + " - " + b + " = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in subtraction: " + e.getMessage());
        }
    }
    
    /**
     * Performs multiplication operation
     */
    private ExecutionResult performMultiplication(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue aValue = block.getParameter("a");
            DataValue bValue = block.getParameter("b");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (aValue == null || bValue == null) {
                return ExecutionResult.error("Both operands (a, b) are required for multiplication");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double a = resolver.resolve(context, aValue).asNumber().doubleValue();
            double b = resolver.resolve(context, bValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = a * b;
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Multiplication result: " + a + " * " + b + " = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in multiplication: " + e.getMessage());
        }
    }
    
    /**
     * Performs division operation
     */
    private ExecutionResult performDivision(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue aValue = block.getParameter("a");
            DataValue bValue = block.getParameter("b");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (aValue == null || bValue == null) {
                return ExecutionResult.error("Both operands (a, b) are required for division");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double a = resolver.resolve(context, aValue).asNumber().doubleValue();
            double b = resolver.resolve(context, bValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            if (b == 0) {
                return ExecutionResult.error("Division by zero is not allowed");
            }
            
            double result = a / b;
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Division result: " + a + " / " + b + " = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in division: " + e.getMessage());
        }
    }
    
    /**
     * Performs power operation
     */
    private ExecutionResult performPower(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue baseValue = block.getParameter("base");
            DataValue exponentValue = block.getParameter("exponent");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (baseValue == null || exponentValue == null) {
                return ExecutionResult.error("Base and exponent parameters are required for power operation");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double base = resolver.resolve(context, baseValue).asNumber().doubleValue();
            double exponent = resolver.resolve(context, exponentValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = Math.pow(base, exponent);
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Power result: " + base + " ^ " + exponent + " = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in power operation: " + e.getMessage());
        }
    }
    
    /**
     * Performs square root operation
     */
    private ExecutionResult performSquareRoot(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue valueValue = block.getParameter("value");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (valueValue == null) {
                return ExecutionResult.error("Value parameter is required for square root");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double value = resolver.resolve(context, valueValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            if (value < 0) {
                return ExecutionResult.error("Cannot calculate square root of negative number");
            }
            
            double result = Math.sqrt(value);
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Square root result: √" + value + " = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in square root: " + e.getMessage());
        }
    }
    
    /**
     * Performs absolute value operation
     */
    private ExecutionResult performAbsolute(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue valueValue = block.getParameter("value");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (valueValue == null) {
                return ExecutionResult.error("Value parameter is required for absolute value");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double value = resolver.resolve(context, valueValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = Math.abs(value);
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Absolute value result: |" + value + "| = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in absolute value: " + e.getMessage());
        }
    }
    
    /**
     * Performs rounding operation
     */
    private ExecutionResult performRound(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue valueValue = block.getParameter("value");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (valueValue == null) {
                return ExecutionResult.error("Value parameter is required for rounding");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double value = resolver.resolve(context, valueValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            long result = Math.round(value);
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Rounding result: round(" + value + ") = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in rounding: " + e.getMessage());
        }
    }
    
    /**
     * Performs floor operation
     */
    private ExecutionResult performFloor(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue valueValue = block.getParameter("value");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (valueValue == null) {
                return ExecutionResult.error("Value parameter is required for floor operation");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double value = resolver.resolve(context, valueValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = Math.floor(value);
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Floor result: floor(" + value + ") = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in floor operation: " + e.getMessage());
        }
    }
    
    /**
     * Performs ceiling operation
     */
    private ExecutionResult performCeil(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue valueValue = block.getParameter("value");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (valueValue == null) {
                return ExecutionResult.error("Value parameter is required for ceiling operation");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double value = resolver.resolve(context, valueValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = Math.ceil(value);
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Ceiling result: ceil(" + value + ") = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in ceiling operation: " + e.getMessage());
        }
    }
    
    /**
     * Generates a random number
     */
    private ExecutionResult generateRandom(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue minVal = block.getParameter("min");
            DataValue maxVal = block.getParameter("max");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double min = minVal != null ? resolver.resolve(context, minVal).asNumber().doubleValue() : 0.0;
            double max = maxVal != null ? resolver.resolve(context, maxVal).asNumber().doubleValue() : 1.0;
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            if (min > max) {
                return ExecutionResult.error("Minimum value cannot be greater than maximum value");
            }
            
            double result = min + (max - min) * RANDOM.nextDouble();
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Random number generated: " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error generating random number: " + e.getMessage());
        }
    }
    
    /**
     * Finds the minimum of two values
     */
    private ExecutionResult findMinimum(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue aValue = block.getParameter("a");
            DataValue bValue = block.getParameter("b");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (aValue == null || bValue == null) {
                return ExecutionResult.error("Both values (a, b) are required for minimum operation");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double a = resolver.resolve(context, aValue).asNumber().doubleValue();
            double b = resolver.resolve(context, bValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = Math.min(a, b);
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Minimum result: min(" + a + ", " + b + ") = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error finding minimum: " + e.getMessage());
        }
    }
    
    /**
     * Finds the maximum of two values
     */
    private ExecutionResult findMaximum(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue aValue = block.getParameter("a");
            DataValue bValue = block.getParameter("b");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (aValue == null || bValue == null) {
                return ExecutionResult.error("Both values (a, b) are required for maximum operation");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double a = resolver.resolve(context, aValue).asNumber().doubleValue();
            double b = resolver.resolve(context, bValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = Math.max(a, b);
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Maximum result: max(" + a + ", " + b + ") = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error finding maximum: " + e.getMessage());
        }
    }
    
    /**
     * Performs sine operation
     */
    private ExecutionResult performSin(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue angleValue = block.getParameter("angle");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (angleValue == null) {
                return ExecutionResult.error("Angle parameter is required for sine operation");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double angle = resolver.resolve(context, angleValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = Math.sin(Math.toRadians(angle));
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Sine result: sin(" + angle + "°) = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in sine operation: " + e.getMessage());
        }
    }
    
    /**
     * Performs cosine operation
     */
    private ExecutionResult performCos(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue angleValue = block.getParameter("angle");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (angleValue == null) {
                return ExecutionResult.error("Angle parameter is required for cosine operation");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double angle = resolver.resolve(context, angleValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = Math.cos(Math.toRadians(angle));
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Cosine result: cos(" + angle + "°) = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in cosine operation: " + e.getMessage());
        }
    }
    
    /**
     * Performs tangent operation
     */
    private ExecutionResult performTan(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue angleValue = block.getParameter("angle");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (angleValue == null) {
                return ExecutionResult.error("Angle parameter is required for tangent operation");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double angle = resolver.resolve(context, angleValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            double result = Math.tan(Math.toRadians(angle));
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Tangent result: tan(" + angle + "°) = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in tangent operation: " + e.getMessage());
        }
    }
    
    /**
     * Performs logarithm (base 10) operation
     */
    private ExecutionResult performLog(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue valueValue = block.getParameter("value");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (valueValue == null) {
                return ExecutionResult.error("Value parameter is required for logarithm operation");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double value = resolver.resolve(context, valueValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            if (value <= 0) {
                return ExecutionResult.error("Logarithm is undefined for non-positive values");
            }
            
            double result = Math.log10(value);
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Logarithm result: log(" + value + ") = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in logarithm operation: " + e.getMessage());
        }
    }
    
    /**
     * Performs natural logarithm operation
     */
    private ExecutionResult performLn(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue valueValue = block.getParameter("value");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (valueValue == null) {
                return ExecutionResult.error("Value parameter is required for natural logarithm operation");
            }
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            double value = resolver.resolve(context, valueValue).asNumber().doubleValue();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            if (value <= 0) {
                return ExecutionResult.error("Natural logarithm is undefined for non-positive values");
            }
            
            double result = Math.log(value);
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Natural logarithm result: ln(" + value + ") = " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Error in natural logarithm operation: " + e.getMessage());
        }
    }
}