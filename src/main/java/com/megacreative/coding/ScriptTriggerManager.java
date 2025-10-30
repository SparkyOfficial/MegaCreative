package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.logging.Logger;
import java.util.List;

/**
 * Listens to core Bukkit events, finds the corresponding compiled CodeScript
 * and passes it to the ScriptEngine for execution.
 */
public class ScriptTriggerManager implements Listener {
    private static final Logger LOGGER = Logger.getLogger(ScriptTriggerManager.class.getName());
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    private final ScriptEngine scriptEngine;
    
    public ScriptTriggerManager(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.scriptEngine = plugin.getServiceRegistry().getScriptEngine();
    }
    
    // ----- Пример для события входа игрока -----
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Определяем событие, которое мы ищем в скриптах
        String eventAction = "onJoin";
        
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null || !isDevWorld(player.getWorld().getName())) {
            return;
        }

        LOGGER.info("[Trigger] Player " + player.getName() + " joined, searching for '" + eventAction + "' scripts in " + creativeWorld.getName());
        
        // ВАЖНО: В реальном коде скрипты должны быть где-то сохранены/закешированы в creativeWorld
        // Сейчас мы будем их компилировать "на лету" для теста.
        
        // Создаем временный компилятор для поиска и сборки скриптов
        ScriptCompiler compiler = new ScriptCompiler(
            plugin,
            plugin.getServiceRegistry().getBlockConfigService(),
            plugin.getServiceRegistry().getBlockPlacementHandler()
        );
        
        // Компилируем все скрипты в текущем мире
        List<CodeScript> allScriptsInWorld = compiler.compileWorldScripts(player.getWorld());

        // Ищем среди всех скриптов те, что начинаются с нужного нам события
        for (CodeScript script : allScriptsInWorld) {
            if (script.getRootBlock() != null && eventAction.equals(script.getRootBlock().getAction())) {
                LOGGER.info("[Trigger] Found script for '" + eventAction + "'. Executing...");
                player.sendMessage("§a[Trigger] Found script for " + eventAction + ", executing...");
                
                // Запускаем найденный скрипт
                scriptEngine.executeScript(script, player, eventAction);
            }
        }
    }

    // Здесь можно будет добавить другие обработчики: onBlockBreak, onChat и т.д.
    // ...

    private boolean isDevWorld(String worldName) {
         return worldName.startsWith("megacreative_") || worldName.endsWith("-code");
    }
}