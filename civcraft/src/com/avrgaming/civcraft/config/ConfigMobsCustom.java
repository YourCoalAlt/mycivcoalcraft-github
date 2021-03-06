package com.avrgaming.civcraft.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Damageable;

import com.avrgaming.civcraft.main.CivLog;

public class ConfigMobsCustom {
	
	public String id;
	public String name;
	public Boolean visible;
	public String entity;
	public Double max_health;
	public Double move_speed;
	public Double attack_dmg;
	public Double defense_dmg;
	public Double follow_range;
	public Double kb_resistance;
	public Integer exp_min;
	public Integer exp_max;
	public Double res_exp;
	public Double rxp_mod;
	public String mod_type;
	public ArrayList<String> biomes = new ArrayList<String>();
	public ArrayList<String> drops = new ArrayList<String>();
	
	public boolean isBaby = false;
	
	// TOBO Make this check for valid biomes of mobs when spawning
/*	public boolean isAvailable(Town town) {
		if (town.hasTechnology(require_tech)) {
			if (town.hasUpgrade(require_upgrade)) {
				if (town.hasStructure(require_structure)) {
					if (limit == 0 || town.getStructureTypeCount(id) < limit) {					
						boolean capitol = town.isCapitol();
						if (id.equals("s_townhall") && capitol) return false;
						if (id.equals("s_capitol") && !capitol) return false;
						return true;
					}
				}
			}
		}
		return false;
	}*/
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigMobsCustom> mobMap) {
		mobMap.clear();
		List<Map<?, ?>> custom_mobs = cfg.getMapList("custom_mobs");
		for (Map<?, ?> cl : custom_mobs) {
			ConfigMobsCustom minfo = new ConfigMobsCustom();
			minfo.id = (String)cl.get("id");
			minfo.name = (String)cl.get("name");
			minfo.visible = (Boolean)cl.get("visible");
			minfo.entity = (String)cl.get("entity");
			minfo.max_health = (Double)cl.get("max_health");
			minfo.move_speed = (Double)cl.get("move_speed");
			minfo.attack_dmg = (Double)cl.get("attack_dmg");
			minfo.defense_dmg = (Double)cl.get("defense_dmg");
			minfo.follow_range = (Double)cl.get("follow_range");
			minfo.kb_resistance = (Double)cl.get("kb_resistance");
			minfo.exp_min = (Integer)cl.get("exp_min");
			minfo.exp_max = (Integer)cl.get("exp_max");
			minfo.res_exp = (Double)cl.get("res_exp");
			minfo.rxp_mod = (Double)cl.get("rxp_mod");
			minfo.mod_type = (String)cl.get("mod_type");
			
			
			@SuppressWarnings("unchecked")
			ArrayList<String> biomes = (ArrayList<String>) cl.get("biomes");
			if (biomes != null) {
				for (String compObj : biomes) {
					minfo.biomes.add(compObj);	
				}
			}
			
			@SuppressWarnings("unchecked")
			ArrayList<String> drops = (ArrayList<String>) cl.get("drops");
			if (drops != null) {
				for (String compObj : drops) {
					minfo.drops.add(compObj);	
				}
			}
	
			mobMap.put(minfo.id, minfo);
		}
		CivLog.info("Loaded "+mobMap.size()+" Custom Mobs.");
	}
	
	public void setMaxHealth(CraftEntity ent, double health) {
		((Attributable) ent).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
		((Damageable) ent).setHealth(health);
	}
	
	public void modifySpeed(CraftEntity ent, double percent) {
		double speed = (((Attributable) ent).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue()) * percent;
		((Attributable) ent).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
	}
	
	public void setAttack(CraftEntity ent, double attack) {
		((Attributable) ent).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(attack);
	}
	
	public void setFollowRange(CraftEntity ent, double range) {
		((Attributable) ent).getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(range);
	}
	
	public void setKnockbackResistance(CraftEntity ent, double resist) {
		((Attributable) ent).getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(resist);
	}
	
}
