package com.avrgaming.civcraft.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

public class ConfigMaterialPotions {

	/* Required */
	public String id;
	public String name;
	public String required_techs;
	public Color color;
	public HashMap<String, ConfigMaterialPotionEffect> effects;
	public HashMap<String, ConfigMaterialPotionIngredient> ingredients;
	
	@SuppressWarnings("unchecked")
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigMaterialPotions> potions) {
		potions.clear();
		List<Map<?, ?>> configMaterials = cfg.getMapList("potions");
		for (Map<?, ?> c : configMaterials) {
			ConfigMaterialPotions pot = new ConfigMaterialPotions();
			
			pot.id = (String)c.get("id");
			pot.name = (String)c.get("name");
			pot.name = CivColor.colorize(pot.name);
			
			String required_techs = (String)c.get("required_techs");
			if (required_techs != null) {
				pot.required_techs = required_techs;
			}
			
			int red = (Integer)c.get("color.r");
			int green = (Integer)c.get("color.g");
			int blue = (Integer)c.get("color.b");
			pot.color = Color.fromRGB(red, green, blue);
			
			List<Map<?, ?>> effects = (List<Map<?,?>>)c.get("effects");
			if (effects != null) {
				pot.effects = new HashMap<String, ConfigMaterialPotionEffect>();
				for (Map<?, ?> efct : effects) {
					ConfigMaterialPotionEffect effect = new ConfigMaterialPotionEffect();
					effect.effect = PotionEffectType.getByName((String)efct.get("effect"));
					effect.time = (Integer)efct.get("time");
					if ((Integer)efct.get("amplifier") != null) {
						effect.amplifier = (Integer)efct.get("amplifier");
					}
					String key = effect.effect.toString();
					pot.effects.put(key, effect);
				}
			}
			
			List<Map<?, ?>> ingredients = (List<Map<?,?>>)c.get("ingredients");
			if (ingredients != null) {
				pot.ingredients = new HashMap<String, ConfigMaterialPotionIngredient>();
				for (Map<?, ?> ingred : ingredients) {
					ConfigMaterialPotionIngredient ingredient = new ConfigMaterialPotionIngredient();
					String key;
					ingredient.type = Material.valueOf((String)ingred.get("type"));
					String custom_id = (String)ingred.get("custom_id");
					if (custom_id != null) {
						ingredient.custom_id = custom_id;
						key = custom_id;
					} else {
						ingredient.custom_id = null;
						key = "mc_"+ingredient.type.toString();
					}
					
					Integer data = (Integer)ingred.get("data");
					if (data != null) {
						ingredient.data = data;
					}
					
					Integer count = (Integer)ingred.get("count");
					if (count != null) {
						ingredient.count = count;
					}
					
					pot.ingredients.put(key, ingredient);
				}
			}
			
			potions.put(pot.id, pot);
		}
		
		CivLog.info("Loaded "+potions.size()+" Potions.");
	}	
	
	public boolean playerHasTechnology(Player player) {
		if (this.required_techs == null) return true;
		
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) return false;
		
		String[] split = this.required_techs.split(",");
		for (String tech : split) {
			tech = tech.replace(" ", "");
			if (!resident.getCiv().hasTechnology(tech)) return false;
		}
		
		return true;	
	}
	
	public String getRequireString() {
		String out = "";
		if (this.required_techs == null) return out;
		
		String[] split = this.required_techs.split(",");
		for (String tech : split) {
			tech = tech.replace(" ", "");
			ConfigTech technology = CivSettings.techs.get(tech);
			if (technology != null) {
				out += technology.name+", ";
			}
		}
		return out;
	}
	
}
