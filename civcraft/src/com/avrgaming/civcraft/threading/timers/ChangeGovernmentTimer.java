/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.threading.timers;

import java.util.ArrayList;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.database.session.SessionEntry;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.util.CivColor;

public class ChangeGovernmentTimer implements Runnable {
	
	@Override
	public void run() {
		int anarchy_duration = CivSettings.getIntegerGovernment("anarchy_duration");
		// For each town in anarchy, search the session DB for it's timer.
		for (Civilization civ : CivGlobal.getCivs()) {
			if (civ.getGovernment().id.equalsIgnoreCase("gov_anarchy")) {
				String subvertkey = "subvertgov_"+civ.getId();
				ArrayList<SessionEntry> subvertentries = CivGlobal.getSessionDB().lookup(subvertkey);
				if (subvertentries != null && subvertentries.size() > 0) {
					SessionEntry se = subvertentries.get(0);
					int duration = 3600;
					if (CivGlobal.testFileFlag("debug")) {
						duration = 1;
					}
					
					if (CivGlobal.hasTimeElapsed(se, anarchy_duration*duration)) {
						civ.setGovernment(se.value);
						CivMessage.global(CivColor.Yellow+"INTERNATIONAL INCIDENT! "+CivColor.White+civ.getName()+" has emerged from anarchy and has adopted "+
								CivSettings.governments.get(se.value).displayName+" due to a spy subverting their government "+anarchy_duration+" hours ago.");
						CivGlobal.getSessionDB().delete_all(subvertkey);
						civ.save();
					}
					return;
				} else {
					String key = "changegov_"+civ.getId();
					ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
					if (entries == null || entries.size() < 1) { //We are in anarchy but didn't have a sessiondb entry? huh...
						civ.setGovernment("gov_tribalism");
						CivMessage.global(civ.getName()+" has emerged from anarchy and has adopted [Tribalism] due to a database error.");
						CivLog.warning("Civilization "+civ.getName()+" in anarchy but cannot find its session DB entry with key: "+key+"! Setting to default government.");
						return;
					} else {
						SessionEntry se = entries.get(0);
						int duration = 3600;
						if (CivGlobal.testFileFlag("debug")) {
							duration = 1;
						}
						
						if (CivGlobal.hasTimeElapsed(se, anarchy_duration*duration)) {
							civ.setGovernment(se.value);
							CivMessage.global(civ.getName()+" has emerged from anarchy and has adopted "+CivSettings.governments.get(se.value).displayName);
							CivGlobal.getSessionDB().delete_all(key);
							civ.save();
						}
					}
				}
			}
		}
	}
}
