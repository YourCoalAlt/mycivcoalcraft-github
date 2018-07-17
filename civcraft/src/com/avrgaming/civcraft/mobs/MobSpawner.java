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
import com.avrgaming.civcraft.config.ConfigMobsCustom;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.CultureChunk;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.util.CivColor;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityZombie;
import net.minecraft.server.v1_12_R1.EnumItemSlot;

public class MobSpawner {
	
	public static void despawnAllCustom() {
		for (Entity mob : CustomMobListener.customMobs.values()) {
			mob.getBukkitEntity().remove();
		}
	}
	
	public static void despawnMobs(Player p, boolean custom, boolean hostile, boolean townBorders, boolean civBorders, boolean onlinePlayersOnly, boolean loaded, boolean msg) {
		int countCustom = 0;
		int countTotal = 0;
		World w = Bukkit.getWorld(CivCraft.worldName);
		Difficulty d = w.getDifficulty();
		w.setDifficulty(Difficulty.PEACEFUL);
		
		if (custom) {
			for (Entity e : CustomMobListener.customMobs.values()) {
				CustomMobListener.customMobs.remove(e.getUniqueID());
				CustomMobListener.mobList.remove(e.getUniqueID());
				e.getBukkitEntity().remove();
				countCustom++;
				countTotal++;
			}
		}
		
		if (hostile) {
			if (townBorders) {
				if (onlinePlayersOnly) {
					ArrayList<Town> townsToClear = new ArrayList<Town>();
					for (Player onp : Bukkit.getOnlinePlayers()) {
						Resident res = CivGlobal.getResident(onp);
						if (res.hasTown() && !townsToClear.contains(res.getTown())) townsToClear.add(res.getTown());
					}
					
					for (Town t : townsToClear) {
						for (TownChunk tc : t.getTownChunks()) {
							Chunk c = tc.getChunkCoord().getChunk();
							if (c.getEntities().length < 1) continue;
							for (org.bukkit.entity.Entity e : c.getEntities()) {
								boolean r = false;
								if (CivSettings.restrictedSpawns.contains(e.getType()) || CivSettings.vanillaHostileMobs.contains(e.getType())) {
									e.remove();
									countTotal++;
									r = true;
								}
								
								if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
									CustomMobListener.customMobs.remove(e.getUniqueId());
									CustomMobListener.mobList.remove(e.getUniqueId());
									countCustom++;
									if (!r) countTotal++;
								}
							}
						}
					}
				} else {
					for (TownChunk tc : CivGlobal.getTownChunks()) {
						Chunk c = tc.getChunkCoord().getChunk();
						if (c.getEntities().length < 1) continue;
						for (org.bukkit.entity.Entity e : c.getEntities()) {
							boolean r = false;
							if (CivSettings.restrictedSpawns.contains(e.getType()) || CivSettings.vanillaHostileMobs.contains(e.getType())) {
								e.remove();
								countTotal++;
								r = true;
							}
							
							if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
								CustomMobListener.customMobs.remove(e.getUniqueId());
								CustomMobListener.mobList.remove(e.getUniqueId());
								countCustom++;
								if (!r) countTotal++;
							}
						}
					}
				}
				
				if (civBorders) {
					if (onlinePlayersOnly) {
						ArrayList<Town> civsToClear = new ArrayList<Town>();
						for (Player onp : Bukkit.getOnlinePlayers()) {
							Resident res = CivGlobal.getResident(onp);
							if (res.hasTown() && !civsToClear.contains(res.getTown())) civsToClear.add(res.getTown());
						}
						
						for (Town t : civsToClear) {
							for (CultureChunk cc : t.getCultureChunks()) {
								Chunk c = cc.getChunkCoord().getChunk();
								if (c.getEntities().length < 1) continue;
								for (org.bukkit.entity.Entity e : c.getEntities()) {
									boolean r = false;
									if (CivSettings.restrictedSpawns.contains(e.getType()) || CivSettings.vanillaHostileMobs.contains(e.getType())) {
										e.remove();
										countTotal++;
										r = true;
									}
									
									if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
										CustomMobListener.customMobs.remove(e.getUniqueId());
										CustomMobListener.mobList.remove(e.getUniqueId());
										countCustom++;
										if (!r) countTotal++;
									}
								}
							}
						}
					} else {
						for (CultureChunk cc : CivGlobal.getCultureChunks()) {
							Chunk c = cc.getChunkCoord().getChunk();
							if (c.getEntities().length < 1) continue;
							for (org.bukkit.entity.Entity e : c.getEntities()) {
								boolean r = false;
								if (CivSettings.restrictedSpawns.contains(e.getType()) || CivSettings.vanillaHostileMobs.contains(e.getType())) {
									e.remove();
									countTotal++;
									r = true;
								}
								
								if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
									CustomMobListener.customMobs.remove(e.getUniqueId());
									CustomMobListener.mobList.remove(e.getUniqueId());
									countCustom++;
									if (!r) countTotal++;
								}
							}
						}
					}
				}
			}
			
			if (loaded) {
				for (Chunk c : w.getLoadedChunks()) {
					if (c.getEntities().length < 1) continue;
					for (org.bukkit.entity.Entity e : c.getEntities()) {
						if (CustomMobListener.customMobs.containsKey(e.getUniqueId())) {
							CustomMobListener.customMobs.remove(e.getUniqueId());
							CustomMobListener.mobList.remove(e.getUniqueId());
							e.remove();
							countCustom++;
							countTotal++;
							continue;
						}
						
						if (CivSettings.restrictedSpawns.contains(e.getType()) || CivSettings.vanillaHostileMobs.contains(e.getType())) {
							e.remove();
							countTotal++;
						}
					}
				}
			}
			
