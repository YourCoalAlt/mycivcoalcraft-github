package com.avrgaming.civcraft.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.avrgaming.civcraft.command.moderator.ModeratorCommand;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;
import com.dthielke.herochat.ChannelChatEvent;
import com.dthielke.herochat.Chatter;
import com.dthielke.herochat.Chatter.Result;
import com.dthielke.herochat.Herochat;

public class HeroChatListener implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChannelChatEvent(ChannelChatEvent event) {
		Player p = event.getSender().getPlayer();
		Resident resident = CivGlobal.getResident(event.getSender().getName());
		if (resident == null) {
			event.setResult(Result.FAIL);
			return;
		}
		
		if (!resident.isInteractiveMode() && !resident.isCivChat() && !resident.isTownChat()) {
			if (resident.isMuted()) {
				CivMessage.sendError(resident, "You are muted! Reason: "+resident.getMutedMessage());
				event.setResult(Result.MUTED);
			}
			
			if (!ModeratorCommand.global && !p.hasPermission(CivSettings.ADMIN) && !p.hasPermission(CivSettings.MINI_ADMIN)
					 && !p.hasPermission(CivSettings.MODERATOR) && !p.hasPermission(CivSettings.HELPER) && !p.isOp()) {
				CivMessage.sendError(resident, "Global Chat is currently disabled.");
				event.setResult(Result.FAIL);
			}
		}
		
		if (event.getChannel().getDistance() > 0) {
			for (String name : Resident.allchatters) {
				Player player;
				try {
					player = CivGlobal.getPlayer(name);
				} catch (CivException e) {
					continue;
				}
				
				Chatter you = Herochat.getChatterManager().getChatter(player);
				if (!event.getSender().isInRange(you, event.getChannel().getDistance())) {
					player.sendMessage(CivColor.White+event.getSender().getName()+"[Far]: "+event.getMessage());
				}
			}
		}
	}
}
