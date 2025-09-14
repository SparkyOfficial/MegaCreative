package com.megacreative.coding;

import com.megacreative.coding.actions.*;
import com.megacreative.coding.actions.FunctionCallAction;
import com.megacreative.coding.actions.DefineFunctionAction;
import com.megacreative.coding.actions.ReturnAction;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.core.DependencyContainer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory for creating block actions
 *
 * Фабрика для создания действий блоков
 *
 * Fabrik zum Erstellen von Blockaktionen
 */
public class ActionFactory {

    private final Map<String, Supplier<BlockAction>> actionMap = new HashMap<>();
    private final DependencyContainer dependencyContainer;

    /**
     * Creates an ActionFactory
     * @param dependencyContainer Dependency container for resolving dependencies
     *
     * Создает ActionFactory
     * @param dependencyContainer Контейнер зависимостей для разрешения зависимостей
     *
     * Erstellt eine ActionFactory
     * @param dependencyContainer Abhängigkeitscontainer zum Auflösen von Abhängigkeiten
     */
    public ActionFactory(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
        registerAllActions();
    }
    
    /**
     * Registers an action
     * @param actionId Action ID
     * @param supplier Action supplier
     *
     * Регистрирует действие
     * @param actionId ID действия
     * @param supplier Поставщик действия
     *
     * Registriert eine Aktion
     * @param actionId Aktions-ID
     * @param supplier Aktionslieferant
     */
    private void register(String actionId, Supplier<BlockAction> supplier) {
        actionMap.put(actionId, supplier);
    }
    
