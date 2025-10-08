package com.megacreative.coding.script;

import com.megacreative.coding.events.EventAction;
import com.megacreative.coding.events.EventCondition;
import com.megacreative.coding.events.GameEvent;

/**
 * Represents a block in a visual script.
 * A block can be either a condition or an action.
 */
public class ScriptBlock {
    private final ScriptBlockType type;
    private final Object content;
    private ScriptBlock nextBlock;
    private ScriptBlock elseBlock;
    
    public ScriptBlock(ScriptBlockType type, Object content) {
        this.type = type;
        this.content = content;
    }
    
    /**
     * Executes this block with the given event context
     * @return true if execution should continue to the next block,
     *         false if execution should stop
     */
    public boolean execute(GameEvent event) {
        switch (type) {
            case CONDITION:
                if (content instanceof EventCondition) {
                    EventCondition condition = (EventCondition) content;
                    if (condition.check(event)) {
                        return nextBlock == null || nextBlock.execute(event);
                    } else if (elseBlock != null) {
                        return elseBlock.execute(event);
                    }
                }
                return true;
                
            case ACTION:
                if (content instanceof EventAction) {
                    EventAction action = (EventAction) content;
                    action.execute(event);
                    return nextBlock == null || nextBlock.execute(event);
                }
                return true;
                
            default:
                return true;
        }
    }
    
    /**
     * Gets the type of this block
     */
    public ScriptBlockType getType() {
        return type;
    }
    
    /**
     * Gets the content of this block
     */
    public Object getContent() {
        return content;
    }
    
    /**
     * Sets the next block in the script
     */
    public void setNextBlock(ScriptBlock block) {
        this.nextBlock = block;
    }
    
    /**
     * Gets the next block in the script
     */
    public ScriptBlock getNextBlock() {
        return nextBlock;
    }
    
    /**
     * Sets the else block for condition blocks
     */
    public void setElseBlock(ScriptBlock block) {
        this.elseBlock = block;
    }
    
    /**
     * Gets the else block for condition blocks
     */
    public ScriptBlock getElseBlock() {
        return elseBlock;
    }
}