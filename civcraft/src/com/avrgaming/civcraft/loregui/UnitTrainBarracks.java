package com.avrgaming.civcraft.loregui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigUnit;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.permission.PlotPermissions;
import com.avrgaming.civcraft.structure.Barracks;
import com.avrgaming.civcraft.structure.Buildable;

public class UnitTrainBarracks implements GuiAction {
	
	static Inventory guiInventory;
	
	@Override
	public void performAction(InventoryClickEvent event, ItemStack stack) {
		Player p = (Player)event.getWhoClicked();
		Resident res = CivGlobal.getResident(p);
		String gid = LoreGuiItem.getActionData(stack, "unitid");
		Buildable buildable = CivGlobal.getNearestBuildable(p.getLocation());
		TownChunk tc = CivGlobal.getTownChunk(p.getLocation());
		
		p.closeInventory();
		if(tc != null && !tc.perms.hasPermission(PlotPermissions.Type.INTERACT, res)) {
			CivMessage.sendError(res, "You do not have permission to access this barracks.");
			return;
		}
		
		if (buildable.getTown() != res.getTown() || !res.hasTown()) {
			CivMessage.sendError(res, "Cannot access barracks of a town you are not in.");
			return;
		}
		
		if (buildable instanceof Barracks) {
			Barracks b = (Barracks) buildable;
			for (ConfigUnit u : CivSettings.units.values()) {
				if (u.id.equalsIgnoreCase(gid)) {
					b.train(res, u.id);
				}
			}
		} else {
			CivMessage.sendError(res, "It appears you are trying to access an illegal barracks...?");
			CivLog.warning("error with "+buildable.getName()+", at "+buildable.getCenterLocation());
			return;
		}
	}
}