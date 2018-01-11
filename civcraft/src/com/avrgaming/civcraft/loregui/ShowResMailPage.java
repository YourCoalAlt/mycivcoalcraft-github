package com.avrgaming.civcraft.loregui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Resident;

public class ShowResMailPage implements GuiAction {
	
	@Override
	public void performAction(Player p, ItemStack stack) {
		Resident res = CivGlobal.getResident(p);
		res.openMailMenu(p, res, Integer.valueOf(LoreGuiItem.getActionData(stack, "page")));		
	}
}
