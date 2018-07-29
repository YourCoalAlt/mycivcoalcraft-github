package com.avrgaming.civcraft.threading.timers;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;

import com.avrgaming.civcraft.config.perms.CivPerms;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.war.War;

public class PlayerTagUpdateTimer implements Runnable {
	
	@Override
	public void run() {
		for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
			Resident resPl = CivGlobal.getResident(pl);
			if (resPl == null) continue;
			
			String suffix = "";
			String prefix = "";
			if (CivPerms.isYourCoal(pl)) {
				prefix = CivColor.GreenBold+"Plugin ";
			} else if (CivPerms.isControl(pl)) {
				prefix = CivColor.GoldBold+"Owner ";
			} else if (CivPerms.isAdminOP(pl)) {
				prefix = CivColor.RedBold+"Admin OP ";
			} else if (CivPerms.isAdmin(pl)) {
				prefix = CivColor.RedItalic+"Admin ";
			} else if (CivPerms.isDev(pl)) {
				prefix = CivColor.NavyBold+"Dev ";
			} else if (CivPerms.isMiniAdmin(pl)) {
				prefix = CivColor.RoseBold+"Jr Admin ";
			} else if (CivPerms.isMod(pl)) {
				prefix = CivColor.LightBlueBold+"Mod ";
			} else if (CivPerms.isHelper(pl)) {
				prefix = CivColor.LightGreenBold+"Helper ";
			}
			
			if (resPl.hasCiv()) suffix = CivColor.LightPurpleBold+" ["+StringUtils.left(resPl.getCiv().getName(), 4)+"]";
			if (resPl.hasCamp()) suffix = CivColor.GrayBold+" ["+StringUtils.left(resPl.getCamp().getName(), 4)+"]";
			
			String finalName = prefix+CivColor.RESET+pl.getName()+suffix;
			pl.setDisplayName(finalName);
			
			if (War.isWarTime()) { // Change player names on tab for diplomatic relation
				pl.setPlayerListName(pl.getName()); // Has to be changed to player's default name or TagAPI does not update
				for (Player ob : Bukkit.getServer().getOnlinePlayers()) {
					AsyncPlayerReceiveNameTagEvent prte = new AsyncPlayerReceiveNameTagEvent(pl, ob, CivGlobal.updateTag(pl, ob), ob.getUniqueId());
					Bukkit.getServer().getPluginManager().callEvent(prte);
					TagAPI.refreshPlayer(pl, ob);
				}
			} else {
				pl.setPlayerListName(finalName);
			}
			
		}
	}
}