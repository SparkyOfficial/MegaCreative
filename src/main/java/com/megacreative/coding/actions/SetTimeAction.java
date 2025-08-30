package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SetTimeAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        if (context == null) return;
        
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawTime = block.getParameter("time");
        if (rawTime == null) return;

        String timeStr = resolver.resolve(context, rawTime).asString();

        if (timeStr == null) return;

        try {
            long time = Long.parseLong(timeStr);
            World world = player.getWorld();
            world.setTime(time);
            
            player.sendMessage("§a⏰ Время установлено на: " + time);
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: время должно быть числом");
        }
    }
} 