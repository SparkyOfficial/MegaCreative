package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.MegaCreative;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.coding.functions.FunctionDefinition;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SaveFunctionAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        
        DataValue rawFunctionName = block.getParameter("name");
        if (rawFunctionName == null) {
            player.sendMessage("§cОшибка: параметр 'name' не указан");
            return ExecutionResult.error("Function name not specified");
        }
        
        DataValue functionNameValue = resolver.resolve(context, rawFunctionName);
        String functionName = functionNameValue.asString();

        // Fix for Qodana issue: Condition functionName == null is always false
        // This was a false positive - we need to properly check for empty strings
        if (functionName.isEmpty()) {
            player.sendMessage("§cОшибка: параметр 'name' не указан");
            return ExecutionResult.error("Function name not specified");
        }

        
        MegaCreative plugin = context.getPlugin();
        if (plugin == null) {
            player.sendMessage("§cОшибка: плагин недоступен");
            return ExecutionResult.error("Plugin not available");
        }

        
        AdvancedFunctionManager functionManager = plugin.getServiceRegistry().getAdvancedFunctionManager();
        if (functionManager == null) {
            player.sendMessage("§cОшибка: AdvancedFunctionManager недоступен");
            return ExecutionResult.error("AdvancedFunctionManager not available");
        }

        
        var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null) {
            player.sendMessage("§cОшибка: вы не находитесь в творческом мире");
            return ExecutionResult.error("Not in creative world");
        }

        
        CodeBlock functionRoot = block.getNextBlock();
        if (functionRoot == null) {
            player.sendMessage("§cОшибка: нет блока для сохранения как функции");
            return ExecutionResult.error("No block to save as function");
        }

        
        List<CodeBlock> functionBlocks = new ArrayList<>();
        functionBlocks.add(functionRoot);
        
        FunctionDefinition function = new FunctionDefinition(
            functionName,
            "Сохраненная функция: " + functionName,
            player,
            new ArrayList<>(), 
            functionBlocks,
            null, 
            FunctionDefinition.FunctionScope.WORLD 
        );

        
        boolean registered = functionManager.registerFunction(function);
        
        if (registered) {
            
            plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
            player.sendMessage("§a💾 Функция '" + functionName + "' сохранена");
            return ExecutionResult.success("Function '" + functionName + "' saved");
        } else {
            player.sendMessage("§cОшибка: не удалось сохранить функцию '" + functionName + "'");
            return ExecutionResult.error("Failed to save function '" + functionName + "'");
        }
    }
}