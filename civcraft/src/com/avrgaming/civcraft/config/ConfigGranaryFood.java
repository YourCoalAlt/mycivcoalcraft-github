package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigGranaryFood {
	public int food;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigGranaryFood> tasks) {
		tasks.clear();
		List<Map<?, ?>> granary_tasks = cfg.getMapList("granary_food");
		for (Map<?, ?> task : granary_tasks) {
			ConfigGranaryFood granary_task = new ConfigGranaryFood();
			granary_task.food = (Integer)task.get("food");
			tasks.put(granary_task.food, granary_task);
		}
		CivLog.info("Loaded "+tasks.size()+" Granary Foods.");
	}
}
