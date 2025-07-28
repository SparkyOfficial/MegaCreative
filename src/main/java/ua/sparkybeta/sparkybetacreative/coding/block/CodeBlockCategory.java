package ua.sparkybeta.sparkybetacreative.coding.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public enum CodeBlockCategory {
    EVENT("Event", Material.DIAMOND_BLOCK),
    CONDITION("Condition", Material.EMERALD_BLOCK),
    ACTION("Action", Material.GOLD_BLOCK),
    FLOW_CONTROL("Flow Control", Material.RED_WOOL);

    private final String displayName;
    private final Material material;
} 