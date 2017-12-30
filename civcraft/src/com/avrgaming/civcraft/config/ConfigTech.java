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
package com.avrgaming.civcraft.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Civilization;

public class ConfigTech {
	public String id;
	public String name;
	public double beaker_cost;
	public Integer cost;
	public String require_techs;
	public Integer points;
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigTech> tech_maps) {
		tech_maps.clear();
		List<Map<?, ?>> techs = cfg.getMapList("techs");
		for (Map<?, ?> confTech : techs) {
			ConfigTech tech = new ConfigTech();
			
			tech.id = (String)confTech.get("id");
			tech.name = (String)confTech.get("name");
			tech.beaker_cost = (Integer)confTech.get("beaker_cost")+.0;
			tech.cost = (Integer)confTech.get("cost");
			tech.require_techs = (String)confTech.get("require_techs");
			tech.points = (Integer)confTech.get("points");
			
			tech_maps.put(tech.id, tech);
		}
		CivLog.info("Loaded "+tech_maps.size()+" technologies.");		
	}
	
/*	public static double eraRate(Civilization civ) {
		double rate = 0.0;
		double era = (CivGlobal.highestCivEra-1) - civ.getCurrentEra();
		if (era > 0) {
			rate = (era/10);
		}
		return rate;
	}
	
	public double getAdjustedBeakerCost(Civilization civ) {
		double rate = 1.0;
		rate -= eraRate(civ);
		return Math.floor(this.beaker_cost*Math.max(rate, .01));
	}
	
	public double getAdjustedTechCost(Civilization civ) {
		double rate = 1.0;
		
//		for (Town town : civ.getTowns()) {
//			if (town.getBuffManager().hasBuff("buff_profit_sharing")) {
//				rate -= town.getBuffManager().getEffectiveDouble("buff_profit_sharing");
//			}
//		}
		rate = Math.max(rate, 0.75);
		rate -= eraRate(civ);
		return Math.floor(this.cost * Math.max(rate, .01));
	}*/
	
	public static ArrayList<ConfigTech> getAvailableTechs(Civilization civ) {
		ArrayList<ConfigTech> returnTechs = new ArrayList<ConfigTech>();
		for (ConfigTech tech : CivSettings.techs.values()) {
			if (!civ.hasTechnology(tech.id)) {
				if (tech.isAvailable(civ)) {
					returnTechs.add(tech);
				}
			}
		}
		return returnTechs;
	}
	
	public boolean isAvailable(Civilization civ) {
		if (CivGlobal.testFileFlag("debug-norequire")) {
			CivMessage.global("Ignoring requirements! debug-norequire found.");
			return true;
		}
		
		if (require_techs == null || require_techs.equals("")) return true;
		
		String[] requireTechs = require_techs.split(":");
		
		for (String reqTech : requireTechs) {
			if (!civ.hasTechnology(reqTech)) return false;
		}
		return true;
	}
	
}
