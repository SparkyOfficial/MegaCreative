package com.megacreative.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Допоміжний клас для зручного створення предметів
 */
public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    /**
     * Конструктор
     * @param material Матеріал предмета
     */
    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    /**
     * Конструктор
     * @param material Матеріал предмета
     * @param amount Кількість
     */
    public ItemBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    /**
     * Конструктор
     * @param item Базовий предмет
     */
    public ItemBuilder(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    /**
     * Встановлює назву предмета
     * @param name Назва
     * @return this для ланцюжка викликів
     */
    public ItemBuilder setName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    /**
     * Встановлює опис предмета
     * @param lore Рядки опису
     * @return this для ланцюжка викликів
     */
    public ItemBuilder setLore(String... lore) {
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    /**
     * Встановлює опис предмета
     * @param lore Список рядків опису
     * @return this для ланцюжка викликів
     */
    public ItemBuilder setLore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    /**
     * Додає рядки до опису предмета
     * @param lines Рядки для додавання
     * @return this для ланцюжка викликів
     */
    public ItemBuilder addLore(String... lines) {
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.addAll(Arrays.asList(lines));
        meta.setLore(lore);
        return this;
    }

    /**
     * Додає зачарування до предмета
     * @param enchantment Зачарування
     * @param level Рівень зачарування
     * @return this для ланцюжка викликів
     */
    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    /**
     * Додає прапор до предмета
     * @param flag Прапор
     * @return this для ланцюжка викликів
     */
    public ItemBuilder addFlag(ItemFlag flag) {
        meta.addItemFlags(flag);
        return this;
    }

    /**
     * Приховує всі додаткові ефекти предмета (зачарування, атрибути тощо)
     * @return this для ланцюжка викликів
     */
    public ItemBuilder hideAttributes() {
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, 
                ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DESTROYS, 
                ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS);
        return this;
    }

    /**
     * Встановлює незламність предмета
     * @param unbreakable true, якщо предмет незламний
     * @return this для ланцюжка викликів
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    /**
     * Створює готовий предмет
     * @return Готовий предмет
     */
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
