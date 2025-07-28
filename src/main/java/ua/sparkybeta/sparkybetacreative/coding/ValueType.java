package ua.sparkybeta.sparkybetacreative.coding;

import org.bukkit.Material;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ValueType {
    TEXT(Material.BOOK),
    NUMBER(Material.SLIME_BALL),
    LOCATION(Material.PAPER),
    VECTOR(Material.PRISMARINE_SHARD),
    BOOLEAN(Material.LEVER),
    ITEM(Material.CHEST),

    // Dynamic types
    DYNAMIC_PLAYER(Material.PLAYER_HEAD),
    PLAYER(Material.SKELETON_SKULL);

    private final Material material;

    public static ValueType getByMaterial(Material material) {
        for (ValueType type : values()) {
            if (type.getMaterial() == material) {
                return type;
            }
        }
        return ITEM; // Default to item if no specific type matches
    }
} 