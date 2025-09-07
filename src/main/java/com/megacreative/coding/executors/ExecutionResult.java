package com.megacreative.coding.executors;

import com.megacreative.coding.CodeBlock;
import org.bukkit.entity.Player;

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

    private ExecutionResult(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.executedBlock = builder.executedBlock;
        this.executor = builder.executor;
        this.error = builder.error;
        this.executionTime = builder.executionTime;
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

        public ExecutionResult build() {
            return new ExecutionResult(this);
        }
    }
}
