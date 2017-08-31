/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigGranaryTask {
	public int task; // The current task selected by the player
	public int required; // Gets the required bread for the task
	public double reward; // The amount of culture gained to the town on the task completion
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigGranaryTask> tasks) {
		tasks.clear();
		List<Map<?, ?>> granary_tasks = cfg.getMapList("granary_tasks");
		for (Map<?, ?> task : granary_tasks) {
			ConfigGranaryTask granary_task = new ConfigGranaryTask();
			granary_task.task = (Integer)task.get("task");
			granary_task.required = (Integer)task.get("required");
			granary_task.reward = (Double)task.get("reward");
			tasks.put(granary_task.task, granary_task);
		}
		CivLog.info("Loaded "+tasks.size()+" Granary Tasks.");
	}
}
