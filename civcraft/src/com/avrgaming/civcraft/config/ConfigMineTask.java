package com.avrgaming.civcraft.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigMineTask {
	
	public int task;
	public Map<ArrayList<String>, Integer> required;
	public int reward;
	public double quest_exp;
	
	public ConfigMineTask() {
	}
	
	public ConfigMineTask(ConfigMineTask currentlvl) {
		this.task = currentlvl.task;
		this.reward = currentlvl.reward;
		this.quest_exp = currentlvl.quest_exp;
		this.required = new HashMap<ArrayList<String>, Integer>();
		for (Entry<ArrayList<String>, Integer> entry : currentlvl.required.entrySet()) {
			this.required.put(entry.getKey(), entry.getValue());
		}
	}
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigMineTask> mine_tasks) {
		mine_tasks.clear();
		List<Map<?, ?>> mine_list = cfg.getMapList("mine_tasks");
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
			
			ConfigMineTask minetask = new ConfigMineTask();
			minetask.task = (Integer)cl.get("task");
			minetask.required = required_list;
			minetask.reward = (Integer)cl.get("reward");
			minetask.quest_exp = (Integer)cl.get("qexp");
			mine_tasks.put(minetask.task, minetask);
		}
		CivLog.info("Loaded "+mine_tasks.size()+" Mine Tasks.");		
	}
}
