package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigLumberMillItem {
	
	public Integer item;
	public int max_dura;
	public Integer level;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigLumberMillItem> tasks) {
		tasks.clear();
		List<Map<?, ?>> mill_tasks = cfg.getMapList("lumbermill_items");
		for (Map<?, ?> task : mill_tasks) {
			ConfigLumberMillItem mill_task = new ConfigLumberMillItem();
			mill_task.item = (Integer)task.get("item");
			mill_task.max_dura = (Integer)task.get("max_dura");
			mill_task.level = (Integer)task.get("level");
			tasks.put(mill_task.item, mill_task);
		}
		CivLog.info("Loaded "+tasks.size()+" Lumber Mill Items.");
	}
}
