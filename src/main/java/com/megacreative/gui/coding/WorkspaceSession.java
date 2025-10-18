package com.megacreative.gui.coding;

import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

/**
 * Сессия рабочей области для интерфейса программирования
 *
 * Workspace session for coding interface
 *
 * Arbeitsbereichssitzung für die Codierschnittstelle
 */
public class WorkspaceSession {
    
    private final Player player;
    private final CreativeWorld world;
    private final long createdTime;
    
    private Inventory currentGUI;
    private List<WorkspaceScriptBlock> currentScript = new ArrayList<>();
    private Map<String, DataValue> variables = new HashMap<>();
    private Set<UUID> collaborators = new HashSet<>();
    private boolean allowCollaboration = false;
    
    
    private Stack<ScriptSnapshot> undoStack = new Stack<>();
    private Stack<ScriptSnapshot> redoStack = new Stack<>();
    
    /**
     * Инициализирует сессию рабочей области
     * @param player Игрок, который будет использовать сессию
     * @param world Мир, в котором будет использоваться сессия
     *
     * Initializes workspace session
     * @param player Player who will use the session
     * @param world World where the session will be used
     *
     * Initialisiert die Arbeitsbereichssitzung
     * @param player Spieler, der die Sitzung verwenden wird
     * @param world Welt, in der die Sitzung verwendet wird
     */
    public WorkspaceSession(Player player, CreativeWorld world) {
        this.player = player;
        this.world = world;
        this.createdTime = System.currentTimeMillis();
    }
    
    
    /**
     * Добавляет блок в скрипт
     * @param block Блок для добавления
     *
     * Adds block to script
     * @param block Block to add
     *
     * Fügt Block zum Skript hinzu
     * @param block Hinzuzufügender Block
     */
    public void addBlock(WorkspaceScriptBlock block) {
        saveSnapshot("Add " + block.getAction());
        currentScript.add(block);
    }
    
    /**
     * Удаляет блок из скрипта
     * @param index Индекс блока для удаления
     *
     * Removes block from script
     * @param index Index of block to remove
     *
     * Entfernt Block aus dem Skript
     * @param index Index des zu entfernenden Blocks
     */
    public void removeBlock(int index) {
        if (index >= 0 && index < currentScript.size()) {
            WorkspaceScriptBlock removed = currentScript.get(index);
            saveSnapshot("Remove " + removed.getAction());
            currentScript.remove(index);
        }
    }
    
    
    /**
     * Сохраняет снимок скрипта для отмены/повтора
     * @param description Описание снимка
     *
     * Saves script snapshot for undo/redo
     * @param description Snapshot description
     *
     * Speichert Skript-Snapshot für Rückgängig/Wiederholen
     * @param description Snapshot-Beschreibung
     */
    private void saveSnapshot(String description) {
        undoStack.push(new ScriptSnapshot(new ArrayList<>(currentScript), description));
        redoStack.clear();
        if (undoStack.size() > 50) undoStack.remove(0);
    }
    
    /**
     * Отменяет последнее действие
     *
     * Performs undo
     *
     * Führt Rückgängig durch
     */
    public void undo() {
        if (undoStack.size() > 1) {
            redoStack.push(undoStack.pop());
            currentScript = new ArrayList<>(undoStack.peek().getScript());
            player.sendMessage("§aUndo performed");
        }
    }
    
    /**
     * Повторяет последнее отмененное действие
     *
     * Performs redo
     *
     * Führt Wiederholen durch
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            ScriptSnapshot next = redoStack.pop();
            undoStack.push(next);
            currentScript = new ArrayList<>(next.getScript());
            player.sendMessage("§aRedo performed");
        }
    }
    
    
    /**
     * Включает режим совместной работы
     *
     * Enables collaboration mode
     *
     * Aktiviert den Zusammenarbeitsmodus
     */
    public void enableCollaboration() {
        allowCollaboration = true;
        player.sendMessage("§aCollaboration enabled!");
    }
    
