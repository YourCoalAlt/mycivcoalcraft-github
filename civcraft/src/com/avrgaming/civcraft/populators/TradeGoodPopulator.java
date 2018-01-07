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
package com.avrgaming.civcraft.populators;

import java.sql.SQLException;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigTradeGood;
import com.avrgaming.civcraft.main.CivCraft;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.ProtectedBlock;
import com.avrgaming.civcraft.object.TradeGood;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class TradeGoodPopulator extends BlockPopulator {
	
	private static int FLAG_HEIGHT = 3;
	
	@SuppressWarnings("deprecation")
	public static void buildTradeGoodie(ConfigTradeGood good, BlockCoord coord, World world, boolean sync) {
		TradeGood new_good = new TradeGood(good, coord);			
		CivGlobal.addTradeGood(new_good);
		
		Block top = null;
		//clear any stack goodies
		for (int y = coord.getY(); y < 256; y++) {
			top = world.getBlockAt(coord.getX(), y, coord.getZ());
			if (ItemManager.getId(top) == CivData.EMERALD) {
				ItemManager.setTypeId(top, CivData.AIR);
			}
		}
		
		for (int y = coord.getY(); y < coord.getY() + FLAG_HEIGHT; y++) {
			top = world.getBlockAt(coord.getX(), y, coord.getZ());
			top.setType(Material.EMERALD_BLOCK);
			
			ProtectedBlock pb = new ProtectedBlock(new BlockCoord(top), ProtectedBlock.Type.TRADE_MARKER);
			CivGlobal.addProtectedBlock(pb);
			if (sync) {
			try {
				pb.saveNow();
			} catch (SQLException e) {
				e.printStackTrace();
			}	
			} else {
				pb.save();
			}
		}
		
		Location loc = new Location(coord.getBlock().getWorld(), coord.getBlock().getX()+0.5, coord.getBlock().getY()+5, coord.getBlock().getZ()+0.5);
		if (CivSettings.hasHolographicDisplays) {
			Hologram hologram = HologramsAPI.createHologram(CivCraft.getPlugin(), loc);
			if (good.water == true) {
				hologram.appendItemLine(new ItemStack(good.material, 1, (short)good.material_data));
				hologram.appendTextLine(CivColor.Gold+CivColor.BOLD+"Trade Resource: "+CivColor.LightBlue+CivColor.BOLD+CivColor.ITALIC+good.name);
				hologram.appendTextLine(CivColor.Gold+CivColor.BOLD+"Value: "+CivColor.Yellow+CivColor.BOLD+CivColor.ITALIC+good.value+" Coins");
				hologram.appendTextLine(CivColor.Gold+CivColor.BOLD+"Culture: "+CivColor.Yellow+CivColor.BOLD+CivColor.ITALIC+"Future Addition"
										+CivColor.Gold+CivColor.BOLD+" | Food: "+CivColor.Yellow+CivColor.BOLD+CivColor.ITALIC+"Future Addition");
//				hologram.appendTextLine(CivColor.Gold+CivColor.BOLD+"Culture: "+CivColor.Yellow+CivColor.BOLD+CivColor.ITALIC+good.culture
//										+CivColor.Gold+CivColor.BOLD+" | Food:"+CivColor.Yellow+CivColor.BOLD+CivColor.ITALIC+good.food);
			} else {
				hologram.appendItemLine(new ItemStack(good.material, 1, (short)good.material_data));
				hologram.appendTextLine(CivColor.Gold+CivColor.BOLD+"Trade Resource: "+CivColor.LightGreen+CivColor.BOLD+CivColor.ITALIC+good.name);
				hologram.appendTextLine(CivColor.Gold+CivColor.BOLD+"Value: "+CivColor.Yellow+CivColor.BOLD+CivColor.ITALIC+good.value+" Coins");
				hologram.appendTextLine(CivColor.Gold+CivColor.BOLD+"Culture: "+CivColor.Yellow+CivColor.BOLD+CivColor.ITALIC+"Future Addition"
										+CivColor.Gold+CivColor.BOLD+" | Food: "+CivColor.Yellow+CivColor.BOLD+CivColor.ITALIC+"Future Addition");
//				hologram.appendTextLine(CivColor.Gold+CivColor.BOLD+"Culture: "+CivColor.Yellow+CivColor.BOLD+CivColor.ITALIC+good.culture
//										+CivColor.Gold+CivColor.BOLD+" | Food:"+CivColor.Yellow+CivColor.BOLD+CivColor.ITALIC+good..food);
			}
		} else {
			CivLog.warning("Tried generating A Trade Good Hologram without HolographicDisplays plugin! This is fine, but no holograms can generate.");
			CivLog.debug("This generated occured at: "+loc.toString());
		}
		
		if (sync) {
			try {
				new_good.saveNow();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			new_good.save();
		}
	}

	public static boolean checkForDuplicateTradeGood(String worldName, int centerX, int centerY, int centerZ) {
		/* Search downward to bedrock for any trade goodies here. If we find one, don't generate. */
		BlockCoord coord = new BlockCoord(worldName, centerX, centerY, centerZ);
		for (int y = centerY; y > 0; y--) {
			coord.setY(y);			
			
			if (CivGlobal.getTradeGood(coord) != null) {
				/* Already a trade goodie here. DONT Generate it. */
				return true;
			}		
		}
		return false;
	}
	
	@Override
	public void populate(World world, Random random, Chunk chunk) {
		ChunkCoord cCoord = new ChunkCoord(chunk);
		TradeGoodPick pick = CivGlobal.preGenerator.goodPicks.get(cCoord);
		if (pick != null) {
			int cX = (chunk.getX()*16)+7;
			int cZ = (chunk.getZ()*16)+7;
			int centerY = world.getHighestBlockYAt(cX, cZ);
			
			BlockCoord coord = new BlockCoord(world.getName(), cX, centerY, cZ);
			BlockCoord coord2 = new BlockCoord(world.getName(), cX, centerY-1, cZ);

			if (checkForDuplicateTradeGood(world.getName(), cX, centerY, cZ)) {
				return;
			}
			
			// Determine if we should be a water good.
			ConfigTradeGood good;
			if (ItemManager.getBlockTypeIdAt(world, cX, centerY-1, cZ) == CivData.WATER_STILL || 
					ItemManager.getBlockTypeIdAt(world, cX, centerY-1, cZ) == CivData.WATER_RUNNING || 
					ItemManager.getBlockTypeIdAt(world, cX, centerY-1, cZ) == CivData.ICE) {
/*					if (!coord2.getLocation().getBlock().getBiome().equals(Biome.OCEAN) &&
							!coord2.getLocation().getBlock().getBiome().equals(Biome.DEEP_OCEAN) &&
							!coord2.getLocation().getBlock().getBiome().equals(Biome.FROZEN_OCEAN)) {
						CivLog.warning(" -------------------------------- ");
						CivLog.warning("A trade good tried placing on water without proper biome! "+cX+", "+(centerY-1)+", "+cZ
										+" Biome: "+world.getBiome(cX, cZ).toString()+" Block: "+coord2.getCenteredLocation().getBlock().getType().toString());
						CivLog.warning(" (We will not spawn the trade good here as it is an invalid spot.) ");
						CivLog.warning(" -------------------------------- ");
						good = null;
					} else {*/
						good = pick.waterPick;
						CivLog.info(" -------------------------------- ");
						CivLog.info("Trade Good Generate: "+cX+", "+(centerY-1)+", "+cZ
								+" - Biome: "+world.getBiome(cX, cZ).toString()
								+" - Block: "+coord2.getCenteredLocation().getBlock().getType().toString()
								+" - Goodie: "+pick.waterPick.name);
						CivLog.info(" -------------------------------- ");
//					}
				} else {
/*					//TODO For this we don't want to just cancel the trade good spawn, so we should try to make a method
					//     that will check within a 4-5 block radius for a block it can be placed on. If that check fails,
					//     ONLY THEN will we not place a goodie.
					if (coord2.getCenteredLocation().getBlock().getType() == Material.WATER ||
							coord2.getCenteredLocation().getBlock().getType() == Material.STATIONARY_WATER ||
							coord2.getCenteredLocation().getBlock().getType() == Material.LEAVES ||
							coord2.getCenteredLocation().getBlock().getType() == Material.LEAVES_2 ||
							coord2.getCenteredLocation().getBlock().getType() == Material.LOG ||
							coord2.getCenteredLocation().getBlock().getType() == Material.LOG_2 ||
							coord2.getCenteredLocation().getBlock().getType() == Material.WOOD ||
							coord2.getCenteredLocation().getBlock().getType() == Material.STONE_SLAB2) {
						CivLog.warning(" -------------------------------- ");
						CivLog.warning("A trade good tried placing on improper block! "+cX+", "+(centerY-1)+", "+cZ
										+" Biome: "+world.getBiome(cX, cZ).toString()+" Block: "+coord2.getCenteredLocation().getBlock().getType().toString());
						CivLog.warning(" (We will not spawn the trade good here as it is an invalid spot.) ");
						CivLog.warning(" -------------------------------- ");
						good = null;
					} else {*/
						good = pick.landPick;
						CivLog.info(" -------------------------------- ");
						CivLog.info("Trade Good Generate: "+cX+", "+(centerY-1)+", "+cZ
									+" - Biome: "+world.getBiome(cX, cZ).toString()
									+" - Block: "+coord2.getCenteredLocation().getBlock().getType().toString()
									+" - Goodie: "+pick.landPick.name);
						CivLog.info(" -------------------------------- ");
//					}
				}
			// Randomly choose a land or water good.
			if (good == null) {
				System.out.println("Could not find suitable good type during populate! aborting.");
				return;
			}
			// Create a copy and save it in the global hash table.
			buildTradeGoodie(good, coord, world, false);
		}
	}
}