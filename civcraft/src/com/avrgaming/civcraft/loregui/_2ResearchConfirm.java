package com.avrgaming.civcraft.loregui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigTech;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.UpdateTechBar;

public class _2ResearchConfirm implements GuiAction {
	
	@Override
	public void performAction(Player p, ItemStack stack) {
		Resident res = CivGlobal.getResident(p);
		ConfigTech info = CivSettings.techs.get(LoreGuiItem.getActionData(stack, "info"));
		Civilization civ = res.getCiv();
		
		if (!civ.getLeaderGroup().hasMember(res)) {
			CivMessage.sendError(res, "You are not a leader, you wizard of a hoax!");
			return;
		}
		
		if (civ.getResearchTech() != null) {
			CivMessage.sendError(res, "Current researching "+civ.getResearchTech().name+". " +
					"If you want to change your focus, use /civ research switch instead.");
			return;
		}
		
		if (!civ.getTreasury().hasEnough(info.getAdjustedTechCost(civ))) {
			CivMessage.sendError(res, "Our Civilization's treasury does have the required "+info.getAdjustedTechCost(civ)+" coins to start this research.");
			return;
		}
		
		if (civ.hasTechnology(info.id)) {
			CivMessage.sendError(res, "You already have this technology.");
			return;
		}
		
		if (!info.isAvailable(civ)) {
			CivMessage.sendError(res, "You do not have the required technology to research this technology.");
			return;
		}
		
		civ.setResearchTech(info);
		civ.setResearchProgress(0.0);
		CivMessage.sendCiv(civ, "Your civilization started researching "+info.name+"!");
		civ.getTreasury().withdraw(info.getAdjustedTechCost(civ));
		TaskMaster.asyncTask(new UpdateTechBar(civ),0);
		civ.save();
//			res.getCiv().startTechnologyResearch(sinfo);
		p.closeInventory();
		return;
	}
}