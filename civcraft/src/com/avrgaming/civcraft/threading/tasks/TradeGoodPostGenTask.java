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
package com.avrgaming.civcraft.threading.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import com.avrgaming.civcraft.config.ConfigTradeGood;
import com.avrgaming.civcraft.database.SQL;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.TradeGood;
import com.avrgaming.civcraft.populators.TradeGoodPick;
import com.avrgaming.civcraft.populators.TradeGoodPopulator;
import com.avrgaming.civcraft.threading.TaskMaster;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.ChunkCoord;
import com.avrgaming.civcraft.util.CivItem;

public class TradeGoodPostGenTask implements Runnable {

	String playerName;
	int start;
	
	public TradeGoodPostGenTask(String playerName, int start) {
		this.playerName = playerName;
		this.start = 0;
	}
	
	public void deleteAllTradeGoodiesFromDB() {
		/* Delete all existing trade goods from DB. */
		Connection conn = null;
		PreparedStatement ps = null;
		try {
		try {
			conn = SQL.getGameConnection();
			String code = "TRUNCATE TABLE "+TradeGood.TABLE_NAME;
			ps = conn.prepareStatement(code);
			ps.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	@Override
	public void run() {
		CivMessage.console(playerName, "Generating/Clearing Trade goods...");
		CivMessage.console(playerName, "|- Organizing trade picks into a Queue.");
		
		deleteAllTradeGoodiesFromDB();
		
		/* Generate Trade Good Pillars. */
		Queue<TradeGoodPick> picksQueue = new LinkedList<TradeGoodPick>();
		for (TradeGoodPick pick : CivGlobal.preGenerator.goodPicks.values()) {
			picksQueue.add(pick);
		}
		
		int count = 0;
		int amount = 20;
		int totalSize = picksQueue.size();
		while (picksQueue.peek() != null) {
			CivMessage.console(playerName, "|- Placing/Picking Goods:"+count+"/"+totalSize+" current size:"+picksQueue.size());
			
			Queue<TradeGoodPick> processQueue = new LinkedList<TradeGoodPick>();
			for (int i = 0; i < amount; i++) {
				TradeGoodPick pick = picksQueue.poll();
				if (pick == null) {
					break;
				}
				
				count++;
				processQueue.add(pick);
			}
			
			TaskMaster.syncTask(new SyncTradeGenTask(processQueue, amount));
			
			try {
				while (processQueue.peek() != null) {
					Thread.sleep(500);
				}
			} catch (InterruptedException e) {
				return;
			}
		}
		
		
		CivMessage.console(playerName, "Finished!");
	}

	class SyncTradeGenTask implements Runnable {
		public Queue<TradeGoodPick> picksQueue;
		public int amount;
		
		public SyncTradeGenTask(Queue<TradeGoodPick> picksQueue, int amount) {
			this.picksQueue = picksQueue;
			this.amount = amount;
		}
		
		@Override
		public void run() {
			World world = Bukkit.getWorld("world");
			for(int i = 0; i < amount; i++) {
				TradeGoodPick pick = picksQueue.poll();
				if (pick != null) {
					ChunkCoord ccoord = pick.chunkCoord;
					Chunk chunk = world.getChunkAt(ccoord.getX(), ccoord.getZ());
					int cX = (chunk.getX()*16)+7;
					int cZ = (chunk.getZ()*16)+7;
					int centerY = world.getHighestBlockYAt(cX, cZ);
					
					BlockCoord coord = new BlockCoord(world.getName(), cX, centerY, cZ);
					BlockCoord coord2 = new BlockCoord(world.getName(), cX, centerY-1, cZ);

					if (TradeGoodPopulator.checkForDuplicateTradeGood(world.getName(), cX, centerY, cZ)) {
						return;
					}
					
					// Determine if we should be a water good.
					ConfigTradeGood good;
					if (CivItem.getBlockTypeIdAt(world, cX, centerY-1, cZ) == CivData.WATER_STILL || 
							CivItem.getBlockTypeIdAt(world, cX, centerY-1, cZ) == CivData.WATER_RUNNING || 
							CivItem.getBlockTypeIdAt(world, cX, centerY-1, cZ) == CivData.ICE) {
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
//							}
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
//							}
						}
					// Randomly choose a land or water good.
					if (good == null) {
						System.out.println("Could not find suitable good type during populate! aborting.");
						return;
					}
					// Create a copy and save it in the global hash table.
					TradeGoodPopulator.buildTradeGoodie(good, coord, world, false);
				}
			}
		}
	}
}
