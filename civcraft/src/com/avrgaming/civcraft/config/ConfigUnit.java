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
 * is strictly forbidden unless prior written perunit is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Town;

public class ConfigUnit {
	public String id;
	public String name;
	public String class_name;
	public List<String> description;
	public String require_tech;
	public String require_struct;
	public String require_upgrade;
	public Double cost;
	public Integer hammer_cost;
	public int limit;
	public int destroy_chance;
	public int item_id;
	public int item_data;
	public int position;
	
	@SuppressWarnings("unchecked")
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigUnit> units) {
		units.clear();
		List<Map<?, ?>> configUnits = cfg.getMapList("units");
		for (Map<?, ?> b : configUnits) {
			ConfigUnit unit = new ConfigUnit();
			unit.id = (String)b.get("id");
			unit.name = (String)b.get("name");
			unit.class_name = (String)b.get("class_name");
			unit.description = (List<String>) b.get("description");
			unit.require_tech = (String)b.get("require_tech");
			unit.require_struct = (String)b.get("require_struct");
			unit.require_upgrade = (String)b.get("require_upgrade");
			unit.cost = (Double)b.get("cost");
			unit.hammer_cost = (Integer)b.get("hammer_cost");
			unit.limit = (Integer)b.get("limit");
			if ((Integer)b.get("destroy_chance") != null) { unit.destroy_chance = (Integer)b.get("destroy_chance"); } else { unit.destroy_chance = 0; }
			unit.item_id = (Integer)b.get("item_id");
			if ((Integer)b.get("item_data") != null) {
				unit.item_data = (Integer)b.get("item_data");
			} else {
				unit.item_data = 0;
			}
			unit.position = (Integer)b.get("position");
			units.put(unit.id, unit);
		}
		CivLog.info("Loaded "+units.size()+" Units.");
	}
	
	public boolean isAvailable(Town town) {
		if (CivGlobal.testFileFlag("debug-norequire")) {
			CivMessage.global("Ignoring requirements! debug-norequire found.");
			return true;
		}
		
		if (town.hasTechnology(require_tech)) {
			if (town.hasUpgrade(require_upgrade)) {
				if (town.hasStructure(require_struct)) {
					if (limit == 0 || town.getUnitTypeCount(id) < limit) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
