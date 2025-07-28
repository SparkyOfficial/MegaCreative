package ua.sparkybeta.sparkybetacreative.commands.coding;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.sparkybeta.sparkybetacreative.coding.CodingKeys;
import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BlockCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendError(sender, "This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            MessageUtils.sendError(player, "Usage: /block <block_type>");
            return true;
        }

        try {
            CodeBlock codeBlock = CodeBlock.valueOf(args[0].toUpperCase());
            ItemStack item = new ItemBuilder(codeBlock.getMaterial())
                    .setName("§b" + codeBlock.getDisplayName())
                    .setLore("§7Category: " + codeBlock.getCategory().name())
                    .build();

            item.editMeta(meta -> {
                meta.getPersistentDataContainer().set(CodingKeys.CODE_BLOCK, PersistentDataType.STRING, codeBlock.name());
            });

            player.getInventory().addItem(item);
            MessageUtils.sendSuccess(player, "You received a " + codeBlock.getDisplayName() + " block.");

        } catch (IllegalArgumentException e) {
            MessageUtils.sendError(player, "Unknown block type. Use tab-complete to see options.");
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.stream(CodeBlock.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
} 