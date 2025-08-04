package com.megacreative.coding.core;

import com.megacreative.MegaCreative;
import com.megacreative.coding.blocks.BlockRegistry;

/**
 * Основной класс системы блоков.
 * Управляет регистрацией и загрузкой всех блоков.
 */
public class BlockSystem {
    private final MegaCreative plugin;
    private final BlockRegistry blockRegistry;
    
    public BlockSystem(MegaCreative plugin) {
        this.plugin = plugin;
        this.blockRegistry = new BlockRegistry();
    }
    
    /**
     * Инициализирует систему блоков.
     */
    public void initialize() {
        // Регистрация всех типов блоков
        registerDefaultBlocks();
        
        // Загрузка пользовательских блоков
        loadCustomBlocks();
        
        plugin.getLogger().info("Система блоков инициализирована");
    }
    
    /**
     * Регистрирует встроенные блоки.
     */
    private void registerDefaultBlocks() {
        // Будет реализовано в следующих шагах
    }
    
    /**
     * Загружает пользовательские блоки из конфигурации.
     */
    private void loadCustomBlocks() {
        // Будет реализовано позже
    }
    
    public BlockRegistry getBlockRegistry() {
        return blockRegistry;
    }
}
