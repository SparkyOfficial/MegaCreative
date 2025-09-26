package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.CodeBlockBrokenEvent;
import com.megacreative.coding.events.CodeBlockPlacedEvent;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Отвечает ИСКЛЮЧИТЕЛЬНО за компиляцию и управление CodeScript'ами.
 * Слушает события добавления/удаления блоков и пересобирает скрипты в мире.
 */
public class ScriptCompiler implements Listener {
    
    private static final Logger LOGGER = Logger.getLogger(ScriptCompiler.class.getName());

    private final MegaCreative plugin;
    private final BlockConfigService blockConfigService;
    private final AutoConnectionManager connectionManager; // Нужен для сборки цепочки блоков

    public ScriptCompiler(MegaCreative plugin, BlockConfigService blockConfigService, AutoConnectionManager connectionManager) {
        this.plugin = plugin;
        this.blockConfigService = blockConfigService;
        this.connectionManager = connectionManager;
    }
    
    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        if (isEventBlock(event.getCodeBlock())) {
            createAndAddScript(event.getCodeBlock(), event.getPlayer(), event.getLocation());
        }
    }

    @EventHandler
    public void onCodeBlockBroken(CodeBlockBrokenEvent event) {
        if (isEventBlock(event.getCodeBlock())) {
            removeScript(event.getCodeBlock(), event.getLocation());
        }
    }

    private boolean isEventBlock(CodeBlock block) {
        if (block == null || block.getAction() == null) return false;
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(block.getAction());
        if (config == null) return false;
        return "EVENT".equals(config.getType());
    }
    
    private void createAndAddScript(CodeBlock eventBlock, Player player, Location location) {
        try {
            CodeScript script = compileScriptFromEventBlock(eventBlock);
            if (script == null) {
                player.sendMessage("§cОшибка компиляции скрипта!");
                return;
            }

            IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());

            if (creativeWorld != null) {
                addScriptToWorld(eventBlock, script, creativeWorld, worldManager);
                player.sendMessage("§a✓ Скрипт скомпилирован для события: §f" + eventBlock.getAction());
                LOGGER.fine("Compiled and added script for event block: " + eventBlock.getAction());
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to create script: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void removeScript(CodeBlock eventBlock, Location location) {
         try {
            IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());
            if (creativeWorld != null) {
                List<CodeScript> scripts = creativeWorld.getScripts();
                if (scripts != null) {
                    boolean removed = scripts.removeIf(script -> script.getRootBlock().getId().equals(eventBlock.getId()));
                    if (removed) {
                        worldManager.saveWorld(creativeWorld);
                        LOGGER.fine("Removed script for event block: " + eventBlock.getAction());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to remove script: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private CodeScript compileScriptFromEventBlock(CodeBlock eventBlock) {
        // Логика компиляции здесь. Она просто следует по `nextBlock` и `children` связям,
        // которые уже установил AutoConnectionManager.
        
        // ВАЖНО: Мы не перестраиваем связи здесь. Мы им доверяем.
        // AutoConnectionManager отвечает за структуру, а компилятор - за ее чтение.
        
        return new CodeScript(eventBlock); // Для простоты пока создаем скрипт только с корневым блоком
    }

    private void addScriptToWorld(CodeBlock eventBlock, CodeScript script, CreativeWorld creativeWorld, IWorldManager worldManager) {
        List<CodeScript> scripts = creativeWorld.getScripts();
        if (scripts == null) {
            scripts = new ArrayList<>();
            creativeWorld.setScripts(scripts);
        }
        // Избегаем дубликатов, удаляя старый скрипт, если он был привязан к этому же блоку
        scripts.removeIf(existingScript -> existingScript.getRootBlock().getId().equals(eventBlock.getId()));
        scripts.add(script);
        worldManager.saveWorld(creativeWorld);
    }
}