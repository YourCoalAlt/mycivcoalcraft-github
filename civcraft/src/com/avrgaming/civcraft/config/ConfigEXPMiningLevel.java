package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigEXPMiningLevel {
	
	public int level;
	public int amount;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigEXPMiningLevel> levels) {
		levels.clear();
		List<Map<?, ?>> mining_levels = cfg.getMapList("mining_levels");
		for (Map<?, ?> level : mining_levels) {
			ConfigEXPMiningLevel mining_level = new ConfigEXPMiningLevel();
			mining_level.level = (Integer)level.get("level");
			mining_level.amount = (Integer)level.get("amount");
			levels.put(mining_level.level, mining_level);
		}
		CivLog.info("Loaded "+levels.size()+" Mining Levels.");
	}
}
