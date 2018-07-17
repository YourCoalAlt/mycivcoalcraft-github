package com.avrgaming.civcraft.war;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.avrgaming.civcraft.object.Civilization;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.CivColor;

public class WarStats {
	
	private static HashMap<String, Integer> playerDeaths = new HashMap<String, Integer>();
	private static HashMap<String, Integer> playerKills = new HashMap<String, Integer>();
	
	private static HashMap<String, Double> playerDamage = new HashMap<String, Double>();
	private static HashMap<String, Double> playerAttack = new HashMap<String, Double>();
	
	private static HashMap<String, Integer> playerDestroyedBuildings = new HashMap<String, Integer>();
	private static HashMap<String, Integer> playerDamageBuildings = new HashMap<String, Integer>();
	
	// Stores Captured Civs, key = civ who conquered, value = civ defeated
	private static HashMap<String, LinkedList<String>> conqueredCivs = new HashMap<String, LinkedList<String>>();
	// Stores Conquered Towns, key = civ who conquered, value = town defeated
	private static HashMap<String, LinkedList<String>> conqueredTowns = new HashMap<String, LinkedList<String>>();
	
	public static void clearStats() {
		playerDeaths.clear();
		playerKills.clear();
		playerDamage.clear();
		playerAttack.clear();
		playerDestroyedBuildings.clear();
		playerDamageBuildings.clear();
		conqueredCivs.clear();
		conqueredTowns.clear();
	}
	
	public static void incrementPlayerDeaths(String playerName) {
		Integer kills = playerDeaths.get(playerName);
		if (kills == null) {
			kills = 1;
		} else {
			kills++;
		}
		playerDeaths.put(playerName, kills);
	}
	
	public static String getTopDeathProne() {
		String out = "";
		int mostDeaths = 0;
		for (String playerName : playerDeaths.keySet()) {
			int deaths = playerDeaths.get(playerName);
			if (deaths > mostDeaths) {
				out = playerName;
				mostDeaths = deaths;
			}
		}
		return CivColor.LightGreenBold+out+CivColor.Gray+" ("+mostDeaths+" deaths)";
	}
	
	public static void incrementPlayerKills(String playerName) {
		Integer kills = playerKills.get(playerName);
		if (kills == null) {
			kills = 1;
		} else {
			kills++;
		}
		playerKills.put(playerName, kills);
	}
	
	public static String getTopKiller() {
		String out = "";
		int mostKills = 0;
		for (String playerName : playerKills.keySet()) {
			int kills = playerKills.get(playerName);
			if (kills > mostKills) {
				out = playerName;
				mostKills = kills;
			}
		}
		return CivColor.LightGreenBold+out+CivColor.Gray+" ("+mostKills+" kills)";
	}
	
	public static void incrementPlayerDamage(String playerName, double amt) {
		Double dmg = playerDamage.get(playerName);
		if (dmg == null) {
			dmg = amt;
		} else {
			dmg+= amt;
		}
		playerDamage.put(playerName, dmg);
	}
	
	public static String getTopPlayerDamaged() {
		String out = "";
		double mostDmg = 0;
		for (String playerName : playerDamage.keySet()) {
			double dmg = playerDamage.get(playerName);
			if (dmg > mostDmg) {
				out = playerName;
				mostDmg = dmg;
			}
		}
		return CivColor.LightGreenBold+out+CivColor.Gray+" ("+mostDmg+" damage taken)";
	}
	
	public static void incrementPlayerAttack(String playerName, double amt) {
		Double dmg = playerAttack.get(playerName);
		if (dmg == null) {
			dmg = amt;
		} else {
			dmg+= amt;
		}
		playerAttack.put(playerName, dmg);
	}
	
	public static String getTopPlayerAttack() {
		String out = "";
		double mostDmg = 0;
		for (String playerName : playerAttack.keySet()) {
			double dmg = playerAttack.get(playerName);
			if (dmg > mostDmg) {
				out = playerName;
				mostDmg = dmg;
			}
		}
		return CivColor.LightGreenBold+out+CivColor.Gray+" ("+mostDmg+" damage given)";
	}
	
	public static void incrementPlayerDestroyedBuildings(String playerName) {
		Integer b = playerDestroyedBuildings.get(playerName);
		if (b == null) {
			b = 1;
		} else {
			b++;
		}
		playerDestroyedBuildings.put(playerName, b);
	}
	
	public static String getTopDestroyedBuildings() {
		String out = "";
		int mostB = 0;
		for (String playerName : playerDestroyedBuildings.keySet()) {
			int b = playerDestroyedBuildings.get(playerName);
			if (b > mostB) {
				out = playerName;
				mostB = b;
			}
		}
		return CivColor.LightGreenBold+out+CivColor.Gray+" ("+mostB+" structures destroyed)";
	}
	
	public static void incrementPlayerDamageBuildings(String playerName, int amt) {
		Integer dmg = playerDamageBuildings.get(playerName);
		if (dmg == null) {
			dmg = amt;
		} else {
			dmg+= amt;
		}
		playerDamageBuildings.put(playerName, dmg);
	}
	
	public static String getTopPlayerDamageBuildings() {
		String out = "";
		int mostDmg = 0;
		for (String playerName : playerDamageBuildings.keySet()) {
			int dmg = playerDamageBuildings.get(playerName);
			if (dmg > mostDmg) {
				out = playerName;
				mostDmg = dmg;
			}
		}
		return CivColor.LightGreenBold+out+CivColor.Gray+" ("+mostDmg+" hitpoints damaged)";
	}
	
	public static void logCapturedCiv(Civilization winner, Civilization captured) {
		LinkedList<String> civs = conqueredCivs.get(winner.getName());
		if (civs == null) {
			civs = new LinkedList<String>();
		}
		civs.add(captured.getName());
		conqueredCivs.put(winner.getName(), civs);
	}
	
	public static List<String> getCapturedCivs() {
		LinkedList<String> out = new LinkedList<String>();
		for (String key : conqueredCivs.keySet()) {
			LinkedList<String> conquered = conqueredCivs.get(key);
			if (conquered == null) {
				continue;
			}
			
			String line = CivColor.LightGreenBold+key+CivColor.RoseBold+" Conquered: "+CivColor.Gray;
			String tmp = "";
			for (String str : conquered) {
				tmp += str+", ";
			}
			line += tmp;
			out.add(line);
		}
		return out;
	}
	
	public static void logCapturedTown(Civilization winner, Town captured) {
		LinkedList<String> towns = conqueredTowns.get(winner.getName());
		if (towns == null) {
			towns = new LinkedList<String>();
		}
		towns.add(captured.getName());
		conqueredTowns.put(winner.getName(), towns);
	}
	
	public static List<String> getCapturedTowns() {
		LinkedList<String> out = new LinkedList<String>();
		for (String key : conqueredTowns.keySet()) {
			LinkedList<String> conquered = conqueredTowns.get(key);
			if (conquered == null) {
				continue;
			}
			
			String line = CivColor.LightGreenBold+key+CivColor.LightPurpleBold+" Captured Towns of: "+CivColor.Gray;
			String tmp = "";
			for (String str : conquered) {
				tmp += str+", ";
			}
			line += tmp;
			out.add(line);
		}
		return out;
	}
}
