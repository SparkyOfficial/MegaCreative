package com.megacreative.coding;

import com.megacreative.coding.actions.AddPlayerToTeamAction;
import com.megacreative.coding.actions.AddVarAction;
import com.megacreative.coding.actions.AsyncLoopAction;
import com.megacreative.coding.actions.BroadcastAction;
import com.megacreative.coding.actions.CallFunctionAction;
import com.megacreative.coding.actions.control.BreakAction;
import com.megacreative.coding.actions.control.ContinueAction;
import com.megacreative.coding.actions.CommandAction;
import com.megacreative.coding.actions.ConditionalBranchAction;
import com.megacreative.coding.actions.CreateGuiAction;
import com.megacreative.coding.actions.CreateListAction;
import com.megacreative.coding.actions.CreateMapAction;
import com.megacreative.coding.actions.CreateScoreboardAction;
import com.megacreative.coding.actions.CreateTeamAction;
import com.megacreative.coding.actions.CustomFunctionAction;
import com.megacreative.coding.actions.DebugLogAction;
import com.megacreative.coding.actions.DefineFunctionAction;
import com.megacreative.coding.actions.DiscordWebhookAction;
import com.megacreative.coding.actions.DivVarAction;
import com.megacreative.coding.actions.EconomyTransactionAction;
import com.megacreative.coding.actions.EffectAction;
import com.megacreative.coding.actions.ElseAction;
import com.megacreative.coding.actions.ExecuteAsyncCommandAction;
import com.megacreative.coding.actions.ExplosionAction;
import com.megacreative.coding.actions.ForEachAction;
import com.megacreative.coding.actions.FunctionCallAction;
import com.megacreative.coding.actions.base.GenericAction;
import com.megacreative.coding.actions.GetGlobalVarAction;
import com.megacreative.coding.actions.world.GetLocationAction;
import com.megacreative.coding.actions.GetPlayerNameAction;
import com.megacreative.coding.actions.GetServerVarAction;
import com.megacreative.coding.actions.GetVarAction;
import com.megacreative.coding.actions.GiveItemAction;
import com.megacreative.coding.actions.GiveItemsAction;
import com.megacreative.coding.actions.HandleEventAction;
import com.megacreative.coding.actions.HealPlayerAction;
import com.megacreative.coding.actions.IncrementScoreAction;
import com.megacreative.coding.actions.ListOperationAction;
import com.megacreative.coding.actions.ListOperationsAction;
import com.megacreative.coding.actions.MapOperationAction;
import com.megacreative.coding.actions.MulVarAction;
import com.megacreative.coding.actions.PlayCustomSoundAction;
import com.megacreative.coding.actions.PlayParticleAction;
import com.megacreative.coding.actions.PlayParticleEffectAction;
import com.megacreative.coding.actions.PlaySoundAction;
import com.megacreative.coding.actions.RandomNumberAction;
import com.megacreative.coding.actions.RemoveItemsAction;
import com.megacreative.coding.actions.ReturnAction;
import com.megacreative.coding.actions.SaveFunctionAction;
import com.megacreative.coding.actions.SaveLocationAction;
import com.megacreative.coding.actions.SendActionBarAction;
import com.megacreative.coding.actions.SendMessageAction;
import com.megacreative.coding.actions.SendTitleAction;
import com.megacreative.coding.actions.SetArmorAction;
import com.megacreative.coding.actions.SetBlockAction;
import com.megacreative.coding.actions.SetGameModeAction;
import com.megacreative.coding.actions.SetGlobalVarAction;
import com.megacreative.coding.actions.SetScoreAction;
import com.megacreative.coding.actions.SetServerVarAction;
import com.megacreative.coding.actions.SetTimeAction;
import com.megacreative.coding.actions.SetVarAction;
import com.megacreative.coding.actions.SetWeatherAction;
import com.megacreative.coding.actions.SpawnEntityAction;
import com.megacreative.coding.actions.SpawnMobAction;
import com.megacreative.coding.actions.SpawnParticleEffectAction;
import com.megacreative.coding.actions.SubVarAction;
import com.megacreative.coding.actions.TeleportAction;
import com.megacreative.coding.actions.TimedExecutionAction;
import com.megacreative.coding.actions.TriggerCustomEventAction;
import com.megacreative.coding.actions.TriggerEventAction;
import com.megacreative.coding.actions.VariableInspectorAction;
import com.megacreative.coding.actions.WaitAction;
import com.megacreative.coding.actions.condition.HasItemCondition;
import com.megacreative.coding.actions.condition.HasPermissionCondition;
import com.megacreative.coding.actions.condition.IsInWorldCondition;
import com.megacreative.core.DependencyContainer;
import com.megacreative.managers.GUIManager;
import com.megacreative.gui.interactive.InteractiveGUIManager;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.List;
import java.util.ArrayList;

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
    private GUIManager guiManager;
    private InteractiveGUIManager interactiveGUIManager;
    private CustomEventManager eventManager;

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
        initializeManagers();
        registerAllActions();
    }
    
    /**
     * Initialize GUI and event managers
     * 
     * Инициализировать менеджеры GUI и событий
     * 
     * GUI- und Ereignismanager initialisieren
     */
    private void initializeManagers() {
        try {
            // Initialize GUI managers
            this.guiManager = dependencyContainer.resolve(GUIManager.class);
            this.interactiveGUIManager = dependencyContainer.resolve(InteractiveGUIManager.class);
            
            // Initialize event manager
            this.eventManager = dependencyContainer.resolve(CustomEventManager.class);
        } catch (Exception e) {
            // Log error but don't fail initialization
            // This allows the factory to work even if some managers aren't available
        }
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
        
        // Loop control actions
        register("break", () -> new BreakAction((com.megacreative.MegaCreative) dependencyContainer.resolve(com.megacreative.MegaCreative.class)));
        register("continue", () -> new ContinueAction((com.megacreative.MegaCreative) dependencyContainer.resolve(com.megacreative.MegaCreative.class)));

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
        
        // --- GENERIC LIST OPERATIONS ---
        // --- ОБЩИЕ ОПЕРАЦИИ СО СПИСКАМИ ---
        // --- GENERISCHE LISTENOPERATIONEN ---
        registerGeneric("addToList");
        registerGeneric("removeFromList");
        registerGeneric("getListSize");
        registerGeneric("clearList");
        
        // --- DEDICATED LIST OPERATIONS ACTION ---
        // --- СПЕЦИАЛЬНОЕ ДЕЙСТВИЕ ДЛЯ ОПЕРАЦИЙ СО СПИСКАМИ ---
        // --- SPEZIALISIERTE LISTENOPERATIONS-AKTION ---
        register("listOperations", ListOperationsAction::new);

        // --- INTEGRATION BLOCKS ---
        // --- БЛОКИ ИНТЕГРАЦИИ ---
        // --- INTEGRATIONSBLÖCKE ---
        register("economyTransaction", EconomyTransactionAction::new);
        register("discordWebhook", DiscordWebhookAction::new);
        
        // --- GUI BLOCKS ---
        // --- БЛОКИ GUI ---
        // --- GUI-BLÖCKE ---
        register("createGui", () -> new CreateGuiAction());
        register("createInteractiveGui", () -> new BlockAction() {
            @Override
            public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
                // Create interactive GUI using the interactiveGUIManager
                if (interactiveGUIManager != null) {
                    try {
                        // Get parameters from the block
                        String guiTitle = block.getParameter("title") != null ? 
                            block.getParameter("title").asString() : "Interactive GUI";
                        int guiSize = block.getParameter("size") != null ? 
                            block.getParameter("size").asNumber().intValue() : 27;
                        
                        // Create the interactive GUI
                        com.megacreative.gui.interactive.InteractiveGUI interactiveGUI = 
                            interactiveGUIManager.createInteractiveGUI(context.getPlayer(), guiTitle, guiSize);
                        
                        // Add any configured items to the GUI
                        if (block.getParameter("items") != null) {
                            // Process items parameter and add to GUI
                            // This would depend on how items are stored in the block parameters
                        }
                        
                        // Open the GUI for the player
                        if (context.getPlayer() != null) {
                            interactiveGUI.open();
                            return ExecutionResult.success("Interactive GUI created and opened");
                        } else {
                            return ExecutionResult.error("No player context available");
                        }
                    } catch (Exception e) {
                        return ExecutionResult.error("Failed to create interactive GUI: " + e.getMessage());
                    }
                } else {
                    return ExecutionResult.error("Interactive GUI manager not available");
                }
            }
        });
        
        // --- DEBUGGING BLOCKS ---
        // --- БЛОКИ ОТЛАДКИ ---
        // --- DEBUGGING-BLÖCKE ---
        register("debugLog", DebugLogAction::new);
        register("variableInspector", VariableInspectorAction::new);
        
        // --- EVENT HANDLING BLOCKS ---
        // --- БЛОКИ ОБРАБОТКИ СОБЫТИЙ ---
        // --- EREIGNISBEHANDLUNGSBLÖCKE ---
        register("handleEvent", () -> new HandleEventAction(
            dependencyContainer.resolve(CustomEventManager.class)));
        register("triggerEvent", () -> new TriggerEventAction(
            dependencyContainer.resolve(CustomEventManager.class)));
        
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
        
        // List operations
        registerGeneric("addToList");
        registerGeneric("removeFromList");
        registerGeneric("getListSize");
        registerGeneric("clearList");
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
            // Register a generic action that can be configured later
            // This provides a more flexible approach than the previous placeholder implementation
            register(actionId, GenericAction::new);
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
    
    /**
     * Creates an event handler for the given code block
     * 
     * Создает обработчик событий для заданного блока кода
     * 
     * Erstellt einen Ereignishandler für den angegebenen Codeblock
     * 
     * @param handlerBlock The code block to handle events
     * @param eventName The name of the event to handle
     * @param priority The priority of the handler
     * @return true if handler was registered successfully
     */
    public boolean createEventHandler(CodeBlock handlerBlock, String eventName, int priority) {
        if (eventManager == null || handlerBlock == null || eventName == null) {
            return false;
        }
        
        try {
            // Create and register the event handler
            CustomEventManager.EventHandler handler = eventManager.createEventHandler(
                handlerBlock, 
                null, // Player - null for global handlers
                null, // World - null for global handlers
                priority
            );
            
            eventManager.registerEventHandler(eventName, handler);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets the GUI manager
     * 
     * Получает менеджер GUI
     * 
     * Ruft den GUI-Manager ab
     * 
     * @return The GUI manager or null if not available
     */
    public GUIManager getGuiManager() {
        return guiManager;
    }
    
    /**
     * Gets the interactive GUI manager
     * 
     * Получает интерактивный менеджер GUI
     * 
     * Ruft den interaktiven GUI-Manager ab
     * 
     * @return The interactive GUI manager or null if not available
     */
    public InteractiveGUIManager getInteractiveGuiManager() {
        return interactiveGUIManager;
    }
    
    /**
     * Gets the event manager
     * 
     * Получает менеджер событий
     * 
     * Ruft den Ereignismanager ab
     * 
     * @return The event manager or null if not available
     */
    public CustomEventManager getEventManager() {
        return eventManager;
    }
    
    /**
     * Parses a string representation of a list into a ListValue
     * Supports formats like "[item1,item2,item3]" or "item1,item2,item3"
     * 
     * Преобразует строковое представление списка в ListValue
     * Поддерживает форматы типа "[item1,item2,item3]" или "item1,item2,item3"
     * 
     * Parst eine String-Darstellung einer Liste in einen ListValue
     * Unterstützt Formate wie "[item1,item2,item3]" oder "item1,item2,item3"
     * 
     * @param listString The string to parse
     * @return A ListValue containing the parsed items
     */
    public ListValue parseListString(String listString) {
        if (listString == null || listString.trim().isEmpty()) {
            return new ListValue(new ArrayList<>());
        }
        
        // Remove brackets if present
        String cleanString = listString.trim();
        if (cleanString.startsWith("[")) {
            cleanString = cleanString.substring(1);
        }
        if (cleanString.endsWith("]")) {
            cleanString = cleanString.substring(0, cleanString.length() - 1);
        }
        
        // Split by comma and create DataValues
        List<DataValue> values = new ArrayList<>();
        if (!cleanString.isEmpty()) {
            String[] items = cleanString.split(",");
            for (String item : items) {
                String trimmedItem = item.trim();
                // Try to parse as number first
                try {
                    double number = Double.parseDouble(trimmedItem);
                    values.add(DataValue.fromObject(number));
                } catch (NumberFormatException e) {
                    // Treat as string
                    values.add(DataValue.fromObject(trimmedItem));
                }
            }
        }
        
        return new ListValue(values);
    }
    
    /**
     * Parses a list of raw objects into a ListValue
     * 
     * Преобразует список необработанных объектов в ListValue
     * 
     * Parst eine Liste von Rohobjekten in einen ListValue
     * 
     * @param rawList The list of objects to parse
     * @return A ListValue containing the parsed items
     */
    public ListValue parseRawList(List<?> rawList) {
        if (rawList == null) {
            return new ListValue(new ArrayList<>());
        }
        
        List<DataValue> values = new ArrayList<>();
        for (Object obj : rawList) {
            if (obj instanceof DataValue) {
                values.add((DataValue) obj);
            } else {
                values.add(DataValue.fromObject(obj));
            }
        }
        
        return new ListValue(values);
    }
}