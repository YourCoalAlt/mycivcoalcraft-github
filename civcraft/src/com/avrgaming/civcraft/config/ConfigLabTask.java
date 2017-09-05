package com.avrgaming.civcraft.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigLabTask {
	
	public int task;
	public Map<ArrayList<String>, Integer> required;
	public int reward;
	
	public ConfigLabTask() {
	}
	
	public ConfigLabTask(ConfigLabTask currentlvl) {
		this.task = currentlvl.task;
		this.reward = currentlvl.reward;
		this.required = new HashMap<ArrayList<String>, Integer>();
		for (Entry<ArrayList<String>, Integer> entry : currentlvl.required.entrySet()) {
			this.required.put(entry.getKey(), entry.getValue());
		}
	}
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigLabTask> mine_tasks) {
		mine_tasks.clear();
		List<Map<?, ?>> mine_list = cfg.getMapList("lab_tasks");
		Map<ArrayList<String>, Integer> required_list = null;
		for (Map<?,?> cl : mine_list) {
			List<?> required = (List<?>)cl.get("required");
			if (required != null) {
				required_list = new HashMap<ArrayList<String>, Integer>();
				for (int i = 0; i < required.size(); i++) {
					String line = (String) required.get(i);
					String split[];
					split = line.split(",");
					ArrayList<String> s = new ArrayList<String>();
					s.add(split[0]);
					required_list.put(s, Integer.valueOf(split[1]));
				}
			}
			
			ConfigLabTask minetask = new ConfigLabTask();
			minetask.task = (Integer)cl.get("task");
			minetask.required = required_list;
			minetask.reward = (Integer)cl.get("reward");
			mine_tasks.put(minetask.task, minetask);
		}
		CivLog.info("Loaded "+mine_tasks.size()+" Lab Tasks.");		
	}
}
