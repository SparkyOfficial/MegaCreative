package com.megacreative.coding.executors;

import com.megacreative.coding.CodeBlock;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

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
    private boolean paused = false;
    private Long pauseTicks = null;
    private CompletableFuture<?> awaitFuture = null;

    private ExecutionResult(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.executedBlock = builder.executedBlock;
        this.executor = builder.executor;
        this.error = builder.error;
        this.executionTime = builder.executionTime;
        this.details = builder.details != null ? new HashMap<>(builder.details) : new HashMap<>();
        this.pauseTicks = builder.pauseTicks;
        this.awaitFuture = builder.awaitFuture;
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
     * Checks if the execution should be paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Sets whether the execution should be paused
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * Checks if the execution should pause for a specific number of ticks
     */
    public boolean isPause() {
        return pauseTicks != null;
    }

    /**
     * Gets the number of ticks to pause for
     */
    public Long getPauseTicks() {
        return pauseTicks;
    }

    /**
     * Checks if the execution should await a CompletableFuture
     */
    public boolean isAwait() {
        return awaitFuture != null;
    }

    /**
     * Gets the CompletableFuture to await
     */
    public CompletableFuture<?> getAwaitFuture() {
        return awaitFuture;
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
     * Creates a pause result with a specific number of ticks
     */
    public static ExecutionResult pause(long ticks) {
        return new Builder()
            .success(true)
            .message("Execution paused for " + ticks + " ticks")
            .pauseTicks(ticks)
            .build();
    }
    
    /**
     * Creates an await result with a CompletableFuture
     */
    public static ExecutionResult await(CompletableFuture<?> future) {
        return new Builder()
            .success(true)
            .message("Execution awaiting CompletableFuture")
            .awaitFuture(future)
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
     * Creates a new ExecutionResult with pause flag set
     */
    public ExecutionResult withPause() {
        ExecutionResult result = new Builder()
            .success(this.success)
            .message(this.message)
            .executedBlock(this.executedBlock)
            .executor(this.executor)
            .error(this.error)
            .executionTime(this.executionTime)
            .details(this.details)
            .build();
        result.setPaused(true);
        // Copy other important fields
        result.setTerminated(this.terminated);
        result.setReturnValue(this.returnValue);
        return result;
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
        private Long pauseTicks = null;
        private CompletableFuture<?> awaitFuture = null;

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
        
        public Builder pauseTicks(Long pauseTicks) {
            this.pauseTicks = pauseTicks;
            return this;
        }
        
        public Builder awaitFuture(CompletableFuture<?> awaitFuture) {
            this.awaitFuture = awaitFuture;
            return this;
        }

        public ExecutionResult build() {
            return new ExecutionResult(this);
        }
    }
}