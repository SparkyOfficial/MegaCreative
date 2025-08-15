package com.megacreative.coding.linker;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.*;

/**
 * Менеджер для управління з'єднаннями між блоками програмування
 */
public class BlockConnectionManager implements Listener {

	private final MegaCreative plugin;
	private final CodingManager codingManager;
	private final Map<String, Map<String, List<String>>> connections = new HashMap<>();

	/**
	 * Конструктор
	 * @param plugin Посилання на основний плагін
	 * @param codingManager Менеджер програмування
	 */
	public BlockConnectionManager(MegaCreative plugin, CodingManager codingManager) {
		this.plugin = plugin;
		this.codingManager = codingManager;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * Конструктор з одним параметром для сумісності
	 * @param codingManager Менеджер програмування
	 */
	public BlockConnectionManager(CodingManager codingManager) {
		this.codingManager = codingManager;
		this.plugin = com.megacreative.MegaCreative.getInstance();
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	// Методи для роботи з з'єднаннями
}
