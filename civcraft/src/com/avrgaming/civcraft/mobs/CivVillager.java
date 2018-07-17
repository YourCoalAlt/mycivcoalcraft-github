package com.avrgaming.civcraft.mobs;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;

public abstract class CivVillager implements Villager {
	
	private static void onSpawnDefault(Villager v, Location loc) {
		v.setAdult();
		v.setAI(false);
		v.setBreed(false);
		v.setSilent(true);
		v.setGravity(false);
		v.setCollidable(true);
		v.setInvulnerable(true);
		v.setCanPickupItems(false);
		
		Double maxHP = v.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		v.setHealth(maxHP);
	}
	
	public static void onSpawn(Villager v, Location loc, String name, boolean nameVisible, Profession profession) {
		onSpawnDefault(v, loc);
		v.setCustomName(name);
		v.setCustomNameVisible(nameVisible);
		v.setProfession(profession);
	}
	
	public void onSpawnWithTrades(Villager v, Location loc, String name, boolean nameVisible, Profession profession, Career career, List<MerchantRecipe> trades, int richesInt) {
		onSpawnDefault(v, loc);
		this.setCustomName(name);
		v.setCustomNameVisible(nameVisible);
		this.setProfession(profession);
		this.setCareer(career);
		this.setRecipes(trades);
		this.setRiches(richesInt);
	}
	
}
