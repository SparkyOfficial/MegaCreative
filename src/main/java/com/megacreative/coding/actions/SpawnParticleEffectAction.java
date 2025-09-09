package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

// Шаблон для нового ДЕЙСТВИЯ
public class SpawnParticleEffectAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден.");
        }

        try {
            // TODO: Получите параметры из блока, используя block.getParameter("key")
            String particleType = block.getParameter("particle").asString();
            int count = block.getParameter("count").asNumber().intValue();
            double spread = block.getParameter("spread").asNumber().doubleValue();
            double speed = block.getParameter("speed").asNumber().doubleValue();
            
            // TODO: Реализуйте логику создания эффекта частиц
            // player.spawnParticle(...);
            
            return ExecutionResult.success("Эффект частиц создан.");

        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при создании эффекта частиц: " + e.getMessage());
        }
    }
}