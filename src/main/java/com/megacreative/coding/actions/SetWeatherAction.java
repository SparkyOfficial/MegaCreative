package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SetWeatherAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        if (block == null) return;

        String weather = (String) block.getParameter("weather");
        if (weather != null) {
            World world = player != null ? player.getWorld() : 
                org.bukkit.Bukkit.getWorld(context.getCreativeWorld().getWorldName());
            
            if (world != null) {
                switch (weather.toLowerCase()) {
                    case "clear":
                        world.setStorm(false);
                        world.setThundering(false);
                        break;
                    case "rain":
                        world.setStorm(true);
                        world.setThundering(false);
                        break;
                    case "thunder":
                        world.setStorm(true);
                        world.setThundering(true);
                        break;
                    default:
                        if (player != null) {
                            player.sendMessage("§cНеизвестный тип погоды: " + weather);
                        }
                        return;
                }
                
                if (player != null) {
                    player.sendMessage("§aПогода изменена на: " + weather);
                }
            }
        }
    }
} 