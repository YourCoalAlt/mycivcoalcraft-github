package com.avrgaming.civcraft.threading.timers;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.perms.CivPerms;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;
import com.nametagedit.plugin.NametagEdit;

public class PlayerTagUpdateTimer implements Runnable {
	
	@Override
	public void run() {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			Resident res = CivGlobal.getResident(p);
			String suffix = "";
			String prefix = "";
			if (p.hasPermission(CivPerms.CONTROL)) {
				prefix = CivColor.GoldBold+"Owner ";
			} else if (p.hasPermission(CivPerms.ADMIN_OP)) {
				prefix = CivColor.RedBold+"Admin OP ";
			} else if (p.hasPermission(CivPerms.ADMIN)) {
				prefix = CivColor.RedItalic+"Admin ";
			} else if (p.hasPermission(CivPerms.MINI_ADMIN)) {
				prefix = CivColor.RoseBold+"Jr Admin ";
			} else if (p.hasPermission(CivPerms.MODERATOR)) {
				prefix = CivColor.LightBlueBold+"Mod ";
			} else if (p.hasPermission(CivPerms.DEVELOPER)) {
				prefix = CivColor.NavyBold+"Dev ";
			} else if (p.hasPermission(CivPerms.HELPER)) {
				prefix = CivColor.LightGreenBold+"Helper ";
			}
			
			if (res != null) {
				if (res.hasCiv()) suffix = CivColor.LightPurpleBold+" ["+StringUtils.left(res.getCiv().getName(), 4)+"]";
				if (res.hasCamp()) suffix = CivColor.GrayBold+" ["+StringUtils.left(res.getCamp().getName(), 4)+"]";
//				if (!res.hasTown() && !res.hasCamp()) suffix = CivColor.GrayBold+" [None]";
				res.changePlayerName(p, suffix);
			} else {
				suffix = CivColor.RoseItalic+" [NULL]";
			}
			
			p.setDisplayName(prefix+CivColor.RESET+p.getName()+suffix);
			
			if (CivSettings.hasNametagEdit) {
				NametagEdit.getApi().setPrefix(p, prefix+CivColor.RESET);
				NametagEdit.getApi().setSuffix(p, suffix);
			}
			
		}
	}
}