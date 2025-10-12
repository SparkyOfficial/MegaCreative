package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TimedExecutionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            
            DataValue delayValue = block.getParameter("delay", DataValue.of(20)); 
            DataValue repeatValue = block.getParameter("repeat", DataValue.of(false));
            
            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedDelay = resolver.resolve(context, delayValue);
            DataValue resolvedRepeat = resolver.resolve(context, repeatValue);
            
            int delay = resolvedDelay.asNumber().intValue();
            boolean repeat = resolvedRepeat.asBoolean();
            
            
            CodeBlock nextBlock = block.getNextBlock();
            if (nextBlock == null) {
                return ExecutionResult.error("No block to execute after delay.");
            }
            
            
            ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
            if (scriptEngine == null) {
                return ExecutionResult.error("Script engine not available.");
            }
            
            if (repeat) {
                
                Bukkit.getScheduler().runTaskTimer(context.getPlugin(), () -> {
                    scriptEngine.executeBlockChain(nextBlock, player, "timed_execution");
                }, delay, delay);
                
                return ExecutionResult.success("Repeating execution scheduled with delay of " + delay + " ticks.");
            } else {
                
                Bukkit.getScheduler().runTaskLater(context.getPlugin(), () -> {
                    scriptEngine.executeBlockChain(nextBlock, player, "timed_execution");
                }, delay);
                
                return ExecutionResult.success("Delayed execution scheduled with delay of " + delay + " ticks.");
            }

        } catch (Exception e) {
            return ExecutionResult.error("Error scheduling timed execution: " + e.getMessage());
        }
    }
}