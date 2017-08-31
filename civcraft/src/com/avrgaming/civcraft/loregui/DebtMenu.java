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
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

public class DebtMenu implements GuiAction {
	
	static Inventory guiInventory;
	
	@Override
	public void performAction(InventoryClickEvent event, ItemStack stack) {
		Player p = (Player)event.getWhoClicked();
		guiInventory = Bukkit.getServer().createInventory(p, 9*6, "Debt Listings");
		
		for (int i = 0; i < 9*6; i++) {
			ItemStack is = LoreGuiItem.build("", ItemManager.getId(Material.STAINED_GLASS_PANE), 8);
			guiInventory.setItem(i, is);
		}
		
		for (Civilization civ : CivGlobal.getCivs()) {
			if (civ.getTreasury().inDebt()) {
				if (civ.isForSale() && civ.isTownsForSale()) {
					ItemStack global = LoreGuiItem.build(CivColor.YellowBold+civ.getName(), ItemManager.getId(Material.GOLD_INGOT), 0, 
							CivColor.Green+"Debt: "+CivColor.LightGreen+civ.getTreasury().getDebt()+" Coins",
							CivColor.Green+civ.getDaysLeftWarning(),
							CivColor.Green+"   Civ Sale: "+civ.getForSalePriceFromCivOnly()+" Coins",
							CivColor.Green+"   + Towns Sale: "+civ.getTotalSalePrice()+" Coins");
					guiInventory.addItem(global);
					
				} else if (civ.isForSale() && !civ.isTownsForSale()) {
					ItemStack global = LoreGuiItem.build(CivColor.YellowBold+civ.getName(), ItemManager.getId(Material.GOLD_INGOT), 0, 
							CivColor.Green+"Debt: "+CivColor.LightGreen+civ.getTreasury().getDebt()+" Coins",
							CivColor.Green+civ.getDaysLeftWarning(),
							CivColor.Green+"   Civ Sale: "+civ.getForSalePriceFromCivOnly()+" Coins",
							CivColor.Green+"   Towns Not For Sale, Yet");
					guiInventory.addItem(global);
					
				} else {
					ItemStack global = LoreGuiItem.build(CivColor.YellowBold+civ.getName(), ItemManager.getId(Material.GOLD_INGOT), 0, 
							CivColor.Green+"Debt: "+CivColor.LightGreen+civ.getTreasury().getDebt()+" Coins",
							CivColor.Green+civ.getDaysLeftWarning());
					guiInventory.addItem(global);
				}
			}
		}
		
		for (Town t : CivGlobal.getTowns()) {
			if (t.getTreasury().inDebt()) {
				if (!t.isCapitol() && t.isForSale()) {
					ItemStack global = LoreGuiItem.build(CivColor.YellowBold+t.getName(), ItemManager.getId(Material.IRON_INGOT), 0, 
							CivColor.Green+"Debt: "+CivColor.LightGreen+t.getTreasury().getDebt()+" Coins",
							CivColor.Green+t.getDaysLeftWarning(),
							CivColor.Green+"   For Sale: "+t.getForSalePrice()+" Coins");
					guiInventory.addItem(global);
				} else {
					ItemStack global = LoreGuiItem.build(CivColor.YellowBold+t.getName(), ItemManager.getId(Material.IRON_INGOT), 0, 
							CivColor.Green+"Debt: "+CivColor.LightGreen+t.getTreasury().getDebt()+" Coins",
							CivColor.Green+t.getDaysLeftWarning());
					guiInventory.addItem(global);
				}
			}
		}
		
		
		/* Add back buttons. */
		ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Topics");
		backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
		backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
		backButton = LoreGuiItem.setActionData(backButton, "invName", Backpack.guiInventory.getName());
		guiInventory.setItem((9*6)-1, backButton);
		
		LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
		TaskMaster.syncTask(new OpenInventoryTask(p, guiInventory));
	}
}