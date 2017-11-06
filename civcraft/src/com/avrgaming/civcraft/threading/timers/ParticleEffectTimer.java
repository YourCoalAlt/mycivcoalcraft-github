package com.avrgaming.civcraft.threading.timers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;

import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.StructureTables;
import com.avrgaming.civcraft.structure.Barracks;
import com.avrgaming.civcraft.structure.Granary;
import com.avrgaming.civcraft.structure.Mine;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.Temple;
import com.avrgaming.civcraft.structure.Trommel;
import com.avrgaming.civcraft.structure.Warehouse;
import com.avrgaming.civcraft.structure.Windmill;
import com.avrgaming.civcraft.threading.CivAsyncTask;
import com.avrgaming.civcraft.util.BlockCoord;

public class ParticleEffectTimer extends CivAsyncTask {
	
	public static ReentrantLock runningLock = new ReentrantLock();
	public static Map<Location, Material> externalParticleBlocks = new ConcurrentHashMap<Location, Material>();
	
	private void processTick() throws InterruptedException {
		// Loop through each structure, if it has an update function call it in another async process
		Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
		while(iter.hasNext()) {
			ArrayList<Location> allLocations = new ArrayList<Location>();
			Structure struct = iter.next().getValue();
			if (!struct.isActive()) { continue; }
			
			for (Location bc : externalParticleBlocks.keySet()) {
				if (bc != null) {
					allLocations.add(new Location(Bukkit.getWorld("world"), bc.getX()+0.5, bc.getY()+0.4, bc.getZ()+0.5));
				}
			}
			
			if (struct instanceof Barracks) {
				Barracks barracks = (Barracks) struct;
				for (StructureChest chest : barracks.structureChests.values()) {
					if (chest.getChestId() == 0) {
						allLocations.add(new Location(Bukkit.getWorld("world"), chest.getCoord().getX()+0.5, chest.getCoord().getY()+0.4, chest.getCoord().getZ()+0.5));
					}
				}
			}
			
			if (struct instanceof Granary) {
				Granary granary = (Granary) struct;
				for (StructureChest chest : granary.structureChests.values()) {
					if (chest.getChestId() == 1) {
						allLocations.add(new Location(Bukkit.getWorld("world"), chest.getCoord().getX()+0.5, chest.getCoord().getY()+0.4, chest.getCoord().getZ()+0.5));
					}
				}
				for (StructureTables table : granary.structureTables.values()) {
					allLocations.add(new Location(Bukkit.getWorld("world"), table.getCoord().getX()+0.5, table.getCoord().getY()+0.4, table.getCoord().getZ()+0.5));
				}
			}
			
			if (struct instanceof Mine) {
				Mine mine = (Mine) struct;
				for (StructureChest chest : mine.structureChests.values()) {
					if (chest.getChestId() == 0) {
						allLocations.add(new Location(Bukkit.getWorld("world"), chest.getCoord().getX()+0.5, chest.getCoord().getY()+0.4, chest.getCoord().getZ()+0.5));
					}
				}
			}
			
			if (struct instanceof Temple) {
				Temple temple = (Temple) struct;
				for (StructureChest chest : temple.structureChests.values()) {
					if (chest.getChestId() == 1) {
						allLocations.add(new Location(Bukkit.getWorld("world"), chest.getCoord().getX()+0.5, chest.getCoord().getY()+0.4, chest.getCoord().getZ()+0.5));
					}
				}
			}
			
			if (struct instanceof Trommel) {
				Trommel trommel = (Trommel) struct;
				for (StructureChest chest : trommel.structureChests.values()) {
					if (chest.getChestId() == 1 || chest.getChestId() == 2) {
						allLocations.add(new Location(Bukkit.getWorld("world"), chest.getCoord().getX()+0.5, chest.getCoord().getY()+0.4, chest.getCoord().getZ()+0.5));
					}
				}
			}
			
			if (struct instanceof Warehouse) {
				Warehouse wh = (Warehouse) struct;
				for (StructureChest chest : wh.structureChests.values()) {
					if (chest.getChestId() <= wh.getLevel()) {
						allLocations.add(new Location(Bukkit.getWorld("world"), chest.getCoord().getX()+0.5, chest.getCoord().getY()+0.5, chest.getCoord().getZ()+0.5));
					}
				}
			}
			
			if (struct instanceof Windmill) {
				Windmill windmill = (Windmill) struct;
				for (StructureChest chest : windmill.structureChests.values()) {
					if (chest.getChestId() == 0) {
						allLocations.add(new Location(Bukkit.getWorld("world"), chest.getCoord().getX()+0.5, chest.getCoord().getY()+0.4, chest.getCoord().getZ()+0.5));
					}
				}
			}
			
			
			//Execute the particles to these locations
			for (Location loc : allLocations) {
				World world = Bukkit.getWorld("world");
				world.spawnParticle(Particle.ENCHANTMENT_TABLE, loc, 1, 0.3, 0.3, 0.3, 0.3);
			}
		}
	}
	
	private void processSpecialTick() throws InterruptedException {
		// Loop through each structure, if it has an update function call it in another async process
		Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
		while(iter.hasNext()) {
			ArrayList<Location> specialLocations = new ArrayList<Location>();
			Structure struct = iter.next().getValue();
			if (!struct.isActive()) { continue; }
			
			if (struct instanceof Trommel) {
				Trommel trommel = (Trommel) struct;
				for (StructureChest chest : trommel.structureChests.values()) {
					if (chest.getChestId() == 3) {
						specialLocations.add(new Location(Bukkit.getWorld("world"), chest.getCoord().getX()+0.5, chest.getCoord().getY()+0.75, chest.getCoord().getZ()+0.5));
					}
				}
			}
			
			for (Location loc : specialLocations) {
				World world = Bukkit.getWorld("world");
				world.spawnParticle(Particle.SPELL, loc, 1, 0.1, 0.1, 0.1, 0.1);
			}
		}
	}
	
	int specialRun = 0;
	@Override
	public void run() {
		if (runningLock.tryLock()) {
			try {
				try {
					processTick();
					specialRun++;
					if (specialRun >= 5) {
						processSpecialTick();
						specialRun = 0;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} finally {
				runningLock.unlock();
			}
		} else {
			CivLog.error("ParticleEffectTimer trying to double-task?");
		}		
	}
}
