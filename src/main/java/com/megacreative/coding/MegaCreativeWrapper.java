package com.megacreative.coding;

import com.megacreative.MegaCreative;

/**
 * Клас-обгортка для доступу до основного класу плагіна з підсистеми програмування
 */
public class MegaCreativeWrapper {
    private final MegaCreative plugin;
    private BlockConfiguration blockConfiguration;

    public MegaCreativeWrapper(MegaCreative plugin) {
        this.plugin = plugin;
        this.blockConfiguration = new BlockConfiguration(plugin);
    }

    public MegaCreative getPlugin() {
        return plugin;
    }

    public BlockConfiguration getBlockConfiguration() {
        return blockConfiguration;
    }

    public void setBlockConfiguration(BlockConfiguration blockConfiguration) {
        this.blockConfiguration = blockConfiguration;
    }
}
