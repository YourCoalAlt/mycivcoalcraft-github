package com.avrgaming.civcraft.endgame;

import java.util.ArrayList;

import com.avrgaming.civcraft.database.session.SessionEntry;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.util.CivColor;

public class EndConditionNotificationTask implements Runnable {

	@Override
	public void run() {
		
		for (EndGameCondition endCond : EndGameCondition.endConditions) {
			ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(endCond.getSessionKey());
			if (entries.size() == 0) {
				continue;
			}
			
			for (SessionEntry entry : entries) {
				Civilization civ = EndGameCondition.getCivFromSessionData(entry.value);
				Integer daysLeft = endCond.getDaysToHold() - endCond.getDaysHeldFromSessionData(entry.value);
				CivMessage.global(CivColor.LightBlueBold+civ.getName()+CivColor.White+" is "+
				CivColor.YellowBold+daysLeft+CivColor.White+" days away from a "+CivColor.LightPurpleBold+endCond.getVictoryName()+
				CivColor.White+" victory! Capture their capital to prevent it!");
			}
		}
		
	}

}
