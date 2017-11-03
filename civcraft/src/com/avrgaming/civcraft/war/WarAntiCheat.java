package com.avrgaming.civcraft.war;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.PlayerModerationBan;
import com.avrgaming.civcraft.util.CivColor;

public class WarAntiCheat {

	
	public static void kickUnvalidatedPlayers() {
		if (CivGlobal.isCasualMode()) { return; }
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.isOp()) { continue; }
			Resident resident = CivGlobal.getResident(player);
			onWarTimePlayerCheck(resident);
		}
		
		if (War.isOnlyWarriors()) {
			CivMessage.global(CivColor.LightGray+"All invalid players or any players not at war have been expelled during WarTime.");
		} else {
			CivMessage.global(CivColor.LightGray+"All invalid players have been expelled during WarTime.");
		}
	}
	
	public static void onWarTimePlayerCheck(Resident res) {
		if (War.isOnlyWarriors()) {
			if (res == null || !res.hasTown() || !res.getCiv().getDiplomacyManager().isAtWar()) {
				float bantime = War.getEnd().getTime() - System.currentTimeMillis();
				long seconds = (long) (bantime / 1000); long minutes = seconds / 60; long hours = minutes / 60;
//				String time = days + ":" + hours % 24 + ":" + minutes % 60 + ":" + seconds % 60; 
				TaskMaster.syncTask(new PlayerModerationBan(res.getName(), CivColor.RedBold+"Console",
						"Only players at war can be online during wartime! You will be unbanned at the end of war.",
						(int)(hours % 24), (int)(minutes % 60), (int)(seconds % 60)));
			}
		}
	}
	
}
