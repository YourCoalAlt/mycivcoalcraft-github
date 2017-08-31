package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigQuarryItem {
	
	public Integer item;
	public int max_dura;
	public Integer level;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigQuarryItem> tasks) {
		tasks.clear();
		List<Map<?, ?>> quarry_tasks = cfg.getMapList("quarry_items");
		for (Map<?, ?> task : quarry_tasks) {
			ConfigQuarryItem quarry_task = new ConfigQuarryItem();
			quarry_task.item = (Integer)task.get("item");
			quarry_task.max_dura = (Integer)task.get("max_dura");
			quarry_task.level = (Integer)task.get("level");
			tasks.put(quarry_task.item, quarry_task);
		}
		CivLog.info("Loaded "+tasks.size()+" Quarry Items.");
	}
}
