package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.gui.coding.ActionSelectionGUI;
// Import all action editors
import com.megacreative.gui.editors.player.SendMessageEditor;
import com.megacreative.gui.editors.player.TeleportEditor;
import com.megacreative.gui.editors.player.GiveItemEditor;
import com.megacreative.gui.editors.actions.SetVarEditor;
import com.megacreative.gui.editors.actions.GetVarEditor;
// Import all condition editors
import com.megacreative.gui.editors.conditions.HasPermissionEditor;
import com.megacreative.gui.editors.conditions.IfVarEqualsEditor;
import com.megacreative.gui.editors.conditions.IfVarGreaterEditor;
import com.megacreative.gui.editors.conditions.IfVarLessEditor;
import com.megacreative.gui.editors.conditions.HasItemEditor;
import com.megacreative.gui.editors.conditions.PlayerHealthEditor;
import com.megacreative.gui.editors.conditions.IsOpEditor;
import com.megacreative.gui.editors.conditions.IsInWorldEditor;
import com.megacreative.gui.editors.conditions.IsNightEditor;
import com.megacreative.gui.editors.conditions.IsRidingEditor;
import com.megacreative.gui.editors.conditions.CheckPlayerInventoryEditor;
import com.megacreative.gui.editors.conditions.CheckPlayerStatsEditor;
import com.megacreative.gui.editors.conditions.CheckServerOnlineEditor;
import com.megacreative.gui.editors.conditions.CheckWorldWeatherEditor;
import com.megacreative.gui.editors.conditions.IsBlockTypeEditor;
import com.megacreative.gui.editors.conditions.IsNearBlockEditor;
import com.megacreative.gui.editors.conditions.IsNearEntityEditor;
import com.megacreative.gui.editors.conditions.MobNearEditor;
import com.megacreative.gui.editors.conditions.PlayerGameModeEditor;
import com.megacreative.gui.editors.conditions.IsPlayerHoldingEditor;
import com.megacreative.gui.editors.conditions.HasArmorEditor;
import com.megacreative.gui.editors.conditions.CompareVariableEditor;
// Import all event editors
import com.megacreative.gui.editors.events.PlayerJoinEventEditor;
import com.megacreative.gui.editors.events.PlayerLeaveEventEditor;
import com.megacreative.gui.editors.events.PlayerChatEventEditor;
import com.megacreative.gui.editors.events.BlockPlaceEventEditor;
import com.megacreative.gui.editors.events.BlockBreakEventEditor;
import com.megacreative.gui.editors.events.PlayerMoveEventEditor;
import com.megacreative.gui.editors.events.PlayerDeathEventEditor;
import com.megacreative.gui.editors.events.CommandEventEditor;
import com.megacreative.gui.editors.events.OnCommandEventEditor;
import com.megacreative.gui.editors.events.TickEventEditor;
import com.megacreative.gui.editors.events.OnTickEventEditor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Logger;

/**
 * Центральный реестр для сопоставления ID блоков и их GUI-редакторов.
 * Полностью отделяет логику выбора GUI от BlockPlacementHandler.
 */
public class GUIRegistry {
    private static final Logger LOGGER = Logger.getLogger(GUIRegistry.class.getName());

    // Используем TriFunction как "фабрику" для создания GUI.
    // Она принимает (MegaCreative plugin, Player player, CodeBlock codeBlock) и возвращает объект редактора.
    private final Map<String, TriFunction<MegaCreative, Player, CodeBlock, Object>> editors = new HashMap<>();

    public GUIRegistry() {
        // Наполняем наш реестр. Теперь это ЕДИНСТВЕННОЕ место,
        // где нужно прописывать связь между блоком и его редактором.
        registerActionEditors();
        registerConditionEditors();
        registerEventEditors();
        LOGGER.info("GUIRegistry initialized with " + editors.size() + " custom editors.");
    }
    
