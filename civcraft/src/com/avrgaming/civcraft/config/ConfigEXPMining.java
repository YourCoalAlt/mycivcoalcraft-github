package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigEXPMining {
	
	public String type;
	public Integer id;
	public Double resxp;
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigEXPMining> mining_block) {
		mining_block.clear();
		List<Map<?, ?>> mining_list = cfg.getMapList("mining_block");
		for (Map<?,?> cl : mining_list) {
			ConfigEXPMining mininglevel = new ConfigEXPMining();
			mininglevel.type = (String)cl.get("type");
			mininglevel.id = (Integer)cl.get("id");
			mininglevel.resxp = (Double)cl.get("res_xp");
			
			mining_block.put(mininglevel.type, mininglevel);
		}
		CivLog.info("Loaded "+mining_block.size()+" Res EXP Mining Blocks.");		
	}
}
