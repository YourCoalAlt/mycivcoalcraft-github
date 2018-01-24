package com.avrgaming.civcraft.threading.timers;

import com.avrgaming.civcraft.accounts.AccountLogger;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.util.CivColor;

public class CountdownTimer extends CivAsyncTask {
	
	@Override
	public void run() {
		for (AccountLogger al : CivGlobal.getAccounts()) {
			// Assume if they are neither banned or muted they are being good people.
			if (!al.isBanned() && !al.isMuted()) continue;
			
			// Bans
			if (al.isBanned()) {
				if (System.currentTimeMillis() < al.getBanLength()) {
					// Do nothing so we can check anything later.
				} else {
					al.resetBanLength();
					al.setBanned(false);
					al.setBanMessage("null");
					CivMessage.globalModerator(CivColor.LightGreenBold+"[AUTO-UNBAN] "+CivColor.RESET+"Player "+al.getPlayer().getName()+" has been unbanned as their time of punishment is finished.");
					al.save();
				}
				// Mutes
			} else if (al.isMuted()) {
				if (System.currentTimeMillis() < al.getMuteLength()) {
					// Do nothing so we can check anything later.
				} else {
					if (al.isMuted() && System.currentTimeMillis() >= al.getMuteLength()) {
						al.resetMuteLength();
						al.setMuted(false);
						al.setMuteMessage("null");
						CivMessage.globalModerator(CivColor.LightGreenBold+"[AUTO-UNMUTE] "+CivColor.RESET+"Player "+al.getPlayer().getName()+" has been unmuted as their time of punishment is finished.");
						al.save();
					}
				}
			}
			
/*			if (!al.isBanned() && al.getBanLength() != 0 && System.currentTimeMillis() < al.getBanLength()) {
				al.setBanned(true);
				al.setBanMessage(CivColor.RoseBold+"[AUTO-BAN] "+CivColor.RESET+"We think are unbanned unfairly, please contact an admin if this is wrong!");
				CivMessage.globalModerator(CivColor.RoseBold+"[AUTO-BAN] "+CivColor.RESET+"Player "+al.getPlayer().getName()+" has been banned for possible unfair gameplay!");
				al.save();
			}
			
			if (!al.isMuted() && al.getMuteLength() != 0 && System.currentTimeMillis() < al.getMuteLength()) {
				al.setMuted(true);
				al.setMuteMessage(CivColor.RoseBold+"[AUTO-MUTE] "+CivColor.RESET+"We think are unmutes unfairly, please contact an admin if this is wrong!");
				CivMessage.globalModerator(CivColor.RoseBold+"[AUTO-MUTE] "+CivColor.RESET+"Player "+al.getPlayer().getName()+" has been muted for possible unfair gameplay!");
				al.save();
			}*/
		}
	}
}
