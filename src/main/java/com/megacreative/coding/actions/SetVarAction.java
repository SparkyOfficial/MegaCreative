package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

public class SetVarAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;
        
        String varName = (String) block.getParameter("variable");
        Object value = block.getParameter("value");
        
        if (varName != null && value != null) {
            context.setVariable(varName, value);
            if (player != null) {
                player.sendMessage("§aПеременная '" + varName + "' установлена: " + value);
            }
        }
    }
} 