package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BroadcastAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (block == null || variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue messageValue = block.getParameter("message");
        if (messageValue != null) {
            DataValue resolvedMessage = resolver.resolve(context, messageValue);
            String message = resolvedMessage.asString();
            
            if (player != null) {
                message = message.replace("%player%", player.getName());
            }
            Bukkit.broadcastMessage(message);
        }
    }
}