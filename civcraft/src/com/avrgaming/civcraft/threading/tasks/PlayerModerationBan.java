package com.avrgaming.civcraft.threading.tasks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.bukkit.entity.Player;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.Resident;

public class PlayerModerationBan implements Runnable {

	String name;
	String bannedBy;
	String reason;
	Integer hours;
	Integer min;
	Integer sec;
	
	public PlayerModerationBan(String name, String bannedBy, String reason, Integer hours, Integer min, Integer sec) {
		this.name = name;
		this.bannedBy = bannedBy;
		this.reason = reason;
		this.hours = hours;
		this.min = min;
		this.sec = sec;
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
							"§c§lBanned By §r§8»§ §d§o"+bannedBy+"\n"+
							"§c§lReason §r§8»§ §f"+reason+"\n"+
							"§c§lLength §r§8»§ §a"+hours+" Hours, "+min+" Minutes, "+sec+" Seconds"+"\n"+
							"§c§lUnbanned At §r§8»§ §f"+sdf.format(date)+"\n"+
							" "+"\n"+
							" "+"\n"+
							"§e§lAppeal at §r§8»§ §6http://coalcivcraft.enjin.com/forum"+"\n"+
							"§7§o[You are banned, cannot rejoin server.]"+"\n");
	}
}
