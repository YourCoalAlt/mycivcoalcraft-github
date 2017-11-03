package com.avrgaming.civcraft.threading.tasks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Resident;

public class PlayerModerationKickBan implements Runnable {

	String name;
	String kickedBy;
	String reason;
	
	public PlayerModerationKickBan(String name, String kickedBy, String reason) {
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
		Resident res = CivGlobal.getResident(p);
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy h:mm:ss a z");
		sdf.setTimeZone(TimeZone.getTimeZone(res.getTimezone()));
		Date date = new Date(res.getBannedLength());
		
		p.kickPlayer(" §b§l« CivilizationCraft »"+"\n"+
				" "+"\n"+
				"§c§lKicked By §r§8»§ §d§o"+kickedBy+"\n"+
				"§c§lReason §r§8»§ §fBanned: "+reason+"\n"+
				"§c§lUnbanned At §r§8»§ §f"+sdf.format(date)+"\n"+
				" "+"\n"+
				" "+"\n"+
				"§e§lAppeal at §r§8»§ §6http://coalcivcraft.enjin.com/forum"+"\n"+
				"§7§o[You are banned, cannot rejoin server.]"+"\n");
	}
}
