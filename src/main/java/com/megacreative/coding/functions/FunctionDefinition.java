package com.megacreative.coding.functions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import java.util.*;
import java.util.Objects;

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
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    public List<FunctionParameter> getParameters() { return parameters; }
    public void setParameters(List<FunctionParameter> parameters) { this.parameters = parameters; }
    
    public ValueType getReturnType() { return returnType; }
    public void setReturnType(ValueType returnType) { this.returnType = returnType; }
    
    public String getReturnVariableName() { return returnVariableName; }
    public void setReturnVariableName(String returnVariableName) { this.returnVariableName = returnVariableName; }
    
    public CodeBlock getEntryBlock() { return entryBlock; }
    public void setEntryBlock(CodeBlock entryBlock) { this.entryBlock = entryBlock; }
    
    public List<CodeBlock> getCodeBlocks() { return codeBlocks; }
    public void setCodeBlocks(List<CodeBlock> codeBlocks) { this.codeBlocks = codeBlocks; }
    
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }
    
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionDefinition that = (FunctionDefinition) o;
        return createdTime == that.createdTime &&
               isPublic == that.isPublic &&
               Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(author, that.author) &&
               Objects.equals(parameters, that.parameters) &&
               returnType == that.returnType &&
               Objects.equals(returnVariableName, that.returnVariableName) &&
               Objects.equals(entryBlock, that.entryBlock) &&
               Objects.equals(codeBlocks, that.codeBlocks) &&
               Objects.equals(tags, that.tags) &&
               Objects.equals(metadata, that.metadata);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, author, createdTime, parameters, returnType, 
                          returnVariableName, entryBlock, codeBlocks, isPublic, tags, metadata);
    }
    
    // No-args constructor
    public FunctionDefinition() {
        this.id = UUID.randomUUID();
        this.parameters = new ArrayList<>();
        this.codeBlocks = new ArrayList<>();
        this.tags = new HashSet<>();
        this.metadata = new HashMap<>();
    }
    
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