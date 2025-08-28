package com.megacreative.coding.functions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Represents a user-defined function with parameters, return value, and execution logic
 */
@Data
@NoArgsConstructor
public class FunctionDefinition {
    private UUID id;
    private String name;
    private String description;
    private String author;
    private long createdTime;
    
    // Function parameters
    private List<FunctionParameter> parameters = new ArrayList<>();
    
    // Return value configuration
    private ValueType returnType = ValueType.ANY;
    private String returnVariableName = \"result\";
    
    // Function body (code blocks)
    private CodeBlock entryBlock;
    private List<CodeBlock> codeBlocks = new ArrayList<>();
    
    // Function metadata
    private boolean isPublic = false;
    private Set<String> tags = new HashSet<>();
    private Map<String, Object> metadata = new HashMap<>();
    
    public FunctionDefinition(String name, String author) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.author = author;
        this.createdTime = System.currentTimeMillis();
        this.description = \"\";
    }
    
    /**
     * Adds a parameter to this function
     */
    public FunctionDefinition addParameter(FunctionParameter parameter) {
        // Check for duplicate parameter names
        if (parameters.stream().anyMatch(p -> p.getName().equals(parameter.getName()))) {
            throw new IllegalArgumentException(\"Parameter '\" + parameter.getName() + \"' already exists\");
        }
        parameters.add(parameter);
        return this;
    }
    
    /**
     * Adds a simple parameter
     */
    public FunctionDefinition addParameter(String name, ValueType type, boolean required) {
        return addParameter(new FunctionParameter(name, type, required));
    }
    
    /**
     * Adds a parameter with default value
     */
    public FunctionDefinition addParameter(String name, ValueType type, DataValue defaultValue, String description) {
        return addParameter(new FunctionParameter(name, type, defaultValue, description));
    }
    
    /**
     * Sets the return type for this function
     */
    public FunctionDefinition setReturnType(ValueType returnType, String returnVariableName) {
        this.returnType = returnType;
        this.returnVariableName = returnVariableName != null ? returnVariableName : \"result\";
        return this;
    }
    
    /**
     * Gets a parameter by name
     */
    public Optional<FunctionParameter> getParameter(String name) {
        return parameters.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();
    }
    
    /**
     * Validates function call arguments
     */
    public void validateArguments(Map<String, DataValue> arguments) {
        // Check required parameters
        for (FunctionParameter param : parameters) {
            if (param.isRequired() && !arguments.containsKey(param.getName())) {
                throw new IllegalArgumentException(\"Missing required parameter: \" + param.getName());
            }
        }
        
        // Check argument types
        for (Map.Entry<String, DataValue> entry : arguments.entrySet()) {
            String paramName = entry.getKey();
            DataValue value = entry.getValue();
            
            Optional<FunctionParameter> paramOpt = getParameter(paramName);
            if (paramOpt.isEmpty()) {
                throw new IllegalArgumentException(\"Unknown parameter: \" + paramName);
            }
            
            FunctionParameter param = paramOpt.get();
            if (!param.isCompatible(value)) {
                throw new IllegalArgumentException(
                    \"Parameter '\" + paramName + \"' expects \" + param.getExpectedType() + 
                    \" but got \" + (value != null ? value.getType() : \"null\")
                );
            }
        }
    }
    
    /**
     * Prepares arguments for function execution, applying defaults
     */
    public Map<String, DataValue> prepareArguments(Map<String, DataValue> providedArguments) {
        Map<String, DataValue> effectiveArguments = new HashMap<>();
        
        for (FunctionParameter param : parameters) {
            DataValue providedValue = providedArguments.get(param.getName());
            DataValue effectiveValue = param.getEffectiveValue(providedValue);
            effectiveArguments.put(param.getName(), effectiveValue);
        }
        
        return effectiveArguments;
    }
    
    /**
     * Adds a tag to this function
     */
    public FunctionDefinition addTag(String tag) {
        this.tags.add(tag.toLowerCase());
        return this;
    }
    
    /**
     * Checks if function has a specific tag
     */
    public boolean hasTag(String tag) {
        return this.tags.contains(tag.toLowerCase());
    }
    
    /**
     * Sets metadata for this function
     */
    public FunctionDefinition setMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
    
    /**
     * Gets metadata value
     */
    @SuppressWarnings(\"unchecked\")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Gets function signature as string
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(returnType.name().toLowerCase()).append(\" \");
        sb.append(name).append(\"(\");
        
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(\", \");
            FunctionParameter param = parameters.get(i);
            sb.append(param.getExpectedType().name().toLowerCase());
            sb.append(\" \").append(param.getName());
            if (!param.isRequired()) {
                sb.append(\"?\");
            }
        }
        
        sb.append(\")\");
        return sb.toString();
    }
    
    /**
     * Creates a copy of this function definition
     */
    public FunctionDefinition copy() {
        FunctionDefinition copy = new FunctionDefinition(this.name + \"_copy\", this.author);
        copy.setDescription(this.description);
        copy.setReturnType(this.returnType, this.returnVariableName);
        copy.setPublic(this.isPublic);
        
        // Copy parameters
        for (FunctionParameter param : this.parameters) {
            copy.addParameter(new FunctionParameter(
                param.getName(), 
                param.getExpectedType(), 
                param.getDefaultValue(), 
                param.getDescription()
            ));
        }
        
        // Copy tags and metadata
        copy.tags.addAll(this.tags);
        copy.metadata.putAll(this.metadata);
        
        return copy;
    }
}