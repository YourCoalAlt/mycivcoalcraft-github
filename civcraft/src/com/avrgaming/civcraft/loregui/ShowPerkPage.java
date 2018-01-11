package com.avrgaming.civcraft.loregui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Resident;

public class ShowPerkPage implements GuiAction {
	
	@Override
	public void performAction(Player player, ItemStack stack) {
		Resident resident = CivGlobal.getResident(player);
		resident.showPerkPage(Integer.valueOf(LoreGuiItem.getActionData(stack, "page")));				
	}
}
