package com.megacreative.coding;

import com.megacreative.coding.actions.*;
import com.megacreative.core.DependencyContainer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ActionFactory {

    private final Map<String, Supplier<BlockAction>> actionMap = new HashMap<>();
    private final DependencyContainer dependencyContainer;

    public ActionFactory(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
        registerAllActions();
    }
    
    private void register(String actionId, Supplier<BlockAction> supplier) {
        actionMap.put(actionId, supplier);
    }
    
    private void registerAllActions() {
        // --- ADVANCED ACTION BLOCKS ---
        register("playCustomSound", PlayCustomSoundAction::new);
        register("spawnParticleEffect", SpawnParticleEffectAction::new);
        register("sendActionBar", SendActionBarAction::new);
        register("executeAsyncCommand", ExecuteAsyncCommandAction::new);

        // --- CONTROL FLOW BLOCKS (they can have actions) ---
        register("asyncLoop", AsyncLoopAction::new);
        register("conditionalBranch", ConditionalBranchAction::new); // Assuming it has an action part
        register("timedExecution", TimedExecutionAction::new);

        // --- FUNCTION BLOCKS ---
        register("customFunction", CustomFunctionAction::new); // To define a function
        register("callFunction", CallFunctionAction::new);

        // --- DATA MANIPULATION BLOCKS ---
        register("createList", CreateListAction::new);
        register("listOperation", ListOperationAction::new);
        register("createMap", CreateMapAction::new);
        register("mapOperation", MapOperationAction::new);

        // --- INTEGRATION BLOCKS ---
        register("economyTransaction", EconomyTransactionAction::new);
        register("discordWebhook", DiscordWebhookAction::new);
        
        // --- DEBUGGING BLOCKS ---
        register("debugLog", DebugLogAction::new);
        register("variableInspector", VariableInspectorAction::new);
    }
    
    public BlockAction createAction(String actionId) {
        Supplier<BlockAction> supplier = actionMap.get(actionId);
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }
}