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

import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantLock;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.camp.Camp;
import com.avrgaming.civcraft.structure.Lab;
import com.avrgaming.civcraft.structure.Mine;
import com.avrgaming.civcraft.structure.TownHall;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.util.CivColor;

public class EffectEventTimer extends CivAsyncTask {
	
	public static ReentrantLock runningLock = new ReentrantLock();
	
	public EffectEventTimer() {
	}
	
	private void processTick() {
		// Clear the last taxes so they don't accumulate.
		for (Civilization civ : CivGlobal.getCivs()) {
			civ.lastTaxesPaidMap.clear();
		}
		
		CivLog.info("TimerEvent: Camps Tick --------------------");
		for (Camp camp : CivGlobal.getCamps()) {
			try {
				camp.processFirepoints();
				if (camp.isLonghouseEnabled()) {
					camp.processLonghouse();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		CivLog.info("TimerEvent: Mine Tick --------------------");
		for (Mine m : CivGlobal.mines) {
			m.resetTasks();
			if (!m.isActive() || !m.isEnabled()) {
				CivMessage.sendTown(m.getTown(), CivColor.LightGreen+"Level "+m.getLevel()+" Mine "+CivColor.Rose+"is not active."+CivColor.Yellow+" +0 Hammers");
				continue;
			}
			if (!m.isComplete() || m.isDeleted()) {
				CivMessage.sendTown(m.getTown(), CivColor.LightGreen+"Level "+m.getLevel()+" Mine "+CivColor.Rose+"is not completed."+CivColor.Yellow+" +0 Hammers");
				continue;
			}
			if (m.isDestroyed()) {
				CivMessage.sendTown(m.getTown(), CivColor.LightGreen+"Level "+m.getLevel()+" Mine "+CivColor.Rose+"is destroyed."+CivColor.Yellow+" +0 Hammers");
				continue;
			}
			try {
				m.process_consume(this);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		CivLog.info("TimerEvent: Lab Tick --------------------");
		for (Lab l : CivGlobal.labs) {
			l.resetTasks();
			if (!l.isActive() || !l.isEnabled()) {
				CivMessage.sendTown(l.getTown(), CivColor.LightGreen+"Level "+l.getLevel()+" Lab "+CivColor.Rose+"is not active."+CivColor.Yellow+" +0 Beakers");
				continue;
			}
			if (!l.isComplete() || l.isDeleted()) {
				CivMessage.sendTown(l.getTown(), CivColor.LightGreen+"Level "+l.getLevel()+" Lab "+CivColor.Rose+"is not completed."+CivColor.Yellow+" +0 Beakers");
				continue;
			}
			if (l.isDestroyed()) {
				CivMessage.sendTown(l.getTown(), CivColor.LightGreen+"Level "+l.getLevel()+" Lab "+CivColor.Rose+"is destroyed."+CivColor.Yellow+" +0 Beakers");
				continue;
			}
			try {
				l.process_consume(this);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// Process any hourly attributes for this town... Culture
		for (Town town : CivGlobal.getTowns()) {
			// highjack this loop to display town hall warning.
			TownHall townhall = town.getTownHall();
			if (townhall == null) {
				CivMessage.sendTown(town, CivColor.Yellow+"Your town does not have a town hall! Structures have no effect!");
				continue;
			}
			
			// Get amount generated after culture rate/bonus.
			DecimalFormat df = new DecimalFormat("#.#");
			double cultureGenerated = town.getCulture().total;
			cultureGenerated = Double.valueOf(df.format(cultureGenerated));
			town.addAccumulatedCulture(cultureGenerated);
			CivMessage.sendTown(town, CivColor.LightGreen+"Generated "+CivColor.LightPurple+cultureGenerated+CivColor.LightGreen+" culture.");
		}
		
		// Checking for expired vassal states.
		CivGlobal.checkForExpiredRelations();
	}
	
	@Override
	public void run() {
		if (runningLock.tryLock()) {
			try {
				processTick();
			} finally {
				runningLock.unlock();
			}
		} else {
			CivLog.error("COULDN'T GET LOCK FOR HOURLY TICK. LAST TICK STILL IN PROGRESS?");
		}	
	}
}
