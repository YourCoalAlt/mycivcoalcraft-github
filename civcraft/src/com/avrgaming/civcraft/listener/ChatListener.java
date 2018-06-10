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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.avrgaming.civcraft.accounts.AccountLogger;
import com.avrgaming.civcraft.command.moderator.ModeratorCommand;
import com.avrgaming.civcraft.config.perms.CivPerms;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	void OnPlayerAsyncChatEvent(AsyncPlayerChatEvent event) throws CivException {
		Player p = event.getPlayer();
		Resident res = CivGlobal.getResident(p);
		if (res == null) {
			CivMessage.sendError(p, "Resident is null, cannot chat globally!");
			event.setCancelled(true);
			return;
		}
		
		AccountLogger al = CivGlobal.getAccount(p.getUniqueId().toString());
		if (!res.isInteractiveMode() && !res.isCivChat() && !res.isTownChat()) {
			if (al.isMuted()) {
				CivMessage.sendError(res, "You are muted! Reason: "+al.getMuteMessage());
				event.setCancelled(true);
				return;
			}
			
			if (!ModeratorCommand.global && !CivPerms.isHelper(p)) {
				CivMessage.sendError(res, "Global Chat is currently disabled.");
				event.setCancelled(true);
			}
		}
		
		if (res.isInteractiveMode()) {
			res.getInteractiveResponse().respond(event.getMessage(), res);
			event.setCancelled(true);
		}
		
		if (res.isTownChat()) {
			event.setCancelled(true);
			if (res.getTownChatOverride() == null) {
				CivMessage.sendTownChat(res.getTown(), res, event.getFormat(), event.getMessage());
			} else {
				CivMessage.sendTownChat(res.getTownChatOverride(), res, event.getFormat(), event.getMessage());
			}
		}
		
		if (res.isCivChat()) {
			event.setCancelled(true);
			if (res.getCivChatOverride() == null) {
				CivMessage.sendCivChat(res.getTown().getCiv(), res, event.getFormat(), event.getMessage());
			} else {
				CivMessage.sendCivChat(res.getCivChatOverride(), res, event.getFormat(), event.getMessage());
			}
		}
		
		if (!event.isCancelled()) {
			event.setCancelled(true);
			
			String civ;
			if (res.hasCiv()) civ = res.getCiv().getName();
			else civ = "None";
			String town;
			if (res.hasTown()) town = res.getTown().getName();
			else town = "None";
			String onlineTime = "";
			if (res.getLastOnline() < System.currentTimeMillis()) {
				long time = (System.currentTimeMillis() - res.getLastOnline()) / 1000;
			    int hours = (int) time / 3600;
			    int remainder = (int) time - hours * 3600;
			    int mins = remainder / 60;
			    remainder = remainder - mins * 60;
			    int secs = remainder;
				if (hours > 0) onlineTime += hours+" hr, ";
				if (mins > 0) onlineTime += mins+" min, ";
				if (secs > 0) onlineTime += secs+" sec";
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
			Calendar cal = Calendar.getInstance();
			cal.setTimeZone(TimeZone.getTimeZone(res.getTimezone()));
			sdf.setTimeZone(cal.getTimeZone());
			
			CivLog.chat(p.getName()+"("+p.getUniqueId().toString()+")", event.getMessage());
			String format = String.format(event.getFormat(), p.getDisplayName(), event.getMessage());
			TextComponent newFormat = new TextComponent(format);
			newFormat.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
					CivColor.GreenBold+"Civilization: "+CivColor.LightGreen+civ+"\n"+
					CivColor.GreenBold+"Town: "+CivColor.LightGreen+town+"\n"+
					CivColor.GoldBold+"Online For: "+CivColor.Yellow+onlineTime+"\n"+
					CivColor.GrayBold+"Time Sent: "+CivColor.LightGray+sdf.format(cal.getTime())
					).create()));
	        for (Player all : Bukkit.getOnlinePlayers()) {
	            all.spigot().sendMessage(newFormat);
	        }
		}
	}
    
}
