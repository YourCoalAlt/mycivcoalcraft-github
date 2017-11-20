package com.avrgaming.civcraft.command.moderator;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.threading.tasks.PlayerModerationBan;
import com.avrgaming.civcraft.threading.tasks.PlayerModerationKick;
import com.avrgaming.civcraft.util.CivColor;

public class ModeratorCommand extends CommandBase {
	
	public static boolean global = true;
	
	@Override
	public void init() {
		command = "/mod";
		displayName = "Moderator Controls";
		
		commands.put("alert", "Sends a global message alerting players.");
		commands.put("helpmsg", "Sends a global message for helping players.");
		
		commands.put("check", "[player] Displays information on a player's current ban status, reason, mute status, reason, and warning points.");
		commands.put("history", "[player] Displays information on a player's history of being muted, banned, kicked, and warnings.");
		
		commands.put("kick", "[player] [reason...] Kick a player from the server.");
		
		commands.put("ban", "[player] [seconds] [minutes] [hours] [reason...] Ban a player from the server.");
		commands.put("unban", "[player] Unban a player from the server.");
		
		commands.put("banip", "[IP] [reason...] Ban an IP from the server.");
		commands.put("unbaipn", "[IP] Unban an IP from the server.");
		
		commands.put("mute", "[player] [seconds] [minutes] [hours] [reason...] Mutes a player on the server.");
		commands.put("unmute", "[player] Unmute a player on the server.");
		
		commands.put("muteip", "[IP] [reason...] Mutes an IP on the server.");
		commands.put("unmuteip", "[IP] Unmute an IP on the server.");
		
		commands.put("warn", "[player] [points] [reason...] Gives this player warning points.");
		commands.put("toggleglobal", "Changes whether or not global chat is disabled or not.");
	}
	
	public void alert_cmd() throws CivException {
		valMod();
		if (args.length < 2) {
			throw new CivException("Please enter your message.");
		}
		
		StringBuilder buffer = new StringBuilder();
		for(int i = 1; i < args.length; i++) {
			buffer.append(' ').append(args[i]);
		}
		
		String message = buffer.toString();
		CivMessage.globalModerator(CivColor.RedBold+"[Alert] "+CivColor.RESET+message);
	}
	
	public void helpmsg_cmd() throws CivException {
		if (args.length < 2) {
			throw new CivException("Please enter your message.");
		}
		
		StringBuilder buffer = new StringBuilder();
		for(int i = 1; i < args.length; i++) {
			buffer.append(' ').append(args[i]);
		}
		
		String message = buffer.toString();
		CivMessage.globalModerator(CivColor.GoldBold+"[Help/Tip] "+CivColor.RESET+message);
	}
	
	public void check_cmd() throws CivException, SQLException {
		valHelperMod();
		if (args.length < 2) {
			throw new CivException("Enter a player name to check current status.");
		}
		
		Resident s = null;
		if (sender instanceof Player) {
			s = CivGlobal.getResident(getPlayer());
		}
		
		Resident res = CivGlobal.getResident(args[1]);
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
		if (s != null) sdf.setTimeZone(TimeZone.getTimeZone(s.getTimezone())); else sdf.setTimeZone(TimeZone.getDefault());
		
		String out = CivColor.LightBlue+res.getName()+"'s Status;";
		if (res.isBanned()) {
			out += CivColor.Rose+"Banned, Reason:;"+CivColor.RESET+res.getBannedMessage()+";";
			out += CivColor.Rose+"Unbanned at:;"+CivColor.Yellow+sdf.format(res.getBannedLength());
		} else {
			out += CivColor.LightGreen+"Not Banned :)";
		}
		
		out += ";";
		
		if (res.isMuted()) {
			out += CivColor.Rose+"Muted, Reason:;"+CivColor.RESET+res.getMutedMessage()+";";
			out += CivColor.Rose+"Unmuted at:;"+CivColor.Yellow+sdf.format(res.getMutedLength());
		} else {
			out += CivColor.LightGreen+"Not Muted :)";
		}
		
		CivMessage.send(sender, out.split(";"));
	}
	