/*			if (world) {
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
							if (c.getEntities().length < 1) continue;
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
			}*/
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
			
			if (CivSettings.restrictedSpawns.contains(e.getType()) || CivSettings.vanillaHostileMobs.contains(e.getType())) {
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
	
	@SuppressWarnings("deprecation")
	public static void spawnCustomMob(ConfigMobsCustom cmob, Location loc) {
		CraftWorld world = (CraftWorld) loc.getWorld(); 
		world.loadChunk(loc.getChunk());
		
		Entity ent = (Entity) world.createEntity(loc, EntityType.fromName(cmob.entity).getEntityClass());
		cmob.setMaxHealth(ent.getBukkitEntity(), cmob.max_health);
		cmob.modifySpeed(ent.getBukkitEntity(), cmob.move_speed);
		cmob.setAttack(ent.getBukkitEntity(), cmob.attack_dmg);
		cmob.setFollowRange(ent.getBukkitEntity(), cmob.follow_range);
		cmob.setKnockbackResistance(ent.getBukkitEntity(), cmob.kb_resistance);
		if (cmob.name != null && cmob.name != "") ent.setCustomName(CivColor.colorize(cmob.name));
		ent.setCustomNameVisible(cmob.visible);
		
		ent.setEquipment(EnumItemSlot.HEAD, null);
		ent.setEquipment(EnumItemSlot.CHEST, null);
		ent.setEquipment(EnumItemSlot.LEGS, null);
		ent.setEquipment(EnumItemSlot.FEET, null);
		ent.setEquipment(EnumItemSlot.MAINHAND, null);
		ent.setEquipment(EnumItemSlot.OFFHAND, null);
		
		if (ent.getBukkitEntity().getType() == EntityType.ZOMBIE) {
			EntityZombie z = (EntityZombie) ent;
			z.setBaby(cmob.isBaby);
		} else if (ent.getBukkitEntity().getType() == EntityType.PIG_ZOMBIE) {
			EntityZombie z = (EntityZombie) ent;
			z.setBaby(cmob.isBaby);
		} else {
			CivLog.warning("Custom Mob "+cmob.id+" cannot spawn baby");
		}
		
		world.addEntity((Entity) ent, SpawnReason.CUSTOM);
		CustomMobListener.customMobs.put(ent.getUniqueID(), ent);
		CustomMobListener.mobList.put(ent.getUniqueID(), cmob);
	}
	
	public static void spawnRandomCustomMob(Location loc) {
		ArrayList<ConfigMobsCustom> validMobs = new ArrayList<ConfigMobsCustom>();
		for (ConfigMobsCustom cmob : CivSettings.customMobs.values()) {
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
