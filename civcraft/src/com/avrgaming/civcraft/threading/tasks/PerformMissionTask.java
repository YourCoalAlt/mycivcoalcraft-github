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
package com.avrgaming.civcraft.threading.tasks;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.ConfigMission;
import com.avrgaming.civcraft.items.units.SpyMissions;

public class PerformMissionTask implements Runnable {
	
	ConfigMission mission;
	Player p;
	
	public PerformMissionTask (ConfigMission mission, Player p) {
		this.mission = mission;
		this.p = p;
	}
	
	@Override
	public void run() {
		SpyMissions.performMission(mission, p);
	}
}