    /**
     * Registers all actions
     *
     * Регистрирует все действия
     *
     * Registriert alle Aktionen
     */
    private void registerAllActions() {
        // --- BASIC PLAYER ACTIONS ---
        // --- ОСНОВНЫЕ ДЕЙСТВИЯ ИГРОКА ---
        // --- GRUNDLEGENDE SPIELERAKTIONEN ---
        register("sendMessage", SendMessageAction::new);
        register("teleport", TeleportAction::new);
        register("giveItem", GiveItemAction::new);
        register("playSound", PlaySoundAction::new);
        register("effect", EffectAction::new);
        register("broadcast", BroadcastAction::new);
        register("spawnMob", SpawnMobAction::new);
        register("healPlayer", HealPlayerAction::new);
        register("setGameMode", SetGameModeAction::new);
        register("setTime", SetTimeAction::new);
        register("setWeather", SetWeatherAction::new);
        register("command", CommandAction::new);
        register("setVar", SetVarAction::new);
        register("getVar", GetVarAction::new);
        
        // --- NEW BASIC PLAYER ACTIONS ---
        // --- НОВЫЕ ОСНОВНЫЕ ДЕЙСТВИЯ ИГРОКА ---
        // --- NEUE GRUNDLEGENDE SPIELERAKTIONEN ---
        register("giveItems", GiveItemsAction::new);
        register("spawnEntity", SpawnEntityAction::new);
        register("removeItems", RemoveItemsAction::new);
        register("setArmor", SetArmorAction::new);
        register("setGlobalVar", SetGlobalVarAction::new);
        register("getGlobalVar", GetGlobalVarAction::new);
        register("setServerVar", SetServerVarAction::new);
        register("getServerVar", GetServerVarAction::new);
        register("wait", WaitAction::new);
        register("randomNumber", RandomNumberAction::new);
        register("playParticle", PlayParticleAction::new);
        register("sendTitle", SendTitleAction::new);
        register("explosion", ExplosionAction::new);
        register("setBlock", SetBlockAction::new);
        register("getPlayerName", GetPlayerNameAction::new);
        
        // --- VARIABLE MANIPULATION ACTIONS ---
        // --- ДЕЙСТВИЯ МАНИПУЛЯЦИИ ПЕРЕМЕННЫМИ ---
        // --- VARIABLENMANIPULATIONSAKTIONEN ---
        register("addVar", AddVarAction::new);
        register("subVar", SubVarAction::new);
        register("mulVar", MulVarAction::new);
        register("divVar", DivVarAction::new);
        
        // --- SCOREBOARD ACTIONS ---
        // --- ДЕЙСТВИЯ СКОРБОРДА ---
        // --- SCOREBOARD-AKTIONEN ---
        register("createScoreboard", CreateScoreboardAction::new);
        register("setScore", SetScoreAction::new);
        register("incrementScore", IncrementScoreAction::new);
        register("createTeam", CreateTeamAction::new);
        register("addPlayerToTeam", AddPlayerToTeamAction::new);
        
        // --- LOCATION ACTIONS ---
        // --- ДЕЙСТВИЯ ЛОКАЦИИ ---
        // --- ORTSAKTIONEN ---
        register("saveLocation", SaveLocationAction::new);
        register("getLocation", GetLocationAction::new);
        
        // --- ADVANCED ACTION BLOCKS ---
        // --- РАСШИРЕННЫЕ БЛОКИ ДЕЙСТВИЙ ---
        // --- ERWEITERTHE AKTIONSBLÖCKE ---
        register("playCustomSound", PlayCustomSoundAction::new);
        register("spawnParticleEffect", SpawnParticleEffectAction::new);
        register("sendActionBar", SendActionBarAction::new);
        register("executeAsyncCommand", ExecuteAsyncCommandAction::new);

        // --- CONTROL FLOW BLOCKS (they can have actions) ---
        // --- БЛОКИ УПРАВЛЕНИЯ ПОТОКОМ (они могут иметь действия) ---
        // --- KONTROLLFLUSSBLÖCKE (sie können Aktionen haben) ---
        register("asyncLoop", AsyncLoopAction::new);
        register("conditionalBranch", ConditionalBranchAction::new); // Assuming it has an action part
        // Предполагая, что у него есть часть действия
        // Vorausgesetzt, dass es einen Aktionsteil hat
        register("timedExecution", TimedExecutionAction::new);

        // --- FUNCTION BLOCKS ---
        // --- БЛОКИ ФУНКЦИЙ ---
        // --- FUNKTIONSBLÖCKE ---
        register("customFunction", CustomFunctionAction::new); // To define a function
        // Чтобы определить функцию
        // Um eine Funktion zu definieren
        register("callFunction", CallFunctionAction::new);
        
        // Advanced Function System
        // Расширенная система функций
        // Erweitertes Funktionssystem
        register("define_function", () -> new DefineFunctionAction((com.megacreative.MegaCreative) dependencyContainer.resolve(com.megacreative.MegaCreative.class)));
        register("call_function", () -> new FunctionCallAction((com.megacreative.MegaCreative) dependencyContainer.resolve(com.megacreative.MegaCreative.class)));
        register("return", () -> new ReturnAction((com.megacreative.MegaCreative) dependencyContainer.resolve(com.megacreative.MegaCreative.class)));

        // --- DATA MANIPULATION BLOCKS ---
        // --- БЛОКИ МАНИПУЛЯЦИИ ДАННЫМИ ---
        // --- DATENMANIPULATIONSBLÖCKE ---
        register("createList", CreateListAction::new);
        register("listOperation", ListOperationAction::new);
        register("createMap", CreateMapAction::new);
        register("mapOperation", MapOperationAction::new);

        // --- INTEGRATION BLOCKS ---
        // --- БЛОКИ ИНТЕГРАЦИИ ---
        // --- INTEGRATIONSBLÖCKE ---
        register("economyTransaction", EconomyTransactionAction::new);
        register("discordWebhook", DiscordWebhookAction::new);
        
        // --- DEBUGGING BLOCKS ---
        // --- БЛОКИ ОТЛАДКИ ---
        // --- DEBUGGING-BLÖCKE ---
        register("debugLog", DebugLogAction::new);
        register("variableInspector", VariableInspectorAction::new);
        
        // === GENERIC ACTIONS - Mass Production System ===
        // === ОБЩИЕ ДЕЙСТВИЯ - Система массового производства ===
        // === GENERISCHE AKTIONEN - Massenproduktionssystem ===
        // Register all simple actions that can be handled by GenericAction
        // Зарегистрировать все простые действия, которые могут быть обработаны GenericAction
        // Alle einfachen Aktionen registrieren, die von GenericAction behandelt werden können
        registerGenericActions();
    }
    
