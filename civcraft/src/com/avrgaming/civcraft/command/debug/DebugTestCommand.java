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
package com.avrgaming.civcraft.command.debug;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.test.TestGetChestThread;
import com.avrgaming.civcraft.threading.TaskMaster;

public class DebugTestCommand extends CommandBase {
	
	/* Here we'll build a collection of integration tests that can be run on server
	 * start to verify everything is working. */
	
	@Override
	public void init() {
		command = "/dbg test ";
		displayName = "Test Commands";
		
		commands.put("getsyncchesttest", "Does a performance test by getting chests. NEVER RUN THIS ON PRODUCTION.");
	}
	
	public void getsyncchesttest_cmd() throws CivException {
		Integer count = getNamedInteger(1);
		for (int i = 0; i < count; i++) {
			TaskMaster.asyncTask(new TestGetChestThread(), 0);
		}
		CivMessage.sendSuccess(sender, "Started "+count+" threads, watch logs.");
	}

	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}

	@Override
	public void showHelp() {
		showBasicHelp();
	}

	private void isYourCoal() throws CivException {
		if (!getPlayer().getName().equalsIgnoreCase("YourCoal")) {
			throw new CivException("You must be netizen to run these commands.");
		}
	}
	
	
	@Override
	public void permissionCheck() throws CivException {
		isYourCoal();
	}
}
