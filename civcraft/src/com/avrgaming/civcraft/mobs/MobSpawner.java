package com.avrgaming.civcraft.mobs;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigCustomMobs;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityZombie;

public class MobSpawner {
	
	public static void despawnAllCustom() {
		for (Entity mob : CustomMobListener.customMobs.values()) {
			mob.getBukkitEntity().remove();
		}
	}
	
	public static void despawnAllHostile(Player p) {
		int countCustom = 0;
		int countTotal = 0;
		
		for (Entity e : CustomMobListener.customMobs.values()) {
			CustomMobListener.customMobs.remove(e.getUniqueID());
			CustomMobListener.mobList.remove(e.getUniqueID());
			countCustom++;
			countTotal++;
			e.getBukkitEntity().remove();
		}
		
		for (Chunk c : Bukkit.getWorld(CivCraft.worldName).getLoadedChunks()) {
			for (org.bukkit.entity.Entity e : c.getEntities()) {
				if (CustomMobListener.customMobs.get(e.getUniqueId()) != null) {
					CustomMobListener.customMobs.remove(e.getUniqueId());
					CustomMobListener.mobList.remove(e.getUniqueId());
					countCustom++;
					countTotal++;
					e.remove();
				} else if (e.getType() == EntityType.ZOMBIE || e.getType() == EntityType.ZOMBIE_VILLAGER || e.getType() == EntityType.PIG_ZOMBIE ||
						e.getType() == EntityType.SKELETON || e.getType() == EntityType.CREEPER || e.getType() == EntityType.SPIDER) {
					countTotal++;
					e.remove();
				}
			}
		}
		
		if (p != null) {
			CivMessage.sendSuccess(p, "Removed "+countCustom+ " custom mobs, grand total of "+countTotal+".");
		}
		CivLog.adminlog("CONSOLE", "Removed "+countCustom+ " custom mobs, grand total of "+countTotal+".");
	}
	
	public static void spawnCustomMob(ConfigCustomMobs cmob, Location loc) {
		CraftWorld world = (CraftWorld) loc.getWorld(); 
		world.loadChunk(loc.getChunk());
		
		@SuppressWarnings("deprecation")
		Entity ent = (Entity) world.createEntity(loc, EntityType.fromName(cmob.entity).getEntityClass());
		cmob.setMaxHealth(ent.getBukkitEntity(), cmob.max_health);
		cmob.modifySpeed(ent.getBukkitEntity(), cmob.move_speed);
		cmob.setAttack(ent.getBukkitEntity(), cmob.attack_dmg);
		cmob.setFollowRange(ent.getBukkitEntity(), cmob.follow_range);
		cmob.setKnockbackResistance(ent.getBukkitEntity(), cmob.kb_resistance);
		ent.setCustomName(CivColor.colorize(cmob.name));
		ent.setCustomNameVisible(cmob.visible);
		
		if (cmob.isBaby) {
			if (ent.getBukkitEntity().getType() == EntityType.ZOMBIE) {
				EntityZombie z = (EntityZombie) ent;
				z.setBaby(true);
			} else if (ent.getBukkitEntity().getType() == EntityType.PIG_ZOMBIE) {
				EntityZombie z = (EntityZombie) ent;
				z.setBaby(true);
			} else {
				CivLog.warning("Mob "+cmob.id+" cannot spawn baby");
			}
		}
		
		world.addEntity((Entity) ent, SpawnReason.CUSTOM);
		CustomMobListener.customMobs.put(ent.getUniqueID(), ent);
		CustomMobListener.mobList.put(ent.getUniqueID(), cmob);
	}
	
	public static void spawnRandomCustomMob(Location loc) {
		ArrayList<ConfigCustomMobs> validMobs = new ArrayList<ConfigCustomMobs>();
		for (ConfigCustomMobs cmob : CivSettings.customMobs.values()) {
			for (String s : cmob.biomes) {
				if (s.toUpperCase().equals(loc.getBlock().getBiome().toString().toUpperCase())) {
					validMobs.add(cmob);
				}
			}
		}
		
		Random random = new Random();
		int idx = random.nextInt(validMobs.size());
		spawnCustomMob(validMobs.get(idx), loc);
	}
	
}
