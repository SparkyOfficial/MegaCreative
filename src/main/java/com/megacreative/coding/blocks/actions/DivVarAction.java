package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.entity.Player;

import java.util.Optional;

public class DivVarAction implements BlockAction {

    private final ParameterArgument varNameArgument;
    private final ParameterArgument valueArgument;

    public DivVarAction() {
        this.varNameArgument = new ParameterArgument("var");
        this.valueArgument = new ParameterArgument("value");
    }

    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            context.getPlugin().getLogger().warning("DivVarAction: Игрок не найден в контексте");
            return;
        }

        if (context.getCurrentBlock() == null) {
            context.getPlugin().getLogger().warning("DivVarAction: Текущий блок не найден");
            return;
        }

        // Получаем имя переменной
        Optional<TextValue> varNameValueOpt = varNameArgument.parse(context.getCurrentBlock());
        if (varNameValueOpt.isEmpty()) {
            context.getPlugin().getLogger().warning("DivVarAction: Не удалось получить имя переменной");
            return;
        }

        String varName = varNameValueOpt.get().get(context);
        if (varName == null || varName.trim().isEmpty()) {
            context.getPlugin().getLogger().warning("DivVarAction: Имя переменной пусто");
            return;
        }

        // Получаем значение для деления
        Optional<TextValue> valueValueOpt = valueArgument.parse(context.getCurrentBlock());
        if (valueValueOpt.isEmpty()) {
            context.getPlugin().getLogger().warning("DivVarAction: Не удалось получить значение");
            return;
        }

        String valueStr = valueValueOpt.get().get(context);
        if (valueStr == null || valueStr.trim().isEmpty()) {
            context.getPlugin().getLogger().warning("DivVarAction: Значение пусто");
            return;
        }

        try {
            // Получаем текущее значение переменной
            Object currentValue = context.getVariable(varName);
            double currentNum = 0.0;
            
            if (currentValue != null) {
                try {
                    currentNum = Double.parseDouble(currentValue.toString());
                } catch (NumberFormatException e) {
                    // Если не число, начинаем с 0
                    context.getPlugin().getLogger().info("DivVarAction: Переменная '" + varName + "' не была числом, начинаем с 0");
                }
            }
            
            // Делим на новое значение
            double divValue = Double.parseDouble(valueStr);
            
            if (divValue == 0.0) {
                player.sendMessage("§cОшибка: деление на ноль невозможно");
                context.getPlugin().getLogger().warning("DivVarAction: Попытка деления на ноль для игрока " + player.getName());
                return;
            }
            
            double result = currentNum / divValue;
            
            context.setVariable(varName, result);
            player.sendMessage("§a✓ Переменная '" + varName + "' разделена на " + divValue + " = " + result);
            context.getPlugin().getLogger().info("DivVarAction: Переменная '" + varName + "' = " + result + " (разделено на " + divValue + ")");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: значение должно быть числом");
            context.getPlugin().getLogger().warning("DivVarAction: Неверный формат числа '" + valueStr + "' для игрока " + player.getName());
        }
    }

    /**
     * Возвращает имя действия
     */
    public String getActionName() {
        return "divVar";
    }

    /**
     * Возвращает описание действия
     */
    public String getDescription() {
        return "Делит числовую переменную на значение";
    }
} 