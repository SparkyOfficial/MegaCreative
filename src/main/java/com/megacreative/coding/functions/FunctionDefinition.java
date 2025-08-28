package com.megacreative.coding.functions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
public class FunctionDefinition {
    
    private UUID id;
    private String name;
    private String description = "";
    private String author;
    private long createdTime;
    
    private List<FunctionParameter> parameters = new ArrayList<>();
    private ValueType returnType = ValueType.ANY;
    private String returnVariableName = "result";
    
    private CodeBlock entryBlock;
    private List<CodeBlock> codeBlocks = new ArrayList<>();
    
    private boolean isPublic = false;
    private Set<String> tags = new HashSet<>();
    private Map<String, Object> metadata = new HashMap<>();
    
    public FunctionDefinition(String name, String author) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.author = author;
        this.createdTime = System.currentTimeMillis();
    }
    
    public FunctionDefinition addParameter(FunctionParameter parameter) {
        parameters.add(parameter);
        return this;
    }
    
    public FunctionDefinition setReturnType(ValueType returnType, String returnVariableName) {
        this.returnType = returnType;
        this.returnVariableName = returnVariableName != null ? returnVariableName : "result";
        return this;
    }
    
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(returnType.name().toLowerCase()).append(" ");
        sb.append(name).append("(");
        
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            FunctionParameter param = parameters.get(i);
            sb.append(param.getExpectedType().name().toLowerCase());
            sb.append(" ").append(param.getName());
            if (!param.isRequired()) {
                sb.append("?");
            }
        }
        
        sb.append(")");
        return sb.toString();
    }
}