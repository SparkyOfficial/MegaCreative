package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class GetVarAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        CodeBlock block = context.getCurrentBlock();
        if (block == null) return;
        
        String varName = (String) block.getParameter("var");
        if (varName != null) {
            Object value = context.getVariable(varName);
            String stringValue = value != null ? value.toString() : "";
            context.setVariable("lastValue", stringValue);
            
            Player player = context.getPlayer();
            if (player != null) {
                player.sendMessage("§aЗначение переменной '" + varName + "': " + stringValue);
            }
        }
    }
} 