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
package com.avrgaming.civcraft.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.ResidentExperience;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.camp.Camp;
import com.avrgaming.civcraft.util.CivColor;

public class CivMessage {
	
	// Stores the player name and the hash code of the last message sent to prevent error spamming the player.
	private static HashMap<String, Integer> lastMessageHashCode = new HashMap<String, Integer>();
	
	// Indexed off of town names, contains a list of extra people who listen to town chats.(mostly for admins to listen to towns)
	private static Map<String, ArrayList<String>> extraTownChatListeners = new ConcurrentHashMap<String, ArrayList<String>>();
	
	// Indexed off of civ names, contains a list of extra people who listen to civ chats. (mostly for admins to list to civs)
	private static Map<String, ArrayList<String>> extraCivChatListeners = new ConcurrentHashMap<String, ArrayList<String>>();
	
	public static void sendNoRepeat(Object sender, String line) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			Integer hashcode = lastMessageHashCode.get(player.getName());
			if (hashcode != null && hashcode == line.hashCode()) return;
			lastMessageHashCode.put(player.getName(), line.hashCode());
		} else if (sender instanceof Resident) {
			Resident res = (Resident)sender;
			Integer hashcode = lastMessageHashCode.get(res.getName());
			if (hashcode != null && hashcode == line.hashCode()) return;
			lastMessageHashCode.put(res.getName(), line.hashCode());
		}
		sendError(sender, line);
	}
	
	public static void send(Object sender, String line) {
		if ((sender instanceof Player)) {
			((Player)sender).sendMessage(line);
		} else if (sender instanceof CommandSender) {
			((CommandSender)sender).sendMessage(line);
		} else if (sender instanceof ResidentExperience) {
			try {
				CivGlobal.getPlayerE(((ResidentExperience) sender)).sendMessage(line);
			} catch (CivException e) {} // No player online
		} else if (sender instanceof Resident) {
			try {
				CivGlobal.getPlayer(((Resident) sender)).sendMessage(line);
			} catch (CivException e) {} // No player online
		} else if (sender == null) return;
		else {
			CivLog.warning("Could not send message to '"+sender+"' with message: '"+line+"'");
		}
	}
	public static void send(Object sender, String[] lines) {
		boolean isPlayer = false;
		if (sender instanceof Player) {
			isPlayer = true;
		}
		
		for (String line : lines) {
			if (isPlayer) {
				((Player) sender).sendMessage(line);
			} else {
				((CommandSender) sender).sendMessage(line);
			}
		}
	}
	
	public static void send(Object sender, List<String> outs) {
		for (String str : outs) {
			send(sender, str);
		}
	}
	
	public static void sendQuestExp(Object sender, String message) {
		send(sender, CivColor.Green+"[Quest] "+CivColor.Gray+message);
	}
	
	public static void sendSuccess(Object sender, String message) {
		send(sender, CivColor.GrayBold+"  � "+CivColor.LightGreenItalic+"[Success] "+CivColor.LightGreen+message);
	}
	
	public static void sendWarning(Object sender, String message) {		
		send(sender, CivColor.GrayBold+"  � "+CivColor.YellowItalic+"[Warning] "+CivColor.Rose+message);
	}
	
	public static void sendError(Object sender, String message) {		
		send(sender, CivColor.GrayBold+"  � "+CivColor.RoseItalic+"[Error] "+CivColor.Rose+message);
	}
	
	public static void sendErrorPlayerCmd(Object sender) {		
		send(sender, CivColor.GrayBold+"  � "+CivColor.RoseItalic+"[Error] "+CivColor.Rose+"Only a player can execute this command.");
	}
	
	public static void sendErrorNoRepeat(Object sender, String line) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			Integer hashcode = lastMessageHashCode.get(player.getName());
			if (hashcode != null && hashcode == line.hashCode()) return;
			lastMessageHashCode.put(player.getName(), line.hashCode());
		}
		sendError(sender, line);
	}
	
	// Sends message to playerName(if online) AND console.
	public static void console(String playerName, String line) {
		try {
			Player player = CivGlobal.getPlayer(playerName);
			send(player, line);
		} catch (CivException e) {
		}
		CivLog.info(line);	
	}
	
	public static void sendTitle(Object sender, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
		Player player = null;
		if ((sender instanceof Player)) {
			player = (Player) sender;
		} else if (sender instanceof Resident) {
			try {
				player = CivGlobal.getPlayer((Resident) sender);
			} catch (CivException e) {} // Not online
		}
		if (player != null) {
			player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
		} else {
			CivLog.debug("Could not send PCN Title to player "+player+" {title: "+title+"}, {subtitle: "+subTitle+"}");
		}
	}
	
	public static void sendTitle(Object sender, String title, String subTitle) {
		sendTitle(sender, title, subTitle, 10, 60, 10);
	}
	
	public static void globalTitle(String title, String subTitle, int fadeIn, int show, int fadeOut) {
		CivLog.info("[GlobalTitle] "+title+" - "+subTitle);
		for (Player player : Bukkit.getOnlinePlayers()) {
			CivMessage.sendTitle(player, title, subTitle, fadeIn, show, fadeOut);
		}
	}
	
	public static void globalTitle(String title, String subTitle) {
		globalTitle(title, subTitle, 10, 60, 10);
	}
	
	public static String buildHeading(String title) {
		String line = "================================================";
		String titleBracket = "[ "+CivColor.YellowBold+title+CivColor.LightBlue+" ]";
		
		if (titleBracket.length() > line.length()) {
			return CivColor.LightBlue+"=[ "+titleBracket+" ]=";
		}
		
		int min = (line.length() / 2) - titleBracket.length() / 2;
		int max = (line.length() / 2) + titleBracket.length() / 2;
		
		String out = CivColor.LightBlue+line.substring(0, Math.max(0, min));
		out += titleBracket + line.substring(max);
		return out;
	}
	
	public static String buildSubheading(String title) {
		String line = "--------------------------------";
		String titleBracket = "[ "+CivColor.Yellow+title+CivColor.LightBlue+" ]";
		
		if (titleBracket.length() > line.length()) {
			return CivColor.LightBlue+"-[ "+titleBracket+" ]-";
		}
		
		int min = (line.length() / 2) - titleBracket.length() / 2;
		int max = (line.length() / 2) + titleBracket.length() / 2;
		
		String out = CivColor.LightBlue+line.substring(0, Math.max(0, min));
		out += titleBracket + line.substring(max);
		return out;
	}
	
	public static void sendHeading(Object sender, String title) {
		send(sender, buildHeading(title));
	}
	
	public static void sendSubHeading(Object sender, String title) {
		send(sender, buildSubheading(title));
	}
	
	public static void sendAll(String str) {
		CivLog.info("[sendAll] "+str);
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(str);
		}
	}
	
	public static void sendAllNoLog(String str) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(str);
		}
	}
	
	public static void global(String str) {
		CivLog.info("[Global] "+str);
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(CivColor.LightBlue+"[Global] "+CivColor.White+str);
		}
	}
	
	public static void globalModerator(String string) {
		CivLog.info("[GlobalModerator] "+string);
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(CivColor.LightPurple+"[Moderator] "+CivColor.White+string);
		}
	}
	
	public static void globalHeading(String string) {
		CivLog.info("[GlobalHeading] "+string);
		for (Player player : Bukkit.getOnlinePlayers()) {
			send(player, buildHeading(string));
		}
	}
	
	public static void sendCamp(Camp camp, String message) {
		for (Resident resident : camp.getMembers()) {
			try {
				Player player = CivGlobal.getPlayer(resident);
				player.sendMessage(CivColor.Yellow+"[Camp] "+CivColor.Yellow+message);		
				CivLog.info("[Camp:"+camp.getName()+"] "+message);

			} catch (CivException e) {
				//player not online.
			}
		}
	}
	
	public static void sendScout(Civilization civ, String string) {
		CivLog.info("[Scout:"+civ.getName()+"] "+string);
		for (Town t : civ.getTowns()) {
			for (Resident resident : t.getResidents()) {
				if (!resident.isShowScout()) continue;
				
				Player player;
				try {
					player = CivGlobal.getPlayer(resident);
					if (player != null) {
						CivMessage.send(player, CivColor.Purple+"[Scout] "+CivColor.White+string);
					}
				} catch (CivException e) { // Not online, skip.
				}
			}
		}
	}
	
	public static void sendTown(Town town, String string) {
		CivLog.info("[Town:"+town.getName()+"] "+string);
		for (Resident resident : town.getResidents()) {
			if (!resident.isShowTown()) continue;
			
			Player player;
			try {
				player = CivGlobal.getPlayer(resident);
				if (player != null) {
					CivMessage.send(player, CivColor.Gold+"[Town] "+CivColor.White+string);
				}
			} catch (CivException e) { // Not online, skip.
			}
		}
	}
	
	public static void sendCiv(Civilization civ, String string) {
		CivLog.info("[Civ:"+civ.getName()+"] "+string);
		for (Town t : civ.getTowns()) {
			for (Resident resident : t.getResidents()) {
				if (!resident.isShowCiv()) continue;
				
				Player player;
				try {
					player = CivGlobal.getPlayer(resident);
					if (player != null) {
						CivMessage.send(player, CivColor.LightPurple+"[Civ] "+CivColor.White+string);
					}
				} catch (CivException e) { // Not online, skip.
				}
			}
		}
	}
	
	public static void sendCivTechError(Civilization civ, String string) {
		CivLog.info("[Civ:"+civ.getName()+"][TECH_ERROR] "+string);
		for (Town t : civ.getTowns()) {
			for (Resident resident : t.getResidents()) {
				if (!resident.isShowCiv()) continue;
				
				Player player;
				try {
					player = CivGlobal.getPlayer(resident);
					if (player != null) {
						CivMessage.send(player, CivColor.LightPurple+"[Civ] "+CivColor.RoseItalic+"[Tech Error] "+CivColor.White+string);
					}
				} catch (CivException e) { // Not online, skip.
				}
			}
		}
	}
	
	public static void sendTownChat(Town town, Resident resident, String format, String message) {
		if (town == null) {
			try {
				Player player = CivGlobal.getPlayer(resident);
				player.sendMessage(CivColor.Rose+"You are not part of a town, nobody hears you. Type /tc to chat normally.");
			} catch (CivException e) { // Not online, skip.
			}
			return;
		}
		
		CivLog.chat("(Town:"+town.getName()+" Res:"+resident.getName()+")", message);
		for (Resident r : town.getResidents()) {
			try {
				Player player = CivGlobal.getPlayer(r);
				String msg = CivColor.LightBlue+"[TC]"+CivColor.White+String.format(format, resident.getName(), message);
				player.sendMessage(msg);
			} catch (CivException e) { // Not online, skip.
			}
		}
		
		for (String name : getExtraTownChatListeners(town)) {
			try {
				Player player = CivGlobal.getPlayer(name);
				String msg = CivColor.LightBlue+"[TC:"+town.getName()+"]"+CivColor.White+String.format(format, resident.getName(), message);
				player.sendMessage(msg);
			} catch (CivException e) { // Not online, skip.
			}
		}
	}
	
	public static void sendCivChat(Civilization civ, Resident resident, String format, String message) {
		if (civ == null) {
			try {
				Player player = CivGlobal.getPlayer(resident);
				player.sendMessage(CivColor.Rose+"You are not part of a civ, nobody hears you. Type /cc to chat normally.");
			} catch (CivException e) { // Not online, skip.
			}
			return;
		}
		
		String townName = "";
		if (resident.getTown() != null) {
			townName = resident.getTown().getName();
		}
		
		CivLog.chat("(Civ:"+civ.getName()+" Res:"+resident.getName()+")", message);
		for (Town t : civ.getTowns()) {
			for (Resident r : t.getResidents()) {
				try {
					Player player = CivGlobal.getPlayer(r);
					String msg = CivColor.Gold+"[CC "+townName+"]"+CivColor.White+String.format(format, resident.getName(), message);
					player.sendMessage(msg);
				} catch (CivException e) { // Not online, skip.
				}
			}
		}
		
		for (String name : getExtraCivChatListeners(civ)) {
			try {
				Player player = CivGlobal.getPlayer(name);
				String msg = CivColor.Gold+"[CC:"+civ.getName()+" "+townName+"]"+CivColor.White+String.format(format, resident.getName(), message);
				player.sendMessage(msg);
			} catch (CivException e) { // Not online, skip.
			}
		}
		return;
	}
	
	public static void addExtraTownChatListener(Town town, String name) {
		ArrayList<String> names = extraTownChatListeners.get(town.getName().toLowerCase());
		if (names == null) {
			names = new ArrayList<String>();
		}
		
		for (String str : names) {
			if (str.equals(name)) return;
		}
		
		names.add(name);		
		extraTownChatListeners.put(town.getName().toLowerCase(), names);
	}
	
	public static void removeExtraTownChatListener(Town town, String name) {
		ArrayList<String> names = extraTownChatListeners.get(town.getName().toLowerCase());
		if (names == null) return;
		
		for (String str : names) {
			if (str.equals(name)) {
				names.remove(str);
				break;
			}
		}
		extraTownChatListeners.put(town.getName().toLowerCase(), names);
	}
	
	public static ArrayList<String> getExtraTownChatListeners(Town town) {
		ArrayList<String> names = extraTownChatListeners.get(town.getName().toLowerCase());
		if (names == null) {
			return new ArrayList<String>();
		}
		return names;
	}
	
	public static void addExtraCivChatListener(Civilization civ, String name) {
		ArrayList<String> names = extraCivChatListeners.get(civ.getName().toLowerCase());
		if (names == null) {
			names = new ArrayList<String>();
		}
		
		for (String str : names) {
			if (str.equals(name)) return;
		}
		
		names.add(name);
		extraCivChatListeners.put(civ.getName().toLowerCase(), names);
	}
	
	public static void removeExtraCivChatListener(Civilization civ, String name) {
		ArrayList<String> names = extraCivChatListeners.get(civ.getName().toLowerCase());
		if (names == null) return;
		
		for (String str : names) {
			if (str.equals(name)) {
				names.remove(str);
				break;
			}
		}
		extraCivChatListeners.put(civ.getName().toLowerCase(), names);
	}
	
	public static ArrayList<String> getExtraCivChatListeners(Civilization civ) {
		ArrayList<String> names = extraCivChatListeners.get(civ.getName().toLowerCase());
		if (names == null) {
			return new ArrayList<String>();
		}
		return names;
	}
	
	public static void sendSound(Object sender, Sound sound, float v, float p) {
		if ((sender instanceof Player)) {
			((Player) sender).playSound(((Player) sender).getLocation(), sound, v, p);
		} else if (sender instanceof CommandSender) {
			CivLog.error("Cannot send sound to cmd sender!");
		} else if (sender instanceof ResidentExperience) {
			try {
				CivGlobal.getPlayerE(((ResidentExperience) sender)).playSound(((Player) sender).getLocation(), sound, v, p);
			} catch (CivException e) { // No player online
			}
		} else if (sender instanceof Resident) {
			try {
				CivGlobal.getPlayer(((Resident) sender)).playSound(CivGlobal.getPlayer(((Resident) sender)).getLocation(), sound, v, p);
			} catch (CivException e) { // No player online
			}
		} else if (sender == null) return;
		else {
			CivLog.warning("Could not send sound to '"+sender+"' with sound: '"+sound.name()+"' of "+sound.getDeclaringClass());
		}
	}
	
	public static void sendTownSound(Town town, Sound sound, float v, float p) {
		for (Resident resident : town.getResidents()) {
			Player player;
			try {
				player = CivGlobal.getPlayer(resident);
				player.playSound(player.getLocation(), sound, v, p);
			} catch (CivException e) { // Not online, skip.
			}
		}
	}
	
	public static void sendCivSound(Civilization c, Sound sound, float v, float p) {
		for (Town t : c.getTowns()) {
			for (Resident resident : t.getResidents()) {
				Player player;
				try {
					player = CivGlobal.getPlayer(resident);
					player.playSound(player.getLocation(), sound, v, p);
				} catch (CivException e) { // Not online, skip.
				}
			}
		}
	}
	
	public static void playGlobalSound(Sound sound, float f, float g) {
		CivLog.info("[GlobalSound] "+sound.toString());
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.playSound(p.getLocation(), sound, f, g);
		}
	}
	
	public static void worldSound(Sound sound, float g) {
		World w = Bukkit.getWorld("world");
		Location l = new Location(w, 0, 0, 0);
		w.playSound(l, sound, 100000, g);
	}
	
	public static void sendTownHeading(Town town, String string) {
		CivLog.info("[Town:"+town.getName()+"] "+string);
		for (Resident resident : town.getResidents()) {
			if (!resident.isShowTown()) continue;
			
			Player player;
			try {
				player = CivGlobal.getPlayer(resident);
				if (player != null) {
					sendHeading(player, string);
				}
			} catch (CivException e) { // Not online, skip.
			}
		}
	}
}
