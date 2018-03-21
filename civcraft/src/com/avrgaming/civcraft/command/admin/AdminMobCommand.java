package com.avrgaming.civcraft.command.admin;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigCustomMobs;
import com.avrgaming.civcraft.config.perms.CivPerms;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.mobs.CustomMobListener;
import com.avrgaming.civcraft.mobs.MobSpawner;
import com.avrgaming.civcraft.mobs.MobSpawnerTimer;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.EntityProximity;

import net.minecraft.server.v1_12_R1.EntityCreature;

public class AdminMobCommand extends CommandBase {

	@Override
	public void init() {
		command = "/ad mob";
		displayName = "Admin Mob";		
		
		commands.put("killall", "[name] Removes all of these mobs from the game instantly.");
		commands.put("purgehostile", "Removes every hostile mob inside the server.");
		commands.put("count", "Shows mob totals globally");
		commands.put("spawn", "remote entities test");
	}
	
	public void purgehostile_cmd() throws CivException {
		if (sender instanceof Player) {
			Player p = getPlayer();
			MobSpawner.despawnAllHostile(p);
		} else {
			MobSpawner.despawnAllHostile(null);
		}
	}
	
	public void spawn_cmd() throws CivException {
		Player player = getPlayer();
		String smob = getNamedString(1, "Please enter an id").toUpperCase();
		int amt = getNamedInteger(2);
		
		if (smob == null) {
			throw new CivException("No custom mob with id: "+smob);
		}
		
		ConfigCustomMobs cmob = CivSettings.customMobs.get(smob);
		if (cmob == null) {
			throw new CivException(smob+" is not a valid ID.");
		}
		
		for (int i = 0; i < amt; i++) {
			MobSpawner.spawnCustomMob(cmob, player.getLocation());
		}
		
		CivMessage.sendSuccess(player, "Spawned "+amt+" "+CivColor.colorize(cmob.name));
	}
	
	public void killall_cmd() throws CivException {
		Player player = getPlayer();
		String name = getNamedString(1, "Enter a mob ID.").toUpperCase();
		ConfigCustomMobs cmob = CivSettings.customMobs.get(name);
		if (cmob == null) {
			throw new CivException(name+" is not a valid ID.");
		}
		
		int count = 0;
		for (Chunk c : Bukkit.getWorld(CivCraft.worldName).getLoadedChunks()) {
			for (Entity e : c.getEntities()) {
				if (CustomMobListener.customMobs.get(e.getUniqueId()) != null) {
					if (ChatColor.stripColor(e.getCustomName()).toString().replaceAll(" ", "_").equalsIgnoreCase(cmob.id)) {
						CustomMobListener.customMobs.remove(e.getUniqueId());
						e.remove();
						count++;
					}
				}
				
				if (CustomMobListener.mobList.get(e.getUniqueId()) != null) {
					CustomMobListener.mobList.remove(e.getUniqueId());
				}
			}
		}
		
		CivMessage.sendSuccess(player, "Removed "+count+ " mobs of type "+name);
	}

	public void count_cmd() throws CivException {
		Player player = getPlayer();
		
		HashMap<String, Integer> amounts = new HashMap<String, Integer>();
		int total = CustomMobListener.customMobs.size();
		for (net.minecraft.server.v1_12_R1.Entity mob : CustomMobListener.customMobs.values()) {
			Integer count = amounts.get(ChatColor.stripColor(mob.getCustomName()));
			if (count == null) count = 0;
			amounts.put(ChatColor.stripColor(mob.getCustomName()), count+1);
		}
		
		CivMessage.sendHeading(player, "Custom Mob Counts");
		CivMessage.send(player, CivColor.LightGray+"Red mobs are over their count limit for this area and should no longer spawn.");
		for (String mob : amounts.keySet()) {
			int count = amounts.get(mob);
			
			LinkedList<Entity> entities = EntityProximity.getNearbyEntities(null, player.getLocation(), MobSpawnerTimer.MOB_AREA, EntityCreature.class);
			if (entities.size() > MobSpawnerTimer.MOB_AREA_LIMIT) {
				CivMessage.send(player, CivColor.Red+mob+": "+CivColor.Rose+count);
			} else {
				CivMessage.send(player, CivColor.Green+mob+": "+CivColor.LightGreen+count);
			}
			
		}
		CivMessage.send(player, CivColor.Green+"Total Mobs: "+CivColor.LightGreen+total);
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}
	
	@Override
	public void showHelp() {
		showBasicHelp();
	}
	
	@Override
	public void permissionCheck() throws CivException {
		if (sender instanceof Player) {
			CivPerms.validAdMob(getPlayer());
		}
	}
	
}
