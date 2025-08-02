package io.github.twowr.containerperms.listeners;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import io.github.twowr.containerperms.util.ManagerMenu;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.block.Container;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;

import org.bukkit.inventory.InventoryHolder;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;

import org.bukkit.event.inventory.InventoryMoveItemEvent;

public final class InventoryMoveItem implements Listener {
	private final JavaPlugin plugin;

	public InventoryMoveItem(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		InventoryHolder srcHolder = event.getSource().getHolder();
		InventoryHolder dstHolder = event.getDestination().getHolder();
		boolean srcIsContainer = (srcHolder instanceof Container);
		boolean dstIsContainer = (dstHolder instanceof Container);
		if (!srcIsContainer || !dstIsContainer) return;

		String srcOwner = null;
		String dstOwner = null;
		List<String> srcAllowed = null;
		List<String> dstAllowed = null;

		if (srcIsContainer) {
			Container srcContainer = (Container) srcHolder;
			if (srcContainer.getInventory().getHolder() instanceof DoubleChest) {
				DoubleChest dbcHolder = (DoubleChest) srcContainer.getInventory().getHolder();
				if (dbcHolder.getLeftSide() == null) return;
				if (!(dbcHolder.getLeftSide() instanceof Chest)) return;
				srcContainer = (Chest) dbcHolder.getLeftSide();
			}
			srcOwner = srcContainer.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "owner"), PersistentDataType.STRING);
			srcAllowed = srcContainer.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "allowed"), PersistentDataType.LIST.strings());
		}

		if (dstIsContainer) {
			Container dstContainer = (Container) dstHolder;
			if (dstContainer.getInventory().getHolder() instanceof DoubleChest) {
				DoubleChest dbcHolder = (DoubleChest) dstContainer.getInventory().getHolder();
				if (dbcHolder.getLeftSide() == null) return;
				if (!(dbcHolder.getLeftSide() instanceof Chest)) return;
				dstContainer = (Chest) dbcHolder.getLeftSide();
			}
			dstOwner = dstContainer.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "owner"), PersistentDataType.STRING);
			dstAllowed = dstContainer.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "allowed"), PersistentDataType.LIST.strings());
		}

		if (!(Objects.equals(srcOwner, dstOwner) || ((srcAllowed != null && srcAllowed.contains(dstOwner)) && (dstAllowed != null && dstAllowed.contains(srcOwner)))))
			event.setCancelled(true);
	}
}
