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


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.object.StructureChest;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.StructureTables;
import com.avrgaming.civcraft.object.camp.Camp;
import com.avrgaming.civcraft.structure.ArrowTower;
import com.avrgaming.civcraft.structure.Blacksmith;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.structure.CannonTower;
import com.avrgaming.civcraft.structure.Farm;
import com.avrgaming.civcraft.structure.TownHall;
import com.avrgaming.civcraft.structure.TradeOutpost;
import com.avrgaming.civcraft.structure.Warehouse;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;
import com.avrgaming.civcraft.util.SimpleBlock.Type;

public class PostBuildSyncTask implements Runnable {

	/*
	 * Search the template for special command blocks and handle them *after* the structure 
	 * has finished building.
	 */
	
	Template tpl;
	Buildable buildable;
	Boolean forceRefresh;
	
	public PostBuildSyncTask(Template tpl, Buildable buildable, boolean forceRefresh) {
		this.tpl = tpl;
		this.buildable = buildable;
		this.forceRefresh = forceRefresh;
	}
	
	public static void start(Template tpl, Buildable buildable, boolean forceRefresh) {
		if (forceRefresh) {
			if (!(buildable instanceof Farm)) {
				// Resets all structure blocks
				for (int x = 0; x < tpl.size_x; x++) {
					for (int y = 0; y < tpl.size_y; y++) {
						for (int z = 0; z < tpl.size_z; z++) {
							Block b = buildable.getCorner().getBlock().getRelative(x, y, z);
							SimpleBlock sb = tpl.blocks[x][y][z];
							if (sb.getMaterial() == Material.AIR) continue;
							if (b.getType() != sb.getMaterial()) {
								if (sb.getMaterial() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST ||
										sb.getMaterial() == Material.FURNACE || sb.getMaterial() == Material.BURNING_FURNACE) continue;
								ItemManager.setTypeIdAndData(b, tpl.blocks[x][y][z].getType(), (byte)tpl.blocks[x][y][z].getData(), false);
							}
							
							if (ItemManager.getId(b) == CivData.WALL_SIGN || ItemManager.getId(b) == CivData.SIGN) {
								Sign s2 = (Sign)b.getState();
								if (s2.getLine(0) == "" && s2.getLine(1) == "" && s2.getLine(2) == ""  && s2.getLine(3) == "") {
									ItemManager.setTypeIdAndData(b, 0, 0, false);
								} else {
									s2.setLine(0, tpl.blocks[x][y][z].message[0]);
									s2.setLine(1, tpl.blocks[x][y][z].message[1]);
									s2.setLine(2, tpl.blocks[x][y][z].message[2]);
									s2.setLine(3, tpl.blocks[x][y][z].message[3]);
									s2.update();
								}
							}
						}
					}
				}
			}
		}
		
		for (BlockCoord relativeCoord : tpl.doorRelativeLocations) {
			SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
			BlockCoord absCoord = new BlockCoord(buildable.getCorner().getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));

			Block block = absCoord.getBlock();
			if (ItemManager.getId(block) != sb.getType()) {
				if (buildable.getCiv().isAdminCiv()) {
					ItemManager.setTypeIdAndData(block, CivData.AIR, (byte)0, false);
				} else {
					ItemManager.setTypeIdAndData(block, sb.getType(), (byte)sb.getData(), false);
				}
			}
		}
			
		for (BlockCoord relativeCoord : tpl.attachableLocations) {
			SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
			BlockCoord absCoord = new BlockCoord(buildable.getCorner().getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));
			Block block = absCoord.getBlock();
			if (sb.getType() == CivData.WALL_SIGN || sb.getType() == CivData.SIGN) {
				if (sb.specialType == Type.COMMAND) continue;
			}
			
