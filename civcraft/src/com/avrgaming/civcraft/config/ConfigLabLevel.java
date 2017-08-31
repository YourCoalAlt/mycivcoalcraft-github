package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigLabLevel {
	public int level;
	public int amount;
	public int count;
	public double beakers;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigLabLevel> levels) {
		levels.clear();
		List<Map<?, ?>> lab_levels = cfg.getMapList("lab_levels");
		for (Map<?, ?> level : lab_levels) {
			ConfigLabLevel lab_level = new ConfigLabLevel();
			lab_level.level = (Integer)level.get("level");
			lab_level.amount = (Integer)level.get("amount");
			lab_level.beakers = (Double)level.get("beakers");
			lab_level.count = (Integer)level.get("count"); 
			levels.put(lab_level.level, lab_level);
		}
		CivLog.info("Loaded "+levels.size()+" lab Levels.");
	}
}
