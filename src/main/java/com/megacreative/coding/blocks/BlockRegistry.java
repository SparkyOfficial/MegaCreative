package com.megacreative.coding.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Реестр всех доступных блоков в системе.
 * Обеспечивает создание и управление блоками.
 */
public class BlockRegistry {
    private final Map<String, Supplier<Block>> registeredBlocks = new HashMap<>();
    
    /**
     * Регистрирует новый тип блока.
     * 
     * @param id Уникальный идентификатор блока
     * @param supplier Поставщик экземпляров блока
     */
    public void registerBlock(String id, Supplier<Block> supplier) {
        if (registeredBlocks.containsKey(id)) {
            throw new IllegalArgumentException("Блок с ID '" + id + "' уже зарегистрирован");
        }
        registeredBlocks.put(id, supplier);
    }
    
    /**
     * Создает новый экземпляр блока по его ID.
     * 
     * @param id Идентификатор блока
     * @return Новый экземпляр блока
     * @throws IllegalArgumentException если блок с указанным ID не найден
     */
    public Block createBlock(String id) {
        Supplier<Block> supplier = registeredBlocks.get(id);
        if (supplier == null) {
            throw new IllegalArgumentException("Неизвестный тип блока: " + id);
        }
        return supplier.get();
    }
    
    /**
     * Проверяет, зарегистрирован ли блок с указанным ID.
     */
    public boolean isBlockRegistered(String id) {
        return registeredBlocks.containsKey(id);
    }
    
    /**
     * Возвращает количество зарегистрированных блоков.
     */
    public int getRegisteredBlocksCount() {
        return registeredBlocks.size();
    }
}
