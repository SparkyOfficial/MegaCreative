package com.megacreative.coding;

import com.google.common.base.CaseFormat;
import com.megacreative.MegaCreative;
import com.megacreative.coding.actions.*;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class NewScriptExecutionEngine {

    private final MegaCreative plugin;
    private final ExecutorService executor;
    private final Map<UUID, Set<CodeScript>> playerScripts = new ConcurrentHashMap<>();
    private final Set<CodeScript> activeScripts = ConcurrentHashMap.newKeySet();
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final Map<String, BlockAction> actions = new ConcurrentHashMap<>();

    public NewScriptExecutionEngine(MegaCreative plugin) {
        this.plugin = plugin;
        this.executor = Executors.newFixedThreadPool(4);
        registerActions();
    }

    private String getActionNameFromClass(Class<? extends BlockAction> clazz) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, clazz.getSimpleName().replace("Action", ""));
    }

    private void registerAction(BlockAction action) {
        actions.put(getActionNameFromClass(action.getClass()), action);
    }

    private void registerActions() {
        registerAction(new AddVarAction());
        registerAction(new BroadcastAction());
        registerAction(new CallFunctionAction());
        registerAction(new CommandAction());
        registerAction(new DivVarAction());
        registerAction(new EffectAction());
        registerAction(new ExplosionAction());
        registerAction(new GetGlobalVariableAction());
        registerAction(new GetPlayerNameAction());
        registerAction(new GetServerVariableAction());
        registerAction(new GetVarAction());
        registerAction(new GiveItemAction());
        registerAction(new GiveItemsAction());
        registerAction(new HealPlayerAction());
        registerAction(new MulVarAction());
        registerAction(new PlayParticleEffectAction());
        registerAction(new PlaySoundAction());
        registerAction(new RandomNumberAction());
        registerAction(new RemoveItemsAction());
        registerAction(new RepeatAction());
        registerAction(new RepeatTriggerAction());
        registerAction(new SaveFunctionAction());
        registerAction(new SendMessageAction());
        registerAction(new SetArmorAction());
        registerAction(new SetBlockAction());
        registerAction(new SetGameModeAction());
        registerAction(new SetGlobalVariableAction());
        registerAction(new SetServerVariableAction());
        registerAction(new SetTimeAction());
        registerAction(new SetVarAction());
        registerAction(new SetWeatherAction());
        registerAction(new SpawnEntityAction());
        registerAction(new SpawnMobAction());
        registerAction(new SubVarAction());
        registerAction(new TeleportAction());
        registerAction(new TestMessageAction());
        registerAction(new WaitAction());
    }

    public void execute(CodeScript script, ExecutionContext initialContext) {
        if (script == null || initialContext == null || initialContext.getPlayer() == null) {
            return;
        }

        Player player = initialContext.getPlayer();
        UUID playerId = player.getUniqueId();

        playerScripts.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet());

        if (playerScripts.get(playerId).size() >= 10) {
            player.sendMessage("§cYou have too many scripts running at once.");
            return;
        }

        activeScripts.add(script);
        totalExecutions.incrementAndGet();
        long startTime = System.currentTimeMillis();

        executor.submit(() -> {
            try {
                CodeBlock currentBlock = script.getRootBlock();
                int operations = 0;

                while (currentBlock != null) {
                    if (System.currentTimeMillis() - startTime > 5000) {
                        player.sendMessage("§cScript timed out!");
                        break;
                    }

                    if (operations++ > 1000) { // Security limit
                        player.sendMessage("§cScript exceeded operation limit!");
                        break;
                    }

                    BlockAction action = actions.get(currentBlock.getAction());

                    if (action != null) {
                        // Create a new context for this specific block, using the builder pattern.
                        ExecutionContext blockContext = new ExecutionContext.Builder(initialContext)
                                .codeBlock(currentBlock)
                                .build();
                        action.execute(blockContext);
                    } else {
                        plugin.getLogger().warning("Unknown action in new engine: " + currentBlock.getAction());
                    }

                    currentBlock = currentBlock.getNextBlock();
                }
            } finally {
                activeScripts.remove(script);
                if (playerScripts.containsKey(playerId)) {
                    playerScripts.get(playerId).remove(script);
                }
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }

    public Statistics getStatistics() {
        return new Statistics(activeScripts.size(), totalExecutions.intValue());
    }

    public static class Statistics {
        private final int activeScripts;
        private final int totalExecutions;

        public Statistics(int activeScripts, int totalExecutions) {
            this.activeScripts = activeScripts;
            this.totalExecutions = totalExecutions;
        }

        public int getActiveScripts() {
            return activeScripts;
        }

        public int getTotalExecutions() {
            return totalExecutions;
        }
    }
}