    /**
     * Добавляет соавтора
     * @param playerId Идентификатор игрока-соавтора
     * @return true, если соавтор добавлен успешно
     *
     * Adds collaborator
     * @param playerId Collaborator player ID
     * @return true if collaborator added successfully
     *
     * Fügt Mitarbeiter hinzu
     * @param playerId Mitarbeiter-Spieler-ID
     * @return true, wenn Mitarbeiter erfolgreich hinzugefügt wurde
     */
    public boolean addCollaborator(UUID playerId) {
        if (!allowCollaboration) return false;
        collaborators.add(playerId);
        return true;
    }
    
    /**
     * Сохраняет текущий скрипт
     *
     * Saves current script
     *
     * Speichert das aktuelle Skript
     */
    public void saveCurrentScript() {
        player.sendMessage("§aScript saved!");
    }
    
    
    /**
     * Получает игрока сессии
     * @return Игрок сессии
     *
     * Gets session player
     * @return Session player
     *
     * Ruft den Sitzungsspieler ab
     * @return Sitzungsspieler
     */
    public Player getPlayer() { return player; }
    
    /**
     * Получает мир сессии
     * @return Мир сессии
     *
     * Gets session world
     * @return Session world
     *
     * Ruft die Sitzungswelt ab
     * @return Sitzungswelt
     */
    public CreativeWorld getWorld() { return world; }
    
    /**
     * Получает текущий скрипт
     * @return Текущий скрипт
     *
     * Gets current script
     * @return Current script
     *
     * Ruft das aktuelle Skript ab
     * @return Aktuelles Skript
     */
    public List<WorkspaceScriptBlock> getCurrentScript() { return currentScript; }
    
    /**
     * Получает текущий графический интерфейс
     * @return Текущий графический интерфейс
     *
     * Gets current GUI
     * @return Current GUI
     *
     * Ruft die aktuelle GUI ab
     * @return Aktuelle GUI
     */
    public Inventory getCurrentGUI() { return currentGUI; }
    
    /**
     * Устанавливает текущий графический интерфейс
     * @param gui Графический интерфейс для установки
     *
     * Sets current GUI
     * @param gui GUI to set
     *
     * Setzt die aktuelle GUI
     * @param gui Zu setzende GUI
     */
    public void setCurrentGUI(Inventory gui) { this.currentGUI = gui; }
    
    /**
     * Проверяет, является ли игрок соавтором
     * @param playerId Идентификатор игрока
     * @return true, если игрок является соавтором
     *
     * Checks if player is collaborator
     * @param playerId Player ID
     * @return true if player is collaborator
     *
     * Prüft, ob der Spieler ein Mitarbeiter ist
     * @param playerId Spieler-ID
     * @return true, wenn der Spieler ein Mitarbeiter ist
     */
    public boolean isCollaborator(UUID playerId) { return collaborators.contains(playerId); }
}

/**
 * Определение блока для палитры
 *
 * Block definition for palette
 *
 * Blockdefinition für die Palette
 */
class BlockDefinition {
    private final String name;
    private final String action;
    private final BlockCategory category;
    private final org.bukkit.Material material;
    private final List<BlockParameter> parameters;
    
    /**
     * Инициализирует определение блока
     * @param name Имя блока
     * @param action Действие блока
     * @param category Категория блока
     * @param material Материал блока
     * @param parameters Параметры блока
     *
     * Initializes block definition
     * @param name Block name
     * @param action Block action
     * @param category Block category
     * @param material Block material
     * @param parameters Block parameters
     *
     * Initialisiert die Blockdefinition
     * @param name Blockname
     * @param action Block-Aktion
     * @param category Block-Kategorie
     * @param material Block-Material
     * @param parameters Block-Parameter
     */
    public BlockDefinition(String name, String action, BlockCategory category, 
                          org.bukkit.Material material, List<BlockParameter> parameters) {
        this.name = name;
        this.action = action;
        this.category = category;
        this.material = material;
        this.parameters = parameters;
    }
    
    /**
     * Получает имя блока
     * @return Имя блока
     *
     * Gets block name
     * @return Block name
     *
     * Ruft den Blocknamen ab
     * @return Blockname
     */
    public String getName() { return name; }
    
    /**
     * Получает действие блока
     * @return Действие блока
     *
     * Gets block action
     * @return Block action
     *
     * Ruft die Block-Aktion ab
     * @return Block-Aktion
     */
    public String getAction() { return action; }
    
