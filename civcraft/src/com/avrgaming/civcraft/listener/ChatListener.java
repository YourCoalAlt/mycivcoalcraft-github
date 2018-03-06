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
package com.avrgaming.civcraft.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.avrgaming.civcraft.accounts.AccountLogger;
import com.avrgaming.civcraft.command.moderator.ModeratorCommand;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;

public class ChatListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	void OnPlayerAsyncChatEvent(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		Resident resident = CivGlobal.getResident(p);
		if (resident == null) {
			CivMessage.sendError(p, "Resident is null, cannot chat globally!");
			event.setCancelled(true);
			return;
		}
		
		AccountLogger al = CivGlobal.getAccount(p.getUniqueId().toString());
		if (!resident.isInteractiveMode() && !resident.isCivChat() && !resident.isTownChat()) {
			if (al.isMuted()) {
				CivMessage.sendError(resident, "You are muted! Reason: "+al.getMuteMessage());
				event.setCancelled(true);
				return;
			}
			
			if (!ModeratorCommand.global && !p.hasPermission(CivSettings.ADMIN) && !p.hasPermission(CivSettings.MINI_ADMIN)
					 && !p.hasPermission(CivSettings.MODERATOR) && !p.hasPermission(CivSettings.HELPER) && !p.isOp()) {
				CivMessage.sendError(resident, "Global Chat is currently disabled.");
				event.setCancelled(true);
			}
		}
		
		if (resident.isTownChat()) {
			event.setCancelled(true);
			if (resident.getTownChatOverride() == null) {
				CivMessage.sendTownChat(resident.getTown(), resident, event.getFormat(), event.getMessage());
			} else {
				CivMessage.sendTownChat(resident.getTownChatOverride(), resident, event.getFormat(), event.getMessage());
			}
		}
		
		if (resident.isCivChat()) {
			event.setCancelled(true);
			if (resident.getCivChatOverride() == null) {
				CivMessage.sendCivChat(resident.getTown().getCiv(), resident, event.getFormat(), event.getMessage());
			} else {
				CivMessage.sendCivChat(resident.getCivChatOverride(), resident, event.getFormat(), event.getMessage());
			}
		}
		
		if (resident.isInteractiveMode()) {
			resident.getInteractiveResponse().respond(event.getMessage(), resident);
			event.setCancelled(true);
		}
	}
}
