package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigTrommelItem {
	
	public Integer item;
	public int item_data;
	public Integer level;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigTrommelItem> tasks) {
		tasks.clear();
		List<Map<?, ?>> trommel_tasks = cfg.getMapList("trommel_items");
		for (Map<?, ?> task : trommel_tasks) {
			ConfigTrommelItem trommel_task = new ConfigTrommelItem();
			trommel_task.item = (Integer)task.get("item");
			if ((Integer)task.get("item_data") != null) {
				trommel_task.item_data = (Integer)task.get("item_data");
			} else {
				trommel_task.item_data = 0;
			}
			trommel_task.level = (Integer)task.get("level");
			tasks.put(trommel_task.item, trommel_task);
		}
		CivLog.info("Loaded "+tasks.size()+" Trommel Items.");
	}
}