    /**
     * Получает категорию блока
     * @return Категория блока
     *
     * Gets block category
     * @return Block category
     *
     * Ruft die Block-Kategorie ab
     * @return Block-Kategorie
     */
    public BlockCategory getCategory() { return category; }
    
    /**
     * Получает материал блока
     * @return Материал блока
     *
     * Gets block material
     * @return Block material
     *
     * Ruft das Block-Material ab
     * @return Block-Material
     */
    public org.bukkit.Material getMaterial() { return material; }
    
    /**
     * Получает параметры блока
     * @return Параметры блока
     *
     * Gets block parameters
     * @return Block parameters
     *
     * Ruft die Block-Parameter ab
     * @return Block-Parameter
     */
    public List<BlockParameter> getParameters() { return parameters; }
}

/**
 * Определение параметра блока
 *
 * Block parameter definition
 *
 * Blockparameter-Definition
 */
class BlockParameter {
    private final String name;
    private final com.megacreative.coding.values.ValueType type;
    private final String description;
    private final boolean required;
    
    /**
     * Инициализирует параметр блока
     * @param name Имя параметра
     * @param type Тип параметра
     * @param description Описание параметра
     * @param required Обязательность параметра
     *
     * Initializes block parameter
     * @param name Parameter name
     * @param type Parameter type
     * @param description Parameter description
     * @param required Parameter required
     *
     * Initialisiert den Blockparameter
     * @param name Parametername
     * @param type Parametertyp
     * @param description Parameterbeschreibung
     * @param required Parametererforderlichkeit
     */
    public BlockParameter(String name, com.megacreative.coding.values.ValueType type, 
                         String description, boolean required) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.required = required;
    }
    
    /**
     * Получает имя параметра
     * @return Имя параметра
     *
     * Gets parameter name
     * @return Parameter name
     *
     * Ruft den Parameternamen ab
     * @return Parametername
     */
    public String getName() { return name; }
    
    /**
     * Получает тип параметра
     * @return Тип параметра
     *
     * Gets parameter type
     * @return Parameter type
     *
     * Ruft den Parametertyp ab
     * @return Parametertyp
     */
    public com.megacreative.coding.values.ValueType getType() { return type; }
    
    /**
     * Получает описание параметра
     * @return Описание параметра
     *
     * Gets parameter description
     * @return Parameter description
     *
     * Ruft die Parameterbeschreibung ab
     * @return Parameterbeschreibung
     */
    public String getDescription() { return description; }
    
    /**
     * Проверяет, является ли параметр обязательным
     * @return true, если параметр обязательный
     *
     * Checks if parameter is required
     * @return true if parameter is required
     *
     * Prüft, ob der Parameter erforderlich ist
     * @return true, wenn der Parameter erforderlich ist
     */
    public boolean isRequired() { return required; }
}

/**
 * Режимы рабочей области
 *
 * Workspace modes
 *
 * Arbeitsbereichsmodi
 */
enum WorkspaceMode {
    BUILDING("Building", "Creating and editing scripts"),
    TESTING("Testing", "Testing script execution"),
    DEBUGGING("Debugging", "Debugging script issues"),
    COLLABORATING("Collaborating", "Working with others");
    
    private final String name;
    private final String description;
    
    /**
     * Инициализирует режим рабочей области
     * @param name Имя режима
     * @param description Описание режима
     *
     * Initializes workspace mode
     * @param name Mode name
     * @param description Mode description
     *
     * Initialisiert den Arbeitsbereichsmodus
     * @param name Modusname
     * @param description Modusbeschreibung
     */
    WorkspaceMode(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    /**
     * Получает имя режима
     * @return Имя режима
     *
     * Gets mode name
     * @return Mode name
     *
     * Ruft den Modusnamen ab
     * @return Modusname
     */
    public String getName() { return name; }
    
    /**
     * Получает описание режима
     * @return Описание режима
     *
     * Gets mode description
     * @return Mode description
     *
     * Ruft die Modusbeschreibung ab
     * @return Modusbeschreibung
     */
    public String getDescription() { return description; }
}