package ua.sparkybeta.sparkybetacreative.coding.executable;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.coding.executable.resolve.ValueResolver;
import ua.sparkybeta.sparkybetacreative.coding.executable.resolved.ResolvedArgument;
import ua.sparkybeta.sparkybetacreative.coding.models.ActionBlockData;
import ua.sparkybeta.sparkybetacreative.coding.models.CodeLine;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.worlds.settings.WorldMode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class CodeExecutor implements Listener {

    private final ValueResolver valueResolver = new ValueResolver();
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SparkyWorld world = SparkyBetaCreative.getInstance().getWorldManager().getWorld(event.getPlayer());
        if (world == null || world.getMode() != WorldMode.PLAY) return;

        ExecutionContext context = new ExecutionContext(event);
        context.setVariable("player", event.getPlayer());

        findAndExecute(world, ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock.PLAYER_JOIN, context);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        SparkyWorld world = SparkyBetaCreative.getInstance().getWorldManager().getWorld(event.getPlayer());
        if (world == null || world.getMode() != WorldMode.PLAY) return;

        ExecutionContext context = new ExecutionContext(event);
        context.setVariable("player", event.getPlayer());

        findAndExecute(world, ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock.PLAYER_QUIT, context);
    }

    private void findAndExecute(SparkyWorld world, ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock eventType, ExecutionContext context) {
        Bukkit.getScheduler().runTaskAsynchronously(SparkyBetaCreative.getInstance(), () -> {
            List<CodeLine> linesToExecute = world.getCodeScript().getLines().stream()
                    .filter(line -> line.getEvent() == eventType)
                    .toList();

            // The new stateful execution logic will go here.
            // For now, let's keep the old loop to refactor it in the next step.
            for (CodeLine line : linesToExecute) {
                executeLine(line, context);
            }
        });
    }

    private void executeLine(CodeLine line, ExecutionContext context) {
        Bukkit.getScheduler().runTask(SparkyBetaCreative.getInstance(), () -> {

            Deque<Boolean> conditionStack = new ArrayDeque<>();
            boolean skip = false;

            for (ActionBlockData action : line.getActions()) {
                if (action.getType() == ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock.IF_PLAYER) {
                    boolean conditionResult = evaluatePlayerCondition(action, context);
                    conditionStack.push(conditionResult);
                    skip = !conditionResult;
                    continue;
                }

                if (action.getType() == ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock.ELSE) {
                    if (conditionStack.isEmpty()) {
                        // Syntax error
                        if(context.getPlayer() != null) context.getPlayer().sendMessage("§c[Coding] Error: 'ELSE' without 'IF'.");
                        break;
                    }
                    boolean lastCondition = conditionStack.peek();
                    skip = lastCondition; // If last was true, we skip ELSE block. If false, we execute it.
                    continue;
                }

                if (action.getType() == ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock.END_IF) {
                    if (conditionStack.isEmpty()) {
                        // Syntax error
                        if(context.getPlayer() != null) context.getPlayer().sendMessage("§c[Coding] Error: 'END IF' without 'IF'.");
                        break;
                    }
                    conditionStack.pop();
                    // Recalculate skip based on the new top of the stack
                    skip = conditionStack.isEmpty() || !conditionStack.peek();
                    continue;
                }

                if (!skip) {
                    executeSimpleAction(action, context);
                }
            }
        });
    }

    private boolean evaluatePlayerCondition(ActionBlockData action, ExecutionContext context) {
        if (context.getPlayer() == null) return false;

        // For now, we only have one condition type for IF_PLAYER, which is hasPermission.
        // We will need a more robust way to determine the condition type later.
        if (action.getArguments().isEmpty()) return false;

        ResolvedArgument<?> permArg = valueResolver.resolve(action.getArguments().getFirst(), context);
        if (permArg.getType() == ua.sparkybeta.sparkybetacreative.coding.ValueType.TEXT) {
            String permission = permArg.getValue().toString();
            return context.getPlayer().hasPermission(permission);
        }

        return false;
    }

    private void executeSimpleAction(ActionBlockData action, ExecutionContext context) {
        // The entire switch from the old executeAction method will go here.
        switch (action.getType()) {
            case PLAYER_SEND_MESSAGE:
                if (context.getPlayer() != null && !action.getArguments().isEmpty()) {
                    ResolvedArgument<?> resolvedArgument = valueResolver.resolve(action.getArguments().getFirst(), context);
                    context.getPlayer().sendMessage(Component.text(resolvedArgument.getValue().toString()));
                }
                break;
            case GAME_CREATE_EXPLOSION:
                if (context.getLocation() != null) {
                    float power = 4.0f; // Default power
                    if (!action.getArguments().isEmpty()) {
                        ResolvedArgument<?> resolvedArgument = valueResolver.resolve(action.getArguments().getFirst(), context);
                        try {
                            power = (float) (double) resolvedArgument.getValue();
                        } catch (Exception ignored) {
                        }
                    }
                    context.getLocation().getWorld().createExplosion(context.getLocation(), power);
                }
                break;
            case PLAYER_GIVE_ITEM:
                if (action.getArguments().size() >= 2) {
                    ResolvedArgument<?> playerArg = valueResolver.resolve(action.getArguments().get(0), context);
                    ResolvedArgument<?> itemArg = valueResolver.resolve(action.getArguments().get(1), context);
                    if (playerArg.getType() == ua.sparkybeta.sparkybetacreative.coding.ValueType.PLAYER && playerArg.getValue() instanceof Player targetPlayer) {
                        if (itemArg.getType() == ua.sparkybeta.sparkybetacreative.coding.ValueType.ITEM && itemArg.getValue() instanceof org.bukkit.inventory.ItemStack itemStack) {
                            targetPlayer.getInventory().addItem(itemStack.clone());
                        } else {
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage(Component.text("[Coding] Ошибка: второй аргумент не является предметом."));
                            }
                        }
                    } else {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage(Component.text("[Coding] Ошибка: первый аргумент не является игроком."));
                        }
                    }
                } else {
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage(Component.text("[Coding] Ошибка: недостаточно аргументов для Give Item."));
                    }
                }
                break;
            case PLAYER_TELEPORT:
                if (action.getArguments().size() >= 2) {
                    ResolvedArgument<?> playerArg = valueResolver.resolve(action.getArguments().get(0), context);
                    ResolvedArgument<?> locationArg = valueResolver.resolve(action.getArguments().get(1), context);
                    if (playerArg.getType() == ua.sparkybeta.sparkybetacreative.coding.ValueType.PLAYER && playerArg.getValue() instanceof Player targetPlayer) {
                        if (locationArg.getType() == ua.sparkybeta.sparkybetacreative.coding.ValueType.LOCATION && locationArg.getValue() instanceof Location location) {
                            targetPlayer.teleport(location);
                        } else {
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage(Component.text("[Coding] Ошибка: второй аргумент не является локацией."));
                            }
                        }
                    } else {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage(Component.text("[Coding] Ошибка: первый аргумент не является игроком."));
                        }
                    }
                } else {
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage(Component.text("[Coding] Ошибка: недостаточно аргументов для Teleport."));
                    }
                }
                break;
            case GAME_SET_BLOCK:
                if (action.getArguments().size() >= 2) {
                    ResolvedArgument<?> locationArg = valueResolver.resolve(action.getArguments().get(0), context);
                    ResolvedArgument<?> blockArg = valueResolver.resolve(action.getArguments().get(1), context);

                    if (locationArg.getType() == ua.sparkybeta.sparkybetacreative.coding.ValueType.LOCATION && locationArg.getValue() instanceof Location location) {
                        if (blockArg.getType() == ua.sparkybeta.sparkybetacreative.coding.ValueType.ITEM && blockArg.getValue() instanceof org.bukkit.inventory.ItemStack itemStack) {
                            Bukkit.getScheduler().runTask(SparkyBetaCreative.getInstance(), () -> {
                                location.getBlock().setType(itemStack.getType());
                            });
                        } else {
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage(Component.text("[Coding] Ошибка: второй аргумент не является предметом (ITEM)."));
                            }
                        }
                    } else {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage(Component.text("[Coding] Ошибка: первый аргумент не является локацией (LOCATION)."));
                        }
                    }
                } else {
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage(Component.text("[Coding] Ошибка: недостаточно аргументов для Set Block. Требуется: LOCATION, ITEM."));
                    }
                }
                break;
            // Пустые case-ветки для покрытия всех enum-констант
            case PLAYER_QUIT:
            case IF_VARIABLE:
            case PLAYER_HAS_PERMISSION:
            case PLAYER_JOIN:
            case ELSE:
            case IF_PLAYER:
            case PLAYER_BREAK_BLOCK:
            case END_IF:
                break;
        }
    }
} 