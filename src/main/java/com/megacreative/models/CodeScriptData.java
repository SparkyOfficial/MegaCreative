package com.megacreative.models;

import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlockData;
import java.util.UUID;

/**
 * Safe serializable data transfer object for CodeScript.
 * Contains only serializable data needed for persistence.
 *
 * Безопасный сериализуемый объект передачи данных для CodeScript.
 * Содержит только сериализуемые данные, необходимые для сохранения.
 *
 * Sicheres serialisierbares Datenübertragungsobjekt für CodeScript.
 * Enthält nur serialisierbare Daten, die für die Persistenz benötigt werden.
 */
public class CodeScriptData {
    public UUID id;
    public String name;
    public boolean enabled;
    public String type;
    public CodeBlockData rootBlock;
    
    /**
     * Default constructor
     *
     * Конструктор по умолчанию
     *
     * Standardkonstruktor
     */
    public CodeScriptData() {}
    
    /**
     * Constructor from CodeScript
     * @param script Source CodeScript
     *
     * Конструктор из CodeScript
     * @param script Исходный CodeScript
     *
     * Konstruktor von CodeScript
     * @param script Quell-CodeScript
     */
    public CodeScriptData(CodeScript script) {
        this.id = script.getId();
        this.name = script.getName();
        this.enabled = script.isEnabled();
        this.type = script.getType().name();
        if (script.getRootBlock() != null) {
            this.rootBlock = new CodeBlockData(script.getRootBlock());
        }
    }
}