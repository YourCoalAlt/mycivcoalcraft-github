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

import java.util.ArrayList;
import java.util.Random;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.structure.Windmill;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.MultiInventory;

public class WindmillPostProcessSyncTask implements Runnable {
	
	ArrayList<BlockCoord> plantBlocks;
	Windmill windmill;
	int breadCount;
	int carrotCount;
	int potatoCount;
	int beetrootCount;
	MultiInventory source_inv;
	
	public WindmillPostProcessSyncTask(Windmill windmill, ArrayList<BlockCoord> plantBlocks,
			int breadCount, int carrotCount, int potatoCount, int beetrootCount, MultiInventory source_inv) {
		this.plantBlocks = plantBlocks;
		this.windmill = windmill;
		this.breadCount = breadCount;
		this.carrotCount = carrotCount;
		this.potatoCount = potatoCount;
		this.beetrootCount = beetrootCount;
		this.source_inv = source_inv;
	}
	
	@Override
	public void run() {
		Random rand = new Random();
		for (BlockCoord coord : plantBlocks) {
			switch (rand.nextInt(4)) {
			case 0:
				if (breadCount > 0) {
					try {
						source_inv.removeItem(CivData.WHEAT_SEED, 1);
					} catch (CivException e) {
						e.printStackTrace();
					}
					breadCount--;
					ItemManager.setTypeId(coord.getBlock(), CivData.WHEAT_CROP);
					ItemManager.setData(coord.getBlock(), 0, true);
					continue;
				}
			case 1:
				if (carrotCount > 0) {
					try {
						source_inv.removeItem(CivData.CARROT_ITEM, 1);
					} catch (CivException e) {
						e.printStackTrace();
					}
					carrotCount--;
					ItemManager.setTypeId(coord.getBlock(), CivData.CARROT_CROP);
					ItemManager.setData(coord.getBlock(), 0, true);
					continue;
				}
				break;
			case 2: 
				if (potatoCount > 0) {
					try {
						source_inv.removeItem(CivData.POTATO_ITEM, 1);
					} catch (CivException e) {
						e.printStackTrace();
					}
					potatoCount--;
					ItemManager.setTypeId(coord.getBlock(), CivData.POTATO_CROP);
					ItemManager.setData(coord.getBlock(), 0, true);
					continue;
				}
			case 3: 
				if (beetrootCount > 0) {
					try {
						source_inv.removeItem(CivData.BEETROOT_SEED, 1);
					} catch (CivException e) {
						e.printStackTrace();
					}
					beetrootCount--;
					ItemManager.setTypeId(coord.getBlock(), CivData.BEETROOT_CROP);
					ItemManager.setData(coord.getBlock(), 0, true);
					continue;
				}
			}	
			
			// our randomly selected crop couldn't be placed, try them all now.
			if (breadCount > 0) {
				try {
					source_inv.removeItem(CivData.WHEAT_SEED, 1);
				} catch (CivException e) {
					e.printStackTrace();
				}
				breadCount--;
				ItemManager.setTypeId(coord.getBlock(), CivData.WHEAT_CROP);
				ItemManager.setData(coord.getBlock(), 0, true);
				continue;
			}
			if (carrotCount > 0) {
				try {
					source_inv.removeItem(CivData.CARROT_ITEM, 1);
				} catch (CivException e) {
					e.printStackTrace();
				}
				carrotCount--;
				ItemManager.setTypeId(coord.getBlock(), CivData.CARROT_CROP);
				ItemManager.setData(coord.getBlock(), 0, true);
				continue;
			}
			if (potatoCount > 0) {
				try {
					source_inv.removeItem(CivData.POTATO_ITEM, 1);
				} catch (CivException e) {
					e.printStackTrace();
				}
				potatoCount--;
				ItemManager.setTypeId(coord.getBlock(), CivData.POTATO_CROP);
				ItemManager.setData(coord.getBlock(), 0, true);
				continue;
			}
			if (beetrootCount > 0) {
				try {
					source_inv.removeItem(CivData.BEETROOT_SEED, 1);
				} catch (CivException e) {
					e.printStackTrace();
				}
				beetrootCount--;
				ItemManager.setTypeId(coord.getBlock(), CivData.BEETROOT_CROP);
				ItemManager.setData(coord.getBlock(), 0, true);
				continue;
			}
		}
	}
}
