package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigEXPFishingLevel {
	
	public int level;
	public int amount;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigEXPFishingLevel> levels) {
		levels.clear();
		List<Map<?, ?>> fishing_levels = cfg.getMapList("fishing_levels");
		for (Map<?, ?> level : fishing_levels) {
			ConfigEXPFishingLevel fishing_level = new ConfigEXPFishingLevel();
			fishing_level.level = (Integer)level.get("level");
			fishing_level.amount = (Integer)level.get("amount");
			levels.put(fishing_level.level, fishing_level);
		}
		CivLog.info("Loaded "+levels.size()+" Fishing Levels.");
	}
}
