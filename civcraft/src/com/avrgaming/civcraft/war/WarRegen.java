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
package com.avrgaming.civcraft.war;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;

import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.util.BukkitObjects;
import com.avrgaming.civcraft.util.CivItem;

import gpl.ItemSerializer;

public class WarRegen {
	
	//  Saves an regenerates blocks during a war.
	private static Map<Block, Boolean> blockCache = new HashMap<Block, Boolean>(); 
	
	private static String blockAsAir(Block blk) {
		return CivData.AIR+":0:"+blk.getX()+":"+blk.getY()+":"+blk.getZ()+":"+blk.getWorld().getName(); 
	}
	
	private static String blockBasicString(Block blk) {
		return CivItem.getId(blk)+":"+CivItem.getData(blk)+":"+blk.getX()+":"+blk.getY()+":"+blk.getZ()+":"+blk.getWorld().getName();
	}
	
	public static String blockInventoryString(Inventory inv) {
		String out = ":";
		out += ItemSerializer.InventoryToString(inv);
		return out;
	}
	
	public static String blockSignString(Sign sign) {
		String out = ":";
		for (String str : sign.getLines()) {
			out += str+",";
		}
		return out;
	}
	
	private static String blockToString(Block blk, boolean save_as_air) {
		if (save_as_air) return blockAsAir(blk);
		else {
			String str = blockBasicString(blk);
			Inventory inv = null;
			switch (blk.getType()) {
			case TRAPPED_CHEST:
			case CHEST:
				inv = ((Chest)blk.getState()).getInventory();
				str += blockInventoryString(inv);
				break;
			case WHITE_SHULKER_BOX:
			case ORANGE_SHULKER_BOX:
			case MAGENTA_SHULKER_BOX:
			case LIGHT_BLUE_SHULKER_BOX:
			case YELLOW_SHULKER_BOX:
			case LIME_SHULKER_BOX:
			case PINK_SHULKER_BOX:
			case GRAY_SHULKER_BOX:
			case SILVER_SHULKER_BOX:
			case CYAN_SHULKER_BOX:
			case PURPLE_SHULKER_BOX:
			case BLUE_SHULKER_BOX:
			case BROWN_SHULKER_BOX:
			case GREEN_SHULKER_BOX:
			case RED_SHULKER_BOX:
			case BLACK_SHULKER_BOX:
				inv = ((ShulkerBox)blk.getState()).getInventory();
				str += blockInventoryString(inv);
				break;
			case BURNING_FURNACE:
			case FURNACE:
				inv = ((Furnace)blk.getState()).getInventory();
				str += blockInventoryString(inv);
				break;
			case DISPENSER:
				inv = ((Dispenser)blk.getState()).getInventory();
				str += blockInventoryString(inv);
				break;
			case DROPPER:
				inv = ((Dropper)blk.getState()).getInventory();
				str += blockInventoryString(inv);
				break;
			case BREWING_STAND:
				inv = ((BrewingStand)blk.getState()).getInventory();
				str += blockInventoryString(inv);
				break;
			case HOPPER:
				inv = ((Hopper)blk.getState()).getInventory();
				str += blockInventoryString(inv);
				break;
			case SIGN_POST:
			case WALL_SIGN:
				Sign sign = (Sign)blk.getState();
				str += blockSignString(sign);
				break;
			default:
				break;
			}
			return str;
		}
	}
	
	private static void restoreBlockFromString(String line) {
		String[] split = line.split(":");
		int type = Integer.valueOf(split[0]);
		byte data = Byte.valueOf(split[1]);
		int x = Integer.valueOf(split[2]);
		int y = Integer.valueOf(split[3]);
		int z = Integer.valueOf(split[4]);
		String world = split[5];
		
		Block blk = BukkitObjects.getWorld(world).getBlockAt(x,y,z);
		CivItem.setTypeId(blk, type);
		CivItem.setData(blk, data, false);

		// End of basic block info, try to get more now.
		Inventory inv = null;
		switch (blk.getType()) {
		case TRAPPED_CHEST:
		case CHEST:
			inv = ((Chest)blk.getState()).getInventory();
			ItemSerializer.StringToInventory(inv, split[6]);
			break;
		case WHITE_SHULKER_BOX:
		case ORANGE_SHULKER_BOX:
		case MAGENTA_SHULKER_BOX:
		case LIGHT_BLUE_SHULKER_BOX:
		case YELLOW_SHULKER_BOX:
		case LIME_SHULKER_BOX:
		case PINK_SHULKER_BOX:
		case GRAY_SHULKER_BOX:
		case SILVER_SHULKER_BOX:
		case CYAN_SHULKER_BOX:
		case PURPLE_SHULKER_BOX:
		case BLUE_SHULKER_BOX:
		case BROWN_SHULKER_BOX:
		case GREEN_SHULKER_BOX:
		case RED_SHULKER_BOX:
		case BLACK_SHULKER_BOX:
			inv = ((ShulkerBox)blk.getState()).getInventory();
			ItemSerializer.StringToInventory(inv, split[6]);
			break;
		case BURNING_FURNACE:
		case FURNACE:
			inv = ((Furnace)blk.getState()).getInventory();
			ItemSerializer.StringToInventory(inv, split[6]);
			break;
		case DISPENSER:
			inv = ((Dispenser)blk.getState()).getInventory();
			ItemSerializer.StringToInventory(inv, split[6]);
			break;
		case DROPPER:
			inv = ((Dropper)blk.getState()).getInventory();
			ItemSerializer.StringToInventory(inv, split[6]);
			break;
		case BREWING_STAND:
			inv = ((BrewingStand)blk.getState()).getInventory();
			ItemSerializer.StringToInventory(inv, split[6]);
			break;
		case HOPPER:
			inv = ((Hopper)blk.getState()).getInventory();
			ItemSerializer.StringToInventory(inv, split[6]);
			break;
		case SIGN_POST:
		case WALL_SIGN:
			Sign sign = (Sign)blk.getState();
			String[] messages = split[6].split(",");
			for (int i = 0; i < 4; i++) {
				if (messages[i] != null) {
					sign.setLine(i, messages[i]);
				}
			}
			sign.update();
			break;
		default:
			break;
		}
	}
	
