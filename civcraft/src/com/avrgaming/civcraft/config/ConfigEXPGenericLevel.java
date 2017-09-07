package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigEXPGenericLevel {
	
	public int level;
	public double amount;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigEXPGenericLevel> levels) {
		levels.clear();
		List<Map<?, ?>> fishing_levels = cfg.getMapList("general_levels");
		for (Map<?, ?> level : fishing_levels) {
			ConfigEXPGenericLevel fishing_level = new ConfigEXPGenericLevel();
			fishing_level.level = (Integer)level.get("level");
			fishing_level.amount = (Integer)level.get("amount");
			levels.put(fishing_level.level, fishing_level);
		}
		CivLog.info("Loaded "+levels.size()+" Generic Experience Levels.");
	}
}
