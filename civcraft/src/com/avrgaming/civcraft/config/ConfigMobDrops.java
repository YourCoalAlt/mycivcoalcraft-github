package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigMobDrops {
	
	public String id;
	public Integer exp_min;
	public Integer exp_max;
	public Double exp_mod;
	public String mod_type;
	public String drops;
	
	public ConfigMobDrops() {
	}
	
	public ConfigMobDrops(ConfigMobDrops lvl) {
		this.id = lvl.id;
		this.exp_min = lvl.exp_min;
		this.exp_max = lvl.exp_max;
		this.exp_mod = lvl.exp_mod;
		this.mod_type = lvl.mod_type;
		this.drops = lvl.drops;
	}
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigMobDrops> mobs) {
		mobs.clear();
		List<Map<?, ?>> mobs_list = cfg.getMapList("mobs");
		for (Map<?,?> cl : mobs_list) {
			ConfigMobDrops mob = new ConfigMobDrops();
			mob.id = (String)cl.get("id");
			mob.exp_min = (Integer)cl.get("exp_min");
			mob.exp_max = (Integer)cl.get("exp_max");
			mob.exp_mod = (Double)cl.get("exp_mod");
			mob.mod_type = (String)cl.get("mod_type");
			mob.drops = (String)cl.get("drops");
			mobs.put(mob.id, mob);
		}
		CivLog.info("Loaded "+mobs.size()+" Custom Mob Variables.");		
	}
}