	public static void explodeThisBlock(Block blk, String file) {
		switch (blk.getType()) {
		case SIGN_POST:
		case WALL_SIGN:
		case TNT:
			return;
		default:
			break;
		}
		
		WarRegen.saveBlock(blk, file, false);
		
		switch (blk.getType()) {
		case TRAPPED_CHEST:
		case CHEST:
			((Chest)blk.getState()).getInventory().clear();
			break;
		case WHITE_SHULKER_BOX:
		case ORANGE_SHULKER_BOX:
		case MAGENTA_SHULKER_BOX:
		case LIGHT_BLUE_SHULKER_BOX:
		case YELLOW_SHULKER_BOX:
		case LIME_SHULKER_BOX:
		case PINK_SHULKER_BOX:
		case GRAY_SHULKER_BOX:
		case SILVER_SHULKER_BOX:
		case CYAN_SHULKER_BOX:
		case PURPLE_SHULKER_BOX:
		case BLUE_SHULKER_BOX:
		case BROWN_SHULKER_BOX:
		case GREEN_SHULKER_BOX:
		case RED_SHULKER_BOX:
		case BLACK_SHULKER_BOX:
			((ShulkerBox)blk.getState()).getInventory().clear();
			break;
		case BURNING_FURNACE:
		case FURNACE:
			((Furnace)blk.getState()).getInventory().clear();
			break;
		case DISPENSER:
			((Dispenser)blk.getState()).getInventory().clear();
			break;
		case DROPPER:
			((Dropper)blk.getState()).getInventory().clear();
			break;
		case BREWING_STAND:
			((BrewingStand)blk.getState()).getInventory().clear();
			break;
		case HOPPER:
			((Hopper)blk.getState()).getInventory().clear();
			break;
		default:
			break;
		}
		
		CivItem.setTypeId(blk, CivData.AIR);
		CivItem.setData(blk, 0x0, true);
	}
	
	public static void destroyThisBlock(Block blk, Town town) {
		WarRegen.saveBlock(blk, town.getName(), false);
		
		switch (blk.getType()) {
		case TRAPPED_CHEST:
		case CHEST:
			((Chest)blk.getState()).getInventory().clear();
			break;
		case WHITE_SHULKER_BOX:
		case ORANGE_SHULKER_BOX:
		case MAGENTA_SHULKER_BOX:
		case LIGHT_BLUE_SHULKER_BOX:
		case YELLOW_SHULKER_BOX:
		case LIME_SHULKER_BOX:
		case PINK_SHULKER_BOX:
		case GRAY_SHULKER_BOX:
		case SILVER_SHULKER_BOX:
		case CYAN_SHULKER_BOX:
		case PURPLE_SHULKER_BOX:
		case BLUE_SHULKER_BOX:
		case BROWN_SHULKER_BOX:
		case GREEN_SHULKER_BOX:
		case RED_SHULKER_BOX:
		case BLACK_SHULKER_BOX:
			((ShulkerBox)blk.getState()).getInventory().clear();
			break;
		case BURNING_FURNACE:
		case FURNACE:
			((Furnace)blk.getState()).getInventory().clear();
			break;
		case DISPENSER:
			((Dispenser)blk.getState()).getInventory().clear();
			break;
		case DROPPER:
			((Dropper)blk.getState()).getInventory().clear();
			break;
		case BREWING_STAND:
			((BrewingStand)blk.getState()).getInventory().clear();
			break;
		case HOPPER:
			((Hopper)blk.getState()).getInventory().clear();
			break;
		default:
			break;
		}
		
		CivItem.setTypeId(blk, CivData.AIR);
		CivItem.setData(blk, 0x0, true);
	}
	
	public static boolean canPlaceThisBlock(Block blk) {
		switch (blk.getType()) {
		case LAVA:
		case WATER:
			return false;
		default:
			break;
		}
		return true;
	}
	
	public static void saveBlock(Block blk, String name, boolean save_as_air) {
		// Open this town's war log file.
		// append this block to the war log file.
		Boolean saved = blockCache.get(blk);
		if (saved == Boolean.TRUE) {
			//Block has already been saved, dont save it again.
			//This should prevent enemies from being able to overwrite
			//legit blocks.
			return;
		}
		
	    try {
			String filepath = "templates/war/"+name;
			FileWriter fstream = new FileWriter(filepath,true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.append(blockToString(blk, save_as_air)+"\n");
			blockCache.put(blk, Boolean.TRUE);
			out.close();
			fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void restoreBlocksFor(String name) {
		// Open the town's war log file and restore all the blocks
		// Call this function when we start up.
		// Delete the war log file to save space.
		try {
			int count = 0;
			String filepath = "templates/war/"+name;
			File warLog = new File(filepath);
			
			if (!warLog.exists())
				return; 
			
			BufferedReader reader = new BufferedReader(new FileReader(warLog));
			
			String line = null;
			while((line=reader.readLine()) != null) {
				try {
					restoreBlockFromString(line);
					count++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			reader.close();
			warLog.delete();
			System.out.println("[CivCraft] Restored "+count+" blocks for town "+name);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
