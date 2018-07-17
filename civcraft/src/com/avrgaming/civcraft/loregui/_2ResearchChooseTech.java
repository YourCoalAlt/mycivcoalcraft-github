package com.avrgaming.civcraft.loregui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigTech;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.CivColor;

public class _2ResearchChooseTech implements GuiAction {

	@Override
	public void performAction(Player p, ItemStack stack) {
		Resident res = CivGlobal.getResident(p);
		ConfigTech info = CivSettings.techs.get(LoreGuiItem.getActionData(stack, "info"));
		Inventory guiInventory = Bukkit.getServer().createInventory(p, 9*1, "Confirm New Research");
		
		ItemStack confirm = LoreGuiItem.build(CivColor.LightGreen+"Begin Researching", CivData.STAINED_CLAY, 5,
				CivColor.Gray+"Name: "+CivColor.Yellow+info.name,
				CivColor.Gray+"Cost: "+CivColor.Yellow+info.getAdjustedTechCost(res.getCiv())+" Coins",
				CivColor.Gray+"Science: "+CivColor.Yellow+info.getAdjustedScienceCost(res.getCiv())+" Science");
		confirm = LoreGuiItem.setAction(confirm, "_2ResearchConfirm");
		confirm = LoreGuiItem.setActionData(confirm, "info", info.id);
		guiInventory.setItem(3, confirm);
		
		ItemStack cancel = LoreGuiItem.build(CivColor.LightGreen+"Cancel Choice", CivData.STAINED_CLAY, 14,
				CivColor.Gray+"Name: "+CivColor.Yellow+info.name,
				CivColor.Gray+"Cost: "+CivColor.Yellow+info.getAdjustedTechCost(res.getCiv())+" Coins",
				CivColor.Gray+"Science: "+CivColor.Yellow+info.getAdjustedScienceCost(res.getCiv())+" Science");
		cancel = LoreGuiItem.setAction(cancel, "OpenInventory");
		cancel = LoreGuiItem.setActionData(cancel, "invType", "cancel_confirmation");
		guiInventory.setItem(5, cancel);
		
		LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);		
		TaskMaster.syncTask(new OpenInventoryTask(p, guiInventory));
	}
}