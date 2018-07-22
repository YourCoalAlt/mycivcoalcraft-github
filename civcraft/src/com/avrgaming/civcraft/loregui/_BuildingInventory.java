package com.avrgaming.civcraft.loregui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.backpack.Backpack;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.CivItem;

public class _BuildingInventory implements GuiAction {
	
	static Inventory guiInventory;
	
	public String[] messages(String... messages) {
		return messages;
	}
	
	@Override
	public void performAction(Player p, ItemStack stack) {
		guiInventory = Bukkit.getServer().createInventory(p ,9*3, "Building Menu");
		
		for (int i = 0; i < 27; i++) {
			ItemStack is = LoreGuiItem.build("", CivItem.getId(Material.STAINED_GLASS_PANE), 8);
			guiInventory.setItem(i, is);
		}
		
		ItemStack bs = LoreGuiItem.build("Build a Structure", CivItem.getId(Material.BRICK_STAIRS), 0, CivColor.GrayItalic+"An in-game wiki for all structure information.");
		bs = LoreGuiItem.setAction(bs, "_BuildingStructureBuildInventory");
		guiInventory.setItem(0, bs);
		
		/* Add back buttons. */
		ItemStack backButton = LoreGuiItem.build("Back", CivItem.getId(Material.MAP), 0, "Back to Topics");
		backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
		backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
		backButton = LoreGuiItem.setActionData(backButton, "invName", Backpack.backpackInventory.getName());
		guiInventory.setItem((9*3)-1, backButton);
		
		LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
		TaskMaster.syncTask(new OpenInventoryTask(p, guiInventory));
	}
}