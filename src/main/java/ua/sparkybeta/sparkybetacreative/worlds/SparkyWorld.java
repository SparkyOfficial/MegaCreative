package ua.sparkybeta.sparkybetacreative.worlds;

import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import ua.sparkybeta.sparkybetacreative.coding.models.CodeScript;
import ua.sparkybeta.sparkybetacreative.worlds.comments.Comment;
import ua.sparkybeta.sparkybetacreative.worlds.settings.WorldMode;
import ua.sparkybeta.sparkybetacreative.worlds.settings.WorldSettings;
import ua.sparkybeta.sparkybetacreative.worlds.StoredCodeBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class SparkyWorld {

    private final UUID owner;
    private final String internalName; // e.g., "player-uuid-1"
    private final WorldType type;
    private final long creationTimestamp;

    private String customId; // Can be changed by player
    private String displayName;
    private String description;
    private WorldMode mode = WorldMode.BUILD;

    private final transient CodeScript codeScript = new CodeScript(); // Keep for execution, but don't save
    private final Map<String, StoredCodeBlock> codeBlocks = new ConcurrentHashMap<>();


    private final WorldSettings settings = new WorldSettings();
    private final Set<UUID> likes = new HashSet<>();
    private final Set<UUID> dislikes = new HashSet<>();
    private final List<Comment> comments = new ArrayList<>();
    
    public SparkyWorld(UUID owner, String internalName, WorldType type, String initialId) {
        this.owner = owner;
        this.internalName = internalName;
        this.type = type;
        this.customId = initialId;
        this.creationTimestamp = System.currentTimeMillis();
        this.displayName = "My World"; // Default name
        this.description = ""; // Default empty description
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }
    
    public void setMode(WorldMode mode) {
        this.mode = mode;
    }

    public void addLike(UUID player) {
        dislikes.remove(player);
        likes.add(player);
    }

    public void addDislike(UUID player) {
        likes.remove(player);
        dislikes.add(player);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public boolean removeComment(Comment comment) {
        return comments.remove(comment);
    }

    public Map<String, StoredCodeBlock> getCodeBlocks() {
        return this.codeBlocks;
    }

    public StoredCodeBlock getCodeBlock(Location location) {
        return codeBlocks.get(locationToString(location));
    }

    public void addCodeBlock(StoredCodeBlock block) {
        codeBlocks.put(locationToString(block.getLocation()), block);
    }

    public void removeCodeBlock(Location location) {
        codeBlocks.remove(locationToString(location));
    }

    public static String locationToString(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    // --- Гарантия не-null codeScript после десериализации ---
    private Object readResolve() {
        if (this.codeScript == null) {
            try {
                java.lang.reflect.Field f = SparkyWorld.class.getDeclaredField("codeScript");
                f.setAccessible(true);
                f.set(this, new ua.sparkybeta.sparkybetacreative.coding.models.CodeScript());
            } catch (Exception ignored) {}
        }
        return this;
    }
} 