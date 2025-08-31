package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

import java.util.Random;

public class RandomNumberAction implements BlockAction {
    private static final Random random = new Random();
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawMin = block.getParameter("min");
        DataValue rawMax = block.getParameter("max");
        DataValue rawVar = block.getParameter("var");
        
        if (rawMin == null || rawMax == null || rawVar == null) return;

        String minStr = resolver.resolve(context, rawMin).asString();
        String maxStr = resolver.resolve(context, rawMax).asString();
        String varName = resolver.resolve(context, rawVar).asString();

        try {
            int min = Integer.parseInt(minStr);
            int max = Integer.parseInt(maxStr);
            
            if (min > max) {
                int temp = min;
                min = max;
                max = temp;
            }
            
            int randomNumber = random.nextInt(max - min + 1) + min;
            context.setVariable(varName, randomNumber);
            
            player.sendMessage("§a🎲 Случайное число " + randomNumber + " сохранено в переменную '" + varName + "'");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметрах min/max: " + minStr + "/" + maxStr);
        }
    }
} 