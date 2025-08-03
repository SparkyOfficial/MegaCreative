package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Обработчик событий изменения мира игроком
 */
public class PlayerWorldChangeListener implements Listener {

    private final MegaCreative plugin;

    public PlayerWorldChangeListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        if (world != null && world.getMode() == WorldMode.DEV) {
            // Игрок вошел в мир разработки - загружаем блоки
            loadDevWorldBlocks(world);
            player.sendMessage("§a✅ Блоки кода загружены для мира разработки!");
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        if (world != null && world.getMode() == WorldMode.DEV) {
            // Игрок присоединился к миру разработки - загружаем блоки
            loadDevWorldBlocks(world);
        }
    }
    
    /**
     * Загружает блоки кода для мира разработки
     */
    private void loadDevWorldBlocks(CreativeWorld world) {
        // Блоки уже загружены в CreativeWorld при инициализации
        // Здесь можно добавить дополнительную логику, если потребуется
        plugin.getLogger().info("Блоки кода загружены для мира: " + world.getName());
    }
} 