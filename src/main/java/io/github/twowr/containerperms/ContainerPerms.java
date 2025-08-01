package io.github.twowr.containerperms;

import org.bukkit.plugin.java.JavaPlugin;
import io.github.twowr.containerperms.listeners.*;

public final class ContainerPerms extends JavaPlugin {
	@Override
	public void onEnable() {
		getLogger().info("Registering event-listeners");
		getServer().getPluginManager().registerEvents(new PlayerInteract(this), this);
		getServer().getPluginManager().registerEvents(new BlockBreak(this), this);
		getServer().getPluginManager().registerEvents(new InventoryClick(), this);
		getServer().getPluginManager().registerEvents(new InventoryMoveItem(this), this);
		getLogger().info("Done");
	}
}
