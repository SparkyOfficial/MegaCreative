package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.comments.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommentsMenu extends AbstractMenu {
    private final SparkyWorld world;
    private int page = 0;
    private final int pageSize = 45;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public CommentsMenu(Player player, SparkyWorld world) {
        super(player, 54, "Comments for: " + world.getDisplayName());
        this.world = world;
        redraw();
    }

    public void redraw() {
        inventory.clear();
        List<Comment> comments = world.getComments();

        // Pagination buttons
        if (page > 0) {
            setButton(45, new ItemBuilder(Material.ARROW).setName("§aPrevious Page").build(), (e) -> {
                page--;
                redraw();
            });
        }
        if ((page + 1) * pageSize < comments.size()) {
            setButton(53, new ItemBuilder(Material.ARROW).setName("§aNext Page").build(), (e) -> {
                page++;
                redraw();
            });
        }

        // Back button
        setButton(48, new ItemBuilder(Material.BARRIER).setName("§cBack").build(), (e) ->
                new WorldActionMenu(player, world).open());

        // Add comment button
        setButton(50, new ItemBuilder(Material.WRITABLE_BOOK).setName("§aWrite a Comment").build(), (e) -> {
            player.closeInventory();
            SparkyBetaCreative.getInstance().getChatInputManager().startInput(player.getUniqueId(), world, ua.sparkybeta.sparkybetacreative.util.InputType.COMMENT);
            player.sendMessage("§aEnter your comment in the chat. Type 'cancel' to abort.");
        });

        // Display comments
        int startIndex = page * pageSize;
        for (int i = 0; i < pageSize; i++) {
            int commentIndex = startIndex + i;
            if (commentIndex >= comments.size()) break;

            Comment comment = comments.get(commentIndex);
            OfflinePlayer author = Bukkit.getOfflinePlayer(comment.getAuthor());

            ItemStack commentItem = new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§e" + (author.getName() != null ? author.getName() : "Unknown"))
                    .setLore(
                            "§7" + comment.getText(),
                            "",
                            "§8" + DATE_FORMAT.format(new Date(comment.getTimestamp()))
                    )
                    .build();

            SkullMeta skullMeta = (SkullMeta) commentItem.getItemMeta();
            skullMeta.setOwningPlayer(author);
            commentItem.setItemMeta(skullMeta);

            setButton(i, commentItem, (e) -> {
                // Clicking the head does nothing for now
            });

            // Add delete button if the player is the author
            if (player.getUniqueId().equals(comment.getAuthor())) {
                setButton(i, new ItemBuilder(Material.REDSTONE_BLOCK).setName("§cClick to delete your comment").build(), (e) -> {
                    world.removeComment(comment);
                    redraw();
                });
            }
        }
    }
} 