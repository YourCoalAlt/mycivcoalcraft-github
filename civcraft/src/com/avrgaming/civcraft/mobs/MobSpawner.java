package com.avrgaming.civcraft.mobs;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigCustomMobs;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.util.CivColor;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityZombie;

public class MobSpawner {
	
	public static void despawnAllCustom() {
		for (Entity mob : CustomMobListener.customMobs.values()) {
			mob.getBukkitEntity().remove();
		}
	}
	
	public static void despawnAllHostile(Player p, boolean msg) {
		int countCustom = 0;
		int countTotal = 0;
		World w = Bukkit.getWorld(CivCraft.worldName);
		Difficulty d = w.getDifficulty();
		w.setDifficulty(Difficulty.PEACEFUL);
		
		for (Entity e : CustomMobListener.customMobs.values()) {
			CustomMobListener.customMobs.remove(e.getUniqueID());
			CustomMobListener.mobList.remove(e.getUniqueID());
			e.getBukkitEntity().remove();
			countCustom++;
			countTotal++;
		}
		
		for (Chunk c : w.getLoadedChunks()) {
			for (org.bukkit.entity.Entity e : c.getEntities()) {
				if (CivSettings.restrictedSpawns.containsKey(e.getType())) {
					e.remove();
					countTotal++;
				}
			}
		}
		
		for (CultureChunk cc : CivGlobal.getCultureChunks()) {
			if (cc.getChunkCoord().getChunk().isLoaded()) {
				MobSpawner.despawnAllHostileInChunk(null, cc.getChunkCoord().getChunk(), false);
			}
		}
		
		if (msg) {
			if (p != null) {
				CivMessage.sendSuccess(p, "Removed "+countCustom+ " custom mobs, grand total of "+countTotal+".");
			}
			CivLog.adminlog("CONSOLE", "Removed "+countCustom+ " custom mobs, grand total of "+countTotal+".");
		}
		w.setDifficulty(d);
	}
	
	public static void despawnAllHostileInChunk(Player p, Chunk c, boolean msg) {
		int countCustom = 0;
		int countTotal = 0;
		
		for (org.bukkit.entity.Entity e : c.getEntities()) {
			if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
				CustomMobListener.customMobs.remove(e.getUniqueId());
				CustomMobListener.mobList.remove(e.getUniqueId());
				e.remove();
				countCustom++;
				countTotal++;
				continue;
			}
			
			if (CivSettings.restrictedSpawns.containsKey(e.getType())) {
				e.remove();
				countTotal++;
			}
		}
		
		if (msg) {
			if (p != null) {
				CivMessage.sendSuccess(p, "Removed "+countCustom+ " custom mobs, grand total of "+countTotal+" at Chunk x"+c.getX()+" z"+c.getZ()+".");
			}
			CivLog.adminlog("CONSOLE", "Removed "+countCustom+ " custom mobs, grand total of "+countTotal+" at Chunk x"+c.getX()+" z"+c.getZ()+".");
		}
	}
	
	public static void despawnAllHostileAllChunks(Player p, boolean msg) {
		int countCustom = 0;
		int countTotal = 0;
		
		int x = (5000/16);
		int z = (5000/16);
		try {
			x = (CivSettings.getInteger(CivSettings.gameConfig, "world.radius_x")/16);
			z = (CivSettings.getInteger(CivSettings.gameConfig, "world.radius_z")/16);
		} catch (InvalidConfiguration e) {
			CivLog.error("-- Error on Reciving Setting --");
			CivLog.error("Could not load game.yml configs either world.radius_x AND/OR world.radius_z when trying to despawn mobs!");
			e.printStackTrace();
		}
		
		for (int sx = -x; sx < x; sx++) {
			for (int sz = -z; sz < z; sz++) {
				if (Bukkit.getWorld(CivCraft.worldName).getChunkAt(sx, sz) != null) {
					Chunk c = Bukkit.getWorld(CivCraft.worldName).getChunkAt(sx, sz);
					for (org.bukkit.entity.Entity e : c.getEntities()) {
						if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
							CustomMobListener.customMobs.remove(e.getUniqueId());
							CustomMobListener.mobList.remove(e.getUniqueId());
							e.remove();
							countCustom++;
							countTotal++;
							continue;
						}
						
						if (CivSettings.restrictedSpawns.containsKey(e.getType())) {
							e.remove();
							countTotal++;
						}
					}
				}
			}
		}
		
		if (msg) {
			if (p != null) {
				CivMessage.sendSuccess(p, "Removed "+countCustom+ " custom mobs, grand total of "+countTotal+" at World Removal.");
			}
			CivLog.adminlog("CONSOLE", "Removed "+countCustom+ " custom mobs, grand total of "+countTotal+" at World Removal.");
		}
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
		
		if (validMobs.size() < 1) return;
		
		Random random = new Random();
		int idx = random.nextInt(validMobs.size());
		spawnCustomMob(validMobs.get(idx), loc);
	}
	
}