	public void kick_cmd() throws CivException {
		valHelperMod();
		String name = "";
		if (sender instanceof Player) {
			name = CivColor.LightPurpleItalic+sender.getName();
		} else if (sender instanceof ConsoleCommandSender) {
			name = CivColor.RedBold+"Console";
		}
		
		//arg 1 = Username to kick
		//arg 2+ = message
		
		if (args.length < 3) {
			throw new CivException("Please enter a playername and then your message.");
		}
		
		StringBuilder buffer = new StringBuilder();
		for(int i = 2; i < args.length; i++) {
			buffer.append(' ').append(args[i]);
		}
		String message = buffer.toString();
		
		Resident res = CivGlobal.getResident(args[1]);
		if (res != null) {
			TaskMaster.syncTask(new PlayerModerationKick(res.getName(), sender.getName(), message));
			
			CivMessage.globalModerator(CivColor.RoseBold+"[Kick] "+CivColor.RESET+"Player "+CivColor.LightGreenItalic+res.getName()+CivColor.RESET+" has been kicked by "+name+CivColor.RESET+
					". Reason:"+CivColor.LightGreenItalic+message);
		} else {
			throw new CivException("Cannot kick player. Please check your arguments are correct: /mod kick [player] [reason...]");
		}
	}
	
	public void ban_cmd() throws CivException {
		valMod();
		String name = "";
		if (sender instanceof Player) {
			name = CivColor.LightPurpleItalic+sender.getName();
		} else if (sender instanceof ConsoleCommandSender) {
			name = CivColor.RedBold+"Console";
		}
		
		//arg 1 = Username to ban
		//arg 2 = Time in seconds
		//arg 3 = time in minutes
		//arg 4 = time in hours
		//arg 5+ = message
		
		if (args.length < 6) {
			throw new CivException("Please enter a playername, seconds, minutes, hours, and then your message.");
		}
		
		int sec = Integer.valueOf(args[2]);
		int min = Integer.valueOf(args[3]);
		int hours = Integer.valueOf(args[4]);
		
		StringBuilder buffer = new StringBuilder();
		for(int i = 5; i < args.length; i++) {
			buffer.append(' ').append(args[i]);
		}
		String message = buffer.toString();
		
		Resident res = CivGlobal.getResident(args[1]);
		if (res != null) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				CivMessage.sendTitle(p, 15, 50, 15, CivColor.RoseBold+"Player Banned", CivColor.LightGreenItalic+res.getName()+CivColor.RESET+" banned by "+name);
			}
			res.setBanned(true);
			res.setBannedMessage(message);
			res.setBannedLength(sec, min, hours);
			try {
				res.saveNow();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			TaskMaster.syncTask(new PlayerModerationBan(res.getName(), sender.getName(), message, hours, min, sec));
			CivMessage.globalModerator(CivColor.RoseBold+"[Banned] "+CivColor.RESET+"Player "+CivColor.LightGreenItalic+res.getName()+CivColor.RESET+" has been banned by "+name+CivColor.RESET+
					". Length: "+CivColor.LightGreenItalic+hours+" Hours, "+min+" Minutes, "+sec+" Seconds"+CivColor.RESET+". Reason:"+CivColor.LightGreenItalic+message);
		} else {
			throw new CivException("Cannot ban player. Please check your arguments are correct: /mod ban [player] [seconds] [minutes] [hours] [reason...]");
		}
	}
	
	public void unban_cmd() throws CivException, SQLException {
		valMod();
		String name = "";
		if (sender instanceof Player) {
			name = CivColor.LightPurpleItalic+sender.getName();
		} else if (sender instanceof ConsoleCommandSender) {
			name = CivColor.RedBold+"Console";
		}
		
		if (args.length < 2) {
			throw new CivException("Enter a player name to unban.");
		}
		
		Resident res = CivGlobal.getResident(args[1]);
		if (res != null && res.isBanned()) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				CivMessage.sendTitle(p, 15, 50, 15, CivColor.RoseBold+"Player Unbanned", CivColor.LightGreenItalic+res.getName()+CivColor.RESET+" unbanned by "+name);
			}
			res.resetBannedLength();
			res.setBanned(false);
			res.setBannedMessage("null");
			CivMessage.globalModerator(CivColor.LightGreenBold+"[Unbanned] "+CivColor.RESET+"Player "+CivColor.LightGreenItalic+res.getName()+CivColor.RESET+" has been unbanned by "+name+CivColor.RESET+".");
			res.saveNow();
			CivMessage.sendSuccess(sender, "Unbanned "+args[1]+".");
		} else if (!res.isBanned()) {
			throw new CivException(args[1]+", is not banned.");
		} else {
			throw new CivException("Couldn't find "+args[1]+".");
		}
	}
	
	public void mute_cmd() throws CivException {
		valHelperMod();
		String name = "";
		if (sender instanceof Player) {
			name = CivColor.LightPurpleItalic+sender.getName();
		} else if (sender instanceof ConsoleCommandSender) {
			name = CivColor.RedBold+"Console";
		}
		
		//arg 1 = Username to ban
		//arg 2 = Time in seconds
		//arg 3 = time in minutes
		//arg 4 = time in hours
		//arg 5+ = message
		
		if (args.length < 6) {
			throw new CivException("Please enter a playername, seconds, minutes, hours, and then your message.");
		}
		
		int sec = Integer.valueOf(args[2]);
		int min = Integer.valueOf(args[3]);
		int hours = Integer.valueOf(args[4]);
		
		StringBuilder buffer = new StringBuilder();
		for(int i = 5; i < args.length; i++) {
			buffer.append(' ').append(args[i]);
		}
		String message = buffer.toString();
		
		Resident res = CivGlobal.getResident(args[1]);
		if (res != null) {
			CivMessage.sendSound(res, Sound.BLOCK_ANVIL_DESTROY, 1.0f, 0.2f);
			for (Player p : Bukkit.getOnlinePlayers()) {
				CivMessage.sendTitle(p, 15, 50, 15, CivColor.RoseBold+"Player Muted", CivColor.LightGreenItalic+res.getName()+CivColor.RESET+" muted by "+name);
			}
			res.setMuted(true);
			res.setMutedMessage(message);
			res.setMutedLength(sec, min, hours);
			try {
				res.saveNow();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			CivMessage.globalModerator(CivColor.RoseBold+"[Muted] "+CivColor.RESET+"Player "+CivColor.LightGreenItalic+res.getName()+CivColor.RESET+" has been muted by "+name+CivColor.RESET+
					". Length: "+CivColor.LightGreenItalic+hours+" Hours, "+min+" Minutes, "+sec+" Seconds"+CivColor.RESET+". Reason:"+CivColor.LightGreenItalic+message);
			
			SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy h:mm:ss a z");
			sdf.setTimeZone(TimeZone.getTimeZone(res.getTimezone()));
			Date date = new Date(res.getMutedLength());
			String out = CivColor.LightBlueBold+"§« CivilizationCraft §»;"+
					" ;"+
					CivColor.RoseBold+"Muted By "+CivColor.GrayBold+"§» "+CivColor.RESET+name+";"+
					CivColor.RoseBold+"Reason "+CivColor.GrayBold+"§» "+CivColor.RESET+message+";"+
					CivColor.RoseBold+"Length "+CivColor.GrayBold+"§» "+CivColor.RESET+hours+" Hours, "+min+" Minutes, "+sec+" Seconds"+";"+
					CivColor.RoseBold+"Unmuted At "+CivColor.GrayBold+"§» "+CivColor.RESET+sdf.format(date)+";"+
					" ;"+
					" ;"+
					CivColor.YellowBold+"Appeal at "+CivColor.GrayBold+"§» "+CivColor.GoldBold+"http://coalcivcraft.enjin.com/forum;";
			Player p = CivGlobal.getPlayer(res);
			CivMessage.send(p, out.split(";"));
		} else {
			throw new CivException("Cannot mute player. Please check your arguments are correct: /mod mute [player] [seconds] [minutes] [hours] [reason...]");
		}
	}
	
	public void unmute_cmd() throws CivException, SQLException {
		valMod();
		String name = "";
		if (sender instanceof Player) {
			name = CivColor.LightPurpleItalic+sender.getName();
		} else if (sender instanceof ConsoleCommandSender) {
			name = CivColor.RedBold+"Console";
		}
		
		if (args.length < 2) {
			throw new CivException("Enter a player name to unmute.");
		}
		
		Resident res = CivGlobal.getResident(args[1]);
		if (res != null && res.isMuted()) {
			CivMessage.sendSound(res, Sound.BLOCK_ANVIL_DESTROY, 1.0f, 0.2f);
			for (Player p : Bukkit.getOnlinePlayers()) {
				CivMessage.sendTitle(p, 15, 50, 15, CivColor.RoseBold+"Player Unmuted", CivColor.LightGreenItalic+res.getName()+CivColor.RESET+" unmuted by "+name);
			}
			res.resetMutedLength();
			res.setMuted(false);
			res.setMutedMessage("null");
			CivMessage.globalModerator(CivColor.LightGreenBold+"[Unmute] "+CivColor.RESET+"Player "+CivColor.LightGreenItalic+res.getName()+CivColor.RESET+" has been unmuted by "+name+CivColor.RESET+".");
			res.saveNow();
			CivMessage.sendSuccess(sender, "Unmuted "+args[1]+".");
		} else if (!res.isMuted()) {
			throw new CivException(args[1]+", is not muted.");
		} else {
			throw new CivException("Couldn't find "+args[1]+".");
		}
	}
	
	public void toggleglobal_cmd() throws CivException {
		valMod();
		if (global) {
			global = false;
			CivMessage.globalModerator(CivColor.RedBold+"[Alert] Global Chat has been disabled!");
		} else {
			global = true;
			CivMessage.globalModerator(CivColor.GreenBold+"[Alert] Global Chat has been enabled!");
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
	
//	private void valHelper() throws CivException {
//		if (!getPlayer().hasPermission(CivSettings.HELPER)) {
//			throw new CivException("You must be a helper to use this command.");
//		}
//	}
	
	private void valMod() throws CivException {
		if (!(sender instanceof ConsoleCommandSender)) {
			if (!getPlayer().hasPermission(CivSettings.MODERATOR)) {
				throw new CivException("You must be a moderator to use this command.");
			}
		}
	}
	
	private void valHelperMod() throws CivException {
		if (!(sender instanceof ConsoleCommandSender)) {
			if (!getPlayer().hasPermission(CivSettings.HELPER) && !getPlayer().hasPermission(CivSettings.MODERATOR)) {
				throw new CivException("You must be a helper or moderator to use this command.");
			}
		}
	}
	
	@Override
	public void permissionCheck() throws CivException {
		if (sender instanceof ConsoleCommandSender) {
		} else if (sender instanceof Player) {
			if (!getPlayer().hasPermission(CivSettings.HELPER) && !getPlayer().hasPermission(CivSettings.MODERATOR) &&
				!getPlayer().hasPermission(CivSettings.MINI_ADMIN)) {
				throw new CivException("Only helpers, moderators, admins, and staff can use this command.");
			}
		}
	}
	
	@Override
	public void doLogging() {
		CivLog.adminlog(sender.getName(), "/mod "+this.combineArgs(args));
	}
}
