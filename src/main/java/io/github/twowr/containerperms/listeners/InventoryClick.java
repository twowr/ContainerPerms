package io.github.twowr.containerperms.listeners;

import io.github.twowr.containerperms.util.ManagerMenu;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.inventory.InventoryHolder;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;

import org.bukkit.event.inventory.InventoryClickEvent;

public final class InventoryClick implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		InventoryHolder holder = event.getClickedInventory().getHolder();
		if (!(holder instanceof ManagerMenu)) return;

		ManagerMenu menu = (ManagerMenu) holder;
		menu.processInput(event.getSlot());
		menu.renderFrame();

		event.setCancelled(true);
	}
}
