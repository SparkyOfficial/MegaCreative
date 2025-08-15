package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SetTimeAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawTime = block.getParameter("time");

        String timeStr = ParameterResolver.resolve(context, rawTime);

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