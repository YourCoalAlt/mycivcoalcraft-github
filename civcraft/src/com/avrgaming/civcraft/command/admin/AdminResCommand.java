/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.command.admin;

import java.sql.SQLException;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigPlatinumReward;
import com.avrgaming.civcraft.exception.AlreadyRegisteredException;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.exception.InvalidNameException;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.global.perks.PlatinumManager;

public class AdminResCommand extends CommandBase {

	@Override
	public void init() {
		command = "/ad res";
		displayName = "Admin Resident";
		
		commands.put("settown", "[player] [town] - puts this player in this town.");
		commands.put("cleartown", "[resident] - clears this residents town.");
		commands.put("enchant", "[enchant] [level] - Adds the enchantment with level to the item in your hand.");
		commands.put("giveplat", "[player] [amount] - Gives this player the specified amount of platinum.");
		commands.put("givereward", "[player] [rewardID] - Gives player this achievement with its plat rewards.");
		commands.put("rename", "[old_name] [new_name] - Rename this resident. Useful if players change their name.");
		commands.put("exposure", "[resident] [amount] - Gives/Takes thie [amount] of exposure to a [resident].");
		commands.put("rtp", "Will randomly teleport you in the world.");
	}
	
	public void rtp_cmd() {
		Player p = (Player) sender;
		Location teleportLocation = null;
		boolean isOnLand = false;
		boolean isClearAbove = false;
		
		int rX = 5000;
		int rZ = 5000;
		try {
			rX = CivSettings.getInteger(CivSettings.gameConfig, "world.radius_x");
			rZ = CivSettings.getInteger(CivSettings.gameConfig, "world.radius_z");
		} catch (InvalidConfiguration e) {
			CivLog.error("-- Error on Reciving Setting --");
			CivLog.error("Could not load either world.radius_x AND/OR world.radius_z when trying to teleport player "+p.getName());
			e.printStackTrace();
		}
		
		Random rand = new Random();
		int x = -(rX/2) + (rand.nextInt(rX)) - (rX/10);
		int y = 64;
		int z = -(rZ/2) + (rand.nextInt(rZ)) - (rZ/10);
		
		while (isOnLand == false) {
			teleportLocation = new Location(p.getWorld(), x, y, z);
			Location teleportLocationBelow = new Location(p.getWorld(), x, y-1, z);
			Location teleportLocationBelow2 = new Location(p.getWorld(), x, y-2, z);
			if (teleportLocation.getBlock().getType() == Material.AIR &&
					teleportLocationBelow.getBlock().getType() == Material.AIR &&
					teleportLocationBelow2.getBlock().getType().isSolid() &&
					
					teleportLocation.getBlock().getBiome() != Biome.DEEP_OCEAN && teleportLocation.getBlock().getBiome() != Biome.FROZEN_OCEAN &&
					teleportLocation.getBlock().getBiome() != Biome.OCEAN && teleportLocation.getBlock().getBiome() != Biome.FROZEN_RIVER &&
					teleportLocation.getBlock().getBiome() != Biome.RIVER && teleportLocation.getBlock().getBiome() != Biome.BEACHES &&
					teleportLocation.getBlock().getBiome() != Biome.COLD_BEACH && teleportLocation.getBlock().getBiome() != Biome.STONE_BEACH) {
				isOnLand = true;
			} else {
				if (x <= rX && x > 0) {
					x += rand.nextInt(3)+1;
				} else if (x >= (-rX) && x < 0) {
					x += -(rand.nextInt(3)+1);
				} else {
					x = -(rX/2) + (rand.nextInt(rX)) - (rX/10);
				}
				
				if (z <= rZ && x > 0) {
					z += rand.nextInt(3)+1;
				} else if (z >= (-rZ) && x < 0) {
					z += -(rand.nextInt(3)+1);
				} else {
					z = -(rZ/2) + (rand.nextInt(rZ)) - (rZ/10);
				}
			}
		}
		
		while (isClearAbove == false) {
			Location tpLocAbove1 = new Location(p.getWorld(), x, y+1, z);
			Location tpLocAbove2 = new Location(p.getWorld(), x, y+2, z);
			if (tpLocAbove1.getBlock().getType() == Material.AIR && tpLocAbove2.getBlock().getType() == Material.AIR) {
				isClearAbove = true;
			} else {
				isOnLand = false;
				if (x <= rX && x > 0) {
					x += rand.nextInt(3)+1;
				} else if (x >= (-rX) && x < 0) {
					x += -(rand.nextInt(3)+1);
				} else {
					x = -(rX/2) + (rand.nextInt(rX)) - (rX/10);
				}
				
				if (z <= rZ && x > 0) {
					z += rand.nextInt(3)+1;
				} else if (z >= (-rZ) && x < 0) {
					z += -(rand.nextInt(3)+1);
				} else {
					z = -(rZ/2) + (rand.nextInt(rZ)) - (rZ/10);
				}
			}
		}
		
		p.setInvulnerable(true);
		p.removePotionEffect(PotionEffectType.REGENERATION);
		p.removePotionEffect(PotionEffectType.SATURATION);
		p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (20*10), 255));
		p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, (20*10), 255));
		p.teleport(teleportLocation);
		p.setInvulnerable(false);
		CivMessage.sendSuccess(p, "You have been teleported to "+x+", "+y+", "+z+"!");
	}
	
	public void exposure_cmd() throws CivException {
		Resident res = getNamedResident(1);
		Integer exposure = getNamedInteger(2);
		res.setSpyExposure(res.getSpyExposure()+exposure);
		CivMessage.sendSuccess(sender, "Gave "+res.getName()+" "+exposure+" Spy Exposure.");
	}
	
	public void rename_cmd() throws CivException {
		Resident resident = getNamedResident(1);
		String newName = getNamedString(2, "Enter a new name");
		
		Resident newResident = CivGlobal.getResident(newName);
		if (newResident != null) {
			throw new CivException("Already another resident with the name:"+newResident.getName()+" cannot rename "+resident.getName());
		}
		
		/* Create a dummy resident to make sure name is valid. */
		try {
			new Resident(null, newName);
		} catch (InvalidNameException e1) {
			throw new CivException("Invalid name. Pick again.");
		}
		
		/* Delete the old resident object. */
		try {
			resident.delete();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CivException(e.getMessage());
		}
		
		/* Remove resident from CivGlobal tables. */
		CivGlobal.removeResident(resident);
		
		/* Change the resident's name. */
		try {
			resident.setName(newName);
		} catch (InvalidNameException e) {
			e.printStackTrace();
			throw new CivException("Internal error:"+e.getMessage());
		}
		
		/* Resave resident to DB and global tables. */
		CivGlobal.addResident(resident);
		resident.save();
		
		CivMessage.send(sender, "Resident renamed.");
	}
	
	public void givereward_cmd() throws CivException {
		Resident resident = getNamedResident(1);
		String rewardID = getNamedString(2, "Enter a Reward ID");
		
		for (ConfigPlatinumReward reward : CivSettings.platinumRewards.values()) {
			if (reward.name.equals(rewardID)) {
				switch (reward.occurs) {
				case "once":
					PlatinumManager.givePlatinumOnce(resident, reward.name, reward.amount, "Sweet! An admin gave you a platinum reward of %d");
					break;
				case "daily":
					PlatinumManager.givePlatinumDaily(resident, reward.name, reward.amount, "Sweet! An admin gave you a platinum reward of %d");
					break;
				default:
					PlatinumManager.givePlatinum(resident, reward.amount, "Sweet! An admin gave you a platinum reward of %d");
					break;
				}
				CivMessage.sendSuccess(sender, "Reward Given.");
				return;
			}
		}
		
		CivMessage.sendError(sender, "Couldn't find reward named:"+rewardID);
	}
	
	
	public void giveplat_cmd() throws CivException {
		Resident resident = getNamedResident(1);
		Integer plat = getNamedInteger(2);
		
		PlatinumManager.givePlatinum(resident, plat, "Sweet! You were given %d by an admin!");
		CivMessage.sendSuccess(sender, "Gave "+resident.getName()+" "+plat+" platinum");
	}
	
	public void enchant_cmd() throws CivException {
		Player player = getPlayer();
		String enchant = getNamedString(1, "Enchant name");
		int level = getNamedInteger(2);
		
		
		ItemStack stack = player.getInventory().getItemInMainHand();
		Enchantment ench = Enchantment.getByName(enchant);
		if (ench == null) {
			String out ="";
			for (Enchantment ench2 : Enchantment.values()) {
				out += ench2.getName()+",";
			}
			throw new CivException("No enchantment called "+enchant+" Options:"+out);
		}
		
		stack.addUnsafeEnchantment(ench, level);
		CivMessage.sendSuccess(sender, "Enchanted.");
	}
	
	public void cleartown_cmd() throws CivException {
		if (args.length < 2) {
			throw new CivException("Enter a player name");
		}
				
		Resident resident = getNamedResident(1);
		
		if (resident.hasTown()) {
			resident.getTown().removeResident(resident);
		}
		
		resident.save();
		CivMessage.sendSuccess(sender, "Cleared "+resident.getName()+" from any town.");

	}
	
	public void settown_cmd() throws CivException {
		
		if (args.length < 3) {
			throw new CivException("Enter player and its new town.");
		}
		
		Resident resident = getNamedResident(1);

		Town town = getNamedTown(2);

		if (resident.hasTown()) {
			resident.getTown().removeResident(resident);
		}
		
		try {
			town.addResident(resident);
		} catch (AlreadyRegisteredException e) {
			e.printStackTrace();
			throw new CivException("Already in this town?");
		}
		
		town.save();
		resident.save();
		CivMessage.sendSuccess(sender, "Moved "+resident.getName()+" into town "+town.getName());
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
		
	}

}
