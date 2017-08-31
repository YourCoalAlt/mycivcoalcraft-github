package com.avrgaming.civcraft.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigQuarry {
	
	public String type;
	public Integer input;
	public Integer input_data;
	public Integer type_id;
	public int type_data;
	public Integer amount;
	public String custom_id;
	public String loot_type;
	public Double drop_chance;
	
	public static void loadConfig(FileConfiguration cfg, ArrayList<ConfigQuarry> configList) {
		configList.clear();
		List<Map<?, ?>> drops = cfg.getMapList("quarry_drops");
		for (Map<?, ?> item : drops) {
			ConfigQuarry g = new ConfigQuarry();
			g.type = (String)item.get("type");
			g.input = (Integer)item.get("input");
			if ((Integer)item.get("input_data") != null) {
				g.input_data = (Integer)item.get("input_data");
			} else {
				g.input_data = 0;
			}
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
			if (item.get("amount") != null) {
				g.amount = (Integer)item.get("amount");
			} else {
				g.amount = 1;
			}
			g.loot_type = (String)item.get("loot_type");
			g.drop_chance = (Double)item.get("drop_chance");
			configList.add(g);
		}
		CivLog.info("Loaded "+configList.size()+" Quarry Drops.");
	}
}
