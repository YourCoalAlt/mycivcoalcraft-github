package com.avrgaming.civcraft.loregui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.backpack.Backpack;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

public class _BuildingInventory implements GuiAction {
	
	static Inventory guiInventory;
	
	public String[] messages(String... messages) {
		return messages;
	}
	
	@Override
	public void performAction(InventoryClickEvent event, ItemStack stack) {
		Player p = (Player)event.getWhoClicked();
		guiInventory = Bukkit.getServer().createInventory(p,9*3, "Building Menu");
		
		for (int i = 0; i < 27; i++) {
			ItemStack is = LoreGuiItem.build("", ItemManager.getId(Material.STAINED_GLASS_PANE), 8);
			guiInventory.setItem(i, is);
		}
		
		ItemStack si = LoreGuiItem.build("Global Structure Info", ItemManager.getId(Material.FENCE), 0, CivColor.LightGrayItalic+"An in-game wiki for all structure information.");
		si = LoreGuiItem.setAction(si, "_BuildingStructureListInventory");
		guiInventory.setItem(0, si);
		
		ItemStack bs = LoreGuiItem.build("Build a Structure", ItemManager.getId(Material.BRICK_STAIRS), 0, CivColor.LightGrayItalic+"An in-game wiki for all structure information.");
		bs = LoreGuiItem.setAction(bs, "_BuildingStructureBuildInventory");
		guiInventory.setItem(2, bs);
		
		/* Add back buttons. */
		ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Topics");
		backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
		backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
		backButton = LoreGuiItem.setActionData(backButton, "invName", Backpack.guiInventory.getName());
		guiInventory.setItem((9*3)-1, backButton);
		
		LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
		TaskMaster.syncTask(new OpenInventoryTask(p, guiInventory));
	}
}