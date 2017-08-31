package com.avrgaming.civcraft.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigMineTask {
	
	public int task;
	public Map<Integer, Integer> required;
	public int reward;
	
	public ConfigMineTask() {
	}
	
	public ConfigMineTask(ConfigMineTask currentlvl) {
		this.task = currentlvl.task;
		this.reward = currentlvl.reward;
		this.required = new HashMap<Integer, Integer>();
		for (Entry<Integer, Integer> entry : currentlvl.required.entrySet()) {
			this.required.put(entry.getKey(), entry.getValue());
		}
	}
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigMineTask> mine_tasks) {
		mine_tasks.clear();
		List<Map<?, ?>> mine_list = cfg.getMapList("mine_tasks");
		Map<Integer, Integer> required_list = null;
		for (Map<?,?> cl : mine_list ) {
			List<?> required = (List<?>)cl.get("required");
			if (required != null) {
				required_list = new HashMap<Integer, Integer>();
				for (int i = 0; i < required.size(); i++) {
					String line = (String) required.get(i);
					String split[];
					split = line.split(",");
					required_list.put(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
				}
			}
			
			ConfigMineTask minetask = new ConfigMineTask();
			minetask.task = (Integer)cl.get("task");
			minetask.required = required_list;
			minetask.reward = (Integer)cl.get("reward");
			mine_tasks.put(minetask.task, minetask);
		}
		CivLog.info("Loaded "+mine_tasks.size()+" Mine Tasks.");		
	}
}
