package com.avrgaming.civcraft.threading.timers;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.util.CivColor;

public class CountdownTimer extends CivAsyncTask {
	
	@Override
	public void run() {
		for (Resident res : CivGlobal.getResidents()) {
			// Bans
			if (res.isBanned()) {
				if (System.currentTimeMillis() < res.getBannedLength()) {
					// Do nothing so we can check anything later.
			} else {
					res.resetBannedLength();
					res.setBanned(false);
					res.setBannedMessage("null");
					CivMessage.globalModerator(CivColor.LightGreenBold+"[AUTO-UNBAN] "+CivColor.RESET+"Player "+res.getName()+" has been unbanned as their time of punishment is finished.");
					res.save();
				}
			}
			
			if (!res.isBanned() && res.getBannedLength() != 0 && System.currentTimeMillis() < res.getBannedLength()) {
				res.setBanned(true);
				res.setBannedMessage(CivColor.RoseBold+"[AUTO-BAN] "+CivColor.RESET+"We think are unbanned unfairly, please contact an admin if this is wrong!");
				CivMessage.globalModerator(CivColor.RoseBold+"[AUTO-BAN] "+CivColor.RESET+"Player "+res.getName()+" has been banned for possible unfair gameplay!");
				res.save();
			}
			
			// Mutes
			if (res.isMuted()) {
				if (System.currentTimeMillis() < res.getMutedLength()) {
					// Do nothing so we can check anything later.
				} else {
					if (res.isMuted() && System.currentTimeMillis() >= res.getMutedLength()) {
						res.resetMutedLength();
						res.setMuted(false);
						res.setMutedMessage("null");
						CivMessage.globalModerator(CivColor.LightGreenBold+"[AUTO-UNMUTE] "+CivColor.RESET+"Player "+res.getName()+" has been unmuted as their time of punishment is finished.");
						res.save();
					}
				}
			}
			
			if (!res.isMuted() && res.getMutedLength() != 0 && System.currentTimeMillis() < res.getMutedLength()) {
				res.setMuted(true);
				res.setMutedMessage(CivColor.RoseBold+"[AUTO-MUTE] "+CivColor.RESET+"We think are unmutes unfairly, please contact an admin if this is wrong!");
				CivMessage.globalModerator(CivColor.RoseBold+"[AUTO-MUTE] "+CivColor.RESET+"Player "+res.getName()+" has been muted for possible unfair gameplay!");
				res.save();
			}
		}
	}
}
