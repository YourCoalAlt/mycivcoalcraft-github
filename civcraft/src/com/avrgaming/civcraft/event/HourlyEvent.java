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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.TradeGood;
import com.avrgaming.civcraft.structure.Cottage;
import com.avrgaming.civcraft.structure.Granary;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.CultureProcessAsyncTask;
import com.avrgaming.civcraft.threading.timers.EffectEventTimer;
import com.avrgaming.civcraft.util.CivColor;

public class HourlyEvent implements EventInterface {
	
	@Override
	public void process() {
		CivLog.info("TimerEvent: Hourly -------------------------------------");
		CivMessage.global("The Hourly Tick... The Savior of us all!!");
		CivCraft.updateStructureArrays();
		for (Structure struc : CivGlobal.getStructures()) {
			if (struc instanceof Granary) {
				Granary granary = (Granary) struc;
				granary.resetTasks();
			}
		}
		
		this.processTradeGoodPayments();
		this.processCottagePayments();
		TaskMaster.asyncTask("EffectEventTimer", new EffectEventTimer(), 0);
		TaskMaster.asyncTask("cultureProcess", new CultureProcessAsyncTask(), 0);
		CivLog.info("TimerEvent: Hourly Finished -----------------------------");
	}
	
	public void processTradeGoodPayments() {
		CivLog.info("TimerEvent: Trade Good Tick --------------------");
		class SyncHourlyTradeTask implements Runnable {
			@Override
			public void run() {
				if (!CivGlobal.tradeEnabled) return;
				CivGlobal.checkForDuplicateGoodies();
				for (Town town : CivGlobal.getTowns()) {
					double payment = TradeGood.getTownTradePayment(town);
					if (payment < 1) continue;
					DecimalFormat df = new DecimalFormat();
					double taxesPaid = payment*town.getDepositCiv().getIncomeTaxRate();
					if (taxesPaid > 0) {
						town.getTreasury().deposit(payment - taxesPaid);
						town.getDepositCiv().taxPayment(town, taxesPaid);
						CivMessage.sendTown(town, CivColor.LightGreen+"Generated "+CivColor.Yellow+df.format(payment)+CivColor.LightGreen+" coins from trade."+
							CivColor.Yellow+" (Paid "+df.format(taxesPaid)+" in taxes to "+town.getDepositCiv().getName()+")");
					} else {
						town.getTreasury().deposit(payment);
						CivMessage.sendTown(town, CivColor.LightGreen+"Generated "+CivColor.Yellow+df.format(payment)+CivColor.LightGreen+" coins from trade.");
					}
				}
			}
		}
		TaskMaster.syncTask(new SyncHourlyTradeTask());
	}
	
	public void processCottagePayments() {
		CivLog.info("TimerEvent: Cottage Tick --------------------");
		class SyncHourlyCottageTask implements Runnable {
			@Override
			public void run() {
				for (Cottage c : CivGlobal.cottages) {
					if (!c.isActive() || !c.isEnabled()) {
						CivMessage.sendTown(c.getTown(), CivColor.LightGreen+"Level "+c.getLevel()+" Cottage "+CivColor.Rose+"is not active."+CivColor.Yellow+" +0 Coins");
						continue;
					}
					if (!c.isComplete() || c.isDeleted()) {
						CivMessage.sendTown(c.getTown(), CivColor.LightGreen+"Level "+c.getLevel()+" Cottage "+CivColor.Rose+"is not completed."+CivColor.Yellow+" +0 Coins");
						continue;
					}
					if (c.isDestroyed()) {
						CivMessage.sendTown(c.getTown(), CivColor.LightGreen+"Level "+c.getLevel()+" Cottage "+CivColor.Rose+"is destroyed."+CivColor.Yellow+" +0 Coins");
						continue;
					}
					c.generateCoins();
				}
			}
		}
		TaskMaster.syncTask(new SyncHourlyCottageTask());
	}
	
	@Override
	public Calendar getNextDate() throws InvalidConfiguration {
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
		Calendar cal = EventTimer.getCalendarInServerTimeZone();
		int hourly_peroid = CivSettings.getInteger(CivSettings.civConfig, "global.hourly_tick");
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.add(Calendar.SECOND, hourly_peroid);
		sdf.setTimeZone(cal.getTimeZone());
		return cal;
	}
}
