package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigEXPQuestLevel {
	
	public int level;
	public int amount;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigEXPQuestLevel> levels) {
		levels.clear();
		List<Map<?, ?>> quest_levels = cfg.getMapList("quest_levels");
		for (Map<?, ?> level : quest_levels) {
			ConfigEXPQuestLevel quest_level = new ConfigEXPQuestLevel();
			quest_level.level = (Integer)level.get("level");
			quest_level.amount = (Integer)level.get("amount");
			levels.put(quest_level.level, quest_level);
		}
		CivLog.info("Loaded "+levels.size()+" Quest Levels.");
	}
}
