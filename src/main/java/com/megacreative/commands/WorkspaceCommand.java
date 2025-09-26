package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для открытия расширенного рабочего пространства кодирования
 * Использование: /workspace [open|close|info]
 *
 * Command to open the advanced coding workspace
 * Usage: /workspace [open|close|info]
 *
 * Befehl zum Öffnen des erweiterten Codier-Arbeitsbereichs
 * Verwendung: /workspace [open|close|info]
 */
public class WorkspaceCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует команду рабочего пространства
     * @param plugin основной экземпляр плагина
     *
     * Initializes the workspace command
     * @param plugin main plugin instance
     *
     * Initialisiert den Arbeitsbereichsbefehl
     * @param plugin Haupt-Plugin-Instanz
     */
    public WorkspaceCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды рабочего пространства
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles workspace command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Arbeitsbereichsbefehls
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player is in a creative world
        CreativeWorld world = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world == null) {
            player.sendMessage("§cYou must be in a creative world to use the coding workspace!");
            return true;
        }
        
        // Check coding permissions
        if (!world.canCode(player)) {
            player.sendMessage("§cYou don't have permission to code in this world!");
            return true;
        }
        
        String action = args.length > 0 ? args[0].toLowerCase() : "open";
        
        switch (action) {
            case "open":
                openAdvancedWorkspace(player, world);
                break;
                
            case "close":
                player.sendMessage("§aWorkspace closed!");
                break;
                
            case "info":
                showWorkspaceInfo(player, world);
                break;
                
            default:
                player.sendMessage("§cUsage: /workspace [open|close|info]");
                break;
        }
        
        return true;
    }
    
    /**
     * Отображает информацию о рабочем пространстве
     * @param player игрок, которому отправляется информация
     * @param world творческий мир
     *
     * Displays workspace information
     * @param player player to send information to
     * @param world creative world
     *
     * Zeigt Arbeitsbereichsinformationen an
     * @param player Spieler, dem die Informationen gesendet werden
     * @param world Creative-Welt
     */
    private void showWorkspaceInfo(Player player, CreativeWorld world) {
        player.sendMessage("§6§l=== MegaCreative Coding Workspace ===");
        player.sendMessage("§aWorld: §f" + world.getName());
        player.sendMessage("§aOwner: §f" + world.getOwnerName());
        player.sendMessage("§aMode: §f" + world.getMode().name());
        player.sendMessage("");
        player.sendMessage("§eFeatures:");
        player.sendMessage("§7• §fDrag & Drop block coding");
        player.sendMessage("§7• §fReal-time visual feedback");
        player.sendMessage("§7• §fAdvanced variable system");
        player.sendMessage("§7• §fCollaborative editing");
        player.sendMessage("§7• §fTemplate library");
        player.sendMessage("§7• §fBuilt-in debugger");
        player.sendMessage("");
        player.sendMessage("§aUse §e/workspace open §ato start coding!");
    }
    
    /**
     * Открывает расширенное рабочее пространство
     * @param player игрок, для которого открывается рабочее пространство
     * @param world творческий мир
     *
     * Opens the advanced workspace
     * @param player player for whom workspace is opened
     * @param world creative world
     *
     * Öffnet den erweiterten Arbeitsbereich
     * @param player Spieler, für den der Arbeitsbereich geöffnet wird
     * @param world Creative-Welt
     */
    private void openAdvancedWorkspace(Player player, CreativeWorld world) {
        player.sendMessage("§6§l=== MegaCreative Advanced Workspace ===");
        player.sendMessage("§aOpening advanced coding environment...");
        player.sendMessage("");
        player.sendMessage("§eFeatures Available:");
        player.sendMessage("§7• §fVisual block placement on structured platform");
        player.sendMessage("§7• §fAutomatic block connections");
        player.sendMessage("§7• §fAdvanced variable system");
        player.sendMessage("§7• §fReal-time execution with visual feedback");
        player.sendMessage("§7• §fContainer-based parameter configuration");
        player.sendMessage("");
        player.sendMessage("§aUse §e/dev §ato enter development mode!");
        
        // Teleport to dev world if not already there
        if (!player.getWorld().getName().endsWith("_dev")) {
            player.performCommand("dev");
        }
    }
}