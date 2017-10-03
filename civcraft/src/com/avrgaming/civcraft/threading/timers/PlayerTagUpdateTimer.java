package com.avrgaming.civcraft.threading.timers;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;
import com.nametagedit.plugin.NametagEdit;

public class PlayerTagUpdateTimer implements Runnable {
	
	@Override
	public void run() {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Resident res = CivGlobal.getResident(p);
			String suffix;
			if (res == null) {
				suffix = CivColor.RoseItalic+" [NULL]";
			} else {
				if (res.getCiv() != null) suffix = CivColor.LightPurpleBold+" ["+StringUtils.left(res.getCiv().getName(), 4)+"]";
					else suffix = CivColor.LightGrayBold+" [None]";
				
				NametagEdit.getApi().setSuffix(p, suffix);
				String prefix = "";
				if (p.hasPermission(CivSettings.MINI_ADMIN)) {
					prefix += CivColor.RedBold+"Admin ";
				} else if (p.hasPermission(CivSettings.MODERATOR)) {
					prefix += CivColor.LightBlueBold+"Mod ";
				} else if (p.hasPermission(CivSettings.HELPER)) {
					prefix += CivColor.LightGreenBold+"Helper ";
				}
				
				if (p.isOp()) {
					prefix += CivColor.LightGrayItalic;
				}
				
				NametagEdit.getApi().setPrefix(p, prefix+CivColor.RESET);
			}
		}
	}
}