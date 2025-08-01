package io.github.twowr.containerperms.util;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import org.bukkit.OfflinePlayer;

import org.bukkit.persistence.PersistentDataType;

import org.bukkit.block.Container;
import org.bukkit.entity.HumanEntity;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import org.bukkit.plugin.java.JavaPlugin;

public final class ManagerMenu implements InventoryHolder {
	private JavaPlugin plugin;

	private final int invHeight;
	private int menuHeight;
	private int offset;
	private Inventory inventory;
	private List<ItemStack> frame;

	private Container container;

	public ManagerMenu(JavaPlugin plugin, Container container, int height) {
		this.plugin = plugin;

		this.invHeight = height;
		this.menuHeight = 0;
		this.offset = 0;
		this.inventory = Bukkit.createInventory(this, this.invHeight * 9, "Permission Manager");
		this.frame = new ArrayList();

		this.container = container;

		updateFrame();
		renderFrame();
	}

	public void openMenu(HumanEntity player) {
		player.openInventory(this.inventory);
	}

	public void closeMenu() {
		for (HumanEntity viewer: new ArrayList<>(this.inventory.getViewers())) {
			viewer.closeInventory();
		}

		this.inventory = null;
		this.frame = null;
	}
	
	public void processInput(int slot) {
		if (this.offset * 9 + slot + 1 > this.menuHeight * 9) {
			this.offset = Math.max(this.menuHeight - this.invHeight, 0);
			return;
		}
		if (this.offset * 9 + slot == 9 + 4) {
			this.container.getPersistentDataContainer().remove(new NamespacedKey(this.plugin, "owner"));
			this.container.getPersistentDataContainer().remove(new NamespacedKey(this.plugin, "allowed"));

			this.container.update(true);

			closeMenu();
			return;
		}
		if (slot % 9 == 5) { //ad hoc
			if (this.inventory == null || this.frame == null) return;

			Material type = this.frame.get(this.offset * 9 + slot).getType();
			SkullMeta headMeta = ((SkullMeta) this.frame.get(this.offset * 9 + slot - 2).getItemMeta()); 
			if (headMeta == null) return;

			OfflinePlayer player = headMeta.getOwningPlayer();
			if (player == null) return;

			ArrayList<String> allowed = new ArrayList<String>(this.container.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "allowed"), PersistentDataType.LIST.strings()));
			if (type == Material.GREEN_STAINED_GLASS_PANE) {
				allowed.remove(player.getUniqueId().toString());
			} else if (type == Material.RED_STAINED_GLASS_PANE) {
				allowed.add(player.getUniqueId().toString());
			}

			this.container.getPersistentDataContainer().set(
				new NamespacedKey(this.plugin, "allowed"),
				PersistentDataType.LIST.strings(),
				allowed
			);

			updateFrame();
			this.container.update(true);
			return;
		}
		if (slot == 8) {
			this.offset = Math.max(this.offset - 1, 0);
			return;
		}
		if (slot == this.invHeight * 9 - 1) {
			this.offset = Math.min(this.offset + 1, Math.max(this.menuHeight - this.invHeight, 0));
			return;
		}
	}

	public void updateFrame() {
		if (this.frame == null) return;

		ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemMeta bgMeta = bg.getItemMeta();
		bgMeta.setHideTooltip(true);
		bg.setItemMeta(bgMeta);

		this.frame.clear();

		// 1 pad top, 1 displaying owner, 1 pad below owner
		for (int i = 0; i < 9 * 3; i++)
			this.frame.add(bg.clone());

		for (OfflinePlayer player: Bukkit.getOfflinePlayers()) {
			boolean isOwner = false;
			if (player.getUniqueId().toString().equals(this.container.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "owner"), PersistentDataType.STRING)))
				isOwner = true;
			ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta headMeta = (SkullMeta) head.getItemMeta();
			headMeta.setDisplayName(player.getName());
			if (isOwner) {
				headMeta.setLore(List.of("Click to disown"));
			}
			headMeta.setOwningPlayer(player);
			head.setItemMeta(headMeta);

			if (isOwner) {
				this.frame.set(9 + 4, head);
			} else {
				if (this.container.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "allowed"), PersistentDataType.LIST.strings()).contains(player.getUniqueId().toString())) {
					ItemStack canAccess = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
					ItemMeta canAccessMeta = canAccess.getItemMeta();
					canAccessMeta.setDisplayName("Can access");
					canAccessMeta.setLore(List.of("Click to switch"));
					canAccess.setItemMeta(canAccessMeta);

					for (int i = 0; i < 3; i++)
						this.frame.add(bg.clone());
					this.frame.add(head);
					this.frame.add(bg.clone());
					this.frame.add(canAccess);
					for (int i = 0; i < 3; i++)
						this.frame.add(bg.clone());
				} else {
					ItemStack noAccess = new ItemStack(Material.RED_STAINED_GLASS_PANE);
					ItemMeta noAccessMeta = noAccess.getItemMeta();
					noAccessMeta.setDisplayName("No access");
					noAccessMeta.setLore(List.of("Click to switch"));
					noAccess.setItemMeta(noAccessMeta);

					for (int i = 0; i < 3; i++)
						this.frame.add(bg.clone());
					this.frame.add(head);
					this.frame.add(bg.clone());
					this.frame.add(noAccess);
					for (int i = 0; i < 3; i++)
						this.frame.add(bg.clone());
				}
			}
		}

		for (int i = 0; i < 9; i++)
			this.frame.add(bg.clone());

		this.menuHeight = this.frame.size() / 9;
	}

	public void renderFrame() {
		if (this.frame == null) return;
		if (this.invHeight < 2) return;
		if (this.offset >= this.menuHeight) {
			ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
			ItemMeta bgMeta = bg.getItemMeta();
			bgMeta.setHideTooltip(true);
			bg.setItemMeta(bgMeta);

			for (int i = 0; i < 9 * this.invHeight; i++)
				this.inventory.setItem(i, bg.clone());
		} else if (this.offset + this.invHeight > this.menuHeight) {
			this.inventory.setContents(this.frame.subList(this.offset * 9, this.menuHeight * 9).toArray(new ItemStack[0]));

			ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
			ItemMeta bgMeta = bg.getItemMeta();
			bgMeta.setHideTooltip(true);
			bg.setItemMeta(bgMeta);

			for (int i = 0; i < 9 * (this.offset + this.invHeight - this.menuHeight); i++)
				this.inventory.setItem(9 * (this.menuHeight - this.offset) + i, bg.clone());
		} else {
			this.inventory.setContents(this.frame.subList(this.offset * 9, (this.offset + this.invHeight) * 9).toArray(new ItemStack[0]));
		}

		ItemStack up = new ItemStack(Material.ARROW);
		ItemMeta upMeta = up.getItemMeta();
		upMeta.setDisplayName("Scroll up");
		up.setItemMeta(upMeta);

		ItemStack down = new ItemStack(Material.ARROW);
		ItemMeta downMeta = down.getItemMeta();
		downMeta.setDisplayName("Scroll down");
		down.setItemMeta(downMeta);

		this.inventory.setItem(8, up);
		this.inventory.setItem(this.invHeight * 9 - 1, down);
	}

	@Override
	public Inventory getInventory() {
		return this.inventory;
	}
}