    private void registerActionEditors() {
        // Действия (Actions)
        register("sendMessage", SendMessageEditor::new);
        register("setVar", SetVarEditor::new);
        register("getVar", GetVarEditor::new);
        register("giveItem", GiveItemEditor::new);
        register("teleport", TeleportEditor::new);
        // ... зарегистрируйте ЗДЕСЬ все остальные редакторы действий
    }

    private void registerConditionEditors() {
        // Условия (Conditions)
        register("hasItem", HasItemEditor::new);
        register("ifVarEquals", IfVarEqualsEditor::new);
        register("ifVarGreater", IfVarGreaterEditor::new);
        register("ifVarLess", IfVarLessEditor::new);
        register("hasPermission", HasPermissionEditor::new);
        register("playerHealth", PlayerHealthEditor::new);
        register("isOp", IsOpEditor::new);
        register("isInWorld", IsInWorldEditor::new);
        register("isNight", IsNightEditor::new);
        register("isRiding", IsRidingEditor::new);
        register("checkPlayerInventory", CheckPlayerInventoryEditor::new);
        register("checkPlayerStats", CheckPlayerStatsEditor::new);
        register("checkServerOnline", CheckServerOnlineEditor::new);
        register("checkWorldWeather", CheckWorldWeatherEditor::new);
        register("isBlockType", IsBlockTypeEditor::new);
        register("isNearBlock", IsNearBlockEditor::new);
        register("isNearEntity", IsNearEntityEditor::new);
        register("mobNear", MobNearEditor::new);
        register("playerGameMode", PlayerGameModeEditor::new);
        register("isPlayerHolding", IsPlayerHoldingEditor::new);
        register("hasArmor", HasArmorEditor::new);
        register("compareVariable", CompareVariableEditor::new);
        // ... зарегистрируйте ЗДЕСЬ все остальные редакторы условий
    }
    
    private void registerEventEditors() {
        // События (Events)
        register("onJoin", PlayerJoinEventEditor::new);
        register("onLeave", PlayerLeaveEventEditor::new);
        register("onChat", PlayerChatEventEditor::new);
        register("onBlockPlace", BlockPlaceEventEditor::new);
        register("onBlockBreak", BlockBreakEventEditor::new);
        register("onPlayerMove", PlayerMoveEventEditor::new);
        register("onPlayerDeath", PlayerDeathEventEditor::new);
        register("onCommand", CommandEventEditor::new);
        register("onCommandEvent", OnCommandEventEditor::new);
        register("onTick", TickEventEditor::new);
        register("onTickEvent", OnTickEventEditor::new);
        // ... зарегистрируйте ЗДЕСЬ все остальные редакторы событий
    }

    /**
     * Регистрирует фабрику для создания GUI-редактора по ID блока.
     * @param id ID блока (например, "sendMessage")
     * @param factory Фабрика, создающая экземпляр редактора (например, SendMessageEditor::new)
     */
    private void register(String id, TriFunction<MegaCreative, Player, CodeBlock, Object> factory) {
        editors.put(id, factory);
    }

    /**
     * Основной метод. Пытается найти и открыть нужный GUI-редактор.
     * @return true, если специализированный редактор был найден и открыт, иначе false.
     */
    public boolean open(String id, MegaCreative plugin, Player player, CodeBlock codeBlock) {
        if (id == null || id.equals("NOT_SET")) {
            return false; // ID не установлен, редактор найти невозможно.
        }

        TriFunction<MegaCreative, Player, CodeBlock, Object> factory = editors.get(id);

        if (factory != null) {
            try {
                // Создаем и открываем редактор
                Object editor = factory.apply(plugin, player, codeBlock);
                // Предполагаем, что у каждого редактора есть метод open()
                editor.getClass().getMethod("open").invoke(editor);
                return true;
            } catch (Exception e) {
                LOGGER.severe("Failed to open GUI editor for ID '" + id + "': " + e.getMessage());
                e.printStackTrace();
                player.sendMessage("§cError opening editor for this block.");
                return false;
            }
        }
        return false; // Специализированный редактор для этого ID не найден.
    }
    
    // Вспомогательный интерфейс для удобства
    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}