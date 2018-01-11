package com.avrgaming.civcraft.loregui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.global.perks.Perk;

public class ActivatePerk implements GuiAction {

	@Override
	public void performAction(Player player, ItemStack stack) {
		Resident resident = CivGlobal.getResident(player);
		String perk_id = LoreGuiItem.getActionData(stack, "perk");
		Perk perk = resident.perks.get(perk_id);
		if (perk != null) {
			perk.onActivate(resident);
		} else {
			CivLog.error("Couldn't activate perk:"+perk_id+" cause it wasn't found in perks hashmap.");
		}
		player.closeInventory();		
	}

}
