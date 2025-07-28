package ua.sparkybeta.sparkybetacreative.commands.coding;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.coding.CodingKeys;
import ua.sparkybeta.sparkybetacreative.coding.ValueType;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.settings.WorldMode;

import java.util.StringJoiner;

public class ValueCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendError(sender, "This command can only be run by a player.");
            return true;
        }

        SparkyWorld world = SparkyBetaCreative.getInstance().getWorldManager().getWorld(player);
        if (world == null || world.getMode() != WorldMode.DEV) {
            MessageUtils.sendError(player, "This command can only be used in a DEV world.");
            return true;
        }

        String type = label.toLowerCase();
        StringJoiner data = new StringJoiner(" ");
        for (String arg : args) {
            data.add(arg);
        }
        
        ItemStack itemStack = null;
        try {
            switch (type) {
                case "text" -> {
                    itemStack = new ItemBuilder(ValueType.TEXT.getMaterial())
                        .setName("§eText: §f" + data)
                        .build();
                }
                case "number" -> {
                    double number = args.length > 0 ? Double.parseDouble(args[0]) : 0.0;
                    itemStack = new ItemBuilder(ValueType.NUMBER.getMaterial())
                        .setName("§aNumber: §f" + number)
                        .build();
                }
                case "location" -> {
                    Location loc = player.getLocation();
                    if(args.length >= 3) {
                        loc = new Location(player.getWorld(), Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]));
                    }
                    String locString = String.format("%.2f, %.2f, %.2f", loc.getX(), loc.getY(), loc.getZ());
                    itemStack = new ItemBuilder(ValueType.LOCATION.getMaterial())
                            .setName("§bLocation: §f" + locString)
                            .build();
                }
                case "boolean" -> {
                    boolean bool = args.length > 0 && Boolean.parseBoolean(args[0]);
                    itemStack = new ItemBuilder(ValueType.BOOLEAN.getMaterial())
                            .setName("§6Boolean: §f" + bool)
                            .build();
                }
            }
        } catch (Exception e) {
            MessageUtils.sendError(player, "Invalid arguments for the command.");
            return true;
        }


        if (itemStack != null) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                ValueType valueType = ValueType.getByMaterial(itemStack.getType());
                meta.getPersistentDataContainer().set(CodingKeys.VALUE_TYPE, PersistentDataType.STRING, valueType.name());
                itemStack.setItemMeta(meta);
            }
            player.getInventory().addItem(itemStack);
            MessageUtils.sendSuccess(player, "You have received a " + type + " value item.");
        } else {
            MessageUtils.sendError(player, "Unknown value type: " + type);
        }
        
        return true;
    }
} 