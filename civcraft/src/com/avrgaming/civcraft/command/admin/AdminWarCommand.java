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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.perms.CivPerms;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.PlayerModerationBan;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.war.War;

public class AdminWarCommand extends CommandBase {
	
	@Override
	public void init() {
		command = "/ad war";
		displayName = "Admin War";
		
		commands.put("start", "Turns on WarTime.");
		commands.put("stop", "Turns off WarTime.");
		commands.put("resetstart", "Resets the war start time to now.");
		//commands.put("setlastwar", "takes a date of the form: DAY:MONTH:YEAR:HOUR:MIN (24 hour time)");
		commands.put("onlywarriors", "Kicks everyone who is not at war from servers and only lets at war players in.");
	}
	
	public void onlywarriors_cmd() throws CivException {
		War.setOnlyWarriors(!War.isOnlyWarriors());
		if (War.isOnlyWarriors()) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				Resident res = CivGlobal.getResident(player);
				if (CivPerms.isMiniAdmin(player)) {
					CivMessage.send(sender, "Skipping "+player.getName()+" - Staff Rank Allowed Online.");
					continue;
				}
				
				if (res == null || !res.hasTown() || !res.getCiv().getDiplomacyManager().isAtWar()) {
					float bantime = War.getEnd().getTime() - System.currentTimeMillis();
					long seconds = (long) (bantime / 1000); long minutes = seconds / 60; long hours = minutes / 60;
//					String time = days + ":" + hours % 24 + ":" + minutes % 60 + ":" + seconds % 60; 
					
					TaskMaster.syncTask(new PlayerModerationBan(res.getName(), CivColor.RedBold+"Console",
							"Only players at war can be online during wartime! You will be unbanned at the end of war.",
							(int)(hours % 24), (int)(minutes % 60), (int)(seconds % 60)));
				}	
			}
			CivMessage.global("All players 'not at war' have been kicked and cannot rejoin.");
		} else {
			CivMessage.global("All players are now allowed to join again.");
		}
	}
	
//	public void setlastwar_cmd() throws CivException {
//		if (args.length < 2) {
//			throw new CivException("Enter a date like DAY:MONTH:YEAR:HOUR:MIN");
//		}
//		
//		String dateStr = args[1];
//		SimpleDateFormat parser = new SimpleDateFormat("d:M:y:H:m");
//		
//		Date lastwar;
//		try {
//			lastwar = parser.parse(dateStr);
//			War.setLastWarTime(lastwar);
//			CivMessage.sendSuccess(sender, "Set last war date");
//		} catch (ParseException e) {
//			throw new CivException("Couldnt parse "+args[1]+" into a date, use format: DAY:MONTH:YEAR:HOUR:MIN");
//		}
//	}
	
	public void start_cmd() {
		War.setWarTime(true);
		CivMessage.sendSuccess(sender, "WarTime enabled.");
	}
	
	public void stop_cmd() {
		War.setWarTime(false);
		CivMessage.sendSuccess(sender, "WarTime disabled.");
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
		if (sender instanceof Player) {
			CivPerms.validAdWar(getPlayer());
		}
	}
}