			if (ItemManager.getId(block) != sb.getType()) {
				ItemManager.setTypeIdAndData(block, sb.getType(), (byte)sb.getData(), false);
			}
		}
		
		/* Use the location's of the command blocks in the template and the buildable's corner 
		 * to find their real positions. Then perform any special building we may want to do
		 * at those locations.
		 */
		/* These block coords do not point to a location in the world, just a location in the template. */
		for (BlockCoord relativeCoord : tpl.commandBlockRelativeLocations) {
			SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
			StructureSign structSign;
			Block block;
			BlockCoord absCoord = new BlockCoord(buildable.getCorner().getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));
			
			if (absCoord.getBlock().getType() == Material.WEB || absCoord.getBlock().getType() == Material.SIGN_POST || absCoord.getBlock().getType() == Material.WALL_SIGN) {
				ItemManager.setTypeIdAndData(absCoord.getBlock(), CivData.AIR, (byte)0, false);
			}
			
			/* Signs and chests should already be handled, look for more exotic things. */
			switch (sb.command) {
			case "/chest":
				StructureChest structChest = CivGlobal.getStructureChest(absCoord);
				if (structChest == null) {
					structChest = new StructureChest(absCoord, buildable);
				}
				structChest.setChestId(Integer.valueOf(sb.keyvalues.get("id")));
				buildable.addStructureChest(structChest);
				CivGlobal.addStructureChest(structChest);
				
				//  Convert sign data to chest data.
				block = absCoord.getBlock();
				if (ItemManager.getId(block) != CivData.CHEST) {
					byte chestData = CivData.convertSignDataToChestData((byte)sb.getData());
					ItemManager.setTypeIdAndData(block, CivData.CHEST, chestData, true);
				}
				break;
			case "/sign":
				structSign = CivGlobal.getStructureSign(absCoord);
				if (structSign == null) {
					structSign = new StructureSign(absCoord, buildable);
				}
				block = absCoord.getBlock();
				ItemManager.setTypeIdAndData(block, sb.getType(), sb.getData(), true);
				
				structSign.setDirection(ItemManager.getData(block.getState()));
				for (String key : sb.keyvalues.keySet()) {
					structSign.setType(key);
					structSign.setAction(sb.keyvalues.get(key));
					break;
				}
				
				structSign.setOwner(buildable);
				buildable.addStructureSign(structSign);
				CivGlobal.addStructureSign(structSign);
				break;
			case "/itemframe":
				String strvalue = sb.keyvalues.get("id");
				if (strvalue != null) {
					int index = Integer.valueOf(strvalue);
					if (buildable instanceof TownHall) {
						TownHall townhall = (TownHall)buildable;
						townhall.createGoodieItemFrame(absCoord, index, sb.getData());
						townhall.addStructureBlock(absCoord, false);
					} 
				}
				break;
			case "/tradeoutpost":
				if (buildable instanceof TradeOutpost) {
					TradeOutpost outpost = (TradeOutpost)buildable;
					outpost.setTradeOutpostTower(absCoord);
					try {
						outpost.build_trade_outpost_tower();
					} catch (CivException e) {
						e.printStackTrace();
					}
				}
				break;
			case "/techbar":
				if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					int index = Integer.valueOf(sb.keyvalues.get("id"));
					townhall.addTechBarBlock(absCoord, index);
				}
				break;
			case "/techname":
				if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					townhall.setTechnameSign(absCoord);
					townhall.setTechnameSignData((byte)sb.getData());
				}							
				break;
			case "/techdata":
				if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					townhall.setTechdataSign(absCoord);
					townhall.setTechdataSignData((byte)sb.getData());
				}
				break;
			case "/respawn":
				if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					townhall.setRespawnPoint(absCoord);
				}
				break;
			case "/revive":
				if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					townhall.setRevivePoint(absCoord);
				}
				break;
			case "/control":
				if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					townhall.createControlPoint(absCoord);
				}
				break;
			case "/towerfire":
				if (buildable instanceof ArrowTower) {
					ArrowTower arrowtower = (ArrowTower)buildable;
					arrowtower.setTurretLocation(absCoord);
				}
				if (buildable instanceof CannonTower) {
					CannonTower cannontower = (CannonTower)buildable;
					cannontower.setTurretLocation(absCoord);
				}
				break;
			case "/smelter":
				if (buildable instanceof Blacksmith) {
					Blacksmith bs = (Blacksmith) buildable;
					bs.spawnSmelterVillager(absCoord.getLocation(), (byte)sb.getData());
				}
				break;
			case "/toggler":
				if (buildable instanceof Warehouse) {
					Warehouse wh = (Warehouse) buildable;
					wh.spawnToggleVillager(absCoord.getLocation(), (byte)sb.getData());
				}
				break;
			case "/upgradetable":
			case "/upgrade":
				StructureTables table2 = CivGlobal.getStructureTable(absCoord);
				if (table2 == null) {
					table2 = new StructureTables(absCoord, buildable);
				}
				table2.setTaskTable(1);
				buildable.addStructureTable(table2);
				CivGlobal.addStructureTable(table2);
				
				/* Convert sign data to enchanting table.*/
				block = absCoord.getBlock();
				ItemManager.setTypeId(block, CivData.END_PORTAL_FRAME);
				break;
			}
			
			buildable.onPostBuild(absCoord, sb);
		}
		/* Run the tech bar task now in order to protect the blocks */
		if (buildable instanceof TownHall) {
			UpdateTechBar techbartask = new UpdateTechBar(buildable.getCiv());
			techbartask.run();
		}
		
		if (buildable instanceof Camp) {
			Camp camp = (Camp) buildable;
			camp.reprocessCommandSigns();
		}
		
	//	if (buildable instanceof Structure) {
		buildable.updateSignText();
	//}
	}
	
	@Override
	public void run() {
		PostBuildSyncTask.start(tpl, buildable, forceRefresh);
	}
	
}
