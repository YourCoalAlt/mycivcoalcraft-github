package com.avrgaming.civcraft.mobs;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigCustomMobs;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;

public class MobSpawner {
	
	public static void despawnAll() {
		for (CustomMobListener mob : CustomMobListener.customMobs.values()) {
			mob.entity.getBukkitEntity().remove();
		}
	}
	
	public static void spawnCustomMob(ConfigCustomMobs cmob, Location loc) {
		CivMessage.global("MST: custom mob spawned: "+cmob.id+" at "+loc.getBlockX()+","+loc.getBlockZ());
		CraftWorld world = (CraftWorld) loc.getWorld(); 
		world.loadChunk(loc.getChunk());
		
		@SuppressWarnings("deprecation")
		LivingEntity ent = (LivingEntity) world.spawnEntity(loc, EntityType.fromName(cmob.entity));
		cmob.setMaxHealth(ent, cmob.max_health);
		cmob.modifySpeed(ent, cmob.move_speed);
		cmob.setAttack(ent, cmob.attack_dmg);
		cmob.setFollowRange(ent, cmob.follow_range);
		cmob.setKnockbackResistance(ent, cmob.kb_resistance);
		ent.setCustomName(CivColor.colorize(cmob.name));
		ent.setCustomNameVisible(cmob.visible);
		
		CustomMobListener.customMobs.put(ent.getUniqueId(), value);
	}
	
	public static void spawnRandomCustomMobNew(Location loc) {
		ArrayList<ConfigCustomMobs> validMobs = new ArrayList<ConfigCustomMobs>();
		for (ConfigCustomMobs cmob : CivSettings.customMobs.values()) {
			for (String s : cmob.biomes) {
				if (s.toUpperCase().equals(loc.getBlock().getBiome().toString().toUpperCase())) {
					validMobs.add(cmob);
				}
			}
		}
		
		if (validMobs.size() < 1) return;
		for (ConfigCustomMobs s : validMobs) {
			if (CustomMobListener.disabledMobs.contains(s)) {
				validMobs.remove(s);
			}
		}
		
		Random random = new Random();
		int idx = random.nextInt(validMobs.size());
		spawnCustomMob(validMobs.get(idx), loc);
	}
}
