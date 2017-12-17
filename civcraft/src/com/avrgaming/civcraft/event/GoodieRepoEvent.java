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
package com.avrgaming.civcraft.event;

import java.util.Calendar;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.items.BonusGoodie;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Town;

public class GoodieRepoEvent implements EventInterface {
	
	@Override
	public void process() {
		CivLog.info("TimerEvent: GoodieRepo -------------------------------------");
		CivMessage.global("Trade Goodies have been respawned at trade outposts.");
		repoProcess();
	}
	
	public static void repoProcess() {
		for (Town town : CivGlobal.getTowns()) {
			for (BonusGoodie goodie : town.getBonusGoodies()) {
				town.removeGoodie(goodie);
			}
		}
		
		for (BonusGoodie goodie : CivGlobal.getBonusGoodies()) {
			try {
				goodie.replenish();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	@Override
	public Calendar getNextDate() throws InvalidConfiguration {
		Calendar cal = EventTimer.getCalendarInServerTimeZone();
		int repo_day = CivSettings.getInteger(CivSettings.goodsConfig, "trade_good_repo_day");
		int repo_hour = CivSettings.getInteger(CivSettings.goodsConfig, "trade_good_repo_hour");
		int repo_minute = CivSettings.getInteger(CivSettings.goodsConfig, "trade_good_repo_minute");
		
		cal.set(Calendar.SECOND, 5);
		cal.set(Calendar.MINUTE, repo_minute);
		cal.set(Calendar.HOUR_OF_DAY, repo_hour);
		cal.set(Calendar.DAY_OF_WEEK, repo_day);
		int day = 7 - cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.DAY_OF_WEEK, repo_day+day);
		return cal;
	}
}
