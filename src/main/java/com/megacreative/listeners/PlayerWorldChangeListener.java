package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.models.CreativeWorld;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener for player world change events
 *
 * Слушатель для событий смены мира игроками
 *
 * Listener für Spieler-Weltwechsel-Ereignisse
 */
public class PlayerWorldChangeListener implements Listener {
    private final MegaCreative plugin;

    /**
     * Constructor for PlayerWorldChangeListener
     * @param plugin the main plugin
     *
     * Конструктор для PlayerWorldChangeListener
     * @param plugin основной плагин
     *
     * Konstruktor für PlayerWorldChangeListener
     * @param plugin das Haupt-Plugin
     */
    public PlayerWorldChangeListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles world change events
     * @param event the player changed world event
     *
     * Обрабатывает события смены мира
     * @param event событие смены мира игроком
     *
     * Verarbeitet Weltwechsel-Ereignisse
     * @param event das Spieler-Weltwechsel-Ereignis
     */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();
        
        // 🎆 ENHANCED: Track world changes for analytics
        trackWorldChange(player, fromWorld, toWorld);
        
        // Когда игрок меняет мир, заново устанавливаем ему скорборд
        plugin.getServiceRegistry().getScoreboardManager().setScoreboard(player);
        
        // Проверяем инвентарь при входе в dev-мир
        if (toWorld.getName().endsWith("_dev") || toWorld.getName().endsWith("-code")) {
            List<String> missingItems = getMissingCodingItems(player);
            if (!missingItems.isEmpty()) {
                CodingItems.giveMissingItems(player, missingItems);
            }
        }
    }
    
    /**
     * Handles player join events
     * @param event the player join event
     *
     * Обрабатывает события входа игроков
     * @param event событие входа игрока
     *
     * Verarbeitet Spieler-Beitritts-Ereignisse
     * @param event das Spieler-Beitritts-Ereignis
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        // 🎆 ENHANCED: Track join to current world
        // 🎆 УСОВЕРШЕНСТВОВАННАЯ: Отслеживание входа в текущий мир
        // 🎆 VERBESSERTE: Verfolgung des Beitritts zur aktuellen Welt
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld != null) {
            String mode = determineWorldMode(world, creativeWorld);
            plugin.getServiceRegistry().getPlayerManager().trackPlayerWorldEntry(player, creativeWorld.getId(), mode);
        }
    }
    
    /**
     * Handles player quit events
     * @param event the player quit event
     *
     * Обрабатывает события выхода игроков
     * @param event событие выхода игрока
     *
     * Verarbeitet Spieler-Verlassen-Ereignisse
     * @param event das Spieler-Verlassen-Ereignis
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        // 🎆 ENHANCED: Track exit from current world
        // 🎆 УСОВЕРШЕНСТВОВАННАЯ: Отслеживание выхода из текущего мира
        // 🎆 VERBESSERTE: Verfolgung des Verlassens der aktuellen Welt
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld != null) {
            plugin.getServiceRegistry().getPlayerManager().trackPlayerWorldExit(player, creativeWorld.getId());
        }
    }
    
    /**
     * 🎆 ENHANCED: Tracks world changes for dual world analytics
     *
     * 🎆 УСОВЕРШЕНСТВОВАННАЯ: Отслеживает изменения мира для аналитики парных миров
     *
     * 🎆 VERBESSERTE: Verfolgt Weltwechsel für duale Welt-Analysen
     */
    private void trackWorldChange(Player player, World fromWorld, World toWorld) {
        // Track exit from previous world
        CreativeWorld fromCreativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(fromWorld);
        if (fromCreativeWorld != null) {
            plugin.getServiceRegistry().getPlayerManager().trackPlayerWorldExit(player, fromCreativeWorld.getId());
        }
        
        // Track entry to new world
        CreativeWorld toCreativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(toWorld);
        if (toCreativeWorld != null) {
            String mode = determineWorldMode(toWorld, toCreativeWorld);
            plugin.getServiceRegistry().getPlayerManager().trackPlayerWorldEntry(player, toCreativeWorld.getId(), mode);
        }
    }
    
    /**
     * 🎆 ENHANCED: Determines the world mode based on world name and dual architecture
     *
     * 🎆 УСОВЕРШЕНСТВОВАННАЯ: Определяет режим мира на основе имени мира и двойной архитектуры
     *
     * 🎆 VERBESSERTE: Bestimmt den Weltmodus basierend auf dem Weltname und der dualen Architektur
     */
    private String determineWorldMode(World world, CreativeWorld creativeWorld) {
        String worldName = world.getName();
        
        if (worldName.endsWith("-code") || worldName.endsWith("_dev")) {
            return "DEV";
        } else if (worldName.endsWith("-world")) {
            return "PLAY";
        } else if (creativeWorld.getDualMode() != null) {
            return creativeWorld.getDualMode().name();
        } else {
            return "UNKNOWN";
        }
    }
    
    /**
     * Проверяет, каких предметов для кодинга не хватает игроку
     *
     * Checks which coding items are missing for the player
     *
     * Prüft, welche Coding-Items dem Spieler fehlen
     */
    private List<String> getMissingCodingItems(Player player) {
        List<String> missingItems = new ArrayList<>();
        
        // Проверяем наличие ключевых предметов
        boolean hasEventBlock = false;
        boolean hasActionBlock = false;
        boolean hasConditionBlock = false;
        boolean hasVariableBlock = false;
        boolean hasRepeatBlock = false;
        boolean hasElseBlock = false;
        boolean hasGameActionBlock = false;
        boolean hasIfVarBlock = false;
        boolean hasIfGameBlock = false;
        boolean hasIfMobBlock = false;
        boolean hasGetDataBlock = false;
        boolean hasCallFunctionBlock = false;
        boolean hasSaveFunctionBlock = false;
        boolean hasRepeatTriggerBlock = false;
        boolean hasBracketBlock = false;
        boolean hasArrowNot = false;
        boolean hasGameValue = false;
        boolean hasCopierTool = false;
        boolean hasDataCreator = false;
        boolean hasCodeMover = false;
        
        for (var item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String name = item.getItemMeta().getDisplayName();

                if (name.equals(CodingItems.EVENT_BLOCK_NAME)) hasEventBlock = true;
                if (name.equals(CodingItems.ACTION_BLOCK_NAME)) hasActionBlock = true;
                if (name.equals(CodingItems.CONDITION_BLOCK_NAME)) hasConditionBlock = true;
                if (name.equals(CodingItems.VARIABLE_BLOCK_NAME)) hasVariableBlock = true;
                if (name.equals(CodingItems.REPEAT_BLOCK_NAME)) hasRepeatBlock = true;
                if (name.equals(CodingItems.ELSE_BLOCK_NAME)) hasElseBlock = true;
                if (name.equals(CodingItems.GAME_ACTION_BLOCK_NAME)) hasGameActionBlock = true;
                if (name.equals(CodingItems.IF_VAR_BLOCK_NAME)) hasIfVarBlock = true;
                if (name.equals(CodingItems.IF_GAME_BLOCK_NAME)) hasIfGameBlock = true;
                if (name.equals(CodingItems.IF_MOB_BLOCK_NAME)) hasIfMobBlock = true;
                if (name.equals(CodingItems.GET_DATA_BLOCK_NAME)) hasGetDataBlock = true;
                if (name.equals(CodingItems.CALL_FUNCTION_BLOCK_NAME)) hasCallFunctionBlock = true;
                if (name.equals(CodingItems.SAVE_FUNCTION_BLOCK_NAME)) hasSaveFunctionBlock = true;
                if (name.equals(CodingItems.REPEAT_TRIGGER_BLOCK_NAME)) hasRepeatTriggerBlock = true;
                if (name.equals(CodingItems.BRACKET_BLOCK_NAME)) hasBracketBlock = true;
                if (name.equals(CodingItems.ARROW_NOT_NAME)) hasArrowNot = true;
                if (name.equals(CodingItems.GAME_VALUE_NAME)) hasGameValue = true;
                if (name.equals(CodingItems.COPIER_TOOL_NAME)) hasCopierTool = true;
                if (name.equals(CodingItems.DATA_CREATOR_NAME)) hasDataCreator = true;
                if (name.equals(CodingItems.CODE_MOVER_NAME)) hasCodeMover = true;
            }
        }

        if (!hasEventBlock) missingItems.add("Блок события");
        if (!hasActionBlock) missingItems.add("Блок действия");
        if (!hasConditionBlock) missingItems.add("Блок условия");
        if (!hasVariableBlock) missingItems.add("Блок переменной");
        if (!hasRepeatBlock) missingItems.add("Блок повтора");
        if (!hasElseBlock) missingItems.add("Блок иначе");
        if (!hasGameActionBlock) missingItems.add("Игровое действие");
        if (!hasIfVarBlock) missingItems.add("Если переменная");
        if (!hasIfGameBlock) missingItems.add("Если игра");
        if (!hasIfMobBlock) missingItems.add("Если существо");
        if (!hasGetDataBlock) missingItems.add("Получить данные");
        if (!hasCallFunctionBlock) missingItems.add("Вызвать функцию");
        if (!hasSaveFunctionBlock) missingItems.add("Сохранить функцию");
        if (!hasRepeatTriggerBlock) missingItems.add("Повторяющийся триггер");
        if (!hasBracketBlock) missingItems.add("Скобка");
        if (!hasArrowNot) missingItems.add("Отрицание НЕ");
        if (!hasGameValue) missingItems.add("Игровое значение");
        if (!hasCopierTool) missingItems.add("Копировщик блоков");
        if (!hasDataCreator) missingItems.add("Создать данные");
        if (!hasCodeMover) missingItems.add("Перемещатель кода");
        
        return missingItems;
    }
}