package com.avrgaming.civcraft.loregui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigBuildableInfo;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

public class _BuildingStructureListInventory implements GuiAction {
	
	static Inventory guiInventory;
	
	public String[] messages(String... messages) {
		return messages;
	}
	
	@Override
	public void performAction(InventoryClickEvent event, ItemStack stack) {
		Player p = (Player)event.getWhoClicked();
		guiInventory = Bukkit.getServer().createInventory(p,9*6, "Global Structure Info");
		
		for (int i = 0; i < 9*6; i++) {
			ItemStack is = LoreGuiItem.build("", ItemManager.getId(Material.STAINED_GLASS_PANE), 8);
			guiInventory.setItem(i, is);
		}
		
		for (ConfigBuildableInfo b : CivSettings.structures.values()) {
			String tech = "None";
			if (b.require_tech != null) {
				tech = b.require_tech.replace("tech_", "");
			}
			
			ItemStack si = LoreGuiItem.build(b.displayName, b.itemId, b.itemData, 
					CivColor.Green+"Cost: "+CivColor.LightGreen+b.cost+" Coins",
               CivColor.LightGreen+"       "+b.hammer_cost+" Hammers",
					CivColor.Green+"Upkeep: "+CivColor.LightGreen+b.upkeep+" Coins/Day",
					CivColor.Green+"Required Tech: "+CivColor.LightGreen+tech,
					CivColor.Green+"Limit Per Town: "+CivColor.LightGreen+b.limit,
					CivColor.Green+"Max Hitpoints: "+CivColor.LightGreen+b.max_hitpoints);
			guiInventory.setItem(b.position, si);
		}
		
		/* Add back buttons. */
		ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Build Menu");
		backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
		backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
		backButton = LoreGuiItem.setActionData(backButton, "invName", _BuildingInventory.guiInventory.getName());
		guiInventory.setItem((9*6)-1, backButton);
		
		LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
		TaskMaster.syncTask(new OpenInventoryTask(p, guiInventory));
	}
}