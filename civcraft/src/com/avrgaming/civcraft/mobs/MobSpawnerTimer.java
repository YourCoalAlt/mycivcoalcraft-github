package com.avrgaming.civcraft.mobs;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.TownChunk;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.EntityProximity;
import com.avrgaming.civcraft.util.ItemManager;

import net.minecraft.server.v1_12_R1.EntityCreature;

public class MobSpawnerTimer implements Runnable {
	
//	public static int UPDATE_LIMIT = 40;
	public static int MOB_AREA_LIMIT = 10;
	public static int MOB_AREA = 48;
	
	public static int MIN_SPAWN_DISTANCE = 12;
	public static int MAX_SPAWN_DISTANCE = 40;
	public static int MIN_SPAWN_AMOUNT = 5;
	
	public static int Y_SHIFT = 4;
	
	public static Queue<String> playerQueue = new LinkedList<String>();
	
	@Override
	public void run() {
		String name = null;
//		for (int i = 0; i < UPDATE_LIMIT; i++) {
		for (int i = 0; i < (Bukkit.getOnlinePlayers().size()*2); i++) {
			// Find a player who is out in the wilderness.
			try {
				name = playerQueue.poll();
				// Queue empty, return.
				if (name == null) return;
				
				Player player = CivGlobal.getPlayer(name);
				World world = player.getWorld();
				if (!world.getAllowMonsters()) continue;
								
				for (int j = 0; j < MIN_SPAWN_AMOUNT; j++) {
					Random random = new Random();
					int x = random.nextInt(MAX_SPAWN_DISTANCE)+MIN_SPAWN_DISTANCE;
					if (x > MAX_SPAWN_DISTANCE) x = MAX_SPAWN_DISTANCE;
					if (random.nextBoolean()) x *= -1;
					
					int z = random.nextInt(MAX_SPAWN_DISTANCE)+MIN_SPAWN_DISTANCE;
					if (z > MAX_SPAWN_DISTANCE) z = MAX_SPAWN_DISTANCE;
					if (random.nextBoolean()) z *= -1;
					
					int y = world.getHighestBlockYAt(((Double) player.getLocation().getX()).intValue() + x, ((Double) player.getLocation().getZ()).intValue() + z);
				    Location loc = new Location(world, player.getLocation().getX() + x, y+Y_SHIFT, player.getLocation().getZ() + z);
					if (!loc.getChunk().isLoaded()) continue;
					
					LinkedList<Entity> entities = EntityProximity.getNearbyEntities(null, loc, MOB_AREA, EntityCreature.class);
					// Dont spawn if we've reach the mob limit
					if (entities.size() > MOB_AREA_LIMIT) continue;
					
					TownChunk tc = CivGlobal.getTownChunk(new ChunkCoord(loc));
					if (tc != null && !tc.perms.isMobs()) continue;
					
					// Dont spawn mobs at invalid blocks
					Location blockLoc = loc; blockLoc.setY(loc.getY()-Y_SHIFT);
					if ((ItemManager.getId(blockLoc.getBlock().getRelative(BlockFace.DOWN)) == CivData.WATER_STILL) ||
					    (ItemManager.getId(blockLoc.getBlock().getRelative(BlockFace.DOWN)) == CivData.WATER_RUNNING) ||
						(ItemManager.getId(blockLoc.getBlock().getRelative(BlockFace.DOWN)) == CivData.LAVA_STILL) ||
						(ItemManager.getId(blockLoc.getBlock().getRelative(BlockFace.DOWN)) == CivData.LAVA_RUNNING)) {
						continue;
					}
					
					MobSpawner.spawnRandomCustomMob(loc);
				}
				break;
			} catch (CivException e) {
				playerQueue.remove(name);
				// player is offline, don't re-add to queue
			} finally {
				if (name != null) {
					// Re-add to end of queue to keep respawning
					playerQueue.remove(name);
					playerQueue.add(name);
				}
			}
		}
	}
}
