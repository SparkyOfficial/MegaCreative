package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

import java.util.Random;

public class RandomNumberAction implements BlockAction {
    private static final Random random = new Random();
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawMin = block.getParameter("min");
        Object rawMax = block.getParameter("max");
        Object rawVar = block.getParameter("var");

        String minStr = ParameterResolver.resolve(context, rawMin);
        String maxStr = ParameterResolver.resolve(context, rawMax);
        String varName = ParameterResolver.resolve(context, rawVar);

        if (minStr == null || maxStr == null || varName == null) return;

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