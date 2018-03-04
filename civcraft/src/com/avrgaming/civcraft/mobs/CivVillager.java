package com.avrgaming.civcraft.mobs;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;

public abstract class CivVillager implements Villager {
	
	private void onSpawnDefault(Location loc, boolean nameVisible) {
		this.setAdult();
		this.setAI(false);
		this.setBreed(false);
		this.setSilent(true);
		this.setGravity(false);
		this.setCollidable(false);
		this.setInvulnerable(true);
		this.setCanPickupItems(false);
		this.setCustomNameVisible(nameVisible);
		
		Double maxHP = this.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		this.setHealth(maxHP);
	}
	
	public void onSpawn(Location loc, String name, boolean nameVisible, Profession profession) {
		this.onSpawnDefault(loc, nameVisible);
		this.setCustomName(name);
		this.setProfession(profession);
	}
	
	public void onSpawnWithTrades(Location loc, String name, boolean nameVisible, Profession profession, Career career, List<MerchantRecipe> trades, int richesInt) {
		this.onSpawnDefault(loc, nameVisible);
		this.setCustomName(name);
		this.setProfession(profession);
		this.setCareer(career);
		this.setRecipes(trades);
		this.setRiches(richesInt);
	}
	
}
