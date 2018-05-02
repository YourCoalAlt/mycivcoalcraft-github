package com.avrgaming.civcraft.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.avrgaming.civcraft.util.ItemManager;

public class ConfigRemovedRecipes {
	
	public int type_id;
	public int data;
	
	public static void removeRecipes(FileConfiguration cfg, HashMap<Integer, ConfigRemovedRecipes> removedRecipies) {
		List<Map<?, ?>> configMaterials = cfg.getMapList("removed_recipes");
		for (Map<?, ?> b : configMaterials) {
			ConfigRemovedRecipes item = new ConfigRemovedRecipes();
			item.type_id = (Integer)b.get("type_id");
			item.data = (Integer)b.get("data");
			removedRecipies.put(item.type_id, item);
			
			ItemStack is = new ItemStack(ItemManager.getMaterial(item.type_id), 1, (short)item.data);
			List<Recipe> backup = new ArrayList<Recipe>();
			Iterator<Recipe> a = Bukkit.getServer().recipeIterator();
			while(a.hasNext()) {
				Recipe recipe = a.next();
				ItemStack result = recipe.getResult();
				if (!result.isSimilar(is)) {
					backup.add(recipe);
				}
				
				if (recipe instanceof FurnaceRecipe) {
					FurnaceRecipe fr = (FurnaceRecipe) recipe;
					if (fr.getInput().getType() == Material.IRON_ORE) {
						if (backup.contains(fr)) {
							backup.remove(fr);
						}
						continue;
					}
				}
			}
			
			 Bukkit.getServer().clearRecipes();
			 for (Recipe r : backup) {
				 Bukkit.getServer().addRecipe(r);
			 }
		}
	}
}
