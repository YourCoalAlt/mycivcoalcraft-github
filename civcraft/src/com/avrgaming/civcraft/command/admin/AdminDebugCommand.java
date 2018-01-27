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
package com.avrgaming.civcraft.command.admin;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.threading.tasks.QuarryAsyncTask;
import com.avrgaming.civcraft.threading.tasks.TrommelAsyncTask;
import com.avrgaming.civcraft.threading.tasks.WarehouseAsyncTask;

public class AdminDebugCommand extends CommandBase {

	@Override
	public void init() {
		command = "/ad debug";
		displayName = "Admin Debug";
		
		commands.put("warehouse", "[town] - turn on this town's warehouse debugging.");
		commands.put("trommel", "[town] - turn on this town's trommel debugging.");
		commands.put("quarry", "[town] - turn on this town's trommel debugging.");
		commands.put("scout", "[civ] - enables debugging for scout towers in this civ.");
	}
	
	public void warehouse_cmd() throws CivException {
		Town town = getNamedTown(1);
		boolean type = false;
		if (WarehouseAsyncTask.debugTowns.contains(town.getName())) {
			WarehouseAsyncTask.debugTowns.remove(town.getName());
		} else {
			type = true;
			WarehouseAsyncTask.debugTowns.add(town.getName());
		}
		CivMessage.send(sender, "Warehouse(s) debug in "+town.getName()+" toggled to "+type+".");
	}
	
	public void trommel_cmd() throws CivException {
		Town town = getNamedTown(1);
		boolean type = false;
		if (TrommelAsyncTask.debugTowns.contains(town.getName())) {
			TrommelAsyncTask.debugTowns.remove(town.getName());
		} else {
			type = true;
			TrommelAsyncTask.debugTowns.add(town.getName());
		}
		CivMessage.send(sender, "Trommel(s) debug in "+town.getName()+" toggled to "+type+".");
	}
	
	public void quarry_cmd() throws CivException {
		Town town = getNamedTown(1);
		boolean type = false;
		if (QuarryAsyncTask.debugTowns.contains(town.getName())) {
			QuarryAsyncTask.debugTowns.remove(town.getName());
		} else {
			type = true;
			QuarryAsyncTask.debugTowns.add(town.getName());
		}
		CivMessage.send(sender, "Quarry(ies) debug in "+town.getName()+" toggled to "+type+".");
	}
	
	public void scout_cmd() throws CivException {
		Civilization civ = getNamedCiv(1);
		
		if (!civ.scoutDebug) {
			civ.scoutDebug = true;
			civ.scoutDebugPlayer = getPlayer().getName();
			CivMessage.sendSuccess(sender, "Enabled scout tower debugging in "+civ.getName());
		} else {
			civ.scoutDebug = false;
			civ.scoutDebugPlayer = null;
			CivMessage.sendSuccess(sender, "Disabled scout tower debugging in "+civ.getName());
		}
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}
	
	@Override
	public void showHelp() {
		showBasicHelp();
	}
	
	@Override
	public void permissionCheck() throws CivException {
	}
}
