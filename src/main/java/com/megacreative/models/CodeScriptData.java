package com.megacreative.models;

import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlockData;
import java.util.UUID;

/**
 * Safe serializable data transfer object for CodeScript.
 * Contains only serializable data needed for persistence.
 */
public class CodeScriptData {
    public UUID id;
    public String name;
    public boolean enabled;
    public String type;
    public CodeBlockData rootBlock;
    
    public CodeScriptData() {}
    
    public CodeScriptData(CodeScript script) {
        this.id = script.getId();
        this.name = script.getName();
        this.enabled = script.isEnabled();
        this.type = script.getType().name();
        if (script.getRootBlock() != null) {
            this.rootBlock = new CodeBlockData(script.getRootBlock());
        }
    }
}