package ua.sparkybeta.sparkybetacreative.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.gui.CommentsMenu;
import ua.sparkybeta.sparkybetacreative.gui.WorldSettingsMenu;
import ua.sparkybeta.sparkybetacreative.util.ChatInputManager;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.comments.Comment;
import ua.sparkybeta.sparkybetacreative.util.ChatInputManager.PendingInput;
import org.bukkit.Location;
import org.bukkit.Material;
import ua.sparkybeta.sparkybetacreative.coding.CodingKeys;
import ua.sparkybeta.sparkybetacreative.coding.ValueType;
import ua.sparkybeta.sparkybetacreative.coding.models.Argument;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;


public class ChatInputListener implements Listener {

    private final ChatInputManager chatInputManager;
    private static final Map<Player, PendingArgumentInput> pendingInputs = new HashMap<>();

    public ChatInputListener(ChatInputManager chatInputManager) {
        this.chatInputManager = chatInputManager;
    }

    public static void requestArgumentInput(Player player, Location blockLocation, int argIndex, ValueType type) {
        pendingInputs.put(player, new PendingArgumentInput(blockLocation, argIndex, type));
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        chatInputManager.getPendingInput(player.getUniqueId()).ifPresent(pendingInput -> {
            event.setCancelled(true);
            chatInputManager.finishInput(player.getUniqueId());

            String message = PlainTextComponentSerializer.plainText().serialize(event.message());

            if (message.equalsIgnoreCase("cancel")) {
                MessageUtils.sendError(player, "Input cancelled.");
                openPreviousMenu(player, pendingInput);
                return;
            }

            boolean success = handleInput(player, pendingInput, message);

            if(success) {
                openPreviousMenu(player, pendingInput);
            } else {
                // If input was invalid, re-engage the input process
                chatInputManager.startInput(player.getUniqueId(), pendingInput.world(), pendingInput.type());
                MessageUtils.sendInfo(player, "Please try again, or type 'cancel' to exit.");
            }
        });
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!pendingInputs.containsKey(player)) return;
        PendingArgumentInput pending = pendingInputs.remove(player);
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(SparkyBetaCreative.getInstance(), () -> {
            if (pending.type() == ValueType.TEXT) {
                // Сохраняем текст в бочку
                saveArgumentToBarrel(pending.blockLocation(), pending.argIndex(), new Argument(ValueType.TEXT, event.getMessage()));
                player.sendMessage("§aТекст сохранён!");
            } else if (pending.type() == ValueType.ITEM) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    player.sendMessage("§cВозьмите предмет в руку!");
                    return;
                }
                saveArgumentToBarrel(pending.blockLocation(), pending.argIndex(), new Argument(ValueType.ITEM, item.clone()));
                player.sendMessage("§aПредмет сохранён!");
            }
        });
    }

    private boolean handleInput(Player player, PendingInput pendingInput, String message) {
        SparkyWorld world = pendingInput.world();
        switch (pendingInput.type()) {
            case COMMENT:
                world.addComment(new Comment(player.getUniqueId(), System.currentTimeMillis(), message));
                MessageUtils.sendSuccess(player, "Your comment has been added!");
                return true;

            case WORLD_NAME:
                world.setDisplayName(message);
                MessageUtils.sendSuccess(player, "World display name changed to: " + message);
                return true;

            case WORLD_DESCRIPTION:
                world.setDescription(message);
                MessageUtils.sendSuccess(player, "World description updated.");
                return true;

            case WORLD_ID:
                if (!message.matches("^[a-zA-Z0-9_]{3,20}$")) {
                    MessageUtils.sendError(player, "ID must be 3-20 characters long and can only contain letters, numbers, and underscores.");
                    return false;
                }
                if (SparkyBetaCreative.getInstance().getWorldManager().getWorldByCustomId(message) != null) {
                    MessageUtils.sendError(player, "This ID is already taken.");
                    return false;
                }
                world.setCustomId(message);
                MessageUtils.sendSuccess(player, "World ID changed to: " + message);
                return true;
            
            case ADD_TRUSTED_BUILDER:
            case ADD_TRUSTED_CODER:
                OfflinePlayer target = Bukkit.getOfflinePlayer(message);
                if (!target.hasPlayedBefore() && !target.isOnline()) {
                    MessageUtils.sendError(player, "Player '" + message + "' not found.");
                    return false;
                }
                
                if (pendingInput.type() == ua.sparkybeta.sparkybetacreative.util.InputType.ADD_TRUSTED_BUILDER) {
                    world.getSettings().getBuildTrusted().add(target.getUniqueId());
                    MessageUtils.sendSuccess(player, target.getName() + " has been added to builders.");
                } else {
                    world.getSettings().getCodeTrusted().add(target.getUniqueId());
                    MessageUtils.sendSuccess(player, target.getName() + " has been added to coders.");
                }
                return true;
        }
        return false;
    }

    public static void saveArgumentToBarrel(Location blockLocation, int argIndex, Argument argument) {
        Location barrelLoc = blockLocation.clone().add(0, 1, 0);
        if (barrelLoc.getBlock().getType() != Material.BARREL) {
            barrelLoc.getBlock().setType(Material.BARREL);
        }
        org.bukkit.block.Barrel barrel = (org.bukkit.block.Barrel) barrelLoc.getBlock().getState();
        ItemStack argItem = argument.getType() == ValueType.ITEM ? ((ItemStack) argument.getValue()).clone() : new ItemStack(argument.getType().getMaterial());
        argItem.editMeta(meta -> {
            meta.getPersistentDataContainer().set(CodingKeys.VALUE_TYPE, PersistentDataType.STRING, argument.getType().name());
            if (argument.getType() == ValueType.TEXT) {
                meta.displayName(Component.text(argument.getValue().toString()));
            }
            if (argument.getType() == ValueType.NUMBER) {
                meta.displayName(Component.text(argument.getValue().toString()));
            }
            if (argument.getType() == ValueType.LOCATION) {
                meta.displayName(Component.text("X:" + ((org.bukkit.Location)argument.getValue()).getBlockX() + " Y:" + ((org.bukkit.Location)argument.getValue()).getBlockY() + " Z:" + ((org.bukkit.Location)argument.getValue()).getBlockZ()));
            }
        });
        ItemStack[] contents = barrel.getInventory().getContents();
        if (argIndex < contents.length) {
            contents[argIndex] = argItem;
        } else {
            // Если индекс больше размера — расширяем
            ItemStack[] newContents = new ItemStack[argIndex + 1];
            System.arraycopy(contents, 0, newContents, 0, contents.length);
            newContents[argIndex] = argItem;
            contents = newContents;
        }
        barrel.getInventory().setContents(contents);
        barrel.update();
    }

    private void openPreviousMenu(Player player, PendingInput pendingInput) {
        Bukkit.getScheduler().runTask(SparkyBetaCreative.getInstance(), () -> {
            switch (pendingInput.type()) {
                case COMMENT:
                    new CommentsMenu(player, pendingInput.world()).open();
                    break;
                case WORLD_NAME, WORLD_DESCRIPTION, WORLD_ID:
                case ADD_TRUSTED_BUILDER, ADD_TRUSTED_CODER:
                    new WorldSettingsMenu(player, pendingInput.world()).open();
                    break;
            }
        });
    }

    private record PendingArgumentInput(Location blockLocation, int argIndex, ValueType type) {}
} 