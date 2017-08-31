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

public class DiplomaticMenu implements GuiAction {
	
	static Inventory guiInventory;
	
	@Override
	public void performAction(InventoryClickEvent event, ItemStack stack) {
		Player p = (Player)event.getWhoClicked();
		guiInventory = Bukkit.getServer().createInventory(p, 9*2, "Diplomatic Relations");
		
		for (int i = 0; i < 9*2; i++) {
			ItemStack is = LoreGuiItem.build("", ItemManager.getId(Material.STAINED_GLASS_PANE), 8);
			guiInventory.setItem(i, is);
		}
		
		ItemStack global = LoreGuiItem.build("Global Relations", ItemManager.getId(Material.MAP), 0, CivColor.Gold+"<Click to View>");
		global = LoreGuiItem.setAction(global, "DiplomaticMenuViewGlobal");
		guiInventory.setItem(0, global);
		
		
		/* Add back buttons. */
		ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Topics");
		backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
		backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
		backButton = LoreGuiItem.setActionData(backButton, "invName", Backpack.guiInventory.getName());
		guiInventory.setItem((9*2)-1, backButton);
		
		LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
		TaskMaster.syncTask(new OpenInventoryTask(p, guiInventory));
	}
}