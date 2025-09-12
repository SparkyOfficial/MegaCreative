package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    
    private final MegaCreative plugin;
    
    public PlayerQuitListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Убираем игрока из менеджера скорбордов
        plugin.getScoreboardManager().removeScoreboard(event.getPlayer());
        
        // Сохраняем данные игрока
        plugin.getVariableManager().savePersistentData();
        
        // Удаление игрока из онлайна всех миров
        plugin.getWorldManager().getAllPublicWorlds().forEach(world -> 
            world.removeOnlinePlayer(event.getPlayer().getUniqueId())
        );
        
        // Также проверяем приватные миры игрока
        plugin.getWorldManager().getPlayerWorlds(event.getPlayer()).forEach(world -> 
            world.removeOnlinePlayer(event.getPlayer().getUniqueId())
        );
        
        // Если игрок выходит из dev мира, нужно восстановить его инвентарь!
        // DevInventoryManager должен знать, что игрок был в dev мире
        com.megacreative.managers.DevInventoryManager devInventoryManager = plugin.getServiceRegistry().getDevInventoryManager();
        if (devInventoryManager.isPlayerInDevWorld(player)) {
            devInventoryManager.restorePlayerInventory(player);
        }
        
        // Очищаем данные из обработчика блоков
        plugin.getBlockPlacementHandler().cleanUpPlayerData(event.getPlayer().getUniqueId());
    }
}