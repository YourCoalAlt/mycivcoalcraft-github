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
import java.util.TreeMap;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.threading.CivAsyncTask;

public class CalculateScoreTimer extends CivAsyncTask {
	
	@Override
	public void run() {
		if (!CivGlobal.scoringEnabled) return;
//		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("endgame:winningCiv");
		TreeMap<Integer, Civilization> civScores = new TreeMap<Integer, Civilization>();
		TreeMap<String, Integer> civScoresTied = new TreeMap<String, Integer>();
		TreeMap<Integer, Town> townScores = new TreeMap<Integer, Town>();
		TreeMap<String, Integer> townScoresTied = new TreeMap<String, Integer>();
/*		if (entries.size() != 0) { // we have a winner, do not accumulate scores anymore.
			for (SessionEntry se : entries) {
				Civilization civ = EndGameCondition.getCivFromSessionData(se.value);
				civScores.put(civ, civ.getScore());
			}
			return;
		}*/
		
		for (Civilization civ : CivGlobal.getCivs()) {
			if (civ.isAdminCiv()) continue;
			if (civScores.containsKey(civ.getScore())) civScoresTied.put(civ.getName(), civ.getScore());
			else civScores.put(civ.getScore(), civ);
		}
			
		for (Town town : CivGlobal.getTowns()) {
			if (town.getCiv().isAdminCiv()) continue;
			if (townScores.containsKey(town.getScore())) townScoresTied.put(town.getName(), town.getScore());
			else townScores.put(town.getScore(), town);
		}
		
		synchronized(CivGlobal.civScores) {
			CivGlobal.civScores = civScores;
		}
		
		synchronized(CivGlobal.townScores) {
			CivGlobal.townScores = townScores;
		}
		
		synchronized(CivGlobal.civScoresTied) {
			CivGlobal.civScoresTied = civScoresTied;
		}
		
		synchronized(CivGlobal.townScoresTied) {
			CivGlobal.townScoresTied = townScoresTied;
		}
	}
}
