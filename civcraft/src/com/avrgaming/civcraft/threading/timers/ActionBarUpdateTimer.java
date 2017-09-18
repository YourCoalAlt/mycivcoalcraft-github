
package com.avrgaming.civcraft.threading.timers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.listener.ActionBar;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.war.War;

public class ActionBarUpdateTimer implements Runnable {
	
	@Override
	public void run() {
		if (CivCraft.isDisable || War.isWarTime()) {
			return;
		}
		
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Resident res = CivGlobal.getResident(p);
			ChunkCoord coord = new ChunkCoord(p.getLocation());
			TownChunk tc = CivGlobal.getTownChunk(coord);
			CultureChunk cc = CivGlobal.getCultureChunk(coord);
			
			String borders = "";
			if (cc != null && tc != null) {
				borders = CivColor.LightGreen+"Borders of "+CivColor.LightPurple+cc.getCiv().getName()+CivColor.LightGreen+", claim of "+CivColor.LightBlue+cc.getTown().getName();
			} else if (cc != null) {
				borders = CivColor.LightGreen+"Borders of "+CivColor.LightPurple+cc.getCiv().getName()+CivColor.LightGreen+", "+CivColor.Gold+"nearby "+CivColor.LightBlue+cc.getTown().getName();
			} else {
				borders = CivColor.Rose+"Wilderness, Unclaimed Land";
			}
			
			if (res != null) {
				int exposure = (int) res.getSpyExposure();
				if (exposure > 0) {
					ActionBar abl = new ActionBar(
							borders+CivColor.GrayBold+" � � "+CivColor.GoldBold+"Spy XP: "+CivColor.LightGreenItalic+exposure);
					abl.sendToPlayer(p);
				} else {
					ActionBar abl = new ActionBar(borders);
					abl.sendToPlayer(p);
				}
			} else {
				ActionBar abl = new ActionBar(
						borders+CivColor.GrayBold+" � � "+CivColor.GoldBold+"Spy XP: "+CivColor.RoseItalic+"null");
				abl.sendToPlayer(p);
			}
		}
	}
}