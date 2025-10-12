package com.megacreative.models;


import com.megacreative.coding.CodeScript;
import java.util.UUID;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

/**
 * Serializable data transfer object for CreativeWorld.
 * Contains only the essential data needed for persistence, 
 * avoiding complex Bukkit objects that cause serialization issues.
 *
 * Сериализуемый объект передачи данных для CreativeWorld.
 * Содержит только основные данные, необходимые для сохранения,
 * избегая сложных объектов Bukkit, которые вызывают проблемы сериализации.
 *
 * Serialisierbares Datenübertragungsobjekt für CreativeWorld.
 * Enthält nur die wesentlichen Daten, die für die Persistenz benötigt werden,
 * und vermeidet komplexe Bukkit-Objekte, die Serialisierungsprobleme verursachen.
 */
public class CreativeWorldData {
    public String id;
    public String name;
    public String description;
    public UUID ownerId;
    public String ownerName;
    public CreativeWorldType worldType;
    public WorldMode mode;
    public boolean isPrivate;
    public long createdTime;
    public long lastActivity;
    public WorldFlags flags;
    public Set<UUID> trustedBuilders;
    public Set<UUID> trustedCoders;
    public int likes;
    public int dislikes;
    public Set<UUID> likedBy;
    public Set<UUID> dislikedBy;
    public Set<UUID> favoriteBy;
    public List<WorldComment> comments;
    public List<CodeScriptData> scripts;
    
    /**
     * 🎆 ENHANCED: Dual world support
     *
     * 🎆 ENHANCED: Двойная поддержка миров
     *
     * 🎆 ENHANCED: Duale Weltunterstützung
     */
    public String pairedWorldId;
    public CreativeWorld.WorldDualMode dualMode;
    
    /**
     * Empty constructor for Gson
     *
     * Пустой конструктор для Gson
     *
     * Leerer Konstruktor für Gson
     */
    public CreativeWorldData() {}

    /**
     * Constructor to create from CreativeWorld
     * @param world Source CreativeWorld
     *
     * Конструктор для создания из CreativeWorld
     * @param world Исходный CreativeWorld
     *
     * Konstruktor zum Erstellen aus CreativeWorld
     * @param world Quell-CreativeWorld
     */
    public CreativeWorldData(CreativeWorld world) {
        this.id = world.getId();
        this.name = world.getName();
        this.description = world.getDescription();
        this.ownerId = world.getOwnerId();
        this.ownerName = world.getOwnerName();
        this.worldType = world.getWorldType();
        this.mode = world.getMode();
        this.isPrivate = world.isPrivate();
        this.createdTime = world.getCreatedTime();
        this.lastActivity = world.getLastActivity();
        this.flags = world.getFlags();
        this.trustedBuilders = world.getTrustedBuilders();
        this.trustedCoders = world.getTrustedCoders();
        this.likes = world.getLikes();
        this.dislikes = world.getDislikes();
        this.likedBy = world.getLikedBy();
        this.dislikedBy = world.getDislikedBy();
        this.favoriteBy = world.getFavoriteBy();
        this.comments = world.getComments();
        this.scripts = new ArrayList<>();
        if (world.getScripts() != null) {
            for (com.megacreative.coding.CodeScript script : world.getScripts()) {
                this.scripts.add(new CodeScriptData(script));
            }
        }
        
        
        
        
        this.pairedWorldId = world.getPairedWorldId();
        this.dualMode = world.getDualMode();
    }
}