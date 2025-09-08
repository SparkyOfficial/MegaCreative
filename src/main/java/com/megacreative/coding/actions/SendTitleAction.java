package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class SendTitleAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player is not available.");
        }

        // Получаем параметры из блока
        String title = block.getParameter("title", DataValue.of("")).asString();
        String subtitle = block.getParameter("subtitle", DataValue.of("")).asString();
        int fadeIn = block.getParameter("fadein", DataValue.of(10)).asNumber().intValue();
        int stay = block.getParameter("stay", DataValue.of(70)).asNumber().intValue();
        int fadeOut = block.getParameter("fadeout", DataValue.of(20)).asNumber().intValue();
        
        // Заменяем плейсхолдеры, если нужно (у тебя есть PlaceholderResolver, используй его!)
        title = title.replace("%player%", player.getName());
        subtitle = subtitle.replace("%player%", player.getName());

        // Отправляем тайтл
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);

        return ExecutionResult.success("Title sent successfully.");
    }
}