package com.avrgaming.civcraft.threading.timers;

import java.util.LinkedList;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;

public class ReduceExposureTimer implements Runnable {
	
	@Override
	public void run() {
		LinkedList<String> playersToReduce = new LinkedList<String>();
		for (Resident resident : CivGlobal.getResidents()) {
			if (!resident.isPerformingMission() && resident.getSpyExposure() > 0) {
				playersToReduce.add(resident.getName());
			}
		}
		
		class SyncTask implements Runnable {
			public LinkedList<String> playersToReduce;
			public SyncTask(LinkedList<String> list) {
				playersToReduce = list;
			}
			
			@Override
			public void run() {
				for (String name : playersToReduce) {
					Resident resident = CivGlobal.getResident(name);
					int removeAmt = 3;
					try {
						removeAmt = CivSettings.getInteger(CivSettings.espionageConfig, "exposure_removed");
					} catch (InvalidConfiguration e) {
						removeAmt = 3;
						CivLog.warning("Could not get Espionage exposure_removed.");
//						e.printStackTrace();
					}
					if (resident.getSpyExposure() <= removeAmt) {
						resident.setSpyExposure(0.0);
					} else {
						resident.setSpyExposure(resident.getSpyExposure() - removeAmt);
					}
				}
			}
		}
		TaskMaster.syncTask(new SyncTask(playersToReduce));
	}
}