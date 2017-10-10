package com.avrgaming.civcraft.interactive;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.questions.TeleportToPlayerRequest;

public class InteractiveTeleportToPlayer implements InteractiveResponse {
	
	@Override
	public void respond(String msg, Resident res) {
		Player tpP;
		try {
			tpP = CivGlobal.getPlayer(res);
		} catch (CivException e) {
			return;
		}
		
		if (msg.equalsIgnoreCase("cancel")) {
			CivMessage.sendSuccess(tpP, "Teleportation cancelled.");
			res.clearInteractiveMode();
			res.undoPreview();
			return;
		}
		
		String tpRes = null;
		for (Player p2 : Bukkit.getOnlinePlayers()) {
			if (p2.getName().equals(msg)) tpRes = p2.getName();
		}
		
		if (tpRes != null) {
			try {
				TeleportToPlayerRequest tp = new TeleportToPlayerRequest();
				tp.tper = tpP;
				Resident tpist = CivGlobal.getResident(tpRes);
				tp.tpist = tpist;
				long INVITE_TIMEOUT = 30000; //30 seconds
				CivGlobal.questionPlayer(tpP, CivGlobal.getPlayer(tpRes), tpP.getName()+" is trying to teleport to you. Do you wish to accept?", INVITE_TIMEOUT, tp);
			} catch (CivException e) {
				e.printStackTrace();
			}
			CivMessage.sendSuccess(tpP, "Requesting to Teleport to this player now...");
			return;
		} else {
			CivMessage.sendError(tpP, "Cannot find named player, please try again. (Tip: Use the TAB button for auto-completion.)");
			return;
		}
	}
}
