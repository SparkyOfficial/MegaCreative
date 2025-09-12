package com.megacreative.coding;

import com.megacreative.coding.actions.*;
import com.megacreative.coding.actions.FunctionCallAction;
import com.megacreative.coding.actions.DefineFunctionAction;
import com.megacreative.coding.actions.ReturnAction;
import com.megacreative.coding.executors.ExecutionResult;
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
        // --- BASIC PLAYER ACTIONS ---
        register("sendMessage", SendMessageAction::new);
        register("teleport", TeleportAction::new);
        register("giveItem", GiveItemAction::new);
        register("playSound", PlaySoundAction::new);
        register("effect", EffectAction::new);
        register("broadcast", BroadcastAction::new);
        register("spawnMob", SpawnMobAction::new);
        register("healPlayer", HealPlayerAction::new);
        register("setGameMode", SetGameModeAction::new);
        register("setTime", SetTimeAction::new);
        register("setWeather", SetWeatherAction::new);
        register("command", CommandAction::new);
        register("setVar", SetVarAction::new);
        register("getVar", GetVarAction::new);
        
        // --- NEW BASIC PLAYER ACTIONS ---
        register("giveItems", GiveItemsAction::new);
        register("spawnEntity", SpawnEntityAction::new);
        register("removeItems", RemoveItemsAction::new);
        register("setArmor", SetArmorAction::new);
        register("setGlobalVar", SetGlobalVarAction::new);
        register("getGlobalVar", GetGlobalVarAction::new);
        register("setServerVar", SetServerVarAction::new);
        register("getServerVar", GetServerVarAction::new);
        register("wait", WaitAction::new);
        register("randomNumber", RandomNumberAction::new);
        register("playParticle", PlayParticleAction::new);
        register("sendTitle", SendTitleAction::new);
        register("explosion", ExplosionAction::new);
        register("setBlock", SetBlockAction::new);
        register("getPlayerName", GetPlayerNameAction::new);
        
        // --- VARIABLE MANIPULATION ACTIONS ---
        register("addVar", AddVarAction::new);
        register("subVar", SubVarAction::new);
        register("mulVar", MulVarAction::new);
        register("divVar", DivVarAction::new);
        
        // --- SCOREBOARD ACTIONS ---
        register("createScoreboard", CreateScoreboardAction::new);
        register("setScore", SetScoreAction::new);
        register("incrementScore", IncrementScoreAction::new);
        register("createTeam", CreateTeamAction::new);
        register("addPlayerToTeam", AddPlayerToTeamAction::new);
        
        // --- LOCATION ACTIONS ---
        register("saveLocation", SaveLocationAction::new);
        register("getLocation", GetLocationAction::new);
        
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
        
        // 🎆 FrameLand: Advanced Function System
        register("define_function", () -> new DefineFunctionAction((com.megacreative.MegaCreative) dependencyContainer.resolve(com.megacreative.MegaCreative.class)));
        register("call_function", () -> new FunctionCallAction((com.megacreative.MegaCreative) dependencyContainer.resolve(com.megacreative.MegaCreative.class)));
        register("return", () -> new ReturnAction((com.megacreative.MegaCreative) dependencyContainer.resolve(com.megacreative.MegaCreative.class)));

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
        
        // === GENERIC ACTIONS - Mass Production System ===
        // Register all simple actions to use the GenericAction handler
        registerGenericActions();
    }
    
    /**
     * Register all simple actions that can be handled by GenericAction
     * This enables rapid addition of new functionality without creating new classes
     */
    private void registerGenericActions() {
        // Player actions
        registerGeneric("sendActionBar");
        registerGeneric("sendTitle");
        registerGeneric("setHealth");
        registerGeneric("setFood");
        registerGeneric("addPotionEffect");
        registerGeneric("removePotionEffect");
        registerGeneric("playSound");
        registerGeneric("setGameMode");
        
        // World actions
        registerGeneric("setBlock");
        registerGeneric("breakBlock");
        registerGeneric("setTime");
        registerGeneric("setWeather");
        
        // Item actions
        registerGeneric("giveItem");
        registerGeneric("removeItem");
        
        // Economy actions (if vault available)
        registerGeneric("giveMoney");
        registerGeneric("takeMoney");
        
        // Permission actions
        registerGeneric("givePermission");
        registerGeneric("removePermission");
    }
    
    /**
     * Helper method to register a generic action
     */
    private void registerGeneric(String actionId) {
        register(actionId, GenericAction::new);
    }
    
    public BlockAction createAction(String actionId) {
        Supplier<BlockAction> supplier = actionMap.get(actionId);
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }
    
    // Add missing methods with proper implementation
    public void registerAction(String actionId, String displayName) {
        // Register the action if it's not already registered
        if (!actionMap.containsKey(actionId)) {
            // For now, we'll register a generic action placeholder
            // In a more advanced implementation, you might want to store display names separately
            register(actionId, () -> new BlockAction() {
                @Override
                public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
                    return ExecutionResult.error("Action not implemented: " + actionId);
                }
            });
        }
    }
    
    public int getActionCount() {
        return actionMap.size();
    }
}