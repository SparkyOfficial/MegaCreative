package com.megacreative.gui.coding;

import com.megacreative.coding.values.DataValue;
import java.util.List;

/**
 * Снимок скрипта для отмены/повтора
 *
 * Script snapshot for undo/redo
 *
 * Skript-Snapshot für Rückgängig/Wiederholen
 */
public class ScriptSnapshot {
    private final List<WorkspaceScriptBlock> script;
    private final String description;
    private final long timestamp;
    
    /**
     * Инициализирует снимок скрипта
     * @param script Скрипт для снимка
     * @param description Описание снимка
     *
     * Initializes script snapshot
     * @param script Script for snapshot
     * @param description Snapshot description
     *
     * Initialisiert den Skript-Snapshot
     * @param script Skript für den Snapshot
     * @param description Snapshot-Beschreibung
     */
    public ScriptSnapshot(List<WorkspaceScriptBlock> script, String description) {
        this.script = script;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Получает скрипт снимка
     * @return Скрипт снимка
     *
     * Gets snapshot script
     * @return Snapshot script
     *
     * Ruft das Snapshot-Skript ab
     * @return Snapshot-Skript
     */
    public List<WorkspaceScriptBlock> getScript() { return script; }
    
    /**
     * Получает описание снимка
     * @return Описание снимка
     *
     * Gets snapshot description
     * @return Snapshot description
     *
     * Ruft die Snapshot-Beschreibung ab
     * @return Snapshot-Beschreibung
     */
    public String getDescription() { return description; }
    
    /**
     * Получает временную метку снимка
     * @return Временная метка снимка
     *
     * Gets snapshot timestamp
     * @return Snapshot timestamp
     *
     * Ruft den Snapshot-Zeitstempel ab
     * @return Snapshot-Zeitstempel
     */
    public long getTimestamp() { return timestamp; }
}