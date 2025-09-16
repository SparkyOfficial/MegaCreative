package com.megacreative.coding.executors;

import com.megacreative.coding.CodeBlock;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents the result of a script or block execution.
 */
public class ExecutionResult {
    private final boolean success;
    private final String message;
    private final CodeBlock executedBlock;
    private final Player executor;
    private final Throwable error;
    private final long executionTime;
    private final Map<String, Object> details;
    private boolean terminated = false;
    private Object returnValue;

    private ExecutionResult(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.executedBlock = builder.executedBlock;
        this.executor = builder.executor;
        this.error = builder.error;
        this.executionTime = builder.executionTime;
        this.details = builder.details != null ? new HashMap<>(builder.details) : new HashMap<>();
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public CodeBlock getExecutedBlock() {
        return executedBlock;
    }

    public Player getExecutor() {
        return executor;
    }

    public Throwable getError() {
        return error;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Checks if the execution was terminated (e.g., by a return statement)
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * Sets whether the execution was terminated
     */
    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    /**
     * Gets the return value from the execution
     */
    public Object getReturnValue() {
        return returnValue;
    }

    /**
     * Sets the return value for the execution
     */
    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * Creates a success result with a message
     */
    public static ExecutionResult success(String message) {
        return new Builder()
            .success(true)
            .message(message)
            .build();
    }

    /**
     * Creates a success result with default message
     */
    public static ExecutionResult success() {
        return success("Operation completed successfully");
    }

    /**
     * Creates an error result with a message
     */
    public static ExecutionResult error(String message) {
        return error(message, null);
    }

    /**
     * Creates an error result with a message and throwable
     */
    public static ExecutionResult error(String message, Throwable error) {
        return new Builder()
            .success(false)
            .message(message)
            .error(error)
            .build();
    }
    
    /**
     * Gets additional details about the execution result
     */
    public Map<String, Object> getDetails() {
        return details != null ? new HashMap<>(details) : new HashMap<>();
    }
    
    /**
     * Gets a specific detail by key
     */
    public Object getDetail(String key) {
        return details != null ? details.get(key) : null;
    }

    /**
     * Creates an error result from an exception
     */
    public static ExecutionResult error(Throwable error) {
        return error(error.getMessage(), error);
    }

    /**
     * Builder for ExecutionResult
     */
    public static class Builder {
        private boolean success;
        private String message;
        private CodeBlock executedBlock;
        private Player executor;
        private Throwable error;
        private long executionTime;
        private Map<String, Object> details;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder executedBlock(CodeBlock block) {
            this.executedBlock = block;
            return this;
        }

        public Builder executor(Player player) {
            this.executor = player;
            return this;
        }

        public Builder error(Throwable error) {
            this.error = error;
            return this;
        }

        public Builder executionTime(long executionTime) {
            this.executionTime = executionTime;
            return this;
        }
        
        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }
        
        public Builder addDetail(String key, Object value) {
            if (this.details == null) {
                this.details = new HashMap<>();
            }
            this.details.put(key, value);
            return this;
        }

        public ExecutionResult build() {
            return new ExecutionResult(this);
        }
    }
}
