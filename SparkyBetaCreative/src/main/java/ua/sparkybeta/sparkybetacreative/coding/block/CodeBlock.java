package ua.sparkybeta.sparkybetacreative.coding.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public enum CodeBlock {
    // Player Events
    PLAYER_JOIN("Player Join", CodeBlockCategory.EVENT, Material.DIAMOND_BLOCK),
    PLAYER_QUIT("Player Quit", CodeBlockCategory.EVENT, Material.DIAMOND_BLOCK),
    PLAYER_BREAK_BLOCK("Player Break Block", CodeBlockCategory.EVENT, Material.DIAMOND_BLOCK),

    // Player Conditions
    PLAYER_HAS_PERMISSION("Player Has Permission", CodeBlockCategory.CONDITION, Material.EMERALD_BLOCK),

    // Player Actions
    PLAYER_SEND_MESSAGE("Send Message", CodeBlockCategory.ACTION, Material.GOLD_BLOCK),
    PLAYER_GIVE_ITEM("Give Item", CodeBlockCategory.ACTION, Material.GOLD_BLOCK),
    PLAYER_TELEPORT("Teleport", CodeBlockCategory.ACTION, Material.GOLD_BLOCK),

    // Game Actions
    GAME_CREATE_EXPLOSION("Create Explosion", CodeBlockCategory.ACTION, Material.OBSIDIAN),
    GAME_SET_BLOCK("Set Block", CodeBlockCategory.ACTION, Material.OBSIDIAN),

    // Flow Control
    IF_PLAYER("If Player", CodeBlockCategory.FLOW_CONTROL, Material.RED_WOOL),
    IF_VARIABLE("If Variable", CodeBlockCategory.FLOW_CONTROL, Material.RED_WOOL),
    ELSE("Else", CodeBlockCategory.FLOW_CONTROL, Material.SMOOTH_STONE),
    END_IF("End If", CodeBlockCategory.FLOW_CONTROL, Material.SMOOTH_STONE_SLAB);


    private final String displayName;
    private final CodeBlockCategory category;
    private final Material material;

    public static CodeBlock getDefaultForCategory(CodeBlockCategory category) {
        for (CodeBlock block : values()) {
            if (block.getCategory() == category) {
                return block;
            }
        }
        return null; // Should not happen if all categories have at least one block
    }

    // NOTE: При добавлении новых значений в этот enum не забудьте обновить switch/case в парсере и исполнителе!
} 