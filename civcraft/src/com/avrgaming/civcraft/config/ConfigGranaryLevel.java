package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigGranaryLevel {
	
	public Integer level;
	public Integer max_storage;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigGranaryLevel> levels) {
		levels.clear();
		List<Map<?, ?>> granary_levels = cfg.getMapList("granary_levels");
		for (Map<?, ?> level : granary_levels) {
			ConfigGranaryLevel granary_level = new ConfigGranaryLevel();
			granary_level.level = (Integer)level.get("level");
			granary_level.max_storage = (Integer)level.get("max_storage");
			levels.put(granary_level.level, granary_level);
		}
		CivLog.info("Loaded "+levels.size()+" Granary Levels.");
	}
}
