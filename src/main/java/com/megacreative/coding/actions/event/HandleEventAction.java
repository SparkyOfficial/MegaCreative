package com.megacreative.coding.actions.event;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import java.util.Optional;

/**
 * Action that registers a code block as an event handler
 */
@BlockMeta(id = "handleEvent", displayName = "§aHandle Event", type = BlockType.ACTION)
public class HandleEventAction implements BlockAction {
    private static final String PARAM_EVENT_NAME = "eventName";
    private static final String PARAM_PRIORITY = "priority";
    private static final String PARAM_WORLD = "worldName";
    private static final String PARAM_GLOBAL = "global";
    private static final String MSG_EVENT_REQUIRED = "§cEvent name is required";
    private static final String MSG_INVALID_EVENT = "§cInvalid event name";
    private static final String MSG_REGISTERED = "§a✓ Registered handler for event: %s %s (priority: %d)";
    private static final String MSG_ERROR = "§c✗ Failed to register event handler: %s";
    
    private final CustomEventManager eventManager;
    
    public HandleEventAction(CustomEventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    @Override
    public com.megacreative.coding.executors.ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            validateAndRegisterHandler(block, context);
            return com.megacreative.coding.executors.ExecutionResult.success("Event handler registered");
        } catch (Exception e) {
            Player player = context.getPlayer();
            if (player != null) {
                player.sendMessage(String.format(MSG_ERROR, e.getMessage()));
            }
            return com.megacreative.coding.executors.ExecutionResult.error("Failed to register event handler: " + e.getMessage());
        }
    }
    
    private void validateAndRegisterHandler(CodeBlock block, ExecutionContext context) {
        Player player = validateContext(context);
        ParameterResolver resolver = new ParameterResolver(context);
        
        String eventName = resolveEventName(block, resolver, context);
        int priority = resolvePriority(block, resolver, context);
        String worldName = resolveWorldName(block, resolver, context);
        boolean isGlobal = resolveIsGlobal(block, resolver, context);
        
        registerEventHandler(player, block, eventName, priority, worldName, isGlobal);
    }
    
    private Player validateContext(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null || context.getCurrentBlock() == null || context.getPlugin().getServiceRegistry().getVariableManager() == null) {
            throw new IllegalStateException("Invalid execution context");
        }
        return player;
    }
    
    private String resolveEventName(CodeBlock block, ParameterResolver resolver, ExecutionContext context) {
        DataValue eventNameParam = block.getParameter(PARAM_EVENT_NAME);
        if (eventNameParam == null) {
            throw new IllegalArgumentException(MSG_EVENT_REQUIRED);
        }
        
        String eventName = resolver.resolve(context, eventNameParam).asString();
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new IllegalArgumentException(MSG_INVALID_EVENT);
        }
        
        return eventName;
    }
    
    private int resolvePriority(CodeBlock block, ParameterResolver resolver, ExecutionContext context) {
        DataValue priorityParam = block.getParameter(PARAM_PRIORITY);
        if (priorityParam == null) {
            return 0; // Default priority
        }
        
        try {
            DataValue resolved = resolver.resolve(context, priorityParam);
            return resolved != null ? resolved.asNumber().intValue() : 0;
        } catch (NumberFormatException e) {
            return 0; // Default priority on error
        }
    }
    
    private String resolveWorldName(CodeBlock block, ParameterResolver resolver, ExecutionContext context) {
        DataValue worldParam = block.getParameter(PARAM_WORLD);
        if (worldParam == null) {
            return null;
        }
        
        DataValue resolved = resolver.resolve(context, worldParam);
        return resolved != null ? resolved.asString() : null;
    }
    
    private boolean resolveIsGlobal(CodeBlock block, ParameterResolver resolver, ExecutionContext context) {
        DataValue globalParam = block.getParameter(PARAM_GLOBAL);
        if (globalParam == null) {
            return false;
        }
        
        DataValue resolved = resolver.resolve(context, globalParam);
        if (resolved == null) {
            return false;
        }
        
        String value = resolved.asString();
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }
    
    private void registerEventHandler(Player player, CodeBlock block, 
                                    String eventName, int priority, String worldName, boolean isGlobal) {
        String effectiveWorldName = getEffectiveWorldName(player, worldName, isGlobal);
        
        CustomEventManager.EventHandler handler = eventManager.createEventHandler(
            block, 
            isGlobal ? null : player, 
            effectiveWorldName,
            priority
        );
        
        eventManager.registerEventHandler(eventName, handler);
        
        String scope = getScopeString(worldName, isGlobal);
        player.sendMessage(String.format(MSG_REGISTERED, eventName, scope, priority));
    }
    
    private String getEffectiveWorldName(Player player, String worldName, boolean isGlobal) {
        if (isGlobal) {
            return null;
        }
        return worldName != null ? worldName : player.getWorld().getName();
    }
    
    private String getScopeString(String worldName, boolean isGlobal) {
        if (isGlobal) {
            return "globally";
        }
        return worldName != null ? "in world " + worldName : "in current world";
    }
}