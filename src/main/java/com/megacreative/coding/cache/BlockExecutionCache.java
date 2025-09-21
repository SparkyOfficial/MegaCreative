package com.megacreative.coding.cache;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.executors.ExecutionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Caches block execution results to improve performance.
 * Implements a time-based eviction policy.
 */
public class BlockExecutionCache {
    
    private static final long DEFAULT_TTL = TimeUnit.MINUTES.toMillis(5);
    private static final int MAX_CACHE_SIZE = 1000;
    
    private final Map<CacheKey, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long ttlInMillis;
    private final int maxSize;
    
    public BlockExecutionCache() {
        this(DEFAULT_TTL, TimeUnit.MINUTES);
    }
    
    public BlockExecutionCache(long ttl, TimeUnit timeUnit) {
        this.ttlInMillis = timeUnit.toMillis(ttl);
        this.maxSize = MAX_CACHE_SIZE;
    }
    
    public BlockExecutionCache(long ttl, TimeUnit timeUnit, int maxSize) {
        this.ttlInMillis = timeUnit.toMillis(ttl);
        this.maxSize = maxSize;
    }
    
    // Add constructor to match usage in DefaultScriptEngine
    public BlockExecutionCache(long ttl, TimeUnit timeUnit, long maxSize) {
        this.ttlInMillis = timeUnit.toMillis(ttl);
        this.maxSize = (int) maxSize;
    }
    
    /**
     * Gets a cached execution result for the given block and context.
     */
    public @Nullable ExecutionResult get(CodeBlock block, Map<String, Object> context) {
        if (block == null) {
            return null;
        }
        
        CacheKey key = new CacheKey(block.getId(), context);
        CacheEntry entry = cache.get(key);
        
        if (entry == null) {
            return null;
        }
        
        if (isExpired(entry)) {
            cache.remove(key);
            return null;
        }
        
        return entry.getResult();
    }
    
    /**
     * Caches an execution result for the given block and context.
     */
    public void put(CodeBlock block, Map<String, Object> context, ExecutionResult result) {
        if (block == null || result == null) {
            return;
        }
        
        // Clean up before adding new entries if we're approaching the limit
        if (cache.size() >= maxSize * 0.9) {
            cleanup();
        }
        
        CacheKey key = new CacheKey(block.getId(), context);
        cache.put(key, new CacheEntry(result, System.currentTimeMillis()));
    }
    
    /**
     * Invalidates the cache for a specific block.
     */
    public void invalidate(CodeBlock block) {
        if (block == null) {
            return;
        }
        
        cache.keySet().removeIf(key -> key.getBlockId().equals(block.getId()));
    }
    
    /**
     * Clears the entire cache.
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Removes expired entries from the cache.
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> now - entry.getValue().getTimestamp() > ttlInMillis);
    }
    
    /**
     * Gets the current cache size.
     */
    public int size() {
        return cache.size();
    }
    
    private boolean isExpired(CacheEntry entry) {
        return System.currentTimeMillis() - entry.getTimestamp() > ttlInMillis;
    }
    
    /**
     * Cache key that combines block ID and context hash.
     */
    private static class CacheKey {
        private final UUID blockId;
        private final int contextHash;
        
        public CacheKey(UUID blockId, Map<String, Object> context) {
            this.blockId = blockId;
            this.contextHash = context != null ? context.hashCode() : 0;
        }
        
        public UUID getBlockId() {
            return blockId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return contextHash == cacheKey.contextHash &&
                   Objects.equals(blockId, cacheKey.blockId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(blockId, contextHash);
        }
    }
    
    /**
     * Cache entry that stores the result and timestamp.
     */
    private static class CacheEntry {
        private final ExecutionResult result;
        private final long timestamp;
        
        public CacheEntry(ExecutionResult result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }
        
        public ExecutionResult getResult() {
            return result;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}