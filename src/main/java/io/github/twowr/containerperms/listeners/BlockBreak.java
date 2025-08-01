package io.github.twowr.containerperms.listeners;

import org.bukkit.ChatColor;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.persistence.PersistentDataType;

import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.EventPriority;

public final class BlockBreak implements Listener {
	private final JavaPlugin plugin;

	public BlockBreak(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		BlockState targetState = event.getBlock().getState();
		if (!(targetState instanceof Container)) return;

		Container targetContainer = (Container) targetState;
		Player targetPlayer = event.getPlayer();

		String owner = targetContainer.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "owner"), PersistentDataType.STRING);
		if (owner != null && !targetPlayer.getUniqueId().toString().equals(owner)) {
			event.setCancelled(true);
			targetPlayer.sendMessage(ChatColor.YELLOW + "bro you dont own this");
		}
	}
}
