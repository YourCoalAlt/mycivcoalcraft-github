package com.avrgaming.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigMobs {
	
	public String id;
	public String name;
	public Boolean name_visible;
	public Double max_health;
	public Double move_speed;
	public Double attack_dmg;
	public Double follow_range;
	public Double kb_resistance;
	public Integer exp_min;
	public Integer exp_max;
	public Double res_exp;
	public Double exp_mod;
	public String mod_type;
	public String drops;
	
	public ConfigMobs() {
	}
	
	public ConfigMobs(ConfigMobs lvl) {
		this.id = lvl.id;
		this.name = lvl.name;
		this.name_visible = lvl.name_visible;
		this.max_health = lvl.max_health;
		this.move_speed = lvl.move_speed;
		this.attack_dmg = lvl.attack_dmg;
		this.follow_range = lvl.follow_range;
		this.kb_resistance = lvl.kb_resistance;
		this.exp_min = lvl.exp_min;
		this.exp_max = lvl.exp_max;
		this.res_exp = lvl.res_exp;
		this.exp_mod = lvl.exp_mod;
		this.mod_type = lvl.mod_type;
		this.drops = lvl.drops;
	}
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigMobs> mobs) {
		mobs.clear();
		List<Map<?, ?>> mobs_list = cfg.getMapList("mobs");
		for (Map<?,?> cl : mobs_list) {
			ConfigMobs mob = new ConfigMobs();
			mob.id = (String)cl.get("id");
			mob.name = (String)cl.get("name");
			mob.name_visible = (Boolean)cl.get("name_visible");
			mob.max_health = (Double)cl.get("max_health");
			mob.move_speed = (Double)cl.get("move_speed");
			mob.attack_dmg = (Double)cl.get("attack_dmg");
			mob.follow_range = (Double)cl.get("follow_range");
			mob.kb_resistance = (Double)cl.get("kb_resistance");
			mob.exp_min = (Integer)cl.get("exp_min");
			mob.exp_max = (Integer)cl.get("exp_max");
			mob.res_exp = (Double)cl.get("res_exp");
			mob.exp_mod = (Double)cl.get("exp_mod");
			mob.mod_type = (String)cl.get("mod_type");
			mob.drops = (String)cl.get("drops");
			mobs.put(mob.id, mob);
		}
		CivLog.info("Loaded "+mobs.size()+" Custom Mobs.");		
	}
	
	public void setMaxHealth(LivingEntity ent, double health) {
		ent.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
		ent.setHealth(health);
	}
	
	public void modifySpeed(LivingEntity ent, double percent) {
		double speed = (ent.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue()) * percent;
		ent.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
	}
	
	public void setAttack(LivingEntity ent, double attack) {
		ent.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(attack);
	}
	
	public void setFollowRange(LivingEntity ent, double range) {
		ent.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(range);
	}
	
	public void setKnockbackResistance(LivingEntity ent, double resist) {
		ent.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(resist);
	}
}
