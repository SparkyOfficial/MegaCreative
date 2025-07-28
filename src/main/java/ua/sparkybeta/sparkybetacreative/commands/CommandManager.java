package ua.sparkybeta.sparkybetacreative.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.sparkybeta.sparkybetacreative.util.MessageUtils;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public void register(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            MessageUtils.sendInfo(sender, "Available commands: /world create");
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            MessageUtils.sendError(sender, "Unknown command. Use /world for help.");
            return true;
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            MessageUtils.sendError(sender, "You do not have permission to use this command.");
            return true;
        }

        String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.onCommand(sender, command, label, subCommandArgs);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .filter(name -> {
                        SubCommand sub = subCommands.get(name);
                        return sub.getPermission() == null || sender.hasPermission(sub.getPermission());
                    })
                    .collect(Collectors.toList());
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand != null) {
            if (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission())) {
                String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.onTabComplete(sender, command, label, subCommandArgs);
            }
        }

        return new ArrayList<>();
    }
} 