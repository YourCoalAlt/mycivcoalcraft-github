package com.avrgaming.civcraft.loregui;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.DiplomaticRelation;
import com.avrgaming.civcraft.object.DiplomaticRelation.Status;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

public class DiplomaticMenuViewGlobal implements GuiAction {
	
	static Inventory guiInventory;
	
	@Override
	public void performAction(InventoryClickEvent event, ItemStack stack) {
		Player p = (Player)event.getWhoClicked();
		guiInventory = Bukkit.getServer().createInventory(p, 9*6, "Global Relations");
		
		for (int i = 0; i < 9*6; i++) {
			ItemStack is = LoreGuiItem.build("", ItemManager.getId(Material.STAINED_GLASS_PANE), 8);
			guiInventory.setItem(i, is);
		}
		
		int stored = 0;
		HashSet<String> usedRelations = new HashSet<String>();
		for (Civilization civ : CivGlobal.getCivs()) {
			for (DiplomaticRelation relation : civ.getDiplomacyManager().getRelations()) {
				if (relation.getStatus().equals(Status.NEUTRAL)) {
					continue;
				}
				
				if (!usedRelations.contains(relation.getPairKey())) {
					usedRelations.add(relation.getPairKey());
					ItemStack is = LoreGuiItem.build(DiplomaticRelation.getRelationColor(relation.getStatus())+relation.getStatus().toString(), ItemManager.getId(Material.QUARTZ_BLOCK), 0,
							CivColor.White+relation.getCiv().getName(),
							CivColor.White+relation.getOtherCiv().getName());
					guiInventory.setItem(stored, is);
					stored++;
				}
			}
		}
		
		
		/* Add back buttons. */
		ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Relations Menu");
		backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
		backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
		backButton = LoreGuiItem.setActionData(backButton, "invName", DiplomaticMenu.guiInventory.getName());
		guiInventory.setItem((9*6)-1, backButton);
		
		LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
		TaskMaster.syncTask(new OpenInventoryTask(p, guiInventory));
	}
}