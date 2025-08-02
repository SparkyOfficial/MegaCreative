package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.entity.Player;

import java.util.Optional;

public class MulVarAction implements BlockAction {

    private final ParameterArgument varNameArgument;
    private final ParameterArgument valueArgument;

    public MulVarAction() {
        this.varNameArgument = new ParameterArgument("var");
        this.valueArgument = new ParameterArgument("value");
    }

    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            context.getPlugin().getLogger().warning("MulVarAction: Игрок не найден в контексте");
            return;
        }

        if (context.getCurrentBlock() == null) {
            context.getPlugin().getLogger().warning("MulVarAction: Текущий блок не найден");
            return;
        }

        // Получаем имя переменной
        Optional<TextValue> varNameValueOpt = varNameArgument.parse(context.getCurrentBlock());
        if (varNameValueOpt.isEmpty()) {
            context.getPlugin().getLogger().warning("MulVarAction: Не удалось получить имя переменной");
            return;
        }

        String varName = varNameValueOpt.get().get(context);
        if (varName == null || varName.trim().isEmpty()) {
            context.getPlugin().getLogger().warning("MulVarAction: Имя переменной пусто");
            return;
        }

        // Получаем значение для умножения
        Optional<TextValue> valueValueOpt = valueArgument.parse(context.getCurrentBlock());
        if (valueValueOpt.isEmpty()) {
            context.getPlugin().getLogger().warning("MulVarAction: Не удалось получить значение");
            return;
        }

        String valueStr = valueValueOpt.get().get(context);
        if (valueStr == null || valueStr.trim().isEmpty()) {
            context.getPlugin().getLogger().warning("MulVarAction: Значение пусто");
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
                    context.getPlugin().getLogger().info("MulVarAction: Переменная '" + varName + "' не была числом, начинаем с 0");
                }
            }
            
            // Умножаем на новое значение
            double mulValue = Double.parseDouble(valueStr);
            double result = currentNum * mulValue;
            
            context.setVariable(varName, result);
            player.sendMessage("§a✓ Переменная '" + varName + "' умножена на " + mulValue + " = " + result);
            context.getPlugin().getLogger().info("MulVarAction: Переменная '" + varName + "' = " + result + " (умножено на " + mulValue + ")");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: значение должно быть числом");
            context.getPlugin().getLogger().warning("MulVarAction: Неверный формат числа '" + valueStr + "' для игрока " + player.getName());
        }
    }

    /**
     * Возвращает имя действия
     */
    public String getActionName() {
        return "mulVar";
    }

    /**
     * Возвращает описание действия
     */
    public String getDescription() {
        return "Умножает числовую переменную на значение";
    }
} 