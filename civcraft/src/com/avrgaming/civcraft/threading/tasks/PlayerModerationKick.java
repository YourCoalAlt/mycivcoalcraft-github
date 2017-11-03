package com.avrgaming.civcraft.threading.tasks;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;

public class PlayerModerationKick implements Runnable {

	String name;
	String kickedBy;
	String reason;
	
	public PlayerModerationKick(String name, String kickedBy, String reason) {
		this.name = name;
		this.kickedBy = kickedBy;
		this.reason = reason;
	}
	
	@Override
	public void run() {
		Player p;
		try {
			p = CivGlobal.getPlayer(name);
		} catch (CivException e) {
			return;
		}
		
		p.kickPlayer(" §b§l« CivilizationCraft »"+"\n"+
				" "+"\n"+
				"§c§lKicked By §r§8»§ §d§o"+kickedBy+"\n"+
				"§c§lReason §r§8»§ §f"+reason+"\n"+
				" "+"\n"+
				" "+"\n"+
				"§7§o[You may re-join the server.]"+"\n");
	}
}
