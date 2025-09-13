package com.megacreative.coding.actions.economy;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EconomyTransactionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get parameters from the block
            DataValue operationValue = block.getParameter("operation");
            DataValue amountValue = block.getParameter("amount");
            DataValue targetValue = block.getParameter("target");
            
            if (operationValue == null || operationValue.isEmpty()) {
                return ExecutionResult.error("Operation parameter is missing.");
            }
            
            if (amountValue == null || amountValue.isEmpty()) {
                return ExecutionResult.error("Amount parameter is missing.");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedOperation = resolver.resolve(context, operationValue);
            DataValue resolvedAmount = resolver.resolve(context, amountValue);
            DataValue resolvedTarget = targetValue != null ? resolver.resolve(context, targetValue) : null;
            
            String operation = resolvedOperation.asString().toLowerCase();
            double amount = resolvedAmount.asNumber().doubleValue();
            String target = resolvedTarget != null ? resolvedTarget.asString() : null;
            
            // Check if Vault is available
            if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
                return ExecutionResult.error("Vault plugin is not installed or enabled. Economy transactions require Vault.");
            }
            
            // Try to load Vault classes dynamically to avoid compilation issues
            try {
                Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
                Class<?> registeredServiceProviderClass = Class.forName("org.bukkit.plugin.RegisteredServiceProvider");
                
                Object rsp = Bukkit.getServicesManager().getRegistration(economyClass);
                if (rsp == null) {
                    return ExecutionResult.error("Economy service not found. Make sure an economy plugin is installed.");
                }
                
                Object economy = registeredServiceProviderClass.getMethod("getProvider").invoke(rsp);
                
                // Perform the transaction based on operation type
                switch (operation) {
                    case "give":
                        if (target != null && !target.isEmpty()) {
                            Player targetPlayer = Bukkit.getPlayer(target);
                            if (targetPlayer != null) {
                                economyClass.getMethod("depositPlayer", Player.class, double.class).invoke(economy, targetPlayer, amount);
                                return ExecutionResult.success("Gave " + amount + " to " + target);
                            } else {
                                return ExecutionResult.error("Target player '" + target + "' not found.");
                            }
                        } else {
                            economyClass.getMethod("depositPlayer", Player.class, double.class).invoke(economy, player, amount);
                            return ExecutionResult.success("Gave " + amount + " to player");
                        }
                        
                    case "take":
                        if (target != null && !target.isEmpty()) {
                            Player targetPlayer = Bukkit.getPlayer(target);
                            if (targetPlayer != null) {
                                boolean hasFunds = (Boolean) economyClass.getMethod("has", Player.class, double.class).invoke(economy, targetPlayer, amount);
                                if (hasFunds) {
                                    economyClass.getMethod("withdrawPlayer", Player.class, double.class).invoke(economy, targetPlayer, amount);
                                    return ExecutionResult.success("Took " + amount + " from " + target);
                                } else {
                                    return ExecutionResult.error("Player " + target + " doesn't have enough money.");
                                }
                            } else {
                                return ExecutionResult.error("Target player '" + target + "' not found.");
                            }
                        } else {
                            boolean hasFunds = (Boolean) economyClass.getMethod("has", Player.class, double.class).invoke(economy, player, amount);
                            if (hasFunds) {
                                economyClass.getMethod("withdrawPlayer", Player.class, double.class).invoke(economy, player, amount);
                                return ExecutionResult.success("Took " + amount + " from player");
                            } else {
                                return ExecutionResult.error("Player doesn't have enough money.");
                            }
                        }
                        
                    case "check":
                        double balance;
                        if (target != null && !target.isEmpty()) {
                            Player targetPlayer = Bukkit.getPlayer(target);
                            if (targetPlayer != null) {
                                balance = (Double) economyClass.getMethod("getBalance", Player.class).invoke(economy, targetPlayer);
                            } else {
                                return ExecutionResult.error("Target player '" + target + "' not found.");
                            }
                        } else {
                            balance = (Double) economyClass.getMethod("getBalance", Player.class).invoke(economy, player);
                        }
                        
                        // Store balance in a variable for use in other blocks
                        context.setVariable("economy_balance", balance);
                        return ExecutionResult.success("Balance: " + balance);
                        
                    default:
                        return ExecutionResult.error("Invalid operation: " + operation);
                }
            } catch (ClassNotFoundException e) {
                return ExecutionResult.error("Vault classes not found. Make sure Vault is properly installed.");
            } catch (Exception e) {
                return ExecutionResult.error("Error during economy transaction: " + e.getMessage());
            }

        } catch (Exception e) {
            return ExecutionResult.error("Error during economy transaction: " + e.getMessage());
        }
    }
}