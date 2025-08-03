package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;

import org.bukkit.entity.Player;

public class ShowMessageInTitleAction implements BlockAction {
    
    private final Argument<TextValue> titleArgument;
    private final Argument<TextValue> subtitleArgument;
    private final Argument<TextValue> fadeInArgument;
    private final Argument<TextValue> stayArgument;
    private final Argument<TextValue> fadeOutArgument;
    
    public ShowMessageInTitleAction() {
        this.titleArgument = new ParameterArgument("title");
        this.subtitleArgument = new ParameterArgument("subtitle");
        this.fadeInArgument = new ParameterArgument("fadeIn");
        this.stayArgument = new ParameterArgument("stay");
        this.fadeOutArgument = new ParameterArgument("fadeOut");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // Получаем параметры
        TextValue titleValue = titleArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue subtitleValue = subtitleArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue fadeInValue = fadeInArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue stayValue = stayArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue fadeOutValue = fadeOutArgument.parse(context.getCurrentBlock()).orElse(null);
        
        try {
            String title = titleValue != null ? titleValue.get(context) : "";
            String subtitle = subtitleValue != null ? subtitleValue.get(context) : "";
            int fadeIn = fadeInValue != null ? Integer.parseInt(fadeInValue.get(context)) : 10;
            int stay = stayValue != null ? Integer.parseInt(stayValue.get(context)) : 40;
            int fadeOut = fadeOutValue != null ? Integer.parseInt(fadeOutValue.get(context)) : 10;
            
            // Показываем заголовок
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            player.sendMessage("§a✓ Показан заголовок: " + title);
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Ошибка: " + e.getMessage());
        }
    }
} 