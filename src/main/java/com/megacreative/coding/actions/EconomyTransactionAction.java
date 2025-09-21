package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Исключение, возникающее при ошибках в операциях с экономикой.
 */
class EconomyException extends Exception {
    public EconomyException(String message) {
        super(message);
    }

    public EconomyException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class EconomyTransactionAction implements BlockAction {
    // Константы для сообщений
    private static final String PLAYER_NOT_FOUND = "Игрок не найден.";
    private static final String VAULT_NOT_FOUND = "Плагин Vault не найден или не включен. Для работы с экономикой требуется Vault.";
    private static final String ECONOMY_PROVIDER_NOT_FOUND = "Провайдер экономики не найден. Убедитесь, что установлен плагин экономики.";
    private static final String INSUFFICIENT_FUNDS = "Недостаточно средств.";
    
    // Константы для работы с экономикой
    private static final String ECONOMY_BALANCE_VAR = "economy_balance";
    private static final String VAULT_ECONOMY_CLASS = "net.milkbowl.vault.economy.Economy";
    private static final String REGISTERED_SERVICE_PROVIDER_CLASS = "org.bukkit.plugin.RegisteredServiceProvider";
    
    // Константы для параметров
    private static final String PARAM_OPERATION = "operation";
    private static final String PARAM_AMOUNT = "amount";
    private static final String PARAM_TARGET = "target";
    
    // Допустимые операции
    private static final String OP_GIVE = "give";
    private static final String OP_TAKE = "take";
    private static final String OP_CHECK = "check";

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get and validate parameters
            OperationParameters params = getAndValidateParameters(block, context);
            if (params.error() != null) {
                return ExecutionResult.error(params.error());
            }

            // Check if Vault is available
            if (!isVaultAvailable()) {
                return ExecutionResult.error("Vault plugin is not installed or enabled. Economy transactions require Vault.");
            }

            // Get economy provider
            Object economy = getEconomyProvider();
            if (economy == null) {
                return ExecutionResult.error("Economy service not found. Make sure an economy plugin is installed.");
            }

            // Выполняем запрошенную операцию
            String operation = params.operation().toLowerCase();
            return switch (operation) {
                case OP_GIVE -> executeGive(economy, player, params.amount(), params.target());
                case OP_TAKE -> executeTake(economy, player, params.amount(), params.target());
                case OP_CHECK -> executeCheck(economy, player, params.target(), context);
                default -> ExecutionResult.error(String.format("Недопустимая операция: %s", operation));
            };

        } catch (EconomyException e) {
            return ExecutionResult.error("Ошибка экономики: " + e.getMessage() + 
                (e.getCause() != null ? " (" + e.getCause().getMessage() + ")" : ""));
        }
    }

    private OperationParameters getAndValidateParameters(CodeBlock block, ExecutionContext context) throws Exception {
        DataValue operationValue = block.getParameter("operation");
        DataValue amountValue = block.getParameter("amount");
        DataValue targetValue = block.getParameter("target");
        
        if (operationValue == null || operationValue.isEmpty()) {
            return new OperationParameters("Operation parameter is missing.");
        }
        
        ParameterResolver resolver = new ParameterResolver(context);
        String operation = resolver.resolve(context, operationValue).asString();
        
        // Only validate amount for operations that need it
        if (!operation.equalsIgnoreCase("check") && (amountValue == null || amountValue.isEmpty())) {
            return new OperationParameters("Amount parameter is missing for operation: " + operation);
        }
        
        double amount = 0;
        if (amountValue != null) {
            DataValue resolvedAmount = resolver.resolve(context, amountValue);
            amount = resolvedAmount.asNumber().doubleValue();
        }
        
        String target = null;
        if (targetValue != null) {
            target = resolver.resolve(context, targetValue).asString();
        }
        
        return new OperationParameters(operation, amount, target);
    }

    private boolean isVaultAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }

    /**
     * Получает провайдер экономики через Vault API.
     * @return Объект экономического провайдера
     * @throws EconomyException если возникла ошибка при получении провайдера экономики
     */
    private Object getEconomyProvider() throws EconomyException {
        try {
            Class<?> economyClass = Class.forName(VAULT_ECONOMY_CLASS);
            Class<?> registeredServiceProviderClass = Class.forName(REGISTERED_SERVICE_PROVIDER_CLASS);
            
            Object rsp = Bukkit.getServicesManager().getRegistration(economyClass);
            return rsp != null ? registeredServiceProviderClass.getMethod("getProvider").invoke(rsp) : null;
        } catch (ClassNotFoundException e) {
            throw new EconomyException("Не удалось загрузить классы Vault. Убедитесь, что Vault установлен корректно.", e);
        } catch (ReflectiveOperationException e) {
            throw new EconomyException("Ошибка при получении провайдера экономики через рефлексию.", e);
        }
    }

    /**
     * Выполняет операцию выдачи денег.
     */
    private ExecutionResult executeGive(Object economy, Player defaultPlayer, double amount, String targetName) {
        try {
            Player target = getTargetPlayer(defaultPlayer, targetName);
            if (target == null) {
                return errorPlayerNotFound(targetName);
            }

            Class<?> economyClass = economy.getClass();
            economyClass.getMethod("depositPlayer", Player.class, double.class)
                       .invoke(economy, target, amount);
                       
            String message = targetName != null 
                ? String.format("Выдано %.2f игроку %s", amount, targetName)
                : String.format("Выдано %.2f игроку", amount);
                
            return ExecutionResult.success(message);
        } catch (ReflectiveOperationException e) {
            return ExecutionResult.error("Ошибка при выполнении операции выдачи денег: " + e.getMessage());
        }
    }

    /**
     * Выполняет операцию списания денег.
     */
    private ExecutionResult executeTake(Object economy, Player defaultPlayer, double amount, String targetName) {
        try {
            Player target = getTargetPlayer(defaultPlayer, targetName);
            if (target == null) {
                return errorPlayerNotFound(targetName);
            }

            Class<?> economyClass = economy.getClass();
            boolean hasFunds = (Boolean) economyClass.getMethod("has", Player.class, double.class)
                                                  .invoke(economy, target, amount);
                                                  
            if (!hasFunds) {
                String errorMsg = targetName != null 
                    ? String.format("У игрока %s %s", targetName, INSUFFICIENT_FUNDS)
                    : INSUFFICIENT_FUNDS;
                return ExecutionResult.error(errorMsg);
            }

            economyClass.getMethod("withdrawPlayer", Player.class, double.class)
                       .invoke(economy, target, amount);
                       
            String message = targetName != null 
                ? String.format("Списано %.2f с игрока %s", amount, targetName)
                : String.format("Списано %.2f", amount);
                
            return ExecutionResult.success(message);
        } catch (ReflectiveOperationException e) {
            return ExecutionResult.error("Ошибка при выполнении операции списания: " + e.getMessage());
        }
    }

    /**
     * Выполняет проверку баланса игрока.
     */
    private ExecutionResult executeCheck(Object economy, Player defaultPlayer, String targetName, ExecutionContext context) {
        try {
            Player target = getTargetPlayer(defaultPlayer, targetName);
            if (target == null) {
                return errorPlayerNotFound(targetName);
            }

            Class<?> economyClass = economy.getClass();
            double balance = (Double) economyClass.getMethod("getBalance", Player.class)
                                                .invoke(economy, target);
            
            // Сохраняем баланс в переменную для использования в других блоках
            context.setVariable(ECONOMY_BALANCE_VAR, balance);
            
            String message = targetName != null
                ? String.format("Баланс игрока %s: %.2f", targetName, balance)
                : String.format("Ваш баланс: %.2f", balance);
                
            return ExecutionResult.success(message);
        } catch (ReflectiveOperationException e) {
            return ExecutionResult.error("Ошибка при проверке баланса: " + e.getMessage());
        }
    }

    private Player getTargetPlayer(Player defaultPlayer, String targetName) {
        return targetName != null && !targetName.isEmpty() 
            ? Bukkit.getPlayer(targetName) 
            : defaultPlayer;
    }

    /**
     * Возвращает сообщение об ошибке, если игрок не найден.
     */
    private ExecutionResult errorPlayerNotFound(String targetName) {
        String message = targetName != null && !targetName.isEmpty()
            ? String.format("Игрок '%s' не найден или не в сети.", targetName)
            : "Целевой игрок не указан.";
        return ExecutionResult.error(message);
    }

    /**
     * Класс для хранения и валидации параметров операции с экономикой.
     */
    private static final class OperationParameters {
        private final String operation;
        private final double amount;
        private final String target;
        private final String error;
        
        /**
         * Создает экземпляр с ошибкой.
         */
        public OperationParameters(String error) {
            this.operation = null;
            this.amount = 0;
            this.target = null;
            this.error = error;
        }
        
        /**
         * Создает экземпляр с параметрами операции.
         */
        public OperationParameters(String operation, double amount, String target) {
            this.operation = operation;
            this.amount = amount;
            this.target = target;
            this.error = null;
        }
        
        public String operation() {
            return operation;
        }
        
        public double amount() {
            return amount;
        }
        
        public String target() {
            return target;
        }
        
        public String error() {
            return error;
        }
        
        public boolean hasError() {
            return error != null;
        }
    }
}