    /**
     * Register all simple actions that can be handled by GenericAction
     * This enables rapid addition of new functionality without creating new classes
     *
     * Зарегистрировать все простые действия, которые могут быть обработаны GenericAction
     * Это позволяет быстро добавлять новую функциональность без создания новых классов
     *
     * Alle einfachen Aktionen registrieren, die von GenericAction behandelt werden können
     * Dies ermöglicht das schnelle Hinzufügen neuer Funktionen ohne Erstellung neuer Klassen
     */
    private void registerGenericActions() {
        // Player actions
        // Действия игрока
        // Spieleraktionen
        registerGeneric("sendActionBar");
        registerGeneric("sendTitle");
        registerGeneric("setHealth");
        registerGeneric("setFood");
        registerGeneric("addPotionEffect");
        registerGeneric("removePotionEffect");
        registerGeneric("playSound");
        registerGeneric("setGameMode");
        
        // World actions
        // Действия мира
        // Weltenaktionen
        registerGeneric("setBlock");
        registerGeneric("breakBlock");
        registerGeneric("setTime");
        registerGeneric("setWeather");
        
        // Item actions
        // Действия предметов
        // Gegenstandsaktionen
        registerGeneric("giveItem");
        registerGeneric("removeItem");
        
        // Economy actions (if vault available)
        // Экономические действия (если доступен vault)
        // Wirtschaftsaktionen (wenn Vault verfügbar)
        registerGeneric("giveMoney");
        registerGeneric("takeMoney");
        
        // Permission actions
        // Действия разрешений
        // Berechtigungsaktionen
        registerGeneric("givePermission");
        registerGeneric("removePermission");
    }
    
    /**
     * Helper method to register a generic action
     *
     * Вспомогательный метод для регистрации общего действия
     *
     * Hilfsmethode zum Registrieren einer generischen Aktion
     */
    private void registerGeneric(String actionId) {
        register(actionId, GenericAction::new);
    }
    
    /**
     * Creates an action by ID
     * @param actionId Action ID
     * @return BlockAction or null if not found
     *
     * Создает действие по ID
     * @param actionId ID действия
     * @return BlockAction или null, если не найдено
     *
     * Erstellt eine Aktion nach ID
     * @param actionId Aktions-ID
     * @return BlockAction oder null, wenn nicht gefunden
     */
    public BlockAction createAction(String actionId) {
        Supplier<BlockAction> supplier = actionMap.get(actionId);
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }
    
    /**
     * Registers an action with display name
     * @param actionId Action ID
     * @param displayName Display name
     *
     * Регистрирует действие с отображаемым именем
     * @param actionId ID действия
     * @param displayName Отображаемое имя
     *
     * Registriert eine Aktion mit Anzeigenamen
     * @param actionId Aktions-ID
     * @param displayName Anzeigename
     */
    public void registerAction(String actionId, String displayName) {
        // Register the action if it's not already registered
        // Зарегистрировать действие, если оно еще не зарегистрировано
        // Die Aktion registrieren, wenn sie noch nicht registriert ist
        if (!actionMap.containsKey(actionId)) {
            // For now, we'll register a generic action placeholder
            // Пока что мы зарегистрируем общий заполнитель действия
            // Vorerst registrieren wir einen generischen Aktionsplatzhalter
            // In a more advanced implementation, you might want to store display names separately
            // В более продвинутой реализации вы можете хранить отображаемые имена отдельно
            // In einer fortschrittlicheren Implementierung möchten Sie möglicherweise Anzeigenamen separat speichern
            register(actionId, () -> new BlockAction() {
                @Override
                public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
                    return ExecutionResult.error("Action not implemented: " + actionId);
                    // Действие не реализовано:
                    // Aktion nicht implementiert:
                }
            });
        }
    }
    
    /**
     * Gets the action count
     * @return Number of registered actions
     *
     * Получает количество действий
     * @return Количество зарегистрированных действий
     *
     * Ruft die Aktionsanzahl ab
     * @return Anzahl der registrierten Aktionen
     */
    public int getActionCount() {
        return actionMap.size();
    }
}