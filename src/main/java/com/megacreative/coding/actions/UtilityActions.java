package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Utility actions for various helper functions
 * Includes string operations, system information, and general utilities
 */
@BlockMeta(id = "utilityActions", displayName = "§fUtility Actions", type = BlockType.ACTION)
public class UtilityActions implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get action type
            DataValue actionValue = block.getParameter("action");
            if (actionValue == null) {
                return ExecutionResult.error("Action parameter is required");
            }
            
            ParameterResolver resolver = new ParameterResolver(context);
            String action = resolver.resolve(context, actionValue).asString();
            
            switch (action.toLowerCase()) {
                case "stringoperation":
                    return performStringOperation(block, context, resolver);
                    
                case "getsysteminfo":
                    return getSystemInfo(block, context, resolver);
                    
                case "formattext":
                    return formatText(block, context, resolver);
                    
                case "converttype":
                    return convertType(block, context, resolver);
                    
                case "gettime":
                    return getTime(block, context, resolver);
                    
                case "getplayercount":
                    return getPlayerCount(block, context, resolver);
                    
                default:
                    return ExecutionResult.error("Unknown utility action: " + action);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error in utility action: " + e.getMessage());
        }
    }
    
    /**
     * Performs string operations like concatenation, substring, etc.
     */
    private ExecutionResult performStringOperation(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue operationValue = block.getParameter("operation");
            if (operationValue == null) {
                return ExecutionResult.error("Operation parameter is required");
            }
            
            String operation = resolver.resolve(context, operationValue).asString();
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            switch (operation.toLowerCase()) {
                case "concat":
                    return concatenateStrings(block, context, resolver, resultVariable);
                    
                case "substring":
                    return substringString(block, context, resolver, resultVariable);
                    
                case "uppercase":
                    return uppercaseString(block, context, resolver, resultVariable);
                    
                case "lowercase":
                    return lowercaseString(block, context, resolver, resultVariable);
                    
                case "length":
                    return getStringLength(block, context, resolver, resultVariable);
                    
                default:
                    return ExecutionResult.error("Unknown string operation: " + operation);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error in string operation: " + e.getMessage());
        }
    }
    
    /**
     * Concatenates multiple strings
     */
    private ExecutionResult concatenateStrings(CodeBlock block, ExecutionContext context, ParameterResolver resolver, String resultVariable) {
        try {
            StringBuilder result = new StringBuilder();
            
            // Get all string parameters
            int index = 1;
            DataValue stringValue;
            while ((stringValue = block.getParameter("string" + index)) != null) {
                String str = resolver.resolve(context, stringValue).asString();
                result.append(str);
                index++;
            }
            
            context.setVariable(resultVariable, result.toString());
            return ExecutionResult.success("Strings concatenated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Error concatenating strings: " + e.getMessage());
        }
    }
    
    /**
     * Extracts a substring from a string
     */
    private ExecutionResult substringString(CodeBlock block, ExecutionContext context, ParameterResolver resolver, String resultVariable) {
        try {
            DataValue stringValue = block.getParameter("string");
            DataValue startValue = block.getParameter("start");
            DataValue endValue = block.getParameter("end");
            
            if (stringValue == null) {
                return ExecutionResult.error("String parameter is required");
            }
            
            String str = resolver.resolve(context, stringValue).asString();
            int start = startValue != null ? resolver.resolve(context, startValue).asNumber().intValue() : 0;
            int end = endValue != null ? resolver.resolve(context, endValue).asNumber().intValue() : str.length();
            
            // Ensure bounds are valid
            start = Math.max(0, Math.min(start, str.length()));
            end = Math.max(start, Math.min(end, str.length()));
            
            String result = str.substring(start, end);
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success("Substring extracted successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Error extracting substring: " + e.getMessage());
        }
    }
    
    /**
     * Converts a string to uppercase
     */
    private ExecutionResult uppercaseString(CodeBlock block, ExecutionContext context, ParameterResolver resolver, String resultVariable) {
        try {
            DataValue stringValue = block.getParameter("string");
            if (stringValue == null) {
                return ExecutionResult.error("String parameter is required");
            }
            
            String str = resolver.resolve(context, stringValue).asString();
            String result = str.toUpperCase();
            
            context.setVariable(resultVariable, result);
            return ExecutionResult.success("String converted to uppercase");
        } catch (Exception e) {
            return ExecutionResult.error("Error converting to uppercase: " + e.getMessage());
        }
    }
    
    /**
     * Converts a string to lowercase
     */
    private ExecutionResult lowercaseString(CodeBlock block, ExecutionContext context, ParameterResolver resolver, String resultVariable) {
        try {
            DataValue stringValue = block.getParameter("string");
            if (stringValue == null) {
                return ExecutionResult.error("String parameter is required");
            }
            
            String str = resolver.resolve(context, stringValue).asString();
            String result = str.toLowerCase();
            
            context.setVariable(resultVariable, result);
            return ExecutionResult.success("String converted to lowercase");
        } catch (Exception e) {
            return ExecutionResult.error("Error converting to lowercase: " + e.getMessage());
        }
    }
    
    /**
     * Gets the length of a string
     */
    private ExecutionResult getStringLength(CodeBlock block, ExecutionContext context, ParameterResolver resolver, String resultVariable) {
        try {
            DataValue stringValue = block.getParameter("string");
            if (stringValue == null) {
                return ExecutionResult.error("String parameter is required");
            }
            
            String str = resolver.resolve(context, stringValue).asString();
            int length = str.length();
            
            context.setVariable(resultVariable, length);
            return ExecutionResult.success("String length calculated: " + length);
        } catch (Exception e) {
            return ExecutionResult.error("Error calculating string length: " + e.getMessage());
        }
    }
    
    /**
     * Gets system information
     */
    private ExecutionResult getSystemInfo(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue infoTypeValue = block.getParameter("infoType");
            if (infoTypeValue == null) {
                return ExecutionResult.error("Info type parameter is required");
            }
            
            DataValue resultVar = block.getParameter("resultVariable");
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            String infoType = resolver.resolve(context, infoTypeValue).asString();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            String result;
            switch (infoType.toLowerCase()) {
                case "serverip":
                    try {
                        result = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        result = "Unknown";
                    }
                    break;
                    
                case "playercount":
                    result = String.valueOf(Bukkit.getOnlinePlayers().size());
                    break;
                    
                case "maxplayers":
                    result = String.valueOf(Bukkit.getMaxPlayers());
                    break;
                    
                case "worldcount":
                    result = String.valueOf(Bukkit.getWorlds().size());
                    break;
                    
                case "version":
                    result = Bukkit.getVersion();
                    break;
                    
                default:
                    return ExecutionResult.error("Unknown info type: " + infoType);
            }
            
            context.setVariable(resultVariable, result);
            return ExecutionResult.success("System info retrieved: " + infoType);
        } catch (Exception e) {
            return ExecutionResult.error("Error getting system info: " + e.getMessage());
        }
    }
    
    /**
     * Formats text with various options
     */
    private ExecutionResult formatText(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue textValue = block.getParameter("text");
            DataValue formatValue = block.getParameter("format");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (textValue == null || resultVar == null) {
                return ExecutionResult.error("Text and result variable parameters are required");
            }
            
            String text = resolver.resolve(context, textValue).asString();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            String result = text;
            if (formatValue != null) {
                String format = resolver.resolve(context, formatValue).asString();
                switch (format.toLowerCase()) {
                    case "bold":
                        result = "§l" + text;
                        break;
                    case "italic":
                        result = "§o" + text;
                        break;
                    case "underline":
                        result = "§n" + text;
                        break;
                    case "strikethrough":
                        result = "§m" + text;
                        break;
                }
            }
            
            context.setVariable(resultVariable, result);
            return ExecutionResult.success("Text formatted successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Error formatting text: " + e.getMessage());
        }
    }
    
    /**
     * Converts between data types
     */
    private ExecutionResult convertType(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue valueValue = block.getParameter("value");
            DataValue targetTypeValue = block.getParameter("targetType");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (valueValue == null || targetTypeValue == null || resultVar == null) {
                return ExecutionResult.error("Value, target type, and result variable parameters are required");
            }
            
            DataValue value = resolver.resolve(context, valueValue);
            String targetType = resolver.resolve(context, targetTypeValue).asString();
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            Object result;
            switch (targetType.toLowerCase()) {
                case "string":
                    result = value.asString();
                    break;
                case "number":
                    result = value.asNumber().doubleValue();
                    break;
                case "boolean":
                    result = value.asBoolean();
                    break;
                default:
                    return ExecutionResult.error("Unknown target type: " + targetType);
            }
            
            context.setVariable(resultVariable, result);
            return ExecutionResult.success("Value converted to " + targetType);
        } catch (Exception e) {
            return ExecutionResult.error("Error converting type: " + e.getMessage());
        }
    }
    
    /**
     * Gets the current time
     */
    private ExecutionResult getTime(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue formatValue = block.getParameter("format");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            String format = formatValue != null ? resolver.resolve(context, formatValue).asString() : "yyyy-MM-dd HH:mm:ss";
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            String result = now.format(formatter);
            
            context.setVariable(resultVariable, result);
            return ExecutionResult.success("Current time retrieved");
        } catch (Exception e) {
            return ExecutionResult.error("Error getting time: " + e.getMessage());
        }
    }
    
    /**
     * Gets the current player count
     */
    private ExecutionResult getPlayerCount(CodeBlock block, ExecutionContext context, ParameterResolver resolver) {
        try {
            DataValue resultVar = block.getParameter("resultVariable");
            if (resultVar == null) {
                return ExecutionResult.error("Result variable parameter is required");
            }
            
            String resultVariable = resolver.resolve(context, resultVar).asString();
            int playerCount = Bukkit.getOnlinePlayers().size();
            
            context.setVariable(resultVariable, playerCount);
            return ExecutionResult.success("Player count: " + playerCount);
        } catch (Exception e) {
            return ExecutionResult.error("Error getting player count: " + e.getMessage());
        }
    }
}