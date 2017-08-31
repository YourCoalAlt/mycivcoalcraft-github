/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *	[2013] AVRGAMING LLC
 *	All Rights Reserved.
 * 
 * NOTICE:	All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.	The intellectual and technical concepts contained
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

import com.avrgaming.civcraft.main.CivLog;

public class ConfigFishing {
	
	public String type;
	public String custom_id;
	public Integer type_id;
	public int type_data;
	public String loot_type;
	public double drop_chance;
	public Integer exp_min;
	public Integer exp_max;
	public double res_exp;
	
	public static void loadConfig(FileConfiguration cfg, ArrayList<ConfigFishing> configList) {
		configList.clear();
		List<Map<?, ?>> drops = cfg.getMapList("fishing_drops");
		for (Map<?, ?> item : drops) {
			ConfigFishing g = new ConfigFishing();
			if (item.get("custom_id") != null) {
				g.custom_id = (String)item.get("custom_id");
			} else {
				g.type_id = (Integer)item.get("type_id");
			}
			if ((Integer)item.get("type_data") != null) {
				g.type_data = (Integer)item.get("type_data");
			} else {
				g.type_data = 0;
			}
			g.loot_type = (String)item.get("loot_type");
			g.drop_chance = (Double)item.get("drop_chance");
			g.exp_min = (Integer)item.get("exp_min");
			g.exp_max = (Integer)item.get("exp_max");
			g.res_exp = (Double)item.get("res_exp");
			configList.add(g);
		}
		CivLog.info("Loaded "+configList.size()+" Fishing Drops.");
	}
}
