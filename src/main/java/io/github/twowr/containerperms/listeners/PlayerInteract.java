package io.github.twowr.containerperms.listeners;

import java.util.List;
import java.util.ArrayList;

import io.github.twowr.containerperms.util.ManagerMenu;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.persistence.PersistentDataType;

import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.Material;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventPriority;

import org.bukkit.event.Event.Result;

public final class PlayerInteract implements Listener {
	private final JavaPlugin plugin;

	public PlayerInteract(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		Block target = event.getClickedBlock();
		if (target == null) return;

		BlockState targetState = target.getState();
		if (!(targetState instanceof Container)) return;

		Container targetContainer = (Container) targetState;
		if (targetContainer.getInventory().getHolder() instanceof DoubleChest) {
			DoubleChest dbcHolder = (DoubleChest) targetContainer.getInventory().getHolder();
			if (dbcHolder.getLeftSide() == null) return;
			if (!(dbcHolder.getLeftSide() instanceof Chest)) return;
			targetContainer = (Chest) dbcHolder.getLeftSide();
		}
		Player targetPlayer = event.getPlayer();
		String targetId = targetPlayer.getUniqueId().toString();

		String owner = targetContainer.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "owner"), PersistentDataType.STRING);
		List<String> allowed = targetContainer.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "allowed"), PersistentDataType.LIST.strings());
		if (event.getMaterial() != Material.STICK) {
			if ((owner != null && !targetId.equals(owner)) && (allowed == null || (allowed != null && !allowed.contains(targetId)))) {
				targetPlayer.sendMessage(ChatColor.RED + "You don't have access to this");
				event.setUseInteractedBlock(Result.DENY);
			}
			return;
		}
		if (owner == null) {
			targetContainer.getPersistentDataContainer().set(
				new NamespacedKey(this.plugin, "owner"),
				PersistentDataType.STRING,
				targetPlayer.getUniqueId().toString()
			);
			targetContainer.getPersistentDataContainer().set(
				new NamespacedKey(this.plugin, "allowed"),
				PersistentDataType.LIST.strings(),
				new ArrayList<String>()
			);
			targetContainer.update();
			
			owner = targetPlayer.getUniqueId().toString();
			targetPlayer.sendMessage(ChatColor.YELLOW + "you now own this container");
		}

		if (targetPlayer.getUniqueId().toString().equals(owner)) {
			ManagerMenu menu = new ManagerMenu(this.plugin, targetContainer, 6);
			menu.openMenu(targetPlayer);
		} else {
			targetPlayer.sendMessage(ChatColor.YELLOW + "bro you dont own this");
		}

		event.setCancelled(true);
	}
}
